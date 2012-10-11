/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.executionreport.ExecutionReport;


/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public abstract class TaskFilter {

	protected static Log log = LogFactory.getLog(TaskFilter.class);
	
	/**
	 * does the processing of this task.  the filter chain will stop if this returns null or throws an exception,
	 * else the next filter will run with the results from this filter.
	 * 
	 * TaskFilters should be stateless, and assumed to be running in multiple threads on multiple tasks. 
	 * 
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public abstract Task doFilter(Task task) throws Exception;
	
//	/**
//	 * increments a key as a child of this filter.
//	 * @param task
//	 * @param key
//	 * @param amount
//	 * @param start
//	 */
//	public void executionReportInc(Task task, String key, long amount, Date start) {
//		ExecutionReport.instance("TaskProcessor").inc(task.getProcessor().getName() + "." + this.getName() + "." + key, amount, start);
//	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
