/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 21, 2011
 * 
 */
public class ExecutionReportConfig {

	protected static Log log = LogFactory.getLog(ExecutionReportConfig.class);
	
	protected AtomicReference<ExecutionReportSerializer> serializer = new AtomicReference<ExecutionReportSerializer>(new DummyExecutionReportDBConnector());
	protected AtomicLong flushMillis = new AtomicLong(1000*30); //how often to flush 
	
	protected Set<TimeAmount> timeamounts = new HashSet<TimeAmount>();
	
	public ExecutionReportConfig() {
		timeamounts.add(TimeAmount.instance(Timeframe.MINUTES, 1));
		timeamounts.add(TimeAmount.instance(Timeframe.HOURS, 1));
		timeamounts.add(TimeAmount.instance(Timeframe.DAYS, 1));
	}
	
	/**
	 * The timeframes that the serializer should use.  this is backed by a set so no dups.
	 * @return
	 */
	public Collection<TimeAmount> getTimeAmounts() {
		return timeamounts;
	}
	public void setTimeAmounts(Collection<TimeAmount> timeamounts) {
		this.timeamounts.clear();
		this.timeamounts.addAll(timeamounts);
	}
	
	public ExecutionReportSerializer getSerializer() {
		return serializer.get();
	}
	public void setSerializer(ExecutionReportSerializer serializer) {
		this.serializer.set(serializer);
	}
	public long getFlushMillis() {
		return flushMillis.get();
	}
	public void setFlushMillis(long flushMillis) {
		this.flushMillis.set(flushMillis);
	}
	
	
	
}
