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
public interface ChannelRequestHandler {
	public Object handleRequest(String endpoint, Object ...inputs) throws Exception;
}			
