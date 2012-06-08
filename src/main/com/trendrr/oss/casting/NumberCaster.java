/**
 * 
 */
package com.trendrr.oss.casting;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.TypeCast;



/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public class NumberCaster extends TypeCaster<Number> {

	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#cast(java.lang.Object)
	 */
	@Override
	protected Number doCast(Class cls, Object obj) {
		try {
			if (obj instanceof Number) {
				Number num = (Number)obj;
				return this.fromNumber(cls, num);
			}
			//we remove characters that could resonably be associated with a number
			String str = TypeCast.cast(String.class, obj);
			if (str == null)
				return null;
			str = str.replaceAll("[^0-9\\.\\-eE]", "");//StringHelper.removeAll(TypeCast.cast(String.class, obj), ' ', ',', '$');
			
			if (cls.equals(Long.class)) {
				return Long.parseLong(str);
			} else {
				//just parse to a double and try again.
				Double num = Double.parseDouble(str);
				if (num.isInfinite() || num.isNaN())
					return null;
				return this.fromNumber(cls, num);
			}
		} catch (Exception x) {
			
		}
		return null;
	}

	private Number fromNumber(Class cls, Number num) {
		if (cls.equals(Number.class)) {
			return num;
		}
		if (cls.equals(Byte.class)) {
			return num.byteValue();
		}
		if (cls.equals(Short.class)) {
			return num.shortValue();
		}
		if (cls.equals(Integer.class)) {
			return num.intValue();
		}
		if (cls.equals(Double.class)) {
			return num.doubleValue();
		}
		if (cls.equals(Long.class)) {
			return num.longValue();
		}
		if (cls.equals(Float.class)) {
			return num.floatValue();
		}
		return null;
		
		
	}
}
