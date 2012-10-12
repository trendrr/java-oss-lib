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
		
		TaskFilter f = t.popFilter();
		
		if (f == null && t.isAsynch()) {
			//still increment firehose on the end of an asynch task.
			this.taskComplete(t);
			return;
		}
		t.asynch = false;
		Exception error = null;
		while(f != null && error == null) {
			Date start = new Date();
			
			try {
				Task newT = f.doFilter(t);
				if (newT == null) {
					if (!t.asynch) {
						this.taskComplete(t);
					}
					return;
				}
				t = newT;
				
			} catch (Exception x) {
				log.error("Caught",x);
				error = x;
			} finally {
				t.getProcessor().getExecutionReport().inc(f.getName(), start);
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
			this.taskComplete(t);
		}
	}
	
	private void taskComplete(Task t) {
		t.getProcessor().taskComplete(t);
		t.getProcessor().getExecutionReport().inc(t.getSubmitted());
	}
}
