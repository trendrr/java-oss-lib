/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.executionreport.ExecutionReport;
import com.trendrr.oss.executionreport.ExecutionReportIncrementor;


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
	
	/**
	 * gets a filter level execution report incrementor.
	 * 
	 * 
	 * @param task
	 * @return
	 */
	public ExecutionReportIncrementor getExecutionReport(Task task) {
		return task.getProcessor().getExecutionReport().getChild(this.getName());
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
