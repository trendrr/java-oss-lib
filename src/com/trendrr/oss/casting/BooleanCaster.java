/**
 * 
 */
package com.trendrr.oss.casting;

import com.trendrr.oss.TypeCast;


/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public class BooleanCaster extends TypeCaster<Boolean> {

	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#doCast(java.lang.Class, java.lang.Object)
	 */
	@Override
	protected Boolean doCast(Class cls, Object obj) {
		String temp = TypeCast.cast(String.class, obj);
		if (temp == null)
			return null;
	
		if (temp.equalsIgnoreCase("true")
				|| temp.equalsIgnoreCase("checked")
				|| temp.equalsIgnoreCase("on")
				|| temp.equals("1")
				|| temp.equalsIgnoreCase("t")
				|| temp.equalsIgnoreCase("yes")
				|| temp.equalsIgnoreCase("y")
				) {
			return true;
		}
		
		if (temp.equalsIgnoreCase("false")
				|| temp.equalsIgnoreCase("off")
				|| temp.equals("0")
				|| temp.equalsIgnoreCase("f")
				|| temp.equalsIgnoreCase("no")
				|| temp.equalsIgnoreCase("n")
				) {
			return false;
		}
		return null;
	}
}
