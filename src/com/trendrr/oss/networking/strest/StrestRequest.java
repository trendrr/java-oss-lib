/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Mar 11, 2011
 * 
 */
public class StrestRequest {

	protected Log log = LogFactory.getLog(StrestRequest.class);
	StrestHeaders headers = new StrestHeaders();
	private String uri = "/";
	private byte[] content = new byte[0];
	private String method = "GET";
	private String userAgent = JSTREST_USERAGENT;
	
	
	public static final String STREST_VERSION = "STREST/0.1";
	public static final String JSTREST_USERAGENT = "JStrest 1.0";
	public StrestRequest() {
		
	}
	
	/**
	 * gets the bytes prepared and ready for sending to the server.
	 * 
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public ByteBuffer getBytesAsBuffer() throws UnsupportedEncodingException {
		this.finish();
		byte[] header = this.encodeHeaders();
		ByteBuffer buf = ByteBuffer.allocate(header.length + this.content.length);
		buf.put(header);
		buf.put(content);
		return buf;
	}
	
	protected void finish() {
		//makes sure everything is set that needs to be.
		if (this.content == null) {
			this.content = new byte[0]; //make sure content exists.
		}
		this.setHeaderIfAbsent(StrestHeaders.Names.CONTENT_LENGTH, this.content.length);
		this.setHeaderIfAbsent(StrestHeaders.Names.STREST_TXN_ACCEPT, StrestHeaders.Values.SINGLE);
		if (this.getHeader(StrestHeaders.Names.STREST_TXN_ID) == null) { 
			this.setHeader(StrestHeaders.Names.STREST_TXN_ID, StrestUtil.generateTxnId());
		}
	}
	
	/**
	 * creates a header string suitable to be sent to the server.
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private byte[] encodeHeaders() throws UnsupportedEncodingException {
		StringBuilder buf = new StringBuilder();
		this.encodeInitialLine(buf);
		for (Map.Entry<String, String> h: this.getHeaders().getHeaders()) {
            encodeHeader(buf, h.getKey(), h.getValue());
        }
		buf.append(StrestUtil.CRLF);
		return buf.toString().getBytes("ASCII");
	}
	
	protected void encodeInitialLine(StringBuilder b) {
        b.append(this.getMethod());
        b.append(" ");
        b.append(this.getUri());
        b.append(" ");
        b.append(StrestRequest.STREST_VERSION);
        b.append(StrestUtil.CRLF);
	}

	protected void encodeHeader(StringBuilder b, String header, String value) {
		b.append(header);
		b.append(": ");
		b.append(value);
		b.append(StrestUtil.CRLF);
	}
	/**
	 * set the content.
	 * @param contentType
	 * @param bytes
	 */
	public void setContent(String contentType, byte[] bytes) {
		this.content = bytes;
		this.setHeader(StrestHeaders.Names.CONTENT_TYPE, contentType);
		this.setHeader(StrestHeaders.Names.CONTENT_LENGTH, bytes.length);
	}
	
	public byte[] getContent() {
		return this.content;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUserAgent() {
		return userAgent;
	}


	
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}



	public String getMethod() {
		return method;
	}



	/**
	 * sets the method: 
	 * 
	 * GET
	 * POST
	 * PUT
	 * 
	 * etc.
	 * 
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method.toUpperCase().trim();
	}
	
	public StrestHeaders getHeaders() {
		return this.headers;
	}
	
	public void setHeader(String name, Object value) {
		this.headers.setHeader(name, value);
	}
	
	public void setHeaderIfAbsent(String name, Object value) {
		if (this.getHeader(name) == null) {
			this.setHeader(name, value);
		}
	}
	
	public String getHeader(String name) {
		return this.headers.getHeader(name);
	}
	
	@Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        return buf.toString();
    }
}
