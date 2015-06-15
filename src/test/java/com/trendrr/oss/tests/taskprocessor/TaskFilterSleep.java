/**
 * 
 */
package com.trendrr.oss.tests.taskprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.taskprocessor.Task;
import com.trendrr.oss.taskprocessor.TaskFilter;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class TaskFilterSleep extends TaskFilter {

	protected static Log log = LogFactory.getLog(TaskFilterSleep.class);

	static long sleepMillis = 10000;
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskFilter#doFilter(com.trendrr.oss.taskprocessor.Task)
	 */
	@Override
	public Task doFilter(Task task) throws Exception {
		log.warn("sleep for " + sleepMillis + " millis");
		Sleep.millis(sleepMillis);
		log.warn("Wake up!");
		return task;
	}
}
