/**
 * 
 */
package com.trendrr.oss.networking.cheshire;

/**
 * @author Dustin Norlander
 * @created Apr 6, 2011
 * 
 */
public enum Verb {
	GET(),
	POST(),
	PUT(),
	DELETE();
	
	
	public static Verb instance(String verb) {
		if (verb == null)
			return null;
		
		String tmp = verb.toUpperCase().trim();
		for (Verb v : Verb.values()) {
			if (v.toString().equals(tmp))
				return v;
		}
		return null;
	}
}
