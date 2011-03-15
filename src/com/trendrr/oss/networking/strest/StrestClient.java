/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.networking.SocketChannelWrapper;



/**
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestClient {

	protected Log log = LogFactory.getLog(StrestClient.class);
	SocketChannelWrapper socket;
	StrestMessageReader reader;
	String host = null;
	int port = 8008;
	
	ConcurrentHashMap<String, StrestCallback> callbacks = new ConcurrentHashMap<String,StrestCallback>();
	
	
	public StrestClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void connect() {
		SocketChannel channel;
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(true); //set to blocking so we wait on connection.
			channel.connect(new InetSocketAddress(this.host, this.port));
			socket = new SocketChannelWrapper(channel);
			reader = new StrestMessageReader();
			reader.start(this, socket);
		} catch (Exception x) {
			x.printStackTrace();
		}
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
	public synchronized void sendRequest(StrestRequest request, StrestCallback callback){
		try {
			ByteBuffer buf = request.getBytesAsBuffer();
			if (callback != null)
				this.callbacks.put(request.getHeader(StrestHeaders.Names.STREST_TXN_ID), callback);
			socket.write(buf);
		} catch (UnsupportedEncodingException e) {
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
		//TODO: fillme in.
		
	}
	
	/*
	 * an error occured in the reader.
	 */
	void error(TrendrrException e) {
		//UMM, what should we do here I wonder?
		log.warn("Caught", e);
	}
}
