/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrNoCallbackException;


/**
 * Singleton selector thread.
 * 
 * Thread is started the first time a SocketChannel is registered. 
 * 
 * 
 * Based on http://rox-xmlrpc.sourceforge.net/niotut/
 * 
 * Updated for improved concurrency, and better use of generics
 * 
 * 
 * @author Dustin Norlander
 * @created Mar 10, 2011
 * 
 */
public class SocketChannelThread implements Runnable{

	protected Log log = LogFactory.getLog(SocketChannelThread.class);

	private static SocketChannelThread instance = null;
	public static synchronized SocketChannelThread registerChannel(SocketChannelReader reader) throws IOException {
		boolean startThread = instance == null;
		if (startThread) {
			instance = new SocketChannelThread();
		}
		instance.register(reader);
		if (startThread) {
			Thread t = new Thread(instance);
			t.setDaemon(true);
			t.start();
		}
		return instance;
	}
	
	
	// A list of PendingChange instances
	private ConcurrentLinkedQueue<ChangeRequest> pendingChanges = new ConcurrentLinkedQueue<ChangeRequest>();
	// Maps a SocketChannel to a list of ByteBuffer instances
	private ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<ByteBuffer>> pendingData = new ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<ByteBuffer>>();
	// The selector we'll be monitoring
	private Selector selector;
	
	private HashMap<SocketChannel, SocketChannelReader> channels = new HashMap<SocketChannel, SocketChannelReader>();
	
	
	
	public SocketChannelThread() throws IOException {
		this.selector = SelectorProvider.provider().openSelector();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			SelectionKey key = null;
			try {
				// Process any pending changes
				while(!this.pendingChanges.isEmpty()) {
					ChangeRequest change = this.pendingChanges.poll();
					System.out.println(change);
					if (change == null)
						continue;
					switch (change.type) {
						case ChangeRequest.CHANGEOPS: {
							key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
							break;
						}
						case ChangeRequest.UNREGISTER: {
							key = change.socket.keyFor(this.selector);
							key.cancel();
							break;
						}
					}

				}
				
				// Wait for an event one of the registered channels
				log.info("SELECTOR WAITING>>>");
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
						log.info("READING!");
						this.read(key);
					} else if (key.isWritable()) {
						log.info("WRITING!");
						this.write(key);
					}
				}
			} catch (TrendrrDisconnectedException x) {
				this.unRegister((SocketChannel)key.channel());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void read(SelectionKey key) throws IOException, TrendrrDisconnectedException, TrendrrException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		SocketChannelReader reader = this.channels.get(socketChannel);
		try {
			reader.attemptRead();
		} catch (TrendrrNoCallbackException e) {
			//no callback.  so we aren't interested in Reading anymore.
			key.interestOps(0); //only interested in write ops.
		}
	}
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ConcurrentLinkedQueue<ByteBuffer> queue = this.pendingData.get(socketChannel);
		if (queue == null) {
			log.info("WRITE OP_READ");
			key.interestOps(SelectionKey.OP_READ);
			return;
		}
		
		// Write until there's not more data ...
		while (!queue.isEmpty()) {
			//does not remove the head element until the buf is completely written
			//maybe that's now, maybe later..
			ByteBuffer buf = queue.peek(); 
			if (buf == null) {
				break; //queue is empty.
			}
			socketChannel.write(buf);
			if (buf.remaining() > 0) {
				// ... or the socket's buffer fills up
				break; 
			}
			queue.poll(); //remove the head element
		}

		if (queue.isEmpty()) {
			// We wrote away all data, so we're no longer interested
			// in writing on this socket. Switch back to waiting for
			// data.
			log.info("QUEUE IS EMPTY!");
			key.interestOps(SelectionKey.OP_READ);
		}
	}
	
	void send(SocketChannel socket, byte[] data) {
		//add to the pending data queue
		this.send(socket, ByteBuffer.wrap(data));
	}
	
	void send(SocketChannel socket, ByteBuffer buf) {
		// And queue the data we want written
		this.pendingData.putIfAbsent(socket, new ConcurrentLinkedQueue<ByteBuffer>());
		this.pendingData.get(socket).add(buf);
		// Indicate we want the interest ops set changed
		log.info("SEND WRITE REQUESTED!");
		this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		// Finally, wake up our selecting thread so it can make the required changes
		log.info("WAKE UP!");
		this.selector.wakeup();
	}
	
	void readRequested(SocketChannel socket) {
		if (this.pendingData.get(socket) != null && ! this.pendingData.get(socket).isEmpty()) {
			return; //there is a waiting write, so we will read directly after that..
		}
		log.info("READ REQUESTED OP READ");
		this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ));
		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}
	
	/**
	 * registers a new channel.  it is assumed that this channel is already connected.
	 * @param reader
	 * @throws IOException
	 */
	public synchronized void register(SocketChannelReader reader) throws IOException {
		reader.getChannel().configureBlocking(false); //set to non-blocking
		log.info("REGISTER OP READ!");
		reader.getChannel().register(this.selector, SelectionKey.OP_READ); //set our interest to reads
		this.channels.put(reader.getChannel(), reader);
	}
	
	/**
	 * Immediately unregisters this reader from the thread.  any waiting writes are 
	 * discarded. 
	 * 
	 * 
	 * @param reader
	 */
	public synchronized void unRegister(SocketChannelReader reader) {
		
		
	}
	
	public synchronized void unRegister(SocketChannel channel) {
		System.out.println("UNREGISTER! " + channel);
		channel.keyFor(this.selector).interestOps(0);
	}
	
	private class ChangeRequest {
		public static final int REGISTER = 1;
		public static final int CHANGEOPS = 2;
		
		public static final int UNREGISTER = 3;
		
		public SocketChannel socket;
		public int type;
		public int ops;
		
		public ChangeRequest(SocketChannel socket, int type, int ops) {
			this.socket = socket;
			this.type = type;
			this.ops = ops;
		}
		
		public String toString() {
			return "Socket: " + socket + " type " + type + " OPS " + ops + " " + SelectionKey.OP_READ;
		}
	}
}
