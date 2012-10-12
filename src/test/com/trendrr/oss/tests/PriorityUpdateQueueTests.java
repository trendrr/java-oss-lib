/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.Comparator;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.PriorityUpdateQueue;


/**
 * @author Dustin Norlander
 * @created Oct 12, 2012
 * 
 */
public class PriorityUpdateQueueTests {

	protected static Log log = LogFactory
			.getLog(PriorityUpdateQueueTests.class);
	
	
	@Test
	public void removeTest() {
		PriorityUpdateQueue<String> queue = new PriorityUpdateQueue<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2); //alpha, backwards
			}
		});
		
		
		queue.push("aaaa");
		queue.push("bbbb");
		queue.push("dddd");
		queue.push("cccc");
		
		
		Assert.assertTrue(queue.remove("aaaa"));
		Assert.assertTrue(queue.remove("dddd"));
		Assert.assertTrue(queue.remove("bbbb"));
		
		Assert.assertEquals("cccc", queue.pop());
		Assert.assertNull(queue.pop());
		
		
	}
}
