/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.Date;
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
	
	/**
	 * passively looks to see if this object needs to be re-initialized
	 * @return
	 */
	public boolean isExpired() {
		return lock.getNextUnlock().getTime() < new Date().getTime();
	}
	
	/**
	 * clears the initialized object if it is expired.
	 */
	public void clearIfExpired() {
		if (lock.lockOrWait()) {
			try {
				ref.set(null);	
			} finally {
				lock.unlock();
			}
		}
		lock.lockUntil(new Date());
	}
}
