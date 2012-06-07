/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.HashFunctions;


/**
 * @author Dustin Norlander
 * @created Feb 27, 2012
 * 
 */
public class ConsistentHashRing<T> {

	protected static Log log = LogFactory.getLog(ConsistentHashRing.class);
	
	protected TreeMap<Integer, T> connections = new TreeMap<Integer, T>();
	
	public static interface Hasher {
		public int hash(String key);
	}
	
	protected Hasher hashFunction = new Hasher() {
		@Override
		public int hash(String key) {
			try {
				return HashFunctions.murmurhash3(key.getBytes("utf8"), 8008);
			} catch (UnsupportedEncodingException e) {
				log.error("Caught", e);
			}
			return 0;
		}
	};
	
	/**
	 * sets a new hashfunction.  Default hashfunction is murmur3_32 with seed 8008
	 * @param hashFunction
	 */
	public synchronized void setHashFunction(ConsistentHashRing.Hasher hashFunction) {
		this.hashFunction = hashFunction;
	}
	
	/**
	 * gets all the items in the ring.  this returns a copy.
	 * @return
	 */
	public synchronized List<T> getAll() {
		return new ArrayList<T>(this.connections.values());
	}
	
	/**
	 * will return the requested number of connections, with no duplicate connections.
	 * @param key
	 * @param num
	 * @return
	 */
	public synchronized List<T> get(String key, int num) {
		int hash = this.hash(key);
		
		List<T> ret = new ArrayList<T>();
		int cur = hash;
		for (int i=0; i < connections.size(); i++) {
			Entry<Integer, T> entry = connections.higherEntry(cur);
			if (entry == null) {
				entry = connections.firstEntry();
			}
			
			cur = entry.getKey();
			T item = entry.getValue();
			if (!ret.contains(item)) {
				ret.add(item);
			}
			if (ret.size() == num) {
				return ret;
			}
		}
		return ret;
	}
	
	/**
	 * removes a connection from the ring
	 * @param connection
	 */
	public synchronized void remove(T connection) {
		List<Integer> removeKeys = new ArrayList<Integer>();
		for (Integer key : connections.keySet()) {
			if (connections.get(key) == connection) {
				removeKeys.add(key);
			}
		}
		for (Integer k : removeKeys) {
			connections.remove(k);
		}
	}
	/**
	 * Adds a new connection to this hashring.  
	 * 
	 * @param identifier
	 * @param connection
	 * @param points
	 */
	public synchronized void add(String identifier, T connection, int points) {
		for (int i=0; i < points; i++) {
			int hash = this.hash(identifier + ":" + i);
			connections.put(hash, connection);
		}
	}
	
	/*
	 * hash the key to an signed integer.
	 */
	protected int hash(String key) {
		return this.hashFunction.hash(key);
	}
}
