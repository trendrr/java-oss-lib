/**
 * 
 */
package com.trendrr.oss.strest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrOverflowException;
import com.trendrr.oss.exceptions.TrendrrIOException;
import com.trendrr.oss.exceptions.TrendrrTimeoutException;
import com.trendrr.oss.networking.SocketChannelWrapper;
import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestRequest;
import com.trendrr.oss.strest.models.StrestResponse;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;
import com.trendrr.oss.strest.models.StrestHeader.TxnStatus;



/**
 * Strest Client.
 * 
 * This class is threadsafe, and handles low level details of the STREST protocol.
 * 
 * The client is implemented using non-blocking sockets, there is a single io thread which is shared
 * across any StrestClient instances. 
 * 
 * Callbacks are executed in the io thread, so it is recommended that any heavy processing be done in a separate thread.
 * 
 * 
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestClient {

	protected static Log log = LogFactory.getLog(StrestClient.class);
	protected SocketChannelWrapper socket = null;
	protected StrestMessageReader reader;
	protected String host = null;
	protected int port = 8008;
	protected ConcurrentHashMap<String, StrestRequestCallback> callbacks = new ConcurrentHashMap<String,StrestRequestCallback>();
	protected AtomicBoolean connected = new AtomicBoolean(false);

	protected int maxWaitingForResponse = 0; //the maximum number of waiting callbacks.
	protected int maxQueuedWrites = 200; //the maximum number of writes that have queued up.
	protected boolean waitOnMaxQueuedWrites = false; //should we block or exception when queue is full?
	


	

	public StrestClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
		
	public synchronized void connect() throws IOException {
		if (this.connected.get()) {
			log.warn("Connect called, but already connected");
			return;
		}
		
		SocketChannel channel;
		channel = SocketChannel.open();
		boolean connected = channel.connect(new InetSocketAddress(this.host, this.port));
		log.info("CONNECTED: " + connected);
		socket = new SocketChannelWrapper(channel);
		reader = new StrestMessageReader();
		reader.start(this, socket);
		this.connected.set(true);
	}
	
	public boolean isConnected() {
		return this.connected.get();
	}
	
	/**
	 * closes the connection and cleans up any resources.  
	 * 
	 * all waiting callbacks will get a disconnected exception..
	 * 
	 */
	public synchronized void close() {
		this.connected.set(false);
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception x) {
			log.info("Caught", x);
		}
		if (reader != null) {
			try {
				reader.stop();
			} catch (Exception x) {
				log.info("Caught", x);
			}
		}
		reader = null;
		log.info("Announcing broken connection to callbacks: " + this.callbacks);
		for (StrestRequestCallback cb : this.callbacks.values()) {
			log.info("CONNECTION BROKEN! : " + cb);
			cb.error(new TrendrrDisconnectedException("Connection Broken"));
		}
		this.callbacks.clear();
		
	}
	
	/**
	 * sends the request asynchrounously. 
	 * 
	 * this method returns immediately, response is sent to the callback.
	 * 
	 * @param request
	 * @param callback
	 * @throws Exception
	 */
	public synchronized void sendRequest(StrestRequest request, StrestRequestCallback callback){
		try {
			if (this.socket == null || this.socket.isClosed()) {
				this.connected.set(false);
				throw new TrendrrDisconnectedException("Not connected");
			}

			if (this.maxWaitingForResponse > 0 && this.maxWaitingForResponse <= this.callbacks.size()) {

				throw new TrendrrIOException(this.maxWaitingForResponse + " waiting for response, me thinks theres a network problem, or you need to slow down! (" + this.host + ")");
			}
			
			if (!this.isWaitOnMaxQueuedWrites() && this.socket.getWriteQueueSize() >= this.maxQueuedWrites) {
				throw new TrendrrIOException(this.socket.getWriteQueueSize() + " queued writes. me thinks theres a network problem, or you need to slow down! (" + this.host + ")");
			}
			
			while (this.isWaitOnMaxQueuedWrites() && this.socket.getWriteQueueSize() >= this.maxQueuedWrites) {
				//wait for space.
				log.warn("Write queue is full, waiting for space... (" + this.host + ")");
				Sleep.millis(25);
			}
			
			
 			
			if (request.getTxnAccept() == null) {
				request.setTxnAccept(TxnAccept.MULTI);
			}
			if (request.getTxnId() == null) {
				request.setTxnId(StrestHeader.generateTxnId());
			}
			
			byte[] bytes = request.toByteArray();
			
			if (callback != null)
				this.callbacks.put(request.getTxnId(), callback);
			if (this.maxQueuedWrites > 0) {
				socket.write(bytes, this.maxQueuedWrites);
			} else {
				socket.write(bytes);
			}
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			}
		}		
	}
	
	public StrestResponse sendRequest(StrestRequest request) throws TrendrrException {
		return this.sendRequest(request, 0l);
	}	
	
	/**
	 * sends a synchronious request, will wait for the result.
	 * @param request
	 * @return
	 */
	public StrestResponse sendRequest(StrestRequest request, long timeoutMillis) throws TrendrrTimeoutException, TrendrrException {
		try {
			if (!this.connected.get()) {
				throw new TrendrrDisconnectedException("Strest Client is not connected!");
			}
			request.setTxnAccept(TxnAccept.SINGLE);
			StrestSynchronousRequest sr = new StrestSynchronousRequest();
			this.sendRequest(request, sr);
			return sr.awaitResponse(timeoutMillis);
		} catch (TrendrrException x) {
			throw x;
		} catch (Throwable t) {
			if (t instanceof IOException) {
				IOException iox = (IOException)t;
				if (iox.getMessage().equalsIgnoreCase("not connected")) {
					throw new TrendrrDisconnectedException(iox);
				}
			}
			throw new TrendrrException(new Exception(t));
		}
	}
	
	/*
	 * incoming message from the reader.
	 */
	void incoming(StrestResponse response) {
		String txnId = response.getTxnId();
		TxnStatus txnStatus = response.getTxnStatus();
		StrestRequestCallback cb = this.callbacks.get(txnId);
		if (cb == null) {
			//Server sent us a response to transaction that doesn't exist or is already closed!
			log.error("SERVER SENT Response to Transaction: " + txnId + " Which is either closed or doesn't exist!");
			return;
		}
		try {
			cb.response(response);
		} catch (Exception x) {
			log.error("Caught", x);
		}
		
		if (txnStatus != TxnStatus.CONTINUE) {
			this.callbacks.remove(txnId);
			cb.txnComplete(txnId);
		}
	}
	
	/*
	 * an error occured in the reader.
	 */
	void error(TrendrrException e) {
		//UMM, what should we do here I wonder?
//		log.warn("Error from reader Caught", e);
		
		if (e instanceof TrendrrDisconnectedException) {
			log.warn("Closing connection!");
			this.close();
		}
	}
	
	/**
	 * The maximum allowed callbacks to be waiting for a response.  once this limit is reached
	 * new requests will get an exception. Default is 0 which means unlimited
	 * @return
	 */
	public int getMaxWaitingForResponse() {
		return maxWaitingForResponse;
	}

	public void setMaxWaitingForResponse(int maxWaitingForResponse) {
		this.maxWaitingForResponse = maxWaitingForResponse;
	}

	/**
	 * Maximum number of queued writes.  once this limit is reached exceptions will be thrown on write.
	 * @return
	 */
	public int getMaxQueuedWrites() {
		return maxQueuedWrites;
	}

	public void setMaxQueuedWrites(int maxQueuedWrites) {
		this.maxQueuedWrites = maxQueuedWrites;
	}
	
	/**
	 * wait or exception when max queued writes is reached? default to false
	 * @return
	 */
	public boolean isWaitOnMaxQueuedWrites() {
		return waitOnMaxQueuedWrites;
	}

	public void setWaitOnMaxQueuedWrites(boolean waitOnMaxQueuedWrites) {
		this.waitOnMaxQueuedWrites = waitOnMaxQueuedWrites;
	}
}
