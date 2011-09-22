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
	ExecutionReportPointId id = new ExecutionReportPointId();

	long millis = 0l;
	long val = 0l;
	
	public ExecutionReportPointId getId() {
		return id;
	}
	public void setId(ExecutionReportPointId id) {
		this.id = id;
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
	
	public String getFullname() {
		return this.id.getFullname();
	}
	
	public void setFullname(String fullname) {
		this.id.setFullname(fullname);
	}
	
	public Date getTimestamp() {
		return this.id.getTimestamp();
	}
	public void setTimestamp(Date timestamp) {
		this.id.setTimestamp(timestamp);
	}
	@Override
	public String toString() {
		return this.id.getFullname() + " | val:" + val + " | millis:" + millis + " | ts:" + IsoDateUtil.getIsoDate(this.id.getTimestamp());
	}
}
