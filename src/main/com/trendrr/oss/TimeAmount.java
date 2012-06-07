/**
 * 
 */
package com.trendrr.oss;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * @author Dustin Norlander
 * @created Apr 10, 2012
 * 
 */
public class TimeAmount {

	protected static Log log = LogFactory.getLog(TimeAmount.class);
	
	Timeframe frame;
	int amount = 1;
	
	public TimeAmount(Timeframe frame, int amount) {
		this.frame = frame;
		this.amount = amount;
	}
	
	public Timeframe getTimeframe() {
		return frame;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public static TimeAmount instance(Timeframe frame, int amount) {
		return new TimeAmount(frame, amount);
	}
	
	/**
	 * parses a string like":
	 * 10 minutes
	 * 5h
	 * 24hrs
	 * 
	 * 
	 * @param str
	 */
	public static TimeAmount instance(String str) throws TrendrrParseException{
		try {
			int amount = Integer.parseInt(str.replaceAll("[^0-9]", ""));
			String fr = str.replaceAll("[^A-Za-z]", "");
			if (fr.equalsIgnoreCase("m")) {
				fr = "minutes";
			}
			Timeframe frame = Timeframe.instance(fr);
			return new TimeAmount(frame, amount);
		} catch (Exception x) {
			throw new TrendrrParseException("unable to parse: " + str, x);
		}
	}
	
	public Date add(Date input, int amount) {
		return this.frame.add(input, this.amount*amount);	
	}
	
	/**
	 * creates a
	 * @param date
	 * @return
	 */
	public Number toTrendrrEpoch(Date date) {
		Number n = frame.toTrendrrEpoch(date);
		if (frame == Timeframe.SECONDS || frame == Timeframe.MILLISECONDS) {
			return (long)(n.longValue() / amount);
		} else {
			return (int)(n.intValue()/ amount);
		}
	}
	
	public Date fromTrendrrEpoch(Number val) {
		return frame.fromTrendrrEpoch(val.intValue()*amount);
	}
	
	public String toString() {
		return this.amount + " " + this.frame.toString();
	}
}
