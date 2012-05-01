/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class ExpirationLock {

	protected static Log log = LogFactory.getLog(ExpirationLock.class);
	
	AtomicLong until = new AtomicLong(0);
	TrendrrLock lock = new TrendrrLock();
	
	public static void main(String ...args) throws Exception{
//		final ExpirationLock lock = new ExpirationLock();
//	
//		for (int i=0; i < 2; i++) {
//			new Thread((new Runnable(){
//				@Override
//				public void run() {
//					log.info("LOCKING ");
//					while(true) {
//						if (lock.lockOrSkip(TimeHelper.addMinutes(new Date(), 1))) {
//							log.info("GOT THE LOCK!");
//							Sleep.seconds(30);
//							log.info("DONE SLEEPING");
//							
//						}
//						Sleep.seconds(5);
//						log.info("Didnt get the lock");
//					}
//					
//				}
//			})).start();
//		}
		
		
		
	}
	
	/**
	 * returns true if the lock was set. false if the lock was already set by someone else.
	 * no need to unlock as the lock expires 
	 * 
	 * non-reentrant.
	 * 
	 * 
	 * @param until
	 * @return
	 */
	public boolean lockOrSkip(Date until) {
		if (!this.isLocked()) {
			log.info("Got timed lock try synch lock");
			
			if (lock.lockOrSkip()) {
				try {
					log.info("Got synch lock, trying timed again");
					if (this.isLocked()) {
						log.info("unable to get second lock");
						//we double check as waiting threads might have just obtained the lock.
						return false;
					}
					this.lockUntil(until);
					log.info("Got lock, locked until: " + this.getLockUntil());
					return true;
				} finally {
					lock.unlock();
				}
			}
			
		}
		return false;
	}
	
	/**
	 * Sets the time that the lock will next be available.
	 * @param date
	 */
	public void lockUntil(Date date) {
		this.until.set(date.getTime());
	}
	
	public Date getLockUntil() {
		return new Date(this.until.get());
	}

	public boolean isLocked() {
		return new Date().before(this.getLockUntil());
	}
}
