/**
 * 
 */
package com.trendrr.oss.taskprocessor;

/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public interface TaskCallback {
	public void taskComplete(Task task);
	public void taskError(Task task, Exception x);
}
