/**
 * 
 */
package com.trendrr.json.simple;

/**
 * 
 * Register a formatter via JSONValue.registerFormatter
 * 
 * 
 * example:
 * 
 *	JSONValue.registerFormatter(Date.class, new JSONFormatter() {
 *		@Override
 *		public String toJSONString(Object value) {
 *			return "\"" + ((Date)value).toGMTString() + "\"";
 *		}
 *	});
 * 
 * 
 * @author Dustin Norlander
 * @created Dec 29, 2010
 * 
 */
public interface JSONFormatter {
	public String toJSONString(Object value);
}
