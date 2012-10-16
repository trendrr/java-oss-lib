/**
 * 
 */
package com.trendrr.oss.cache;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;


/**
 * @author Dustin Norlander
 * @created Oct 9, 2012
 * 
 */
public class DefaultTrendrrCacheKeyMaker implements TrendrrCacheKeyMaker {

	protected static Log log = LogFactory
			.getLog(DefaultTrendrrCacheKeyMaker.class);
	
	/**
	 * we sha1 hash any keys longer then this.
	 */
	public static int MAX_KEY_LENGTH_BEFORE_HASH = 24;
	
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCacheKeyMaker#toKey(java.lang.String, java.lang.String)
	 */
	@Override
	public String toKey(String namespace, String key) {
		if (namespace != null) {
			key = namespace + key;
		}
		if (shouldHashKey(key)) {
			try {
				key = StringHelper.sha1Hex(key.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				log.warn("Invalid key: " + key, e);
			}
		} 
		return key;
	}
	
	
	protected static boolean shouldHashKey(String key) {
		try {
			byte[] keyBytes = key.getBytes("utf8");
			// Validate the key
			if (key.length()> MAX_KEY_LENGTH_BEFORE_HASH) {
				return true;
			}
			for (byte b : keyBytes) {
		      if (b == ' ' || b == '\n' || b == '\r' || b == 0) {
		    	  return true;
		      }
		    }
			return false;
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return true;   
	}
}
