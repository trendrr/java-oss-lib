/**
 * 
 */
package com.trendrr.oss.networking.strest.v2;

import com.trendrr.oss.networking.strest.v2.models.StrestResponse;

/**
 * @author dustin
 *
 */
public interface StrestRequestCallback {
	
	public void response(StrestResponse response);
	
	public void txnComplete(String txnId);
	
	public void error(Throwable x);
	
}