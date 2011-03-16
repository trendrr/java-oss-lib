/**
 * 
 */
package com.trendrr.oss.networking.strest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestResponse {

	protected Log log = LogFactory.getLog(StrestResponse.class);
	
	StrestHeaders headers = new StrestHeaders();
	private byte[] content = new byte[0];
	private int responseCode;
	private String responseMessage;
	
	public void parseHeaders(String header) {
		String[] lines = header.split(StrestUtil.CRLF);
		String firstLine = lines[0];
		int index = firstLine.indexOf(' ');
		String protocol = firstLine.substring(0, index).trim();
		firstLine = firstLine.substring(index).trim();
		index = firstLine.indexOf(' ');
		String code = firstLine.substring(0, index).trim();
		String message = firstLine.substring(index).trim();
		this.responseCode = Integer.parseInt(code);
		this.responseMessage = message;
		for (int i=1; i < lines.length; i++) {
			this.headers.parseHeader(lines[i]);
		}
	}
	
	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getHeader(String name) {
		return this.headers.getHeader(name);
	}
	
	public byte[] getContent() {
		//TODO: auto handle gzip
		return this.content;
	}
	
	public void setContent(byte[] bytes) {
		this.content = bytes;
	}
	
}
