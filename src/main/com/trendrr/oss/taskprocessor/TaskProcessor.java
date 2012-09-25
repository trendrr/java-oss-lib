/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.FileCache;
import com.trendrr.oss.PriorityUpdateQueue;
import com.trendrr.oss.concurrent.LazyInitObject;
import com.trendrr.oss.executionreport.ExecutionReport;
import com.trendrr.oss.taskprocessor.Task.ASYNCH;



/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public abstract class TaskProcessor {
	protected static Log log = LogFactory.getLog(TaskProcessor.class);
	
	static LazyInitObject<AsynchTaskRegistery> asynchTasks = new LazyInitObject<AsynchTaskRegistery>() {
		@Override
		public AsynchTaskRegistery init() {
			AsynchTaskRegistery reg = new AsynchTaskRegistery();
			reg.start();
			return reg;
		}
	};
	
	ExecutorService threadPool = null;
	
	//example threadpool, blocks when queue is full. 
//	ExecutorService threadPool = new ThreadPoolExecutor(
//			1, // core size
//		    30, // max size
//		    130, // idle timeout
//		    TimeUnit.SECONDS,
//		    new ArrayBlockingQueue<Runnable>(30), // queue with a size
//		    new ThreadPoolExecutor.CallerRunsPolicy() //if queue is full run in current thread.
//			);
	
	public TaskProcessor(ExecutorService executor) {
		this.threadPool = executor;
		
	}
	
	/**
	 * submits a task for execution.
	 * @param task
	 */
	public void submitTask(Task task) {
		if (task.getSubmitted() == null) {
			task.submitted();
		}
		task.setProcessor(this);
		//add to the executor service.. 
		TaskFilterRunner runner = new TaskFilterRunner(task);
		this.threadPool.execute(runner);
	}
	
	public void setAsynch(Task t, ASYNCH asynch, long timeout) {
		asynchTasks.get().add(t, asynch, timeout);
	}
	
	public void resumeAsynch(String taskId) {
		asynchTasks.get().resume(taskId);
	}
	
	/**
	 * A unique name for this processor.  only one instance per name will be allowed.
	 * @return
	 */
	public abstract String getName();
	
	public void _taskComplete(Task task) {
		ExecutionReport report = ExecutionReport.instance("TaskProcessor");
		report.inc(this.getName() + ".FILTER_CHAIN", task.getSubmitted());
		this.taskComplete(task);
	}
	
	public abstract void taskComplete(Task task);
	
	
	public abstract void taskError(Task task, Exception error);
	
	
}
