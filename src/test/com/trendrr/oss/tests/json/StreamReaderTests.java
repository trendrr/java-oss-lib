/**
 * 
 */
package com.trendrr.oss.tests.json;

import java.io.StringReader;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.json.stream.JSONStreamReader;
import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Jul 10, 2013
 * 
 */
public class StreamReaderTests {

	protected static Log log = LogFactory.getLog(StreamReaderTests.class);
	
	@Test
	public void escapeTests() throws TrendrrException {
		//test various escaping
		String json = "{\"classifications\":null,\"user\":{\"location\":\"Cape Town \\/\\\\\\/¯¯¯¯¯¯¯\\\\\\/\\\\\",\"listed_count\":0}}";
		
		JSONStreamReader reader = new JSONStreamReader(new StringReader(json));
		DynMap mp = reader.readNext();
		Assert.assertNotNull(mp);
		
	}
}
