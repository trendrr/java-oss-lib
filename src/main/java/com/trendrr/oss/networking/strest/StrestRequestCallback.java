/**
 * 
 */
package com.trendrr.oss.networking.strest;

/**
 * @author dustin
 *
 * @deprecated use com.trendrr.oss.strest
 */
@Deprecated
public interface StrestRequestCallback {
	
	public void response(StrestResponse response);
	
	public void txnComplete(String txnId);
	
	public void error(Throwable x);
	
}
