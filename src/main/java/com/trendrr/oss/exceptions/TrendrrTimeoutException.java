/**
 * 
 */
package com.trendrr.oss.exceptions;


/**
 * @author Dustin Norlander
 * @created Apr 3, 2012
 * 
 */
public class TrendrrTimeoutException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3732598247458776183L;

	public TrendrrTimeoutException () {
		this(null, null);
	}
	
	public TrendrrTimeoutException(String message) {
		this(message, null);
	}
	
	public TrendrrTimeoutException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrTimeoutException(Exception cause) {
		this(null, cause);
	}
}
