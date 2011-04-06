/**
 * 
 */
package com.trendrr.oss.casting;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import com.trendrr.json.simple.JSONValue;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Dec 29, 2010
 * 
 */
public class DynMapCaster extends TypeCaster<DynMap> {
	Logger log = Logger.getLogger(DynMapCaster.class.getCanonicalName());
	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#doCast(java.lang.Class, java.lang.Object)
	 */
	@Override
	protected DynMap doCast(Class clss, Object object) {
		
		if (object instanceof Map) {
			return toDynMap((Map)object);
		}
		
		if (object instanceof String) {
			//try the json simple here. 
			try {
				Object obj = JSONValue.parseWithException((String)object);
				if (obj instanceof Map){
					return toDynMap((Map)obj);
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
			return null;
		}
		
		
		Class cls = object.getClass();
		try {
			Method toMap = cls.getMethod("toMap");
			log.info("found toMap method");
			Map mp = (Map)toMap.invoke(object);
			return toDynMap((Map)mp);
			
		} catch (Exception x) {
//			log.info("Caught",x);
		}
		return null;
	}
	
	private DynMap toDynMap(Map map) {
		DynMap mp = new DynMap();
		for (Object key : map.keySet()) {
			mp.put(key.toString(), map.get(key));
		}
		return mp;
	}
}
