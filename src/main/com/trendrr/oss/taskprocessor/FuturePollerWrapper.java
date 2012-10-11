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
	protected Task task;
	
	
	public FuturePollerWrapper(Future f, FuturePollerCallback callback, long timeout, Task task) {
		this.future = f;
		this.callback = callback;
		this.expire = new Date(new Date().getTime() + timeout);
		this.task = task;
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
		return this.task.getProcessor();
	}
	
	public Task getTask() {
		return this.task;
	}
}
