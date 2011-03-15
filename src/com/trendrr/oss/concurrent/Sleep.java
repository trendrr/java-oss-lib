/**
 * 
 */
package com.trendrr.oss.concurrent;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class Sleep {

	protected static Log log = LogFactory.getLog(Sleep.class);
	
	public static void hours(int hours) {
		minutes(hours * 60);
	}
	
	/**
	 * current thread sleeps for minuts
	 * @param minutes
	 */
	public static void minutes(int minutes) {
		millis(minutes * 60 * 1000);
	}
	public static void seconds(int seconds) {
		millis(seconds * 1000);
	}
	public static void millis(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception x) {
			log.error("CuaghT", x);
		}
		
	}
	
	public static void until(Date until) {
		millis(until.getTime()-new Date().getTime());
	}
}
