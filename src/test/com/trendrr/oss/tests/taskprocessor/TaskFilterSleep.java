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

	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskFilter#doFilter(com.trendrr.oss.taskprocessor.Task)
	 */
	@Override
	public Task doFilter(Task task) throws Exception {
		System.out.println("sleeping for 100 millis");
		Sleep.millis(100);
		return task;
	}
}
