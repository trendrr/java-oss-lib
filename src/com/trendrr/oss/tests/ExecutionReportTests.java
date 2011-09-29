/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.WhatsMyIp;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.executionreport.ExecutionReport;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReportTests {

	protected Log log = LogFactory.getLog(ExecutionReportTests.class);
	
	@Test
	public void test() {
		ExecutionReport report = ExecutionReport.instance("TEST");
		ExecutionReport.setJvmInstanceId(WhatsMyIp.getIP());
		Date start = new Date();
		for (int i=0 ;  i < 100; i++) {
			report.inc("this.key", 1, start);
			report.inc("this", 1, start);
		}
		Sleep.seconds(30);
		
		for (int i=0 ;  i < 150; i++) {
			report.inc("this.key", 1, 1);
			report.inc("this", 1, 1);
		}
		Sleep.seconds(30);
		
	}
}
