/**
 * 
 */
package com.trendrr.oss.counters;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;

/**
 * 
 * Threadsafe counter that keeps counts of the past and current minute, second, ect.
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 31, 2013
 * 
 */
public class TimeAmountCounter {

	protected static Log log = LogFactory.getLog(TimeAmountCounter.class);
	
	protected AtomicLong current = new AtomicLong(0);
	protected AtomicLong previous = new AtomicLong(0l);
	protected AtomicLong previousEpoch = new AtomicLong(0l);
	protected TimeAmount timeframe = TimeAmount.instance(Timeframe.MINUTES);
	protected AtomicLong epoch = new AtomicLong(0);
	protected TimeAmountCounterCallback callback = null;
			
			
	public TimeAmountCounter(TimeAmount timeamount, TimeAmountCounterCallback callback) {
		this.timeframe = timeamount;
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
			//rolled over
			if (callback != null) {
				callback.onRollover(this, this.timeframe.fromTrendrrEpoch(oldepoch), previous.get());
			}
		}
		return current.addAndGet(val);
	}
	
	/**
	 * triggers a rollover if necessary.
	 * this is just a pointer to inc(0l);
	 */
	public void rolloverIfNeeded() {
		this.inc(0);
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
	
	public TimeAmount getTimeAmount() {
		return this.timeframe;
	}
}
