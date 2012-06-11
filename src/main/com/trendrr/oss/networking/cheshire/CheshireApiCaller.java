/**
 * 
 */
package com.trendrr.oss.networking.cheshire;

import java.util.Map;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrTimeoutException;


/**
 * An interface that represents a class that can make an api call either synchronously or asynchronously.
 * 
 * 
 * @author Dustin Norlander
 * @created Oct 20, 2011
 * 
 */
public interface CheshireApiCaller {

	/**
	 * Does an asynchronous api call.  This method returns immediately. the Response or error is sent to the callback.
	 * 
	 * 
	 * @param endPoint
	 * @param method
	 * @param params
	 * @param callback
	 */
	public void apiCall(String endPoint, Verb method, Map params, CheshireApiCallback callback);
	
	/**
	 * closes this connection
	 */
	public void close();
	
	/**
	 * A synchronous call.  blocks until response is available.  Please note that this does *NOT* block concurrent api calls, so you can continue to 
	 * make calls in other threads.
	 * 
	 * If the maxReconnectAttempts is non-zero (-1 is infinit reconnect attempts), then this will attempt to reconnect and send on any io problems. 
	 * 
	 * @param endPoint
	 * @param method
	 * @param params
	 * @param timeoutMillis throw an exception if this # of millis passes,  < 1 should be infinite.
	 * @return
	 * @throws Exception
	 */
	public DynMap apiCall(String endPoint, Verb method, Map params, long timeoutMillis) throws TrendrrTimeoutException, TrendrrException;
	
	
	/**
	 * gets the host address
	 * @return
	 */
	public String getHost();
	
	/**
	 * gets the cheshire port
	 * @return
	 */
	public int getPort();
}
