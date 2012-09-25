/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.PriorityUpdateQueue;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrTimeoutException;
import com.trendrr.oss.taskprocessor.Task.ASYNCH;


/**
 * Holds the tasks that a paused on some asynch task.
 * 
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class AsynchTaskRegistery implements Runnable{

	protected static Log log = LogFactory.getLog(AsynchTaskRegistery.class);

	protected ConcurrentHashMap<String, AsynchTaskWrapper> asynchTasks = new ConcurrentHashMap<String, AsynchTaskWrapper>();
	protected PriorityUpdateQueue<AsynchTaskWrapper> asynchTaskFreeList = new PriorityUpdateQueue<AsynchTaskWrapper>(new Comparator<AsynchTaskWrapper>() {
		@Override
		public int compare(AsynchTaskWrapper o1, AsynchTaskWrapper o2) {
			//want the oldest items first
			return o2.getExpire().compareTo(o1.getExpire());
		}
	});
	
	protected Thread thread = null;
	
	public synchronized void start() {
		if (thread != null) {
			return; //already started
		}
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * Resumes the execution of the task
	 * @param id
	 * @return
	 */
	public Task resume(String id) {
		Task t = this.remove(id);
		if (t == null)
			return null;
		t.asynch = false;
		t.getProcessor().submitTask(t);
		return t;
	}
	
	/**
	 * remove the task from the registry, does not resume its execution
	 * @param id
	 * @return
	 */
	public Task remove(String id) {
		AsynchTaskWrapper w = asynchTasks.remove(id);
		if (w == null)
			return null;
		asynchTaskFreeList.remove(w);
		return w.getTask();
	}
	
	/**
	 * add a task to the registry.
	 * @param task
	 * @param asynch
	 * @param timeout
	 */
	public void add(Task task, ASYNCH asynch, long timeout) {
		AsynchTaskWrapper w = new AsynchTaskWrapper(task, asynch, timeout);
		if (this.asynchTasks.putIfAbsent(task.getId(), w) != null) {
			log.error("task : " + task.getId() + " is already in the task registry!");
			return;
		} 
		this.asynchTaskFreeList.push(w);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//free thread.
		while(true) {
			AsynchTaskWrapper t = asynchTaskFreeList.peek();
			while(t != null && t.getExpire().before(new Date())) {
				t = asynchTaskFreeList.pop();
				this.expired(t);
				t = asynchTaskFreeList.peek();
			}
			System.out.println("Don expiring, sleep ... size: " + this.asynchTasks.size());
			Sleep.millis(500);
		}
	}
	
	protected void expired(AsynchTaskWrapper t) {
		System.out.println("Expiring: " + t.getTask().getId());
		if (this.asynchTasks.remove(t.getTask().getId()) == null) {
			return; //already processed somewhere else
		}
		if (t.getAsynch() == ASYNCH.CONTINUE_ON_TIMEOUT) {
			t.getTask().asynch = false;
			t.getTask().getProcessor().submitTask(t.getTask());
			
		} else if (t.getAsynch() == ASYNCH.FAIL_ON_TIMEOUNT) {
			t.getTask().getProcessor().taskError(t.getTask(), new TrendrrTimeoutException("Asynch Task with id: " + t.getTask().getId() + " timed out!"));
		}
	}
}
