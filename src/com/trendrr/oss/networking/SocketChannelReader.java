/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.concurrent.TrendrrLock;
import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrNoCallbackException;
//import com.trendrr.oss.networking.buffer.SynchronousReadCallback;


/**
 * @author Dustin Norlander
 * @created Mar 9, 2011
 * 
 */
public class SocketChannelReader {

	
	public static void main(String ...strings) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect(new InetSocketAddress("trendrr.com", 80));
			final SocketChannelReader reader = new SocketChannelReader(channel);
			
			reader.write("GET /static/img/homepage_techcrunch.gif HTTP/1.0\r\n\r\n".getBytes());
			
//			System.out.println(reader.readUntil("\r\n\r\n", Charset.forName("utf8")));
//			
//			System.out.println("** READING BYTES **");
//			System.out.println(reader.readBytes(2236));
//			System.out.println("** DONE READING BYTES ** ");
			
//			reader.write("GET /static/img/homepage_techcrunch.gif HTTP/1.0\r\n\r\n".getBytes());
//
//			reader.readUntil("\r\n\r\n", Charset.forName("utf8"), new StringReadCallback() {
//
//				@Override
//				public void onError(TrendrrException ex) {
//					ex.printStackTrace();
//				}
//
//				@Override
//				public void stringResult(String result) {
//					System.out.println("RESULT: " + result);
//				}
//				
//			});
//			
//			
//			Sleep.seconds(10);
//			System.out.println("READING BYTES ");
//			reader.readBytes(2236, new ByteReadCallback() {
//				@Override
//				public void onError(TrendrrException ex) {
//					// TODO Auto-generated method stub
//				}
//				
//				@Override
//				public void byteResult(byte[] result) {
//					System.out.println("GOT BYTES! : " + result.length);
////					log.info(new String(result, Charset.forName("utf8")));
////					for (byte b : result) {
////						System.out.println(b);
////					}
//				}
//			});				
			
			Sleep.seconds(10);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	protected static Log log = LogFactory.getLog(SocketChannelReader.class);
	SocketChannel channel = null;
	int bytesPerRead = 8192;
	int bufferPoolSize = 10; //total (empty) buffer size is buffersize * bytesPerRead
	
	//buffers ready to be written to.
	Stack<ByteBuffer> bufferPool = new Stack<ByteBuffer>();
	//these hold the actual data that has been read from the socket.
	List<ByteBuffer> databuffers = new ArrayList<ByteBuffer>();
	
	AtomicReference<ChannelCallback> callback = new AtomicReference<ChannelCallback>();
	SocketChannelThread thread = null;
	TrendrrLock threadInit = new TrendrrLock();
	
	
	//for requested bytes
	ByteBuffer outbytebuf = null;
	int requestBytes = 0;
	
	//for requested string.
	StringBuilder outstringbuf = null;
	String requestedString = null;
	Charset charset = Charset.forName("utf8");
	
	
	/**
	 * creates a new SocketChannelReader.  channel should already be connected.
	 * @param channel
	 */
	public SocketChannelReader(SocketChannel channel) {
		this.channel = channel;
	}
	
	
	/**
	 * Reads from the socketchannel until the delimiter is encountered. 
	 * 
	 * @param delimiter
	 * @param callback
	 * @param charset
	 */
	public synchronized void readUntil(String delimiter, Charset charset, StringReadCallback callback) {
		this.requestedString = delimiter;
		this.charset = charset;
		this.outstringbuf = new StringBuilder();
		
		//clear the byte buffer
		this.outbytebuf = null;
		this.requestBytes = 0;
		
		this.registerCallback(callback);
	}
	
//	/**
//	 * a synchronous request.  will block until the requested String is available
//	 * @param numBytes
//	 * @return
//	 */
//	public String readUntil(String delimiter, Charset charset) throws TrendrrException{
//		SynchronousReadCallback cb = new SynchronousReadCallback();
//		this.readUntil(delimiter, charset, cb);
//		cb.awaitResponse();
//		if (cb.exception != null) {
//			throw cb.exception;
//		}
//		return cb.stringResult;	
//	}
	
	
	/**
	 * reads the specified number of bytes from the channel, calls the callback when 
	 * requested bytes are available.
	 * @param numBytes
	 * @param callback
	 */
	public synchronized void readBytes(int numBytes, ByteReadCallback callback) {
		this.outbytebuf = ByteBuffer.allocate(numBytes);
		
		//clear the string buffers
		this.outstringbuf = null;
		this.requestedString = null;
		
		this.registerCallback(callback);
	}
	
//	/**
//	 * a synchronous request.  will block until the requested # of bytes are available
//	 * @param numBytes
//	 * @return
//	 */
//	public byte[] readBytes(int numBytes) throws TrendrrException{
//		SynchronousReadCallback cb = new SynchronousReadCallback();
//		this.readBytes(numBytes, cb);
//		cb.awaitResponse();
//		if (cb.exception != null) {
//			throw cb.exception;
//		}
//		return cb.byteResult;	
//	}
	
	/**
	 * queues the data to be written to the socket.
	 * data is guarenteed to be written in the order it was submitted.
	 * 
	 * @param bytes
	 */
	public synchronized void write(byte[] bytes) {
		try {
			this.startThreadIfNeeded();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.thread.send(this.channel, bytes);
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	/**
	 * registers the callback.
	 * 
	 * registers this Channel with the channel thread on first invocation.
	 * this should be called LAST after any other setup logic.
	 * 
	 * @param callback
	 */
	private void registerCallback(ChannelCallback callback) {
		try {
			this.startThreadIfNeeded();
		} catch (IOException e) {
			callback.onError(new TrendrrException("Couldn't register with I/O thread", e));
			return;
		}
		this.callback.set(callback);
		this.thread.readRequested(this.channel);
	}
	

	private void startThreadIfNeeded() throws IOException {
		if (threadInit.lockOnce()) {
			try {
				this.thread = SocketChannelThread.registerChannel(this);
			} finally {
				threadInit.unlock();
			}
		}
	}
	
	/**
	 * returns the current buffer if available, else returns a new buffer from the 
	 * returns the next available buffer from the pool.
	 * @return
	 */
	private ByteBuffer getBuffer() {
		if (bufferPool.isEmpty()) {
			return ByteBuffer.allocate(this.bytesPerRead);
		}
		return bufferPool.pop();
	}
	
	/**
	 * returns a buffer to the pool.
	 * this will automatically clear the buffer, so no need to do that.
	 * 
	 * if the pool is full, then this buffer will just be dropped
	 * @param buf
	 */
	private void returnBuffer(ByteBuffer buf) {
		if (this.bufferPool.size() >= this.bufferPoolSize) {
			return;
		}
		buf.clear();
		this.bufferPool.push(buf);
	}
	
	/**
	 * attempts to read everything from the network
	 * @throws TrendrrDisconnectedException
	 * @throws TrendrrException
	 */
	private void read() throws TrendrrDisconnectedException, TrendrrException{
		int numRead = this.bytesPerRead;
		try {
			//read everything currently in the buffer
			
			while(numRead == this.bytesPerRead) {
				ByteBuffer buf = this.getBuffer();
				numRead = channel.read(buf);
				
				System.out.println(numRead);
				if (numRead < 0) {
					this.returnBuffer(buf);
					throw new TrendrrDisconnectedException("EOF reached!");
				} else if (numRead == 0) {
					//just return the buffer, we didn't get any data.
					this.returnBuffer(buf); 
				} else {
					buf.flip();
					this.databuffers.add(buf);
				}
			}
		} catch (Exception x) {
			this.throwException(x);
		}
	}
	
	
	/**
	 * attempts to read the requested data from the channel.
	 * 
	 * appropriate callback will be called if data completes.
	 * 
	 * @throws TrendrrNoCallbackException if there is no callback set then no read op happens.
	 * @throws TrendrrDisconnectedException
	 * @throws TrendrrException
	 */
	public synchronized void attemptRead() throws TrendrrNoCallbackException, TrendrrDisconnectedException, TrendrrException {
		if (this.callback.get() == null) {
			log.info("No callback set, not reading");
			throw new TrendrrNoCallbackException("No Callback set!");
		}
		
		//so we should still try to read from the databuffers. 
		TrendrrException x = null;
		try {
			this.read();
		} catch (TrendrrException e) {
			x = e;
		}
		if (this.databuffers.isEmpty()) {
			log.info("Buffer is empty!");
			return;
		}
		if (this.callback.get() instanceof StringReadCallback && this.requestedString != null) {
			try {
				this.readString();
			} catch (CharacterCodingException e) {
				throw new TrendrrException("Error trying to read string", e);
			}
		}
		if (this.callback.get() instanceof ByteReadCallback && this.requestBytes > 0) {
			this.readBytes();
		}
		if (x != null)
			throw x;
	}
	
	private void readBytes() {
		List<ByteBuffer> databufs = new ArrayList<ByteBuffer>();
		log.info("reading bytes");
		for (ByteBuffer buf : this.databuffers) {
			if (this.outbytebuf.hasRemaining()) {
				try {
					this.outbytebuf.put(buf);
					this.returnBuffer(buf);
				} catch (BufferOverflowException x) {
					// means that b has more bytes then allocated in outbuf.
					while(this.outbytebuf.hasRemaining()) {
						this.outbytebuf.put(buf.get());
					}
					databufs.add(buf);
				}
			} else {
				databufs.add(buf);
			}	
		}
		this.databuffers = databufs;

		if (!this.outbytebuf.hasRemaining()) {
			log.info("GOT The requested # of bytes!");
			((ByteReadCallback)this.callback.getAndSet(null)).byteResult(this.outbytebuf.array());
			this.outbytebuf = null;
		}
	}
	
	/**
	 * reads characters until the requested string is found, or buffers are exhausted.
	 * @throws CharacterCodingException
	 */
	private void readString() throws CharacterCodingException {
		int fromIndex = Math.max(0, this.outstringbuf.length()-this.requestedString.length());
		List<ByteBuffer> databufs = new ArrayList<ByteBuffer>();
		String retVal = null;
		CharsetDecoder decoder = charset.newDecoder();
		
		CharBuffer charBuf = CharBuffer.allocate(this.bytesPerRead);
		
		for (ByteBuffer buf : this.databuffers) {
			if (retVal != null) {
				databufs.add(buf);
			} else {

				//decode as many bytes into characters as we can.
				decoder.decode(buf, charBuf, false);
				charBuf.flip();
				log.info(charBuf.toString());
				
				this.outstringbuf.append(charBuf);
//				log.info(this.outstringbuf.length() + " :" + this.outstringbuf.toString() + ": end");
				
				charBuf.clear();
				
				if (this.outstringbuf.length() > this.requestedString.length() + fromIndex) {
					int found = this.outstringbuf.indexOf(this.requestedString, fromIndex);
					if (found != -1) {
						String val = this.outstringbuf.toString();
						log.info(val);
						retVal = val.substring(0, found);
						String remaining = val.substring(found+this.requestedString.length());
						log.info(remaining);
						//now need to add this back to the ByteBuffer..
						ByteBuffer remainingAsBuf = this.getBuffer().put(remaining.getBytes(this.charset));
						remainingAsBuf.flip();
						databufs.add(remainingAsBuf);
					} else {
						fromIndex += this.requestedString.length();
					}
				}
				if (buf.hasRemaining()) {
					databufs.add(buf);
					if (retVal == null) {
						//we reached the end of the encodeable data and still haven't found our delimiter
						throw new CharacterCodingException();
					}
					
				} else { 
					this.returnBuffer(buf);
				}
			}	
		}
		
		this.databuffers = databufs;
		if (retVal != null) {
			log.info("GOT The requested String!");
			log.info(retVal);
			((StringReadCallback)this.callback.getAndSet(null)).stringResult(retVal);
			this.outstringbuf = null;
		}
	}
	
	
	private void throwException(Exception x) throws TrendrrDisconnectedException, TrendrrException{
		if (x instanceof NotYetConnectedException) {
			throw new TrendrrDisconnectedException(x);
		}
		if (x instanceof ClosedChannelException) {
			throw new TrendrrDisconnectedException(x);
		}
		if (x instanceof SocketException) {
			throw new TrendrrDisconnectedException(x);
		}
		if (x instanceof AsynchronousCloseException) {
			throw new TrendrrDisconnectedException(x);
		}
		if (x instanceof ClosedByInterruptException) {
			throw new TrendrrDisconnectedException(x);
		}
		if (x instanceof TrendrrException) {
			throw (TrendrrException)x;
		}
		throw new TrendrrException(x);
	}

}
