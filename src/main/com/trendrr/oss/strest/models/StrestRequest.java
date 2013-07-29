/**
 * 
 */
package com.trendrr.oss.strest.models;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public interface StrestRequest extends StrestPacketBase{

	/**
	 * optional shard packet.
	 * @param shard
	 */
	public void setShardRequest(ShardRequest shard);
	public ShardRequest getShardRequest();
	
	public void setMethod(StrestHeader.Method method);
	public StrestHeader.Method getMethod();
	
	public void setUri(String uri);
	public String getUri();
	
	public void setTxnAccept(StrestHeader.TxnAccept accept);
	public StrestHeader.TxnAccept getTxnAccept();
	
	public void setParams(DynMap params);
	public DynMap getParams();
}
