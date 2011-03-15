/**
 * 
 */
package com.trendrr.oss.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class TrendrrException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7864323098074597701L;

	protected Log log = LogFactory.getLog(TrendrrException.class);
	
	protected String message = null;
	
	protected Exception cause = null;
	
	public TrendrrException () {
		this(null, null);
	}
	
	public TrendrrException(String message) {
		this(message, null);
	}
	
	public TrendrrException(String message, Exception cause) {
		this.message = message;
		this.cause = cause;
		if (message == null && cause != null) {
			this.message = cause.getMessage();
		} 
	}
	
	public TrendrrException(Exception cause) {
		this(null, cause);
	}
	
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Exception getCause() {
		return this.cause;
	}

	public void setCause(Exception cause) {
		this.cause = cause;
	}
}
