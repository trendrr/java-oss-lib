/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Apr 5, 2012
 * 
 */
class ByteReadFullyCallback implements ByteReadCallback {

	protected static Log log = LogFactory.getLog(ByteReadFullyCallback.class);
	
	private ByteReadCallback callback;
	private ByteArrayOutputStream baos; 
	private SocketChannelWrapper socket;
	
	static final int NUMBYTES = 1024;
	
	public ByteReadFullyCallback(SocketChannelWrapper socket, ByteReadCallback callback) {
		this.callback = callback;
		this.baos = new ByteArrayOutputStream();
		this.socket = socket;
		this.socket.setCloseListener(this);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		if (ex instanceof TrendrrDisconnectedException) {
			//eof
			byte[] retVal = baos.toByteArray();
			try {
				baos.close();
			} catch (IOException e) {
				log.error("caught", e);
			}
			this.callback.byteResult(retVal);
			return;
		}
		this.callback.onError(ex);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ByteReadCallback#byteResult(byte[])
	 */
	@Override
	public void byteResult(byte[] result) {
		try {
//			System.out.println("READ: " + result.length);
			this.baos.write(result);
			if (!this.socket.hasReads())
				this.socket.readBytes(NUMBYTES, this);
		} catch (IOException e) {
			this.callback.onError(new TrendrrException(e));
		}
	}
	
}
