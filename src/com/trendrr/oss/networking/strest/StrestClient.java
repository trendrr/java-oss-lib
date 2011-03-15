/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * Callbacks are executed in the oi thread, so it is recommended that any heavy processing be done in a separate thread.
 * 
 * 
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestClient {

	protected Log log = LogFactory.getLog(StrestClient.class);
	protected SocketChannelWrapper socket;
	protected StrestMessageReader reader;
	protected String host = null;
	protected int port = 8008;
	protected ConcurrentHashMap<String, StrestRequestCallback> callbacks = new ConcurrentHashMap<String,StrestRequestCallback>();
	protected AtomicBoolean connected = new AtomicBoolean(false);
	
	public StrestClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public synchronized void connect() {
		SocketChannel channel;
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(true); //set to blocking so we wait on connection.
			channel.connect(new InetSocketAddress(this.host, this.port));
			socket = new SocketChannelWrapper(channel);
			reader = new StrestMessageReader();
			reader.start(this, socket);
			connected.set(true);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	/**
	 * closes the connection and cleans up any resources.  
	 * 
	 * all waiting callbacks will get a disconnected exception..
	 * 
	 */
	public synchronized void close() {
		socket.close();
		reader.stop();
		reader = null;
		
		//TODO: issue disconnects to all waiting callbacks.
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
	public synchronized void send(StrestRequest request, StrestRequestCallback callback){
		try {
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
	public StrestResponse send(StrestRequest request) throws TrendrrException {
		try {
			request.setHeader(StrestHeaders.Names.STREST_TXN_ACCEPT, StrestHeaders.Values.SINGLE);
			StrestSynchronousRequest sr = new StrestSynchronousRequest();
			this.send(request, sr);
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
		try {
			cb.messageRecieved(response);
		} catch (Exception x) {
			log.error("Caught", x);
		}
		if (!StrestHeaders.Values.CONTINUE.equalsIgnoreCase(txnStatus)) {
			this.callbacks.remove(txnId);
			cb.txnComplete();
		}
	}
	
	/*
	 * an error occured in the reader.
	 */
	void error(TrendrrException e) {
		//UMM, what should we do here I wonder?
		log.warn("Caught", e);
	}
}
