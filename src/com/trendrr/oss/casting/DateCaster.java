/**
 * 
 */
package com.trendrr.oss.casting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.trendrr.oss.IsoDateUtil;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.TypeCast;



/**
 * @author Dustin Norlander
 * @created Nov 30, 2010
 * 
 */
public class DateCaster extends TypeCaster<Date> {

	public static final DateFormat RSS_DATE_FORMAT =
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.casting.TypeCaster#cast(java.lang.Class, java.lang.Object)
	 */
	@Override
	public Date doCast(Class cls, Object obj) {
		if (obj instanceof Calendar) 
			return ((Calendar)obj).getTime();
		
		if (obj instanceof Long) {
			long val = (Long)obj;
			if (val < 200000000000l) {
				//date is before 1976 so we just assume
				//assume this is seconds since epoch
				val *= 1000;
			}
			return new Date(val);
		}
		
		//convert joda DateTimezx
		if (Reflection.hasMethod(obj, "toDate")) {
			return (Date)Reflection.execute(obj, "toDate");
		}
		String str = TypeCast.cast(String.class, obj);
		try {
			//try iso format
			return IsoDateUtil.parse(str);
		} catch (Exception x) {
			//swallow
		}
		
		try {
			//try RSS 2.0 standard
			return RSS_DATE_FORMAT.parse(str);
		} catch (Exception x) {
			
		}
		
		try {
			//TODO: reverse standard Date().toString()
			
			
		} catch (Exception x) {
			
		}

		
		
		
		
		return null;
	}

}
