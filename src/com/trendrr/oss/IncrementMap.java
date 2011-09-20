package com.trendrr.oss;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A threadsafe map for maintaining counts of string keys.
 * 
 * Uses AtomicInteger internally, with no synchronization so is a fast as could be.
 * 
 * @author dan frank
 *
 */
public class IncrementMap {	
	ConcurrentHashMap<String, AtomicInteger> inc = new ConcurrentHashMap<String, AtomicInteger>();
	
	/**
	 * Will increment key by amount - adds key to the map if not present
	 * @param key
	 * @param amount
	 */
	public void inc(String key, int amount) {
		if (amount == 0) {
			return;
		}
		this.inc.putIfAbsent(key, new AtomicInteger(0));
		this.inc.get(key).addAndGet(amount);
	}
	
	public void clear(){
		this.inc.clear();
	}
	
	public void set(String key, int amount){
		this.inc.put(key, new AtomicInteger(amount));
	}
	
	public int get(String key){
		AtomicInteger gotten = null;
		try{
			gotten = this.inc.get(key);
		}catch (Exception e){}
		if (gotten == null){
			return 0;
		}else{
			return gotten.get();
		}
	}
	
	public int remove(String key){
		AtomicInteger was = this.inc.remove(key);
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
