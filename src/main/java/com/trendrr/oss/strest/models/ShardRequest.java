/**
 * 
 */
package com.trendrr.oss.strest.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapConvertable;


/**
 * @author Dustin Norlander
 * @created Jul 29, 2013
 * 
 */
public class ShardRequest implements DynMapConvertable {

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
	/* (non-Javadoc)
	 * @see com.trendrr.oss.DynMapConvertable#toDynMap()
	 */
	@Override
	public synchronized DynMap toDynMap() {
		DynMap mp = new DynMap();
		if (partition >= 0) {
			mp.put("_p", this.partition);
		}
		
		if (key != null) {
			mp.put("_sk", this.key);
		}
		if (revision >= 0) {
			mp.put("_v", this.revision);
		}
		return mp;
	}
	
}
