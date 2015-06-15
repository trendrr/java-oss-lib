/**
 * 
 */
package com.trendrr.oss.appender;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jun 17, 2013
 * 
 */
public interface TimeAmountFileCallback {
	
	/**
	 * Called when a file is considered stale.  File is deleted after this call.
	 * @param file
	 */
	public void staleFile(TimeAmountFile file);
	
	/**
	 * called on any exception. Usually will be an ioexception.
	 * @param x
	 */
	public void onError(Exception x);
}
