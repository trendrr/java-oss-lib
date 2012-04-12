/**
 * 
 */
package com.trendrr.oss.tests;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * @author Dustin Norlander
 * @created Apr 11, 2012
 * 
 */
public class TimeAmountTests {

	protected static Log log = LogFactory.getLog(TimeAmountTests.class);
	
	@Test
	public void test() throws UnsupportedEncodingException, TrendrrParseException {
		test("10 minutes", Timeframe.MINUTES, 10);
		test("15m", Timeframe.MINUTES, 15);
		test("24h", Timeframe.HOURS, 24);
	}
	
	private void test(String str, Timeframe frame, int amount) throws TrendrrParseException {
		TimeAmount amt = TimeAmount.instance(str);
		Assert.assertNotNull(amt);
		Assert.assertEquals(frame, amt.getTimeframe());
		Assert.assertEquals(amount, amt.getAmount());
	}
}
