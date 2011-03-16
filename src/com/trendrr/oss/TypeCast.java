/**
 * 
 */
package com.trendrr.oss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.trendrr.json.simple.JSONValue;
import com.trendrr.oss.casting.*;


/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public class TypeCast {

	static ConcurrentHashMap<Class, TypeCaster> casters = new ConcurrentHashMap<Class, TypeCaster>();
	static {
		registerTypeCaster(String.class, new StringCaster());
		registerTypeCaster(Number.class, new NumberCaster());
		registerTypeCaster(Byte.class, new NumberCaster());
		registerTypeCaster(Short.class, new NumberCaster());
		registerTypeCaster(Integer.class, new NumberCaster());
		registerTypeCaster(Float.class, new NumberCaster());
		registerTypeCaster(Double.class, new NumberCaster());
		registerTypeCaster(Long.class, new NumberCaster());
		registerTypeCaster(Date.class, new DateCaster());
		registerTypeCaster(Boolean.class, new BooleanCaster());		
		registerTypeCaster(List.class, new ListCaster());
		registerTypeCaster(Collection.class, new ListCaster());
		registerTypeCaster(DynMap.class, new DynMapCaster());
		
	}
	static TypeCaster defaultCaster = new DefaultCaster();
	
	
	public static void main(String ...c) {
		
		System.out.println(TypeCast.cast(String.class, 67, "none"));
		System.out.println(TypeCast.cast(Double.class, "7.34"));
		System.out.println(TypeCast.cast(Boolean.class, "f"));
		
	}
	
	public static void registerTypeCaster(Class cls, TypeCaster caster) {
		casters.put(cls, caster);
	}
	public static <T> T cast(Class<T> cls, Object obj) {
		return cast(cls, obj, null);
	}
	public static <T> T cast(Class<T> cls, Object obj, T defaultVal) {
		TypeCaster caster = casters.get(cls);
		if (caster == null) {
			caster = defaultCaster;
		}
		T retVal = (T)caster.cast(cls, obj);
		if (retVal != null)
			return retVal;
		return defaultVal;
	}
	
	/**
	 * returns the properly typed list.
	 * The list will *always* be a new list, even if the passed in obj is the properly typed list.
	 * If the obj is a string then it will be split using the passed in delimiters.
	 * 
	 * Never returns an empty list, will only return null.
	 * @param <T>
	 * @param cls
	 * @param obj
	 * @param delimiters
	 * @return
	 */
	public static <T> List<T> getTypedList(Class<T> cls, Object obj, String ... delimiters) {
		List lst = toList(obj, delimiters);
		if (lst == null)
			return null;
		
		List<T> list = new ArrayList<T>();
		
		for (Object val : lst) {
			T tmp = cast(cls, val);
			if (tmp != null)
				list.add(tmp);
		}
		if (list.isEmpty())
			return null;
		return list;
	}
	
	/**
	 * converts to a list.  
	 * will convert:
	 * Collection
	 * Enumeration
	 * Object[]
	 * String (will split based on delimiters).
	 * 
	 * in all other cases:
	 * Will return null on null value.
	 * otherwise will return a list of length 1, with the value
	 * 
	 * 
	 * @param obj
	 * @param delimiters
	 * @return
	 */
	public static List toList(Object obj, String ... delimiters) {
		Object val = obj;
		if (val == null)
			return null;
		List list = new ArrayList();
		
		
		if (val instanceof List) {
			list = (List)val;
			
		} else if (val instanceof String[]) {
			String str = TypeCast.cast(String.class, val);
			if (str != null && StringHelper.contains(str, delimiters)) {
				return toList(str, delimiters);
			}
			for (int i=0; i < ((Object[])val).length; i++) {
				Object tmp = (((Object[])val)[i]);
				if (tmp != null)
					list.add(tmp);
			}
			
		} else if (val instanceof Object[]) {
			for (int i=0; i < ((Object[])val).length; i++) {
				Object tmp = (((Object[])val)[i]);
				if (tmp != null)
					list.add(tmp);
			}
		} else if (val instanceof Collection) {
			for (Object tmp : (Collection)val) {
				if (tmp != null)
					list.add(tmp);
			}
		} else if (obj instanceof String) {
			String str = TypeCast.cast(String.class, val);
			if (str == null || str.isEmpty())
				return null;
			if (str.startsWith("[") && str.endsWith("]")) {
				//parse json string. 
				//try the json simple here. 
				try {
					Object jsonobj = JSONValue.parseWithException(str);
					return toList(jsonobj, delimiters);
				} catch (Exception x) {
					return null;
				}
			}
			if (delimiters != null && delimiters.length > 0)
				list.addAll(StringHelper.split(str, delimiters));
		} else if (obj instanceof Enumeration) {
			Enumeration e = (Enumeration)obj;
			while (e.hasMoreElements()) {
				list.add(e.nextElement());
			}
		}
		if (list != null && list.isEmpty()) {
			list.add(obj);
		}
		
		
		if (list != null && !list.isEmpty())
			return list;
		return null;
	}

}
