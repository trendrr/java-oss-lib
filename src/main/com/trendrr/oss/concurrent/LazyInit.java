/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * Class to allow lazy initializations, fully threadsafe without any synchronization.
 * 
 * 
 * usage: 
 * 
 * if (lazyInit.start()) {
 * 	try {
 * 		//initialization code here.
 *  } finally {
 *  	lazyInit.end();
 *  }
 * }
 * 
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 31, 2011
 * 
 */
public class LazyInit {

	protected static Log log = LogFactory.getLog(LazyInit.class);
	
	protected AtomicBoolean once = new AtomicBoolean(false);
	
	protected ReentrantLock lock = new ReentrantLock();

	/**
	 * will return true on the first time it is called, false on every subsequent call.  
	 * it will block only if another thread is running the initialization code. 
	 * 
	 * 
	 * @return
	 */
	public boolean start() {
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

	public void end() {
		once.set(true);
		lock.unlock();
	}

	public void reset() {
		once.set(false);
	}
	
	/**
	 * passively checks if this lock has been initialized.
	 * Will return true if the lock is currently held.
	 * 
	 * @return
	 */
	public boolean isInited() {
		if (once.get()) {
			return true;
		}
		if (lock.isLocked()) {
			return true;
		}
		return once.get();
	}
}
