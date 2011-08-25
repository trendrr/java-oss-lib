/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.networking.SocketChannelWrapper;



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

	protected Log log = LogFactory.getLog(StrestClient.class);
	protected SocketChannelWrapper socket = null;
	protected StrestMessageReader reader;
	protected String host = null;
	protected int port = 8008;
	protected ConcurrentHashMap<String, StrestRequestCallback> callbacks = new ConcurrentHashMap<String,StrestRequestCallback>();
	protected AtomicBoolean connected = new AtomicBoolean(false);
	
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
			socket.close();
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
				throw new IOException("Not connected");
			}
			request.setHeaderIfAbsent(StrestHeaders.Names.STREST_TXN_ACCEPT, StrestHeaders.Values.MULTI);
			ByteBuffer buf = request.getBytesAsBuffer();
			if (callback != null)
				this.callbacks.put(request.getHeader(StrestHeaders.Names.STREST_TXN_ID), callback);
			socket.write(buf);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			}
		}		
	}
	
	/**
	 * sends a synchronious request, will wait for the result.
	 * @param request
	 * @return
	 */
	public StrestResponse sendRequest(StrestRequest request) throws TrendrrException {
		try {
			if (!this.connected.get()) {
				throw new TrendrrDisconnectedException("Strest Client is not connected!");
			}
			request.setHeader(StrestHeaders.Names.STREST_TXN_ACCEPT, StrestHeaders.Values.SINGLE);
			StrestSynchronousRequest sr = new StrestSynchronousRequest();
			this.sendRequest(request, sr);
			return sr.awaitResponse();
		} catch (TrendrrException x) {
			throw x;
		} catch (Throwable t) {
			throw new TrendrrException(new Exception(t));
		}
	}
	
	/*
	 * incoming message from the reader.
	 */
	void incoming(StrestResponse response) {
		String txnId = response.getHeader(StrestHeaders.Names.STREST_TXN_ID);
		String txnStatus = response.getHeader(StrestHeaders.Names.STREST_TXN_STATUS);
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
		
		if (!StrestHeaders.Values.CONTINUE.equalsIgnoreCase(txnStatus)) {
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
}
