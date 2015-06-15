/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class TrendrrLock {

	protected static Log log = LogFactory.getLog(TrendrrLock.class);
	
	protected AtomicBoolean once = new AtomicBoolean(false);
	
	ReentrantLock lock = new ReentrantLock();
	
	public static void main(String ...args) throws Exception{
		final TrendrrLock lock = new TrendrrLock();
	
		for (int i=0; i < 3; i++) {
			new Thread((new Runnable(){
				@Override
				public void run() {
					log.info("LOCKING ");
					if (lock.lockOnce()) {
						log.info("GOT THE LOCK MOFO!");
						Sleep.seconds(30);
						log.info("DONE SLEEPING");
						lock.unlock();
					}
					log.info("UNLOCKING");
				}
			})).start();
		}
		
		
		
	}
	
	/**
	 * returns true if lock was obtained, false otherwise.
	 * @return
	 */
	public boolean lockOrSkip() {
		try {
			return lock.tryLock(0, TimeUnit.SECONDS);
		} catch (Exception x) {
			log.error("Caught", x); //this should never happen
		}
		return false;
	}
	
	public boolean isLocked() {
		return lock.isLocked();
	}
	
	public void lockOrWait() {
		lock.lock();
	}
	
	public void unlock() {
		once.set(true);
		lock.unlock();
	}
	
	/**
	 * used for lazy initializations. will block if and only if initialization is currently happening.
	 * 
	 * returns true if successfully locked, false otherwise
	 * 
	 * remember to unlock!
	 * 
	 * reset by calling resetLockOnce();
	 * 
	 */
	public boolean lockOnce() {
		if (!once.get()) {
			//now lock..
			lock.lock();
			//check if a previous thread already initialized
			if (once.get()) {
				lock.unlock();
			}
		}
		return !once.get();
	}
	
	/**
	 * use this to reset the value for lockOnce..
	 * @param val
	 */
	public void resetLockOnce() {
		once.set(false);
	}
	
	/**
	 * set to true if you say the the lock has already been gotten
	 * @param val
	 */
	public void setLockOnce(boolean val) {
		once.set(val);
	}
}
