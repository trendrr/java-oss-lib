/**
 * 
 */
package com.trendrr.oss.strest.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * The hello packet.  Currently only used in the binary protocol.
 * 
 * @author Dustin Norlander
 * @created Jul 29, 2013
 * 
 */
public class StrestHello extends DynMap {

	protected static Log log = LogFactory.getLog(StrestHello.class);
	
	public String getShardService() {
		return this.getString("service");
	}
	public void setShardService(String shardService) {
		this.put("service", shardService);
	}
	public double getVersion() {
		return this.getDouble("v");
	}
	public void setVersion(double version) {
		this.put("v", version);
	}
	public String getUserAgent() {
		return this.getString("useragent");
	}
	public void setUserAgent(String userAgent) {
		this.put("useragent", userAgent);
	}
}
