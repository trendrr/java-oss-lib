/**
 * 
 */
package com.trendrr.oss.exceptions;


/**
 * @author Dustin Norlander
 * @created Mar 16, 2012
 * 
 */
public class TrendrrOverflowException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8629895285906981853L;

	public TrendrrOverflowException () {
		this(null, null);
	}
	
	public TrendrrOverflowException(String message) {
		this(message, null);
	}
	
	public TrendrrOverflowException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrOverflowException(Exception cause) {
		this(null, cause);
	}
}
