/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;



/**
 * @author Dustin Norlander
 * @created Mar 11, 2011
 * 
 */
public class SelectorThread implements Runnable{

	protected static Log log = LogFactory.getLog(SelectorThread.class);
	
	private static SelectorThread instance = null;
	
	public static synchronized SelectorThread registerChannel(SocketChannelWrapper wrapper) throws IOException {
		boolean startThread = instance == null;
		if (startThread) {
			instance = new SelectorThread();
		}
		instance.register(wrapper);
		if (startThread) {
			Thread t = new Thread(instance);
			t.setDaemon(true);
			t.start();
		}
		return instance;
	}

	private Selector selector;
	
	private ConcurrentHashMap<SocketChannel, SocketChannelWrapper> channels = new ConcurrentHashMap<SocketChannel, SocketChannelWrapper>();
	
	private ConcurrentLinkedQueue<SocketChannelWrapper> changeQueue = new ConcurrentLinkedQueue<SocketChannelWrapper>();

	private ConcurrentLinkedQueue<SocketChannelWrapper> registerQueue = new ConcurrentLinkedQueue<SocketChannelWrapper>();
	public SelectorThread() throws IOException {
		this.selector = SelectorProvider.provider().openSelector();
	}
	
	public void register(SocketChannelWrapper wrapper) throws IOException {
		this.channels.put(wrapper.getChannel(), wrapper);
		wrapper.getChannel().configureBlocking(false); //set to non-blocking
		registerQueue.add(wrapper);
		this.selector.wakeup();
	}
	
	/**
	 * registers that something has changed with the wrapper (read or writes waiting, or disconnect).
	 * @param wrapper
	 */
	public void registerChange(SocketChannelWrapper wrapper) {
		changeQueue.add(wrapper);
		this.selector.wakeup();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			SelectionKey key = null;
			try {
				
				//register any sockets that need it.
				while(!this.registerQueue.isEmpty()) {
					SocketChannelWrapper wrapper = registerQueue.poll();
					//keep key on case of disconnect.
					key = wrapper.getChannel().register(this.selector, SelectionKey.OP_READ); //set our interest to reads
				}
				
				//Process any op changes.
				while(!this.changeQueue.isEmpty()) {
					SocketChannelWrapper wrapper = changeQueue.poll();
					if (wrapper.hasWrites()) {
//						System.out.println("HAS WRITES!");
						key = wrapper.getChannel().keyFor(this.selector);
						key.interestOps(SelectionKey.OP_WRITE);
					} else if (wrapper.hasReads()) {
//						System.out.println("HAS READS!");
						key = wrapper.getChannel().keyFor(this.selector);
						key.interestOps(SelectionKey.OP_READ);
					} else {
						key = wrapper.getChannel().keyFor(this.selector);
						key.interestOps(0);
					}
					
					if (wrapper.isClosed()) {
						continue;
					}
				}
								
				// Wait for an event one of the registered channels
//				log.info("SELECTOR WAITING>>>");
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					key = selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isReadable()) {
//						log.info("READING!");
						this.read(key);
					} else if (key.isWritable()) {
//						log.info("WRITING!");
						this.write(key);
					}
				}
			} catch (TrendrrDisconnectedException x) {
				this.channels.get((SocketChannel)key.channel()).close();
			} catch (IOException e) {
				if (e.getMessage().equals("Broken pipe")) {
					this.channels.get((SocketChannel)key.channel()).close();
				}
			} catch (Exception e) {
				log.error("Caught", e);
			}
		}
		
	}
	
	/**
	 * unregisters the wrapper with this selector thread.
	 * 
	 * it is unnecessary to call this if the wrapper.close() method is invoked.
	 */
	public void unregister(SocketChannelWrapper wrapper) {
		this.channels.remove(wrapper.getChannel());
		wrapper.getChannel().keyFor(this.selector).cancel();
	}
	
	private void read(SelectionKey key) throws IOException, TrendrrDisconnectedException, TrendrrException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		SocketChannelWrapper wrapper = this.channels.get(socketChannel);
		try {
			wrapper.doRead();
		} finally {
			if (!wrapper.hasReads()) {
				if (wrapper.hasWrites()) {
					//there are writes waiting, so we register that for next time.
					this.registerChange(wrapper);
				} else {
					key.interestOps(0);
				}
			}
		}
	}
	
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		SocketChannelWrapper wrapper = this.channels.get(socketChannel);
		if (!wrapper.hasWrites()) {
			this.registerChange(wrapper);
			return;
		}
		
		Queue<ByteBuffer> queue = wrapper.getWrites();
		while (!queue.isEmpty()) {
			
			//does not remove the head element until the buf is completely written
			//maybe that's now, maybe later..
			ByteBuffer buf = queue.peek(); 
//			System.out.println("WRITING: " + buf);
			if (buf == null) {
				break; //queue is empty.
			}
			socketChannel.write(buf);
			if (buf.remaining() > 0) {
				// ... or the socket's buffer fills up
//				System.out.println("buffer isn't totally written");
				break; 
			}
			queue.poll(); //remove the head element
			wrapper.numQueued.decrementAndGet();
		}
		if (queue.isEmpty()) {
			this.registerChange(wrapper);
		}
	}
}

