/**
 * 
 */
package com.trendrr.oss.messaging.channel;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created May 17, 2012
 * 
 */
public class ChannelResponse {

	protected static Log log = LogFactory.getLog(ChannelResponse.class);
	private Object result;
	private Exception error;
	
	public ChannelResponse(Object result, Exception error) {
		this.result = result;
		this.error = error;
	}
	
	public Object getResult() {
		return result;
	}
	public Exception getError() {
		return error;
	}
}
