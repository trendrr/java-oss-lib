/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.TrendrrLock;
import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrNoCallbackException;


/**
 * @author Dustin Norlander
 * @created Mar 11, 2011
 * 
 */
public class SocketChannelWrapper {

	protected Log log = LogFactory.getLog(SocketChannelWrapper.class);
	SocketChannel channel = null;
	AsynchBuffer buffer = null;

	SelectorThread thread = null;
	TrendrrLock threadInit = new TrendrrLock();

	ConcurrentLinkedQueue<ByteBuffer> writes = new ConcurrentLinkedQueue<ByteBuffer>();

	boolean closed = false;

	public SocketChannelWrapper(SocketChannel channel) {
		this.channel = channel;
		this.buffer = new AsynchBuffer();
	}

	public void readUntil(String delimiter, Charset charset, StringReadCallback callback) {
		this.buffer.readUntil(delimiter, charset, callback);
		this.buffer.process();//attempt to read directly from the already buffered bytes
		this.notifyChange();
	}

	/**
	 * reads until the requested string is found.
	 * @param delimiter
	 * @param charset
	 * @return
	 * @throws TrendrrException
	 */
	public String readUntil(String delimiter, Charset charset) throws TrendrrException{
		SynchronousReadCallback callback = new SynchronousReadCallback();
		this.readUntil(delimiter, charset, callback);
		callback.awaitResponse();
		if (callback.exception != null) {
			throw callback.exception;
		}
		return callback.stringResult;
	}

	public void readBytes(int numBytes, ByteReadCallback callback) {
		this.buffer.readBytes(numBytes, callback);
		this.buffer.process();//attempt to read from the already buffered bytes
		this.notifyChange();
	}


	public byte[] readBytes(int numBytes) throws TrendrrException{
		SynchronousReadCallback callback = new SynchronousReadCallback();
		this.readBytes(numBytes, callback);
		callback.awaitResponse();
		if (callback.exception != null) {
			throw callback.exception;
		}
		return callback.byteResult;
	}

	public void write(ByteBuffer buf) {
		this.writes.add(buf);
		this.notifyChange();
	}

	public void write(byte[] bytes) {
		this.write(ByteBuffer.wrap(bytes));
	}

	private void notifyChange() {
		//alerts the selector that we want to read or write
		try {
			this.startThreadIfNeeded();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.thread.registerChange(this);
	}

	/**
	 * returns true if there are any writes waiting.
	 * @return
	 */
	public boolean hasWrites() {
		return !this.writes.isEmpty();
	}

	/**
	 * returns true if any reads are waiting.
	 * @return
	 */
	public boolean hasReads() {
		return this.buffer.hasCallbacksWaiting();
	}

	public SocketChannel getChannel() {
		return this.channel;
	}

	public Queue<ByteBuffer> getWrites() {
		return this.writes;
	}
	/**
	 * Attempts to read from the network, and process any callbacks.  
	 * does nothing if no callbacks have been registered.
	 * 
	 * @throws TrendrrNoCallbackException
	 * @throws TrendrrDisconnectedException
	 * @throws TrendrrException
	 */
	public void doRead() throws TrendrrNoCallbackException, TrendrrDisconnectedException, TrendrrException {
		int numRead = 1;
		while (numRead > 0 && this.buffer.hasCallbacksWaiting()) {
			numRead = this.buffer.read(this.channel);
			this.buffer.process();
		}
	}

	/**
	 * attempts to process the remaining callbacks, then closes the channel and
	 * cleans up any resources.
	 */
	public synchronized void close() {
		if (closed) {
			log.warn("Already closed!");
			return;
		}

		try { this.buffer.process(); } catch (Exception x) {
			log.debug("Caught", x);
		}
		try { this.channel.close();} catch (Exception x) {
			log.debug("Caught", x);
		}
		try { this.buffer.close();} catch (Exception x) {
			log.debug("Caught", x);
		}
		if (this.thread != null) {
			this.thread.unregister(this);
		}
		this.buffer = null;
		this.thread = null;
		this.closed = true;
	}

	public boolean isClosed() {
		return this.closed;
	}

	private void startThreadIfNeeded() throws IOException {
		if (threadInit.lockOnce()) {
			try {
				this.thread = SelectorThread.registerChannel(this);
			} finally {
				threadInit.unlock();
			}
		}
	}
}