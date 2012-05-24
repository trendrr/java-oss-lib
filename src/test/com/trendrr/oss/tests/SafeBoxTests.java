/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.concurrent.SafeBox;


/**
 * @author Dustin Norlander
 * @created May 24, 2012
 * 
 */
public class SafeBoxTests {

	protected static Log log = LogFactory.getLog(SafeBoxTests.class);
	
	@Test
	public void test() throws InterruptedException {
		
		SafeBox<Integer> sb = new SafeBox<Integer>();
		int num = 100000;
		Date start = new Date();
		for (int i=0;i < num; i++) {
			sb.set(10);
			sb.getAndClear();
		}
		System.out.println("SAFEBOX completed: " + num + " in: " + (new Date().getTime()-start.getTime()));
	}
	
	@Test
	public void multiThreadTest() throws Exception {
		
		final SafeBox<String> sb = new SafeBox<String>();
		final int num = 100000;
		Thread t = new Thread() {
			@Override
			public void run() {
				for (int i=0; i < num; i++) {
					try {
						sb.set("Value" + i);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		Date start = new Date();
		for (int i=0 ; i < num; i++) {
			sb.getAndClear();
		}
		System.out.println("SAFEBOX threaded completed: " + num + " in: " + (new Date().getTime()-start.getTime()));
		
	}
}
