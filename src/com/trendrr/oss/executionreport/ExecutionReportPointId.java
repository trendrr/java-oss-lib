/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 22, 2011
 * 
 */
public class ExecutionReportPointId {

	protected Log log = LogFactory.getLog(ExecutionReportPointId.class);
	
	protected String fullname = null;
	protected Date timestamp = null;
	protected Timeframe frame = null;
	
	public static ExecutionReportPointId instance(String fullname, Date timestamp, Timeframe frame) {
		ExecutionReportPointId id = new ExecutionReportPointId();
		id.setFullname(fullname);
		id.setTimestamp(timestamp);
		id.setTimeframe(frame);
		return id;
	}
	
	public byte[] toIdBytes() throws Exception {
		StringBuilder id = new StringBuilder();
		id.append(frame.toTrendrrEpoch(timestamp));
		id.append("::"); 
		id.append(frame);
		id.append("::");
		id.append(this.fullname);
		return StringHelper.sha1(id.toString().getBytes("utf8"));
	}
	
	/**
	 * gets the name (non-qualified)
	 * @return
	 */
	public String getName() {
		String[] nm = this.getFullname().split("\\.");
		return nm[nm.length-1];
	}
	
	public String toIdString() throws Exception {
		return StringHelper.toHex(this.toIdBytes());
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
	public Timeframe getTimeframe() {
		return frame;
	}
	public void setTimeframe(Timeframe frame) {
		this.frame = frame;
	}
	
	
	
}
