/**
 * 
 */
package com.trendrr.oss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.trendrr.json.simple.JSONAware;
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
public class DynMap extends HashMap<String,Object> implements JSONAware{
	
	private static final long serialVersionUID = 6342683643643465570L;

	private static Logger log = Logger.getLogger(DynMap.class.getCanonicalName());
	
	private ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
	private boolean cacheEnabled = false;
	
	
	public DynMap () {
		super();
	}
	
	/**
	 * creates a new DynMap with an initial key, val 
	 * @param key
	 * @param val
	 */
	public DynMap(String key, Object val) {
		this();
		this.put(key, val);
	}
	
	/**
	 * Creates a new dynMap based on the passed in object.  This is just a wrapper
	 * around DynMapFactory.instance().
	 * 
	 * if object is already a DynMap then that dynmap is returned.
	 * 
	 * @param object
	 * @return
	 */
	public static DynMap instance(Object object) {
		return DynMapFactory.instance(object);
	}
	
	/**
	 * Creates a new dynMap based on the passed in object.  This is just a wrapper
	 * around DynMapFactory.instance()
	 * 
	 * @param object
	 * @param 
	 * @return
	 */
	public static DynMap instance(Object object, DynMap defaultMap) {
		DynMap obj = DynMapFactory.instance(object);
		if (obj == null)
			return defaultMap;
		return obj;
	}
	
	/*
	 * Register Date and with the json formatter so we get properly encoded strings.
	 */
	static {
		JSONValue.registerFormatter(Date.class, new JSONFormatter() {
			@Override
			public String toJSONString(Object value) {
				return "\"" + IsoDateUtil.getIsoDate((Date)value) + "\"";
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
	
	/**
	 * puts the value if the key is absent (or null).
	 * @param key
	 * @param val
	 */
	public void putIfAbsentWithDot(String key, Object val) {
		if (this.get(key) == null) {
			this.putWithDot(key, val);
		}
	}
	
	/**
	 * just like putAll from map, will return if passed in map is null instead of throwing NPE
	 */
	@Override
	public void putAll(Map mp) {
		if (mp == null)
			return;
		for (Object k : mp.keySet()) {
			this.put(k.toString(), mp.get(k));
		}
	}
	
	/**
	 * like the regular putAll but only copies the 
	 * passed in keys
	 * @param mp
	 * @param keys
	 */
	public void putAll(Map mp, String ...keys) {
		for (String k : keys) {
			this.put(k, mp.get(k));
		}
	}
	
	/**
	 * like the regular putAll but honors dot notation.
	 * passed in keys
	 * @param mp
	 * @param keys
	 */
	public void putAllWithDot(Map mp) {
		if (mp == null)
			return;
		for (Object k : mp.keySet()) {
			this.putWithDot(k.toString(), mp.get(k));
		}
	}
	
	@Override
	public Object put(String key, Object val) {
		this.ejectFromCache(key);
		return super.put(key, val);
	}
	
	/**
	 * Puts the value if and only if the key and val are not null.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public void putIfNotNull(String key, Object val) {
		if (key == null || val == null)
			return;
		put(key, val);
	}
	/**
	 * Puts the value if and only if the key and val are not null.
	 * Will honor the dot operator of the key
	 * @param key
	 * @param val
	 * @return
	 */
	public void putIfNotNullWithDot(String key, Object val) {
		if (key == null || val == null)
			return;
		putWithDot(key, val);
	}
	
	/**
	 * does a put but will honor the dot operator.  ex:
	 * 
	 * put("this.that.val", 0);
	 * 
	 * will do:
	 * 
	 * {
	 *   this : {
	 *   	that : {
	 *   		val : 0
	 *   	}
	 *   }
	 * }
	 * @param key
	 * @param val
	 */
	public void putWithDot(String key, Object val) {
		if (key == null || key.isEmpty())
			return;
		
		String[] keys = key.split("\\.");
		if (keys.length == 1) {
			this.put(key, val);
			return;
		}
		DynMap mp = this;
		for (int i=0; i < keys.length-1; i++) {
			String k = keys[i];
			mp.putIfAbsent(k, new DynMap());
			DynMap tmp = mp.getMap(k);
			mp.put(k, tmp); //we readd it in case the map needed to be typecasted.
			mp = tmp;
		}
		mp.put(keys[keys.length-1], val);
	}
	
	public void removeAll(String...keys) {
		for (String k : keys) {
			this.remove(k);
		}
	}
	
	/**
	 * renames a key.  this is just shorthand for:
	 * 
	 * mp.put(newKey, mp.remove(currentKey));
	 * 
	 * @param currentKey
	 * @param newKey
	 */
	public void rename(String currentKey, String newKey) {
		Object v = this.remove(currentKey);
		if (v == null)
			return;
		
		this.put(newKey, v);
	}
		
	@Override
	public DynMap clone() {
		return DynMapFactory.clone(this);
	}
	
	private void ejectFromCache(String key) {
		if (!this.isCacheEnabled())
			return;
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
		if (!this.cacheEnabled)
			this.cache.clear();
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
			if (cur == null) {
				return null;
			}
			for (int i= 1; i < items.length-1; i++) {				
				cur = cur.get(DynMap.class, items[i]);
				
				if (cur == null)
					return null;
			}
			
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

	/**
	 * Removes key from the mapping.  works with dot operator.
	 */
	@Override
	public Object remove(Object k)
	{
		this.ejectFromCache((String)k);
		String key = (String)k;
		Object val = super.remove(key);
		
		if (val == null && key.contains(".")) {
			//try to reach into the object..
			String[] items = key.split("\\.");
			DynMap cur = this.getMap(items[0]);
			if (cur == null) {
				return null;
			}
			this.put(items[0], cur);
			for (int i= 1; i < items.length-1; i++) {				
				DynMap cur1 = cur.getMap(items[i]);
				
				if (cur1 == null)
					return null;
				cur.put(items[i], cur1); //we readd it in case the map needed to be typecasted.
				cur = cur1;
				
			}
			return cur.remove(items[items.length-1]);
		}
		return val;
	}
	
	public <T> T get(Class<T> cls, String key, T defaultValue) {
		T val = this.get(cls, key);
		if (val == null )
			return defaultValue;
		return val;
	}
	
	public Boolean getBoolean(String key) {
		return this.get(Boolean.class, key);
	}
	
	public Boolean getBoolean(String key, Boolean defaultValue) {
		return this.get(Boolean.class, key, defaultValue);
	}
	
	public String getString(String key) {
		return this.get(String.class, key);
	}
	
	public String getString(String key, String defaultValue) {
		return this.get(String.class, key, defaultValue);
	}
	
	public Integer getInteger(String key) {
		return this.get(Integer.class, key);
	}
	
	public Integer getInteger(String key, Integer defaultValue) {
		return this.get(Integer.class, key, defaultValue);
	}
	
	public Double getDouble(String key) {
		return this.get(Double.class, key);
	}
	
	public Double getDouble(String key, Double defaultValue) {
		return this.get(Double.class, key, defaultValue);
	}
	
	public Long getLong(String key) {
		return this.get(Long.class, key);
	}
	
	public Long getLong(String key, Long defaultValue) {
		return this.get(Long.class, key, defaultValue);
	}
	
	public DynMap getMap(String key) {
		return this.get(DynMap.class, key);
	}
	
	public DynMap getMap(String key, DynMap defaultValue) {
		return this.get(DynMap.class, key, defaultValue);
	}
	
	public Date getDate(String key) {
		return this.get(Date.class, key);
	}
	
	public Date getDate(String key, Date defaultValue) {
		return this.get(Date.class, key, defaultValue);
	}
	
	/**
	 * Returns a keyset with fullkeys 
	 * @return
	 */
	
	public Set<String> keySetWithDot(){
		Set<String> keyset = new HashSet<String>();
		keyset = getFullKey(this);
		return keyset;
	}
	
	private Set<String> getFullKey(DynMap map){
		//String fullkey = "";
		Set<String> keyset = new HashSet<String>();
		for(String key:map.keySet()){
			
			if(map.getMap((String) key)!=null){
				Iterator it = (getFullKey(map.getMap((String) key))).iterator();
				while(it.hasNext()){
					keyset.add((String)key + "." + it.next());
				}
			}
			else
			{	
				
				keyset.add((String) key);
				
			}
			
		}
		
		return keyset;
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
				List<T> val = this.getListForKey(cls, key, delimiters);
				this.cache.put(cacheKey, val);
				return val;
			}
		} 
		return this.getListForKey(cls, key, delimiters);
	}
	
	public <T> List<T> getListForKey(Class<T> cls, String key, String... delimiters) {
		String topKey = key.split("\\.",2)[0];
		String remainKey = (key.split("\\.",2).length >1) ? key.split("\\.",2)[1] : key;
		List<T> retList = new ArrayList<T>(); 
				
		System.out.println("topkey: "+topKey);
		System.out.println("map: "+this+" get:"+this.get(topKey));
		List<DynMap> dynMapList = TypeCast.toTypedList(DynMap.class, this.get(topKey), delimiters);
		if(dynMapList==null){
			List<T> val = TypeCast.toTypedList(cls, this.get(topKey), delimiters);
			if(val!=null){
				retList.addAll(val);
			}
			System.out.println(topKey+" got: "+retList);
		}else{
			for(DynMap map : dynMapList){
				System.out.println(topKey+": "+retList);
				System.out.println("remain: "+remainKey+"\n");
				retList.addAll(map.getListForKey(cls, remainKey, delimiters));
			}
		}
		return retList;
	}
	
	/**
	 * Same as getList only returns an empty typed list, never null.
	 * @param <T>
	 * @param cls
	 * @param key
	 * @param delimiters
	 * @return
	 */
	public <T> List<T> getListOrEmpty(Class<T> cls, String key, String... delimiters) {
		List<T> lst = this.getList(cls, key, delimiters);
		if (lst == null) {
			return new ArrayList<T>();
		}
		return lst;
	}
	
	
	/**
	 * adds the following elements to a list at the requested key.  
	 * if the item at the key is not currently a list, then it is converted to a list.  
	 * 
	 * @param key
	 * @param elements
	 */
	public void addToList(String key, Object ...elements) {
		List lst = this.getListOrEmpty(Object.class, key);
		for (Object obj : elements) {
			lst.add(obj);
		}
		this.put(key,lst);
	}
	
	/**
	 * same as addToList, but will honor the dot operator.
	 * @param key
	 * @param elements
	 */
	public void addToListWithDot(String key, Object ...elements) {
		List lst = this.getListOrEmpty(Object.class, key);
		for (Object obj : elements) {
			lst.add(obj);
		}
		this.putWithDot(key,lst);
	}
	
	
	/**
	 * same principle as jquery extend.
	 * 
	 * each successive map will override any properties in the one before it. 
	 * 
	 * this works recursively, so properties in embedded maps are extended instead of overwritten.
	 * 
	 * Last map in the params is considered the most important one.
	 * 
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
		
		if (mp1 == null)
			return this;
		
		_extend(this, mp1);
		for (Object m : maps) {
			_extend(this, DynMapFactory.instance(m));
		}
		return this;
	}
	
	/**
	 * updates the mp1 map inline, recursively extends the map
	 * @param mp1
	 * @param mp2
	 * @return
	 */
	private static DynMap _extend(DynMap mp1, DynMap mp2) {
		if (mp2 == null)
			return mp1;
		
		for (String key : mp2.keySet()) {
			if (mp1.containsKey(key)) {
				//need to check if this is a map.
				DynMap mpA = mp1.getMap(key);
				if (mpA != null && !mpA.isEmpty()) {
					DynMap mpB = mp2.getMap(key);
					if (mpB != null) {
						mp1.put(key, _extend(mpA, mpB));
						continue;
					} else {
						mp1.put(key, mp2.get(key));
						continue;
					}
				}
			}
			mp1.put(key, mp2.get(key));
		}
		return mp1;
		
	}
	
	/**
	 * returns true if the passed in object map is equivelent 
	 * to this map.  will check members of lists, String, maps, numbers.
	 * 
	 * 
	 * does not check order
	 * 
	 * @param map
	 * @return
	 */
	public boolean equivalent(Object map) {
		DynMap other = DynMap.instance(map, new DynMap());
		if (!ListHelper.equivalent(other.keySet(), this.keySet())) {
			return false;
		}
		for (String key : this.keySet()) {
			Object mine = this.get(key);
			Object yours = other.get(key);
//			log.info("mine: " + mine + " VS yours: " + yours);
			if (mine == null && yours == null)
				continue;
			if (mine == null || yours == null) {
//				log.info("key : " + key + " is null ");
				return false;
			}
			
			 if (ListHelper.isCollection(mine)) {
				if (!ListHelper.isCollection(yours)) {
//					log.info("key : " + key + " is not a collection ");
					return false;
				}
				if (!ListHelper.equivalent(mine, yours)) {
//					log.info("key : " + key + " collection not equiv ");
					return false;
				}
			} else if (isMap(mine)) {
				if (!DynMap.instance(mine, new DynMap()).equivalent(yours)) {
//					log.info("key : " + key + " map not equiv ");
					return false;
				}
			} else {
				//default to string compare.
				if (!this.getString(key).equals(other.getString(key))) {
//					log.info("key : " + key + " " + this.getString(key) + " VS " + other.getString(key));
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isMap(Object obj) {
		if (obj instanceof Map) 
			return true;
		if (obj instanceof DynMapConvertable)
			return true;
		
		if (Reflection.hasMethod(obj, "toMap"))
			return true;
		return false;
	}
	
	public String toJSONString() {
		return JSONObject.toJSONString(this);
	}
	
	/**
	 * returns [key,value]
	 * @param mp
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private Map<String,Object> toUrlMap(String keyStart, Map mp) throws UnsupportedEncodingException {
		Map<String, Object> ret = new TreeMap<String,Object>();
		for (Object key : mp.keySet()) {
			Object val = mp.get(key);
			String k = keyStart + "[" + URLEncoder.encode(key.toString(), "utf-8") + "]";
			if (val instanceof Map) {
				ret.putAll(toUrlMap(k, (Map)val));
			} else {
				ret.put(k, val);
			}
		}
		return ret;
		
	}
	
	/**
	 * adds an encoded key, not encoded value.
	 * @param str
	 * @param k
	 * @param value
	 * @throws UnsupportedEncodingException 
	 */
	private void addUrlKey(StringBuilder str, String k, Object value) throws UnsupportedEncodingException {
		if (k == null || value == null)
			return;
		
		if (ListHelper.isCollection(value)) {
			for (String v : TypeCast.toTypedList(String.class, value)) {
				this.addUrlKey(str, k, v);
			}
		} else {
			String v = URLEncoder.encode(TypeCast.cast(String.class, value), "utf-8");
			str.append(k);
			str.append("=");
			str.append(v);
			str.append("&");
		}
	}
	/**
	 * will return the map as a url encoded string in the form:
	 * key1=val1&key2=val2& ...
	 * 
	 * This can be used as getstring or form-encoded post. 
	 * Lists are handled as multiple key /value pairs.
	 * 
	 * Maps are encoded rails style, so key[key1][key2]=value
	 * 
	 * will skip keys that contain null values.
	 * keys are sorted alphabetically so ordering is consistent
	 * 
	 * 
	 * 
	 * 
	 * @return The url encoded string, or empty string.
	 */
	public String toURLString() {
		StringBuilder str = new StringBuilder();
		
		List<String> keys = new ArrayList<String>();
		keys.addAll(this.keySet());
		Collections.sort(keys);
		
		for (String key : keys) {
			try {
				String k = URLEncoder.encode(key, "utf-8");
				Object val = this.get(k);
				if (val instanceof Map) {
					Map<String, Object> kv = this.toUrlMap(k, (Map)val);
					for (String mpkey : kv.keySet()) {
						this.addUrlKey(str, mpkey, kv.get(mpkey));
					}
				} else {
					this.addUrlKey(str, k, val);
				}
			} catch (Exception x) {
				log.log(Level.INFO, "Caught", x);
				continue;
			}
		}
		//trim trailing amp?
		return StringHelper.trim(str.toString(), "&");
	}
	
	private String toXMLStringCollection(java.util.Collection c) {
		if (c == null)
			return "";

		String collection = "";
		for (Object o : c) {
			collection += "<item>";
			if (o instanceof DynMap)
				collection += ((DynMap) o).toXMLString();
			else if (o instanceof java.util.Collection) {
				for (Object b : (java.util.Collection) o) {
					collection += "<item>";
					if (b instanceof java.util.Collection)
						collection += this
								.toXMLStringCollection((java.util.Collection) b);
					else
						collection += b.toString();
					collection += "</item>";
				}
			} else if (o instanceof java.util.Map) {
				DynMap dm = new DynMap();
				dm.putAll((java.util.Map) o);
				collection += dm.toXMLString();
			} else
				collection += o.toString();
			collection += "</item>";
		}
		return collection;
	}
	
	
	/**
	 * Constructs an xml string from this dynmap.  
	 * @return
	 */
	public String toXMLString() {
		if (this.isEmpty())
			return null;

		StringBuilder buf = new StringBuilder();
		Iterator iter = this.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String element = String.valueOf(entry.getKey());
			buf.append("<" + element + ">");
			if (entry.getValue() instanceof DynMap) {
				buf.append(((DynMap) entry.getValue())
						.toXMLString());
			} else if ((entry.getValue()) instanceof java.util.Collection) {
				buf.append(this
						.toXMLStringCollection((java.util.Collection) entry
								.getValue()));
			} else if ((entry.getValue()) instanceof java.util.Map) {
				DynMap dm = DynMapFactory.instance(entry.getValue());
				buf.append(dm.toXMLString());
			} else if ((entry.getValue()) instanceof Date) {
				buf.append(IsoDateUtil.getIsoDateNoMillis(((Date)entry.getValue())));
			} else {
				buf.append(entry.getValue());
			}
			buf.append("</" + element + ">");
		}

		return buf.toString();
	}
}
