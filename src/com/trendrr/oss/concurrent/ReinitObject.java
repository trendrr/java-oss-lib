/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An object that will be lazily initialized, and reinitialize every X millis
 * 
 * 
 * @author Dustin Norlander
 * @created Dec 6, 2010
 * 
 */
public abstract class ReinitObject<T> {

	protected Log log = LogFactory.getLog(ReinitObject.class);
	
	PeriodicLock lock = null;
	AtomicReference<T> ref = new AtomicReference<T>();
	
	public ReinitObject(long millisBetweenInit) {
		lock = new PeriodicLock(0, millisBetweenInit);
	}
	
	public abstract T init();
	
	public T get() {
		if (lock.lockOrWait()) {
			try {
				ref.set(this.init());	
			} finally {
				lock.unlock();
			}
		}
		return ref.get();
	}
}
