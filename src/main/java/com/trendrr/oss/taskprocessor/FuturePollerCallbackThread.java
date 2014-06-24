/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	FuturePollerWrapper wrapper;
	boolean completed;
	
	public FuturePollerCallbackThread(FuturePollerWrapper wrapper, boolean completed) {
		this.wrapper = wrapper;
		this.completed = completed;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (completed) {
				wrapper.getCallback().futureComplete(wrapper.getFuture(), wrapper.getFuture().get(10, TimeUnit.MILLISECONDS));
			} else {
				wrapper.getCallback().futureExpired(wrapper.getFuture());
			}
		} catch (TimeoutException e) {
			wrapper.getFuture().cancel(true);
			wrapper.getCallback().futureExpired(wrapper.getFuture());
		} catch (InterruptedException e) {
			log.error("Caught", e);
			wrapper.getFuture().cancel(true);
			wrapper.getCallback().futureExpired(wrapper.getFuture());
		} catch (ExecutionException e) {
			log.error("Caught", e);
			wrapper.getFuture().cancel(true);
			wrapper.getCallback().futureExpired(wrapper.getFuture());
		}
	}
}
