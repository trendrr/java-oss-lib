/**
 * 
 */
package com.trendrr.oss.appender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 30, 2011
 * 
 */
public interface RollingFileCallback {
	
	/**
	 * this is called whenever a file is rolled over.  also called on the first init of the 
	 * @param oldFilename  The previous filename.  this will be null on initial load.
	 * @param newFilename  The current filename that will be written to.
	 */
	public void onRollover(String oldFilename, String newFilename);
	
	/**
	 * called before a file is deleted.
	 * @param filename
	 */
	public void beforeDelete(String filename);
	
	/**
	 * called on any exception. Usually will be an ioexception.
	 * @param x
	 */
	public void onError(Exception x);
}
