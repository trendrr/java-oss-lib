/**
 * 
 */
package com.trendrr.oss.casting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.trendrr.oss.TypeCast;


/**
 * @author Dustin Norlander
 * @created Dec 1, 2010
 * 
 */
public class ListCaster extends TypeCaster<Collection> {
	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#doCast(java.lang.Class, java.lang.Object)
	 */
	@Override
	protected Collection doCast(Class cls, Object obj) {
		return TypeCast.toList(obj);
	}
}
