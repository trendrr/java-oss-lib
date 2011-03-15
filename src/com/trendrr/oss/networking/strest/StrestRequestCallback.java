/**
 * 
 */
package com.trendrr.oss.networking.strest;

/**
 * @author dustin
 *
 */
public interface StrestRequestCallback {
	
	public void messageRecieved(StrestResponse response);
	
	public void txnComplete();
	
	public void error(Throwable x);
	
}
