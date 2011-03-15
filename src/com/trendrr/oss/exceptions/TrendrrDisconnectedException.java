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
public class TrendrrDisconnectedException extends TrendrrNetworkingException {

	protected Log log = LogFactory.getLog(TrendrrDisconnectedException.class);
	
	public TrendrrDisconnectedException () {
		this(null, null);
	}
	
	public TrendrrDisconnectedException(String message) {
		this(message, null);
	}
	
	public TrendrrDisconnectedException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrDisconnectedException(Exception cause) {
		this(null, cause);
	}
}
