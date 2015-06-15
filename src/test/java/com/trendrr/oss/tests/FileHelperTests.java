/**
 * 
 */
package com.trendrr.oss.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import com.trendrr.oss.FileHelper;


/**
 * @author Dustin Norlander
 * @created Jul 10, 2012
 * 
 */
public class FileHelperTests {

	protected static Log log = LogFactory.getLog(FileHelperTests.class);
	
	@Test
	public void test() throws Exception {
		
		for (int i=0 ; i < 100000; i++) {
		byte[] bytes = FileHelper.loadBytes("LICENSE");
		Assert.assertTrue(bytes.length > 10);
		}
	}
}
