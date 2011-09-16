/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Simple threadsafe lazy init object.
 * 
 * usage:
 * 
 * LazyInit<MyObject> obj = new LazyInit<MyObject>() {
 * 	@Override
 * 	public MyObject init() {
 * 		return new MyObject();
 * 	}
 * }
 * 
 * 
 * MyObject my = obj.get();
 * 
 * @author Dustin Norlander
 * @created Aug 31, 2011
 * 
 */
public abstract class LazyInitObject<T> {

	protected Log log = LogFactory.getLog(LazyInitObject.class);
	
	AtomicReference<T> object;
	LazyInit lock = new LazyInit();
	
	public abstract T init();
	
	public T get() {
		if (lock.start()) {
			try {
				this.object.set(this.init());
			} finally {
				lock.end();
			}
		}
		return object.get();
	}
	
	/**
	 * will reset this object to initialize again (on the next get() method call)
	 */
	public void reset() {
		lock.reset();
	}
	
	/**
	 * atomically sets the reference and sets the init to not run.
	 * @param object
	 */
	public void set(T object) {
		lock.start();
		try {
			this.object.set(object);
		} finally {
			lock.end();
		}
	}
}
