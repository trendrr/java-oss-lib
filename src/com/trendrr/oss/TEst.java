/**
 * 
 */
package com.trendrr.oss;

import java.util.Date;

import com.trendrr.json.simple.JSONFormatter;
import com.trendrr.json.simple.JSONValue;



/**
 * @author Dustin Norlander
 * @created Dec 29, 2010
 * 
 */
public class TEst {
	public static void main(String ...strings) {
		/*
		 * to create the patch:
		 * diff -crB Desktop/json_simple-1.1-all/src/org/ /media/work/java-workspace/TrendrrOSS/src/org/ > simplejson.patch
		 * 
		 */
		
		JSONValue.registerFormatter(Date.class, new JSONFormatter() {
			@Override
			public String toJSONString(Object value) {
				return ((Date)value).toGMTString();
			}
		});
		
		System.out.println(JSONValue.toJSONString(new Date()));
		System.out.println(JSONValue.toJSONString("BLAH"));
		String [] tmp = new String[3];
		System.out.println(tmp.getClass().getCanonicalName());
		System.out.println(tmp instanceof Object[]);
		System.out.println(tmp instanceof String[]);
		
	}
}
