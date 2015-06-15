package com.trendrr.oss;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A threadsafe map for maintaining counts of string keys.
 * 
 * Uses AtomicLong internally, with no synchronization so is a fast as could be.
 * 
 * @author dan frank
 *
 */
public class IncrementMap {	
	ConcurrentHashMap<String, AtomicLong> inc = new ConcurrentHashMap<String, AtomicLong>();
	
	/**
	 * Will increment key by amount - adds key to the map if not present
	 * @param key
	 * @param amount
	 */
	public void inc(String key, long amount) {
		if (amount == 0) {
			return;
		}
		this.inc.putIfAbsent(key, new AtomicLong(0));
		this.inc.get(key).addAndGet(amount);
	}
	
	public void clear(){
		this.inc.clear();
	}
	
	public void set(String key, long amount){
		this.inc.put(key, new AtomicLong(amount));
	}
	
	public long get(String key){
		AtomicLong gotten = null;
		try{
			gotten = this.inc.get(key);
		}catch (Exception e){}
		if (gotten == null){
			return 0;
		}else{
			return gotten.get();
		}
	}
	
	public long remove(String key){
		AtomicLong was = this.inc.remove(key);
		return (was != null) ? was.get() : 0;
	}
	
	public Set<String> keySet(){
		return this.inc.keySet();
	}

	
	@Override
	public String toString() {
		return this.inc.toString();
	}
	
	
	
}
