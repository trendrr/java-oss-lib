/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 22, 2011
 * 
 */
public class ExecutionReportPointId {

	protected static Log log = LogFactory.getLog(ExecutionReportPointId.class);
	
	protected String fullname = null;
	protected Date timestamp = null;
	protected TimeAmount amount = null;
	
	public static ExecutionReportPointId instance(String fullname, Date timestamp, TimeAmount amount) {
		ExecutionReportPointId id = new ExecutionReportPointId();
		id.setFullname(fullname);
		id.setTimestamp(timestamp);
		id.setTimeAmount(amount);
		return id;
	}
	
	@Override
	public String toString() {
		StringBuilder id = new StringBuilder();
		id.append(amount.toTrendrrEpoch(timestamp));
		id.append("::"); 
		id.append(amount.abbreviation());
		id.append("::");
		id.append(this.fullname);
		return id.toString();
	}
	
	/**
	 * gets the name (non-qualified)
	 * @return
	 */
	public String getName() {
		String[] nm = this.getFullname().split("\\.");
		return nm[nm.length-1];
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public TimeAmount getTimeAmount() {
		return amount;
	}
	public void setTimeAmount(TimeAmount amount) {
		this.amount = amount;
	}
}
