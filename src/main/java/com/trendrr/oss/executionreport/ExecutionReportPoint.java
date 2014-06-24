/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.IsoDateUtil;
import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReportPoint implements Comparable<ExecutionReportPoint> {

	protected static Log log = LogFactory.getLog(ExecutionReportPoint.class);
	ExecutionReportPointId id = new ExecutionReportPointId();

	long millis = 0l;
	long val = 0l;
	
	public ExecutionReportPointId getId() {
		return id;
	}
	public void setId(ExecutionReportPointId id) {
		this.id = id;
	}
	
	public double getAverageMillis() {
		if (val == 0)
			return 0d;
		return (double)millis / (double)val;
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
	
	public String getName() {
		return this.id.getName();
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
	
	public TimeAmount getTimeAmount() {
		return this.id.getTimeAmount();
	}
	
	public void setTimeAmount(TimeAmount amount) {
		this.id.setTimeAmount(amount);
	}
	@Override
	public String toString() {
		return this.id.getFullname() + " | val:" + val + " | millis:" + millis + " | ts:" + this.getTimestamp();//IsoDateUtil.getIsoDate(this.id.getTimestamp());
	}
	
	/**
	 * converts to a map.  useful so we can cast to a dynmap if needed
	 * @return
	 */
	public Map<String, Object> toMap() {
		DynMap mp = new DynMap();
		mp.put("name", this.getName());
		mp.put("fullname", this.getFullname());
		mp.put("val", this.getVal());
		mp.put("millis", this.getMillis());
		mp.put("ave_millis", this.getAverageMillis());
		mp.put("ts", this.getTimestamp());
		return mp;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExecutionReportPoint o) {
		if (o == null || o.getTimestamp() == null) {
			return -1;
		}
		if (this.getTimestamp() == null) {
			return 1;
		}
		
		int val = this.getTimestamp().compareTo(o.getTimestamp());
		if (val == 0) {
			try {
				return this.getId().toString().compareTo(o.getId().toString());
			} catch (Exception e) {
				log.warn("Caught", e);
			}
		}
		return val;
	}
}
