/**
 * 
 */
package com.trendrr.oss.concurrent;

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
	
	T object;
	LazyInit lock = new LazyInit();
	
	public abstract T init();
	
	public T get() {
		if (lock.start()) {
			try {
				this.object = this.init();
			} finally {
				lock.end();
			}
		}
		return object;
	}
}
