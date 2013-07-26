/**
 * 
 */
package com.trendrr.oss.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jul 26, 2013
 * 
 */
public class TrendrrClosedException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7512869009718546223L;
	protected static Log log = LogFactory.getLog(TrendrrClosedException.class);
	
	public TrendrrClosedException () {
		this(null, null);
	}
	
	public TrendrrClosedException(String message) {
		this(message, null);
	}
	
	public TrendrrClosedException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrClosedException(Exception cause) {
		this(null, cause);
	}
}
