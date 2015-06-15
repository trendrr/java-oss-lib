/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public class AsynchTaskWrapper {

	protected static Log log = LogFactory.getLog(AsynchTaskWrapper.class);
	
	Date expire;
	Task task;
	Task.ASYNCH asynch;
	
	public AsynchTaskWrapper(Task task, Task.ASYNCH asynch, Date timeout) {
		this.expire = timeout;
		this.asynch = asynch;
		this.task = task;
	}
	public AsynchTaskWrapper(Task task, Task.ASYNCH asynch, long timeoutMillis) {
		this(task, asynch, new Date(new Date().getTime() + timeoutMillis));
	}
	public Date getExpire() {
		return expire;
	}
	public void setExpire(Date expire) {
		this.expire = expire;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public Task.ASYNCH getAsynch() {
		return asynch;
	}
	public void setAsynch(Task.ASYNCH asynch) {
		this.asynch = asynch;
	}
	
}
