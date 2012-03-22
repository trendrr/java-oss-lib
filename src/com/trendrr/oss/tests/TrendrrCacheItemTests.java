/**
 * 
 */
package com.trendrr.oss.tests;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.cache.TrendrrCacheItem;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * @author Dustin Norlander
 * @created Jan 9, 2012
 * 
 */
public class TrendrrCacheItemTests {

	protected static Log log = LogFactory.getLog(TrendrrCacheItemTests.class);
	
	@Test
	public void test1() throws UnsupportedEncodingException, TrendrrParseException {
		DynMap metadata = new DynMap();
		metadata.put("key1", "something something");
		
		TrendrrCacheItem item = TrendrrCacheItem.instance(metadata, "this is the message".getBytes("utf8"));
		
		byte[] bytes = item.serialize();
		
		TrendrrCacheItem item2 = TrendrrCacheItem.deserialize(bytes);
		
		System.out.println(item2.getMetadata().toJSONString());
		
		Assert.assertArrayEquals(bytes, item2.serialize());
		
		
	}
	
	@Test
	public void test2() throws UnsupportedEncodingException, TrendrrParseException {
		TrendrrCacheItem item = TrendrrCacheItem.instance(null, "this is the message".getBytes("utf8"));
		
		byte[] bytes = item.serialize();
		
		TrendrrCacheItem item2 = TrendrrCacheItem.deserialize(bytes);
		Assert.assertArrayEquals(bytes, item2.serialize());
	}
}
