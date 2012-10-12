/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.FileCache;
import com.trendrr.oss.PriorityUpdateQueue;
import com.trendrr.oss.concurrent.LazyInitObject;
import com.trendrr.oss.executionreport.ExecutionReport;
import com.trendrr.oss.executionreport.ExecutionReportIncrementor;
import com.trendrr.oss.executionreport.ExecutionSubReport;
import com.trendrr.oss.taskprocessor.Task.ASYNCH;



/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public class TaskProcessor {
	protected static Log log = LogFactory.getLog(TaskProcessor.class);
	
	
	static class TaskProcessorThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        TaskProcessorThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "TP-" + name +"-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
	
	static LazyInitObject<AsynchTaskRegistery> asynchTasks = new LazyInitObject<AsynchTaskRegistery>() {
		@Override
		public AsynchTaskRegistery init() {
			AsynchTaskRegistery reg = new AsynchTaskRegistery();
			reg.start();
			return reg;
		}
	};
	
	protected ExecutorService threadPool = null;
	protected String name;
	protected TaskCallback callback;
	
	//example threadpool, blocks when queue is full. 
//	ExecutorService threadPool = new ThreadPoolExecutor(
//			1, // core size
//		    30, // max size
//		    130, // idle timeout
//		    TimeUnit.SECONDS,
//		    new ArrayBlockingQueue<Runnable>(30), // queue with a size
//		    new ThreadPoolExecutor.CallerRunsPolicy() //if queue is full run in current thread.
//			);
	
	
	/**
	 * creates a new TaskProcessor with a new executorService 
	 * ExecutorService threadPool = new ThreadPoolExecutor(
			1, // core size
		    numThreads, // max size
		    130, // idle timeout
		    TimeUnit.SECONDS,
		    new ArrayBlockingQueue<Runnable>(numThreads), // queue with a size
		    new ThreadPoolExecutor.CallerRunsPolicy() //if queue is full run in current thread.
			);
	 * 
	 * @param name
	 * @param callback
	 * @param numThreads
	 */
	public static TaskProcessor defaultInstance(String name, TaskCallback callback, int numThreads) {
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
				1, // core size
			    numThreads, // max size
			    130, // idle timeout
			    TimeUnit.SECONDS,
			    new ArrayBlockingQueue<Runnable>(numThreads), // queue with a size
			    new TaskProcessorThreadFactory(name),
			    new ThreadPoolExecutor.CallerRunsPolicy() //if queue is full run in current thread.
				);
		
		return new TaskProcessor(name, callback, threadPool);
	}
	
	/**
	 * creates a new task processor.  
	 * 
	 * 
	 * 
	 * 
	 * @param name The name of this processor. used for execution reporting.
	 * @param callback Methods called for every task execution.  this callback is called before the callback specified in the task. can be null. 
	 * @param executor the executor
	 */
	public TaskProcessor(String name, TaskCallback callback, ExecutorService executor) {
		this.threadPool = executor;
		this.name = name;
		this.callback = callback;
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
	
	/**
	 * submit a future, a separate thread will poll it on interval and 
	 * call your callback when isDone is set, or cancel once the timeout has.
	 * 
	 * callback is executed in one of this processors executor threads.
	 * @param future
	 * @param callback
	 */
	public void submitFuture(Task task, Future future, FuturePollerCallback callback, long timeout) {
		FuturePollerWrapper wrapper = new FuturePollerWrapper(future, callback, timeout, task);
		asynchTasks.get().addFuture(wrapper);
	}
	
//	public void resumeAsynch(String taskId) {
//		asynchTasks.get().resume(taskId);
//	}
	
	public void resumeAsynch(Task task) {
		asynchTasks.get().resume(task);
	}
	
	public ExecutorService getExecutor() {
		return this.threadPool;
	}
	/**
	 * A unique name for this processor.  only one instance per name will be allowed.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	public void taskComplete(Task task) {
		if (this.callback != null) {
			this.callback.taskComplete(task);
		}
		if (task.getCallback() != null) {
			task.getCallback().taskComplete(task);
		}
	}
	
	public void taskError(Task task, Exception error) {
		if (this.callback != null) {
			this.callback.taskError(task, error);
		}
		if (task.getCallback() != null) {
			task.getCallback().taskError(task, error);
		}
	}
	
	/**
	 * gets the execution report incrementor for TaskProcessor.{this.getName}
	 * 
	 * @return
	 */
	public ExecutionReportIncrementor getExecutionReport() {
		return new ExecutionSubReport(this.getName(), ExecutionReport.instance("TaskProcessor"));
	}
}
