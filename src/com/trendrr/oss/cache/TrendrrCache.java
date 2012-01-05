/**
 * 
 */
package com.trendrr.oss.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.concurrent.LazyInit;


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

	protected Log log = LogFactory.getLog(TrendrrCache.class);

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
	protected abstract Collection<Object> _getMulti(Collection<String> keys);
	
	/**
	 * increment a key.
	 * @param key
	 * @param value
	 */
	protected abstract void _inc(String key, Number value);
	
	/**
	 * Add to a set
	 * @param str
	 * @return
	 */
	protected abstract Set<String> _addToSet(String key, Collection<String> str);
	
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
	
	
	public void set(String key, Object obj, Date expires) {
		this.init();
		this._set(key, obj, expires);
	}
	
	public boolean setIfAbsent(String key, Object value, Date expires) {
		this.init();
		return this._setIfAbsent(key, value, expires);
	}
	
	public Object get(String key) {
		this.init();
		return this._get(key);
	}
	
	public Collection<Object> getMulti(Collection<String> keys) {
		this.init();
		return this._getMulti(keys);
	}
	
	public void delete(String key) {
		this.init();
		this._del(key);
	}
	
	public void inc(String key, Number value) {
		this.init();
		this._inc(key, value);
	}
}
