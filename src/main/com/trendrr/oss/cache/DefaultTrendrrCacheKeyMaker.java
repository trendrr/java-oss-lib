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

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCacheKeyMaker#toKey(java.lang.String, java.lang.String)
	 */
	@Override
	public String toKey(String namespace, String key) {
		if (namespace != null) {
			key = namespace + key;
		}
		if (key.length() > 24) {
			try {
				key = StringHelper.sha1Hex(key.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				log.warn("Invalid key: " + key, e);
			}
		} 
//		log.info("key: " + key);
		return key;
	}
}
