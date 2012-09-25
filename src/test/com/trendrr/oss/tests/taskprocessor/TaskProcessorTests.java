/**
 * 
 */
package com.trendrr.oss.tests.taskprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.taskprocessor.Task;
import com.trendrr.oss.taskprocessor.TaskProcessor;


/**
 * @author Dustin Norlander
 * @created Sep 25, 2012
 * 
 */
public class TaskProcessorTests {

	protected static Log log = LogFactory.getLog(TaskProcessorTests.class);
	static TaskProcessor proc;
	@BeforeClass
	public static void init() {
		proc = new TestTaskProcessor();
	}
	
	@Test
	public void test() {
		try {
			for (int i=0; i < 100; i++) {
				Task t = Task.instance(TaskFilterSleep.class, TaskFilterAsynch.class);
				proc.submitTask(t);
			}
			Sleep.seconds(100);
			
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}
	
}
