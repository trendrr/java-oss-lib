/**
 * 
 */
package com.trendrr.oss.tests.taskprocessor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.taskprocessor.Task;
import com.trendrr.oss.taskprocessor.TaskProcessor;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class TestTaskProcessor extends TaskProcessor {

	/**
	 * @param executor
	 */
	public TestTaskProcessor() {
		super("UNITTEST", null, new ThreadPoolExecutor(
			30, // core size
		    30, // max size
		    130, // idle timeout
		    TimeUnit.SECONDS,
		    new ArrayBlockingQueue<Runnable>(30), // queue with a size
		    new ThreadPoolExecutor.CallerRunsPolicy() //if queue is full run in current thread.
			));
	}

	protected static Log log = LogFactory.getLog(TestTaskProcessor.class);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskProcessor#getName()
	 */
	@Override
	public String getName() {
		return "UNITTESTER";
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskProcessor#taskComplete(com.trendrr.oss.taskprocessor.Task)
	 */
	@Override
	public void taskComplete(Task task) {
		System.out.println(task.getId() + " COMPLETED");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskProcessor#taskError(com.trendrr.oss.taskprocessor.Task, java.lang.Exception)
	 */
	@Override
	public void taskError(Task task, Exception error) {
		error.printStackTrace();
	}
	
	
	public void printFreeList() {
		
		
		
	}
}
