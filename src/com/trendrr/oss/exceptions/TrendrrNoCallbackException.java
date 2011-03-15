/**
 * 
 */
package com.trendrr.oss.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Mar 10, 2011
 * 
 */
public class TrendrrNoCallbackException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1364455592424623331L;
	protected Log log = LogFactory.getLog(TrendrrNoCallbackException.class);
	
	public TrendrrNoCallbackException () {
		this(null, null);
	}
	
	public TrendrrNoCallbackException(String message) {
		this(message, null);
	}
	
	public TrendrrNoCallbackException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrNoCallbackException(Exception cause) {
		this(null, cause);
	}
}
