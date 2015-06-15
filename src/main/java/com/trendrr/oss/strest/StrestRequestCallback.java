/**
 * 
 */
package com.trendrr.oss.strest;

import com.trendrr.oss.strest.models.StrestResponse;

/**
 * @author dustin
 *
 */
public interface StrestRequestCallback {
	
	public void response(StrestResponse response);
	
	public void txnComplete(String txnId);
	
	public void error(Throwable x);
	
}
