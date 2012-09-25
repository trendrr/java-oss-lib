/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Date;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class FuturePollerWrapper {

	protected static Log log = LogFactory.getLog(FuturePollerWrapper.class);
	
	protected Future future;
	protected Date expire;
	protected FuturePollerCallback callback;
	protected TaskProcessor processor;
	
	
	public FuturePollerWrapper(Future f, FuturePollerCallback callback, long timeout, TaskProcessor processor) {
		this.future = f;
		this.callback = callback;
		this.expire = new Date(new Date().getTime() + timeout);
		this.processor = processor;
	}

	public Future getFuture() {
		return future;
	}

	public Date getExpire() {
		return expire;
	}

	public FuturePollerCallback getCallback() {
		return callback;
	}
	
	public TaskProcessor getProcessor() {
		return this.processor;
	}
}
