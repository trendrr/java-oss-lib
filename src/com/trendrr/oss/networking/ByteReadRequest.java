/**
 * 
 */
package com.trendrr.oss.networking;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Mar 10, 2011
 * 
 */
class ByteReadRequest implements ChannelCallback {

	protected Log log = LogFactory.getLog(ByteReadRequest.class);

	private ByteReadCallback callback;
	private int numBytes = 0;
	ByteBuffer buf = null;
	
	
	public ByteReadRequest(int numBytes, ByteReadCallback callback) {
		this.callback = callback;
		this.numBytes = numBytes;
		this.buf = ByteBuffer.allocate(numBytes);
	}
	
	public ByteBuffer getBuf() {
		return buf;
	}

	public void setBuf(ByteBuffer buf) {
		this.buf = buf;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		this.callback.onError(ex);
	}

	public ByteReadCallback getCallback() {
		return callback;
	}

	public void setCallback(ByteReadCallback callback) {
		this.callback = callback;
	}

	public int getNumBytes() {
		return numBytes;
	}

	public void setNumBytes(int numBytes) {
		this.numBytes = numBytes;
	}
}
