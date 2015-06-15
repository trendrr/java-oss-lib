/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.messaging.channel.ChannelMethodRequestHandler;
import com.trendrr.oss.messaging.channel.MessageChannel;
import com.trendrr.oss.messaging.channel.MessageChannel;
import com.trendrr.oss.tests.messaging.EchoClass;


/**
 * @author Dustin Norlander
 * @created May 18, 2012
 * 
 */
public class MessageChannelTests {

	protected static Log log = LogFactory.getLog(MessageChannelTests.class);
	
	@Test
	public void channelTest() throws Exception {
		MessageChannel channel = MessageChannel.create("test", new ChannelMethodRequestHandler(new EchoClass()));
		
		Object val = channel.request("stringLength", "1234");
		Assert.assertEquals(val, 4);
		
		List<Object> vals = (List<Object>)channel.request("inputToList", "one", "two");
		Assert.assertTrue(vals.size() == 2);
		
		channel.stop();
	}
	
	
	@Test
	public void threadedRequesterSpeedTest() throws Exception {
		final int num = 100000;
		final MessageChannel channel = MessageChannel.create("test", new ChannelMethodRequestHandler(new EchoClass()));
		
		int numThreads = 10;
		final CountDownLatch latch = new CountDownLatch(numThreads);
		Date start = new Date();
		for (int i=0; i< numThreads; i++) {
			Thread t = new Thread() {
				@Override
				public void run() {
					for (int i=0; i < num; i++) {
						try {
							Object val = channel.request("stringLength", "1234 " + i);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					latch.countDown();
				}
			};
			t.start();
		}
		latch.await();
		System.out.println("CHANNEL COMPLETED " + (num*numThreads) + " IN " + (new Date().getTime()-start.getTime()));
		
	}
	
	@Test
	public void speedTest() throws Exception {
		EchoClass echo = new EchoClass();
		ChannelMethodRequestHandler handler = new ChannelMethodRequestHandler(echo);
		MessageChannel channel = MessageChannel.create("test", handler);
		int num = 100000;
		//warm up.
		for (int i=0; i < 100; i++) {
			Object val = channel.request("stringLength", "1234");
			List<Object> vals = (List<Object>)channel.request("inputToList", "one", "two");
		}
		
		
		Date start = new Date();
		for (int i=0; i < num; i++) {
			Object val = channel.request("stringLength", "1234");
			List<Object> vals = (List<Object>)channel.request("inputToList", "one", "two");
		}
		long millis = (new Date().getTime()-start.getTime());
		System.out.println("MESSAGE CHANNEL2 COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		start = new Date();
		for (int i=0; i < num; i++) {
			Object val = handler.handleRequest("stringLength", "1234");
			List<Object> vals = (List<Object>)handler.handleRequest("inputToList", "one", "two");
		}
		
		System.out.println("HANDLER COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		
		
		start = new Date();
		for (int i=0; i < num; i++) {
			Object val = echo.stringLength("1234");
			List<Object> vals = echo.inputToList( "one", "two");
		}
		
		System.out.println("COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		
		channel.stop();
		
	}
	
}
