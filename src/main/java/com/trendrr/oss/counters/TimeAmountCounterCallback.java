/**
 * 
 */
package com.trendrr.oss.counters;

import java.util.Date;



/**
 * @author Dustin Norlander
 * @created Jan 31, 2013
 * 
 */
public interface TimeAmountCounterCallback {
	public void onRollover(TimeAmountCounter counter, Date date, long value);
}
