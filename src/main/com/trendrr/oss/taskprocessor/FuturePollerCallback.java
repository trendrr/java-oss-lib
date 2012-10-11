/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public interface FuturePollerCallback {
	/**
	 * this is called once the future returns isDone or isCancelled. 
	 * @param f
	 */
	public void futureComplete(Future f, Object result);
	
	
	/**
	 * the future has expired.  The poller has already cancelled this future, so no need to cancel.
	 * @param f
	 */
	public void futureExpired(Future f);
	
}
