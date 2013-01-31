/**
 * 
 */
package com.trendrr.oss.counters;

import com.trendrr.oss.TimeAmount;


/**
 * @author Dustin Norlander
 * @created Jan 31, 2013
 * 
 */
public interface TimeAmountCounterCallback {
	public void onRollover(TimeAmount timeamount, long epoch, long value);
}
