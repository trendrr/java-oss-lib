/**
 * 
 */
package com.trendrr.oss.messaging.channel;


import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created May 16, 2012
 * 
 */
public class ChannelRequest {
	String endpoint = "";
	Object[] inputs;
	
	public ChannelRequest(String endpoint, Object ...inputs) {
		this.endpoint = endpoint;
		this.inputs = inputs;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Object[] getInputs() {
		return inputs;
	}
}
