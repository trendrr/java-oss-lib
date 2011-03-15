/**
 * 
 */
package com.trendrr.oss;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.trendrr.json.simple.JSONFormatter;
import com.trendrr.json.simple.JSONObject;
import com.trendrr.json.simple.JSONValue;

/**
 * 
 * A dynamic map.
 * 
 * 
 * caching:
 * 
 * set cacheEnabled to use an internal cache of TypeCasted results.
 * This is usefull for frequently read maps, with expensive conversions (i.e. string -> map, or list conversions)
 * Raises the memory footprint somewhat and adds some time to puts and removes 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created Dec 29, 2010
 * 
 */
public class DynMap extends HashMap<String,Object>{
	
	Logger log = Logger.getLogger(DynMap.class.getCanonicalName());
	
	ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
	boolean cacheEnabled = false;
	
	
	/*
	 * Register Date and DynMap with the json formatter so we get properly encoded strings.
	 */
	static {
		JSONValue.registerFormatter(Date.class, new JSONFormatter() {
			@Override
			public String toJSONString(Object value) {
				return IsoDateUtil.getIsoDate((Date)value);
			}
		});
		
		JSONValue.registerFormatter(DynMap.class, new JSONFormatter() {
			@Override
			public String toJSONString(Object value) {
				return ((DynMap)value).toJSONString();
			}
		});
	}
	
	
	/**
	 * puts the value if the key is absent (or null).
	 * @param key
	 * @param val
	 */
	public void putIfAbsent(String key, Object val) {
		if (this.get(key) == null) {
			this.put(key, val);
		}
	}
	
	@Override
	public Object put(String key, Object val) {
		this.ejectFromCache(key);
		return super.put(key, val);
	}
	
	public void removeAll(String...keys) {
		for (String k : keys) {
			this.remove(k);
		}
	}
	
	@Override
	public Object remove(Object k) {
		this.ejectFromCache((String)k);
		return super.remove(k);
	}
	
	private void ejectFromCache(String key) {
		//TODO: this is a dreadful implementation.
		Set<String> keys = new HashSet<String>();
		for (String k : cache.keySet()) {
			if (k.startsWith(key + ".")) {
				keys.add(k);
			}
		}
		for (String k : keys) {
			cache.remove(k);
		}
		cache.remove(key);
	}
	
	boolean isCacheEnabled() {
		return cacheEnabled;
	}

	void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	/**
	 * Gets the requested object from the map.
	 * 
	 * this differs from the standard map.get in that you can 
	 * use the dot operator to get a nested value:
	 * 
	 * map.get("key1.key2.key3");
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Object get(Object k) {
		String key = (String)k;
		Object val = super.get(key);
		if (val != null) {
			return val;
		}
		
		if (key.contains(".")) {
			//try to reach into the object..
			String[] items = key.split("\\.");
			DynMap cur = this.get(DynMap.class, items[0]);
			for (int i= 1; i < items.length-1; i++) {
				
				cur = cur.get(DynMap.class, items[i]);
//				log.info("got map: " + items[i] + " " + cur);
				
				if (cur == null)
					return null;
			}
//			log.info("returning : " + items[items.length-1] + " " + cur.get(items[items.length-1]));
			return cur.get(items[items.length-1]);
		}
		return null;
	}
	
	public <T> T get(Class<T> cls, String key) {
		//cache the result.. 
		if (this.cacheEnabled) {
			String cacheKey = key + "." + cls.getCanonicalName(); 
			if (this.cache.containsKey(cacheKey)) {
				//null is an acceptable cache result.
				return (T)this.cache.get(cacheKey);
			} else {
				T val = TypeCast.cast(cls,this.get(key));
				this.cache.put(cacheKey, val);
				return val;
			}
		} 
		return TypeCast.cast(cls, this.get(key));
	}

	public <T> T get(Class<T> cls, String key, T defaultValue) {
		T val = this.get(cls, key);
		if (val == null )
			return defaultValue;
		return val;
	}
	
	/**
	 * Returns a typed list.  See TypeCast.getTypedList
	 * 
	 * returns the typed list, or null, never empty.
	 * @param <T>
	 * @param cls
	 * @param key
	 * @param delimiters
	 * @return
	 */
	public <T> List<T> getList(Class<T> cls, String key, String... delimiters) {
		//cache the result.. 
		if (this.cacheEnabled) {
			String cacheKey = key + ".LIST." + cls.getCanonicalName() + "."; 
			if (this.cache.containsKey(cacheKey)) {
				//null is an acceptable cache result.
				return (List<T>)this.cache.get(cacheKey);
			} else {
				List<T> val = TypeCast.getTypedList(cls, this.get(key), delimiters);
				this.cache.put(cacheKey, val);
				return val;
			}
		} 
		return TypeCast.getTypedList(cls, this.get(key), delimiters);
	}
	
	/**
	 * same principle as jquery extend.
	 * 
	 * each successive map will override any properties in the one before it. 
	 * 
	 * Last map in the params is considered the most important one.
	 * 
	 * 
	 * @param map1
	 * @param maps
	 * @return this, allows for chaining
	 */
	public DynMap extend(Object map1, Object ...maps) {
		if (map1 == null)
			return this;
		
		DynMap mp1 = DynMapFactory.instance(map1);
		this.putAll(mp1);
		for (Object m : maps) {
			this.putAll(DynMapFactory.instance(m));
		}
		return this;
	}
	
	
	public String toJSONString() {
		return JSONObject.toJSONString(this);
	}
	
	/**
	 * will return the map as a url encoded string in the form:
	 * key1=val1&key2=val2& ...
	 * 
	 * This can be used as getstring or form-encoded post. 
	 * Lists are handled as multiple key /value pairs.
	 * 
	 * will skip keys that contain null values.
	 * keys are sorted alphabetically so ordering is consistent
	 * 
	 * 
	 * @return The url encoded string, or empty string.
	 */
	public String toURLString() {
		StringBuilder str = new StringBuilder();
		
		boolean amp = false;
		List<String> keys = new ArrayList<String>();
		keys.addAll(this.keySet());
		Collections.sort(keys);
		
		for (String key : keys) {
			try {
				String k = URLEncoder.encode(key, "utf-8");
				List<String> vals = this.getList(String.class, key);
				for (String v : vals) {
					v = URLEncoder.encode(v, "utf-8");
					if (v != null) {
						if (amp)
							str.append("&");
						
						str.append(k);
						str.append("=");
						str.append(v);
						amp = true;
					}
				}
			} catch (Exception x) {
				log.log(Level.INFO, "Caught", x);
				continue;
			}
		}
		return str.toString();
	}
}
