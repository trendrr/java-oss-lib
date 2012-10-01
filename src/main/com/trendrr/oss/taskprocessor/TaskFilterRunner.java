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
class TaskFilterRunner implements Runnable {

	protected static Log log = LogFactory.getLog(TaskFilterRunner.class);

	Task task;
	
	TaskFilterRunner(Task task) {
		this.task = task;
	}
	
	@Override
	public void run() {
		Task t = task;
		if (t == null) {
			log.error("Task is null, can't run anything..");
			return;
		}
		ExecutionReport report = ExecutionReport.instance("TaskProcessor");
		TaskFilter f = t.popFilter();
		Exception error = null;
		while(f != null && error == null) {
			Date start = new Date();
			
			try {
				Task newT = f.doFilter(t);
				if (newT == null) {
					if (!t.asynch) {
						t.getProcessor().taskComplete(t);
					}
					return;
				}
				t = newT;
				
			} catch (Exception x) {
				log.error("Caught",x);
				error = x;
			} finally {
				report.inc(t.getProcessor().getName() + "." + f.getClass().getSimpleName(), start);
			}
			
			if (t.asynch) {
				return;
			}
			
			if (error == null) {
				f = t.popFilter();
			}
			
		}
		if (error != null) {
			t.getProcessor().taskError(t, error);
		} else if (!t.asynch) {
			t.getProcessor().taskComplete(t);
			report.inc(t.getProcessor().getName(), t.getSubmitted());
		}
	}
}
