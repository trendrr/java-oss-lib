/**
 * 
 */
package com.trendrr.oss.casting;



/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public class DefaultCaster extends TypeCaster {

	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#doCast(java.lang.Class, java.lang.Object)
	 */
	@Override
	protected Object doCast(Class cls, Object obj) {
		try {
			//try a straight cast.
			return cls.cast(obj);
		} catch (ClassCastException x) {
			//swallow
		}
		return null;
	}
}
