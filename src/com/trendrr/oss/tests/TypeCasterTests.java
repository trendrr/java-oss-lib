/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;

import com.trendrr.oss.TypeCast;


/**
 * @author Dustin Norlander
 * @created Dec 28, 2010
 * 
 */
public class TypeCasterTests {

	Logger log = Logger.getLogger(TypeCasterTests.class.getCanonicalName());
	
	@Test
	public void booleanTests() {
		Assert.assertTrue(TypeCast.cast(Boolean.class, "true"));
		Assert.assertTrue(TypeCast.cast(Boolean.class, "t"));
		Assert.assertTrue(TypeCast.cast(Boolean.class, "on"));
		Assert.assertTrue(TypeCast.cast(Boolean.class, "yes"));
		
		
		Assert.assertFalse(TypeCast.cast(Boolean.class, "false"));
		Assert.assertFalse(TypeCast.cast(Boolean.class, "f"));
		Assert.assertFalse(TypeCast.cast(Boolean.class, "off"));
		Assert.assertFalse(TypeCast.cast(Boolean.class, "no"));
	}
	
	@Test
	public void dateTests() {
		Assert.assertNotNull(TypeCast.cast(Date.class,Calendar.getInstance()));
		Assert.assertNotNull(TypeCast.cast(Date.class,new java.util.Date()));
		
		Assert.assertNotNull(TypeCast.cast(Date.class, "1997-07-16T19:20:30.45+01:00"));
		Assert.assertNotNull(TypeCast.cast(Date.class, "2011-05-18T18:43:30.834000 00:00"));
	}
	
	@Test
	public void numberTests() {
		Assert.assertTrue(TypeCast.cast(Long.class, "30335816244924417") == 30335816244924417l);
		Assert.assertTrue(TypeCast.cast(Double.class, "303358162.44924417") == 303358162.44924417);
		
	}
	
	@Test
	public void listTests() {
		Assert.assertNull(TypeCast.toList(null, ","));
		Assert.assertNull(TypeCast.toList(""));
		Assert.assertTrue(listTest(TypeCast.toList("one,two", ","), 2));
		Assert.assertTrue(listTest(TypeCast.toList("[\"one\",\"two\"]"), 2));
		Assert.assertTrue(listTest(TypeCast.toList("100"), 1));
		
		Assert.assertNull(TypeCast.toList(new ArrayList()));
		
	}
	
	private static boolean listTest(List list, int expectedSize) {
		if (list == null)
			return false;
		if (list.size() != expectedSize) 
			return false;
		return true;
	}
	
	
	
}
