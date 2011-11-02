/**
 * 
 */
package com.trendrr.oss.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Nov 2, 2011
 * 
 */
public class TrendrrParseException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6171885682624609544L;
	protected Log log = LogFactory.getLog(TrendrrParseException.class);
	
	public TrendrrParseException () {
		this(null, null);
	}
	
	public TrendrrParseException(String message) {
		this(message, null);
	}
	
	public TrendrrParseException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrParseException(Exception cause) {
		this(null, cause);
	}
}
