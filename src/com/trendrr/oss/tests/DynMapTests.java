/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;



/**
 * @author Dustin Norlander
 * @created Dec 30, 2010
 * 
 */
public class DynMapTests {

	protected Logger log = Logger.getLogger(DynMapTests.class.getCanonicalName());
	
	@Test
	public void test1() {
		DynMap mp = DynMapFactory.instance("{ \"key\" : 90 }");
		log.info(mp.toJSONString());
		Assert.assertTrue(mp.get(Integer.class, "key") == 90);
		
		Assert.assertTrue(mp.get(String.class, "key").equals("90"));
		
		mp = DynMapFactory.instance("{ \"key\" : 90," +
				"\"map1\" : {" +
					"\"key2\" : \"today\"" +
				"}," +
				"\"list\" : [ 1,2,3]" +
			" }");
		
		Assert.assertEquals(mp.get(String.class, "map1.key2"), "today");
		mp.remove("map1");
		Assert.assertEquals("key=90&list=1&list=2&list=3", mp.toURLString());
	}
	
	
	
	
}
