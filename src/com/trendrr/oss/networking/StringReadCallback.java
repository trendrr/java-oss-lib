/**
 * 
 */
package com.trendrr.oss.networking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Mar 9, 2011
 * 
 */
public interface StringReadCallback extends ChannelCallback {

	public void stringResult(String result);
}
