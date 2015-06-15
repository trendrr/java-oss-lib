/**
 * 
 */
package com.trendrr.oss.tests.taskprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.taskprocessor.Task;
import com.trendrr.oss.taskprocessor.Task.ASYNCH;
import com.trendrr.oss.taskprocessor.TaskFilter;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class TaskFilterAsynch extends TaskFilter {

	protected static Log log = LogFactory.getLog(TaskFilterAsynch.class);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.taskprocessor.TaskFilter#doFilter(com.trendrr.oss.taskprocessor.Task)
	 */
	@Override
	public Task doFilter(Task task) throws Exception {
//		if (task.getId().endsWith("10")) {
//			throw new Exception("THIS IS AN EXCEPTION");
//		}
		task.asynch(ASYNCH.FAIL_ON_TIMEOUNT, (1000 + (100*Integer.parseInt(task.getId()))));
		return task;
	}
}
