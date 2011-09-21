/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.IsoDateUtil;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReportPoint {

	protected Log log = LogFactory.getLog(ExecutionReportPoint.class);
	String fullname = null;
	long millis = 0l;
	long val = 0l;
	Date timestamp;
	
	
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public long getMillis() {
		return millis;
	}
	public void setMillis(long millis) {
		this.millis = millis;
	}
	public long getVal() {
		return val;
	}
	public void setVal(long val) {
		this.val = val;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return fullname + " | val:" + val + " | millis:" + millis + " | ts:" + IsoDateUtil.getIsoDate(timestamp);
	}
}
