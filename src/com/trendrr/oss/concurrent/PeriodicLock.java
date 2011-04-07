/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * this lock will allow one thread access every x milliseconds.
 * 
 * all threads will block while the single thread has the lock.
 * it is useful for reinitialization code. 
 * 
 * @author dustin
 *
 */
public class PeriodicLock {

	protected Log log = LogFactory.getLog(PeriodicLock.class);
	
	ExpirationLock timedlock = new ExpirationLock();
	TrendrrLock lock = new TrendrrLock();
	long period;
	public PeriodicLock(long delay, long period) {		
		Date until = new Date( new Date().getTime() + delay);
		timedlock.lockUntil(until);
		this.period = period;
	}
	
	/**
	 * will return true if the lock is obtained. false otherwise.
	 * 
	 * will block if another thread currently holds the lock, then will return 
	 * false when the other thread releases the lock.
	 * 
	 * @return
	 */
	public boolean lockOrWait() {
		if (timedlock.isLocked() && !lock.isLocked()) {
//			log.info("timedlock is not locked: returning");
			return false;
		}
//		log.info("lock or wait");
		lock.lockOrWait();
//		log.info("got periodic lock");
		if (timedlock.isLocked()) {
			lock.unlock();
			return false;
		}
		timedlock.lockUntil(new Date( new Date().getTime() + this.period));
		return true;
	}
	
	/**
	 * same as above, only will return false if the timedlock is locked or if another thread is
	 * accessing the restricted code.
	 * 
	 * @return
	 */
	public boolean lockOrSkip() {
		if (timedlock.isLocked())
			return false;

		if (lock.lockOrSkip() ) {
			if (timedlock.isLocked()) {
				lock.unlock();
				return false;
			}
			timedlock.lockUntil(new Date( new Date().getTime() + this.period));
			return true;
		} 
		return false;
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	/**
	 * forces the next time the lock will unlock.
	 * @param date
	 */
	public void lockUntil(Date date) {
		lock.lockOrWait();
		try {
			this.timedlock.lockUntil(date);
		} finally {
			lock.unlock();
		}
	}
	
	public Date getNextUnlock() {
		return timedlock.getLockUntil();
	}
}
