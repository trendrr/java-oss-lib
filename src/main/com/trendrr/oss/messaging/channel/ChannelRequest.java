/**
 * 
 */
package com.trendrr.oss.messaging.channel;


import com.trendrr.oss.DynMap;
import com.trendrr.oss.concurrent.SafeBox;


/**
 * @author Dustin Norlander
 * @created May 16, 2012
 * 
 */
public class ChannelRequest {
	String endpoint = "";
	Object[] inputs;
	
	SafeBox<ChannelResponse> response = new SafeBox<ChannelResponse>();
	
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
	
	public void setResponse(ChannelResponse response) {
		try {
			this.response.set(response);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ChannelResponse awaitResponse() throws InterruptedException {
		return response.getAndClear();
		
	}
}
