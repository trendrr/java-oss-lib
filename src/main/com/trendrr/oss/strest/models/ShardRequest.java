/**
 * 
 */
package com.trendrr.oss.strest.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jul 29, 2013
 * 
 */
public class ShardRequest {

	protected static Log log = LogFactory.getLog(ShardRequest.class);
	
	private int partition = -1;
	private String key;
	private long revision = -1l;
	
	public synchronized int getPartition() {
		return partition;
	}
	public synchronized void setPartition(int partition) {
		this.partition = partition;
	}
	public synchronized String getKey() {
		return key;
	}
	public synchronized void setKey(String key) {
		this.key = key;
	}
	public synchronized long getRevision() {
		return revision;
	}
	public synchronized void setRevision(long revision) {
		this.revision = revision;
	}
}
