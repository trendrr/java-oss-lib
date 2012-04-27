/**
 * 
 */
package com.trendrr.oss.cache;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * 
 * A cache wrapper.  should be be easily adapted for in memory caching as
 * well as many other datastores. 
 * 
 * @author Dustin Norlander
 * @created Dec 29, 2011
 * 
 */
public abstract class TrendrrCache {

	protected static Log log = LogFactory.getLog(TrendrrCache.class);

	/**
	 * initialize the connection.  called exactly once the first time the cache is accessed.
	 */
	protected abstract void _init(DynMap config);
	
	/**
	 * Set a key - value pair
	 * 
	 * @param key
	 * @param obj
	 * @param expires
	 */
	protected abstract void _set(String key, Object obj, Date expires);
	
	/**
	 * Deletes the key value pair
	 * @param key
	 */
	protected abstract void _del(String key);
	
	/**
	 * get the value at a key
	 * @param key
	 * @return
	 */
	protected abstract Object _get(String key);
	
	/**
	 * returns the values at the given set of keys
	 * @param keys
	 * @return
	 */
	protected abstract Map<String, Object> _getMulti(Set<String> keys);
	
	/**
	 * increment a key.
	 * @param key
	 * @param value
	 * @param expire when this key should expire.  null for forever (if available)
	 */
	protected abstract long _inc(String key, int value, Date expire);
	
	/**
	 * increments multiple keys in a map.  
	 * @param key
	 * @param values
	 * @param expire
	 */
	protected abstract void _incMulti(String key, Map<String, Integer> values, Date expire);
	
	/**
	 * gets the map from an incMulti call.
	 * @param key
	 * @return
	 */
	protected abstract Map<String,Long> _getIncMulti(String key);
	
	/**
	 * Add these items into a set add the given key.
	 * @param str
	 * @return
	 */
	protected abstract Set<String> _addToSet(String key, Collection<String> str, Date expire);
	
	/**
	 * loads a previous created set.
	 * @param key
	 * @return
	 */
	protected abstract Set<String> _getSet(String key);
	
	/**
	 * Remove from a set
	 * @param str
	 * @return
	 */
	protected abstract Set<String> _removeFromSet(String key, Collection<String> str);
	
	/**
	 * should set the key if and only if the value doesn't already exist. Should return whether the item was inserted or not.
	 * @param key
	 * @param value
	 * @param expires
	 */
	protected abstract boolean _setIfAbsent(String key, Object value, Date expires);
	
	/**
	 * initialize a new cache.  should be passed any config params that the specific implementation should need.  
	 * 
	 * @param config
	 */
	public TrendrrCache(DynMap config) {
		this.config = config;
		
	}
	protected DynMap config = new DynMap();
	
	private LazyInit initLock = new LazyInit();
	/**
	 * initializes the cache.  this is called exactly once.  is not required to explicitly call this, as it will be called the 
	 * first time the cache is accessed.
	 */
	public void init() {
		if (initLock.start()) {
			try {
				this._init(this.config);
			} finally {
				initLock.end();
			}
		}
	}
	
	/**
	 * reinitializes.
	 */
	protected void reinit() {
		this.initLock.reset();
		this.init();
	}
	
	/**
	 * sets a key given the requested namespace.
	 * @param namespace
	 * @param key
	 * @param obj
	 * @param expires
	 */
	public void set(String namespace, String key, Object obj, Date expires) {
		this.init();
		this._set(this.getKey(namespace, key), obj, expires);
		
	}
	
	/**
	 * atomically adds the values to a Set (a collection with no duplicates).
	 * 
	 * This is meant for record keeping, for small collections.  definitly do not use for 
	 * queues, or any set with many items.
	 * 
	 * @param namespace
	 * @param key
	 * @param values
	 * @param expires
	 */
	public void addToSet(String namespace, String key, Collection<String> values, Date expire) {
		this.init();
		this._addToSet(this.getKey(namespace, key), values, expire);
	}
	 
	/**
	 * loads a Set 
	 * @param namespace
	 * @param key
	 * @return
	 */
	public Set<String> getSet(String namespace, String key) {
		this.init();
		return this._getSet(this.getKey(namespace, key));
	}
	public Set<String> getSet(String key) {
		return this.getSet(null, key);
	}
	
	
	/**
	 * sets the key with the default namespace
	 * @param namespace
	 * @param key
	 * @param obj
	 * @param expires
	 */
	public void set(String key, Object obj, Date expires) {
		this.set(null, key, obj, expires);
	}
	
	protected String getKey(String namespace, String key){
//		log.info("Getting key from: " + namespace + " " + key );
		if (namespace != null) {
			key = namespace + key;
		}
		if (key.length() > 24) {
			try {
				key = StringHelper.sha1Hex(key.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				log.warn("Invalid key: " + key, e);
			}
		} 
//		log.info("key: " + key);
		return key;
	}
	
	/**
	 * sets the key if and only if the key does not already exist
	 * @param namespace
	 * @param key
	 * @param value
	 * @param expires
	 * @return
	 */
	public boolean setIfAbsent(String namespace, String key, Object value, Date expires) {
		this.init();
		return this._setIfAbsent(this.getKey(namespace, key), value, expires);
	}
	
	/**
	 * uses the default namespace
	 * @param key
	 * @param value
	 * @param expires
	 * @return
	 */
	public boolean setIfAbsent(String key, Object value, Date expires) {
		return this.setIfAbsent(null, key, value, expires);
	}
	
	/**
	 * Gets the value at the specified namespace and key.
	 * @param namespace
	 * @param key
	 * @return
	 */
	public Object get(String namespace, String key) {
		this.init();
		return this._get(this.getKey(namespace, key));
	}
	
	/**
	 * gets the value from the default namespace.
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return this.get(null, key);
	}
	
	/**
	 * returns a map of 
	 * @param namespace
	 * @param keys
	 * @return
	 */
	public Map<String, Object> getMulti(String namespace, Collection<String> keys) {
		this.init();
		
		/*
		 * does some copying around here in order to keep with our namespaced keys.
		 */
		HashSet<String> k = new HashSet<String>();
		
		HashMap<String, String> newKeys = new HashMap<String,String>();
		
		for (String key : keys) {
			String newKey = this.getKey(namespace, key);
			newKeys.put(newKey, key);
			k.add(newKey);
		}
		Map<String, Object> results =  this._getMulti(k);
		HashMap<String, Object> newResults = new HashMap<String,Object>();
		for (String key : results.keySet()) {
			newResults.put(newKeys.get(key), results.get(key));
		}
		return newResults;
	}
	
	public Map<String, Object> getMulti(Collection<String> keys) {
		return this.getMulti(null, keys);
	}
	
	/**
	 * deletes the specified key
	 * @param namespace
	 * @param key
	 */
	public void delete(String namespace, String key) {
		this.init();
		this._del(this.getKey(namespace, key));
	}
	
	/**
	 * 
	 * @param key
	 */
	public void delete(String key) {
		this.delete(null, key);
	}
	
	/**
	 * increments the specified key
	 * @param namespace 
	 * @param key
	 * @param value
	 */
	public long inc(String namespace, String key, int value, Date expire) {
		this.init();
		return this._inc(this.getKey(namespace, key), value, expire);
	}
	
	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param value
	 */
	public long inc(String key, int value, Date expire) {
		return this.inc(null, key, value, expire);
	}
	
	/**
	 * increments multiple keys in a map (ex: a redis hashmap).
	 * @param namespace
	 * @param key
	 * @param values
	 * @param expire
	 */
	public void incMulti(String namespace, String key, Map<String, Integer> values, Date expire) {
		this.init();
		this._incMulti(this.getKey(namespace, key), values, expire);
	}
	
	/**
	 * increments multiple keys in a map (ex: a redis hashmap).
	 * @param namespace
	 * @param key
	 * @param values
	 * @param expire
	 */
	public void incMulti(String key, Map<String, Integer> values, Date expire) {
		this.incMulti(null, key, values, expire);
	}
}
