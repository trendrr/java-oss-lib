/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.messaging.channel.ChannelMethodRequestHandler;
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
	
	
}
