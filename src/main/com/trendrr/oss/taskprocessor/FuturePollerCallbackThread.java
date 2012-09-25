/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class FuturePollerCallbackThread implements Runnable {

	protected static Log log = LogFactory
			.getLog(FuturePollerCallbackThread.class);

	List<FuturePollerWrapper> completed;
	List<FuturePollerWrapper> expired;
	
	public FuturePollerCallbackThread(List<FuturePollerWrapper> completed, List<FuturePollerWrapper> expired) {
		
		this.completed = completed;
		this.expired = expired;
		
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for (FuturePollerWrapper w : completed) {
			w.getCallback().futureComplete(w.getFuture());
		}
		for (FuturePollerWrapper w : expired) {
			w.getFuture().cancel(true);
			w.getCallback().futureExpired(w.getFuture());
		}
	}
}
