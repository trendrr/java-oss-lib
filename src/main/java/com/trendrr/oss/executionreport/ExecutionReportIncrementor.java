/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Date;

/**
 * @author Dustin Norlander
 * @created Oct 11, 2012
 * 
 */
public interface ExecutionReportIncrementor {
	public void inc(String key, long amount, Date start);
	public void inc(String key, long amount, long millis);
	public void inc(String key, long amount);
	public void inc(String key, Date start);
	public void inc(String key);
	
	public void inc(long amount, Date start);
	public void inc(long amount, long millis);
	public void inc(long amount);
	public void inc(Date start);
	public void inc();
	
	/**
	 * should return the parent execution report incrementor, or null if this is top level.
	 * @return
	 */
	public ExecutionReportIncrementor getParent();
	
	public ExecutionReportIncrementor getChild(String key);
}
