/**
 * 
 */
package com.trendrr.oss.messaging.channel;


import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created May 17, 2012
 * 
 */
class ChannelStopRequest extends ChannelRequest {

	/**
	 * @param endpoint
	 * @param params
	 */
	public ChannelStopRequest() {
		super("kill", new Object[0]);
	}
}
