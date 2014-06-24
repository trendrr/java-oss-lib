/**
 * 
 */
package com.trendrr.oss.tests.reflection;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;


/**
 * @author Dustin Norlander
 * @created Dec 10, 2012
 * 
 */
public class ReflectionTests {

	protected static Log log = LogFactory.getLog(ReflectionTests.class);
	
	
	@Test
	public void testExec() throws Exception {
		DynMap d = new DynMap();
		d.put("test", "testval");
		
		Assert.assertEquals("testval", Reflection.exec(d, "get", "test"));
				
	}
}
