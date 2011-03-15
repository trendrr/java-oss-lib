/**
 * 
 */
package com.trendrr.oss.exceptions;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class TrendrrIOException extends TrendrrNetworkingException{

	protected Log log = LogFactory.getLog(TrendrrIOException.class);
	
	public TrendrrIOException () {
		this(null, null);
	}
	
	public TrendrrIOException(String message) {
		this(message, null);
	}
	
	public TrendrrIOException(String message, Exception cause) {
		super(message, cause);
	}
	
	public TrendrrIOException(Exception cause) {
		this(null, cause);
	}
}
