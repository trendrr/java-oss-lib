/**
 * 
 */
package com.trendrr.oss.networking.cheshire;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Apr 6, 2011
 * 
 */
public interface CheshireApiCallback {
	/**
	 * the response
	 * @param response
	 */
	public void response(DynMap response);
	
	/**
	 * called when the specific transaction is complete. See STREST docs.  In most cases you can ignore this
	 * @param txnId
	 */
	public void txnComplete(String txnId);
	
	/**
	 * An error occured.  This will indication a transport error.
	 * @param x
	 */
	public void error(Throwable x);
	
}
