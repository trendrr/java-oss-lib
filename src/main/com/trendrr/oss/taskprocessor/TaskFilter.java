/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public abstract class TaskFilter {

	protected static Log log = LogFactory.getLog(TaskFilter.class);
	
	
	public abstract Task doFilter(Task task) throws Exception;
	
}
