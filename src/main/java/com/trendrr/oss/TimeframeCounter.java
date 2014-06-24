/**
 * 
 */
package com.trendrr.oss;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;


/**
 * 
 * Threadsafe counter that keeps counts of the past and current minute, second, ect.
 * 
 * 
 * @author Dustin Norlander
 * @created Mar 22, 2012
 * @deprecated use TimeAmountCounter instead
 */
public class TimeframeCounter {

	protected static Log log = LogFactory.getLog(TimeframeCounter.class);
	
	AtomicLong current = new AtomicLong(0);
	AtomicLong previous = new AtomicLong(0l);
	AtomicLong previousEpoch = new AtomicLong(0l);
	Timeframe timeframe = Timeframe.MINUTES;
	AtomicLong epoch = new AtomicLong(0);
	
	public TimeframeCounter(Timeframe frame) {
		this.timeframe = frame;
	}

	public long inc() {
		return this.inc(1);
	}
	public long inc(long val) {
		long curepoch = timeframe.toTrendrrEpoch(new Date()).longValue();
		long oldepoch = this.epoch.getAndSet(curepoch);
		if (oldepoch != curepoch) {
			previous.set(current.getAndSet(0));
			previousEpoch.set(oldepoch);
		}
		return current.addAndGet(val);
	}
	
	/**
	 * Gets the current value for this timeframe
	 * @return
	 */
	public long getCurrent() {
		if (timeframe.toTrendrrEpoch(new Date()).longValue() != this.epoch.get()) {
			return 0l;
		}
		return this.current.get();
	}
	
	/**
	 * gets the most recently completed from one timeframe ago (ie. yesterday, an hour ago, ect).
	 * @return
	 */
	public long getPrevious() {
		if ((timeframe.toTrendrrEpoch(new Date()).longValue()-1) != this.previousEpoch.get()) {
			return 0l;
		}
		return this.previous.get();
	}
	
}
