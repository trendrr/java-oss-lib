/**
 * 
 */
package com.trendrr.oss.networking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Mar 9, 2011
 * 
 */
public interface ChannelCallback {

	public void onError(TrendrrException ex);
}
