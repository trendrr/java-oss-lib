/**
 * 
 */
package com.trendrr.oss.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Oct 9, 2012
 * 
 */
public interface TrendrrCacheKeyMaker {
	public String toKey(String namespace, String key);
}
