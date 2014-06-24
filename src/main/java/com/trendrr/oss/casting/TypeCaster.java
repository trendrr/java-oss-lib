/**
 * 
 */
package com.trendrr.oss.casting;



/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public abstract class TypeCaster<T> {
	
	public <C> C cast(Class<C> cls, Object obj) {
		return cast(cls, obj, null);
	}
	
	public <C> C cast(Class<C> cls, Object obj, C defaultVal) {
		if (obj == null)
			return defaultVal;
		//check if this is already a correct instance.
		if (cls.isInstance(obj)) {
			return (C)obj;
		}
		try {
			T retVal = this.doCast(cls, obj);
			if (retVal != null)
				return (C)retVal;
		} catch (Exception x) {
			//swallow
		}
		return defaultVal;
	}
	
	/**
	 * should cast to the registered type. if unable to cast then return null.
	 * @param obj
	 * @return
	 */
	protected abstract T doCast(Class cls, Object obj);
}
