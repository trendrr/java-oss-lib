/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.networking.ByteReadCallback;
import com.trendrr.oss.networking.StringReadCallback;
import com.trendrr.oss.networking.buffer.SocketChannelWrapper;


/**
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestMessageReader implements StringReadCallback,
		ByteReadCallback {

	protected Log log = LogFactory.getLog(StrestMessageReader.class);
	
	StrestResponse current = null;
	AtomicReference<SocketChannelWrapper> socket = new AtomicReference<SocketChannelWrapper>();
	AtomicReference<StrestClient> client = new AtomicReference<StrestClient>();
	
	public void start(StrestClient client, SocketChannelWrapper socket) {
		this.client.set(client);
		this.socket.set(socket);
	}
	
	public void stop() {
		this.socket.set(null);
		this.client.set(null);
	}
	
	protected void readNextMessage() {
		SocketChannelWrapper sock = this.socket.get();
		if (sock == null) {
			log.info("No socketchannelwrapper, returning");
		}
		
		this.current = null;
		sock.readUntil(StrestUtil.CRLF + StrestUtil.CRLF, StrestUtil.DEFAULT_CHARSET, this);
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		StrestClient client = this.client.get();
		if (client == null)
			return;
		client.error(ex);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ByteReadCallback#byteResult(byte[])
	 */
	@Override
	public void byteResult(byte[] result) {
		this.current.setContent(result);
		//TODO: do the callback.
		StrestClient client = this.client.get();
		if (client == null)
			return;
		
		//call the client.
		client.incoming(this.current);
		
		//now read the next message.
		this.readNextMessage();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.StringReadCallback#stringResult(java.lang.String)
	 */
	@Override
	public void stringResult(String result) {
		current = new StrestResponse();
		current.parseHeaders(result);
		
		String contentLength = current.getHeader(StrestHeaders.Names.CONTENT_LENGTH);
		if (contentLength == null) {
			log.warn("No content length set by server. ");
			return;
		}
		int length = Integer.parseInt(contentLength);
		
		SocketChannelWrapper sock = this.socket.get();
		if (sock == null) {
			log.info("No socketchannelwrapper, returning");
		}
		sock.readBytes(length, this);
	}
}
