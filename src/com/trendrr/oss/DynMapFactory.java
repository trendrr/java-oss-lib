/**
 * 
 */
package com.trendrr.oss;

import java.net.URLDecoder;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * @author Dustin Norlander
 * @created Dec 29, 2010
 * 
 */
public class DynMapFactory {
	protected static Logger log = Logger.getLogger(DynMapFactory.class.getCanonicalName());
	

	/**
	 * returns a comparator that will compare based on the passed in key.
	 * value is assumed to be a String and case insensitive.
	 * 
	 * @return
	 */
	public static Comparator<DynMap> comparatorString(String key) {
		final String k = key;
		return new Comparator<DynMap>() {
			@Override
			public int compare(DynMap o1, DynMap o2) {
				String v1 = o1.getString(k);
				String v2 = o2.getString(k);
				if (v1 == null && v2 == null)
					return 0;
				if (v2 == null) {
					return 1;
				}
				if (v1 == null) {
					return -1;
				}
				return v1.toLowerCase().compareTo(v2.toLowerCase());
			}
		};
	}
	
	
	/**
	 * returns a comparator that will compare based on the passed in key
	 * @return
	 */
	public static Comparator<DynMap> comparator(Class cls, String key) {
		final String k = key;
		final Class c = cls;
		return new Comparator<DynMap>() {

			@Override
			public int compare(DynMap o1, DynMap o2) {
				Object v1 = o1.get(c, k);
				Object v2 = o2.get(c, k);
				if (v1 == null && v2 == null)
					return 0;
				if (v2 == null) {
					return 1;
				}
				if (v1 == null) {
					return -1;
				}
				return ((Comparable)v1).compareTo(v2);
			}
			
		};
	}
	
	
	/**
	 * Creates a new DynMap instance from the passed in object. 
	 * 
	 * This differs from instance ONLY when obj is a DynMap instance.
	 * @param obj
	 * @return
	 */
	public static DynMap clone(Object obj) {
		DynMap val = instance(obj);
		if (val == null)
			return val;
		if (val != obj) {
			return val;
		}
		DynMap tmp = new DynMap();
		tmp.putAll(val);
		return tmp;
	}
	
	/**
	 * creates a DynMap instance.
	 * 
	 * Will convert a regular map, or any object that has a toMap method
	 * a string is assumed to be json.
	 * 
	 * if obj is an instance of DynMap then that DynMap instance is returned.
	 * 
	 * @param obj
	 * @return
	 */
	public static DynMap instance(Object obj) {
		return TypeCast.cast(DynMap.class, obj);
	}
	
	/**
	 * 
	 * @param json
	 * @return
	 */
	public static DynMap instanceFromJSON(String json) {
		return instance(json);
	}
	
	/**
	 * loads a dynmap from a file.  file is assumed to be text, json encoded.
	 * 
	 * 
	 * @param file
	 * @return
	 */
	public static DynMap instanceFromFile(String filename) {
		try {
			String val = FileHelper.loadString(filename);
			return instance(val);
		} catch (Exception x) {
			log.log(Level.WARNING, "Unable to load filename: " + filename, x);
		}
		return null;
	}
	
	
	/**
	 * parses a urlencoded string
	 * 
	 * @param urlencoded
	 * @return
	 */
	public static DynMap instanceFromURLEncoded(String urlencoded) {
		DynMap params = new DynMap();
		for (String param : urlencoded.split("\\&")) {
			String[] tmp = param.split("\\=");
			if (tmp == null || tmp.length != 2)
				continue;
			try {
				String key = URLDecoder.decode(tmp[0], "utf8");
				String value = URLDecoder.decode(tmp[1], "utf8");
				
				if (params.containsKey(key)) {
					List<String> list = params.getList(String.class, key);
					list.add(value);
					params.put(key, list);
				} else {
					params.put(key, value);
				}
			} catch (Exception x) {
				log.log(Level.SEVERE, "Caught", x);
			}
		}
		return params;
	}
	
	/**
	 * parses any of the query params into a DynMap.  returns an empty map if
	 * there are no params to parse.
	 * 
	 * @param url
	 * @return
	 */
	public static DynMap instanceFromURL(String url) {
		if (!url.contains("?")) {
//			log.info("DynMap.instanceFromUrl : no params on the url, returning empty");
			return new DynMap();
		}
		String q = url.split("\\?")[1];
		
		return instanceFromURLEncoded(q);
	}
}
