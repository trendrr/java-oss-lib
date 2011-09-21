/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 21, 2011
 * 
 */
public class ExecutionReportConfig {

	protected Log log = LogFactory.getLog(ExecutionReportConfig.class);
	
	protected AtomicReference<ExecutionReportSerializer> serializer = new AtomicReference<ExecutionReportSerializer>(new DummyExecutionReportDBConnector());
	protected AtomicLong flushMillis = new AtomicLong(1000*30); //how often to flush 
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
