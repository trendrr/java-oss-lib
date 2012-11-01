/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.executionreport.ExecutionReportIncrementor;


/**
 * static ExecutionReportIncrementer. 
 * 
 * @author Dustin Norlander
 * @created Nov 1, 2012
 * 
 */
public class ER {

	protected static Log log = LogFactory.getLog(ER.class);

	protected static AtomicReference<ExecutionReportIncrementor> er = new AtomicReference<ExecutionReportIncrementor>();
	
	public static void setExecutionReport(ExecutionReportIncrementor ex) {
		er.set(ex);
	}
	
	public static boolean isInitialized() {
		return er.get() != null;
	}
	
	public static void inc(String key, long amount, Date start) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(key, amount, start);
	}


	public static void inc(String key, long amount, long millis) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(key, amount, millis);
	}


	public static void inc(String key, long amount) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(key, amount);
	}

	public static void inc(String key, Date start) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(key, start);
	}

	public static void inc(String key) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(key);
	}

	public static void inc(long amount, Date start) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(amount, start);
	}

	public static void inc(long amount, long millis) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(amount, millis);
	}

	
	public static void inc(long amount) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(amount);
	}

	
	public static void inc(Date start) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc(start);
	}

	public void inc() {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return;
		ex.inc();
	}

	public static ExecutionReportIncrementor getParent() {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return null;
		return ex.getParent();
	}

	public static ExecutionReportIncrementor getChild(String key) {
		ExecutionReportIncrementor ex = er.get();
		if (ex == null)
			return null;
		return ex.getChild(key);
	}
}
