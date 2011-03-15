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
public abstract class TrendrrNetworkingException extends TrendrrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7179100604781833744L;
	protected Log log = LogFactory.getLog(TrendrrNetworkingException.class);
	
	public TrendrrNetworkingException () {
		this(null, null);
	}
	
	public TrendrrNetworkingException(String message) {
		this(message, null);
	}
	
	public TrendrrNetworkingException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrNetworkingException(Exception cause) {
		this(null, cause);
	}
	
}
