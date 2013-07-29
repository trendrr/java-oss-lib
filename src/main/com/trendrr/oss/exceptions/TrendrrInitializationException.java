/**
 * 
 */
package com.trendrr.oss.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An exception for when an initialization fails.
 * 
 * 
 * @author Dustin Norlander
 * @created Jul 26, 2013
 * 
 */
public class TrendrrInitializationException extends TrendrrException {

	protected static Log log = LogFactory
			.getLog(TrendrrInitializationException.class);
	
	public TrendrrInitializationException () {
		this(null, null);
	}
	
	public TrendrrInitializationException(String message) {
		this(message, null);
	}
	
	public TrendrrInitializationException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrInitializationException(Exception cause) {
		this(null, cause);
	}
}
