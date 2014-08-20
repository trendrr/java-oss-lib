/**
 * 
 */
package com.trendrr.oss.appender.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrIOException;


/**
 * @author Dustin Norlander
 * @created Jun 17, 2013
 * 
 */
public class FileClosedException extends TrendrrIOException {

	protected static Log log = LogFactory.getLog(FileClosedException.class);
}
