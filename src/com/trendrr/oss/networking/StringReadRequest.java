/**
 * 
 */
package com.trendrr.oss.networking;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Mar 10, 2011
 * 
 */
class StringReadRequest implements ChannelCallback{

	protected Log log = LogFactory.getLog(StringReadRequest.class);

	private String delimiter;
	private Charset charset;
	private StringReadCallback callback;
	StringBuilder buf = null;
	
	

	public StringReadRequest(String delimiter, Charset charset, StringReadCallback cb) {
		this.delimiter = delimiter;
		this.charset = charset;
		this.callback = cb;
		this.buf = new StringBuilder();
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		this.callback.onError(ex);
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public StringReadCallback getCallback() {
		return callback;
	}

	public void setCallback(StringReadCallback callback) {
		this.callback = callback;
	}
	
	public StringBuilder getBuf() {
		return buf;
	}
}
