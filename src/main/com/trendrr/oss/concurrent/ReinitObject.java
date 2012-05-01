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
public abstract class ReinitObject<T> implements Initializer<T>{

	protected static Log log = LogFactory.getLog(ReinitObject.class);
	
	private PeriodicLock lock = null;
	private AtomicReference<T> ref = new AtomicReference<T>();
	private boolean usePreviousIfNull = false;
	
	public ReinitObject(long millisBetweenInit) {
		lock = new PeriodicLock(0, millisBetweenInit);
	}
	
	/**
	 * 
	 * @param millisBetweenInit
	 * @param usePreviousIfNull if init returns null should we use the old result?
	 */
	public ReinitObject(long millisBetweenInit, boolean usePreviousIfNull) {
		lock = new PeriodicLock(0, millisBetweenInit);
		this.usePreviousIfNull = usePreviousIfNull;
	}
	
	public T get() {
		if (ref.get() == null) {
			//we block if the object is unitialized.  otherwise we skip while initia
			if (lock.lockOrWait()) {
				try {
					ref.set(this.init());	
				} finally {
					lock.unlock();
				}
			}
		} else {
			// else we return the old results while the new ones are being initialized
			if (lock.lockOrSkip()) {
				try {
					T val = this.init();
					if (val != null || !this.usePreviousIfNull) {
						ref.set(val);	
					} else {
//						log.info("Skipping cause this init is null");
					}
				} finally {
					lock.unlock();
				}
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
