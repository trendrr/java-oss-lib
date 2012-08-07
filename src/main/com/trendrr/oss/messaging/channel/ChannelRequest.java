/**
 * 
 */
package com.trendrr.oss.messaging.channel;


import java.util.concurrent.Semaphore;

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
	
	Semaphore lock = new Semaphore(1, true);
	ChannelResponse response = null;
	
	public ChannelRequest(String endpoint, Object ...inputs) {
		this.endpoint = endpoint;
		this.inputs = inputs;
		try {
			lock.acquire(1);
		} catch (Exception x) {
			x.printStackTrace();
		}
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
			this.response = response;
			lock.release(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ChannelResponse awaitResponse() throws Exception {
		lock.acquire(1);
		return response;
		
	}
}