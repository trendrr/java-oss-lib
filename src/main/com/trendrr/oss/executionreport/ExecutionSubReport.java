/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;


/**
 * @author Dustin Norlander
 * @created Oct 11, 2012
 * 
 */
public class ExecutionSubReport implements ExecutionReportIncrementor {

	protected static Log log = LogFactory.getLog(ExecutionSubReport.class);

	ExecutionReport report;
	String namespace;
	
	public ExecutionSubReport(String namespace, ExecutionReport report) {
		this.report = report;
		this.namespace = StringHelper.trim(namespace, ".");
	}
	
	/**
	 * 
	 * will return null when it reaches the top.
	 * 
	 * @return
	 */
	public ExecutionReportIncrementor getParent() {
		int ind = namespace.lastIndexOf('.');
		if (ind == -1) {
			return report;
		}
		return new ExecutionSubReport(namespace.substring(0, ind), report);
	}
	
	protected String getKey(String key) {
		return this.namespace + "." + key;
	}
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExReport#inc(java.lang.String, long, java.util.Date)
	 */
	@Override
	public void inc(String key, long amount, Date start) {
		this.report.inc(this.getKey(key), amount, start);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExReport#inc(java.lang.String, long, long)
	 */
	@Override
	public void inc(String key, long amount, long millis) {
		this.report.inc(this.getKey(key), amount, millis);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExReport#inc(java.lang.String, long)
	 */
	@Override
	public void inc(String key, long amount) {
		this.report.inc(this.getKey(key), amount);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExReport#inc(java.lang.String, java.util.Date)
	 */
	@Override
	public void inc(String key, Date start) {
		this.report.inc(this.getKey(key), start);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExReport#inc(java.lang.String)
	 */
	@Override
	public void inc(String key) {
		this.report.inc(this.getKey(key));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#inc(long, java.util.Date)
	 */
	@Override
	public void inc(long amount, Date start) {
		this.inc("", amount, start);
	}
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#inc(long, long)
	 */
	@Override
	public void inc(long amount, long millis) {
		this.inc("", amount, millis);
	}
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#inc(long)
	 */
	@Override
	public void inc(long amount) {
		this.inc("", amount);
	}
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#inc(java.util.Date)
	 */
	@Override
	public void inc(Date start) {
		this.inc("", start);
	}
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#inc()
	 */
	@Override
	public void inc() {
		this.inc("");
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportIncrementor#getChild(java.lang.String)
	 */
	@Override
	public ExecutionReportIncrementor getChild(String key) {
		return new ExecutionSubReport(this.namespace + "." + key, this.report);
	}
}
