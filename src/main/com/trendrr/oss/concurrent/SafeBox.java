/**
 * 
 */
package com.trendrr.oss.concurrent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides a safe way to wait on a value to be set.
 * 
 * from the guava wiki.
 * 
 * The box holds exactly one value at a time
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created May 23, 2012
 * 
 */
public class SafeBox<V> {

	protected static Log log = LogFactory.getLog(SafeBox.class);
	private V value = null;
	
	public synchronized V getAndClear() throws InterruptedException {
	  while (value == null) {
	    wait();
	  }
	  V result = value;
	  value = null;
	  notify();
	  return result;
	}
	
	public synchronized void set(V newValue) throws InterruptedException {
	  while (value != null) {
	    wait();
	  }
	  value = newValue;
	  notify();
	}




}
