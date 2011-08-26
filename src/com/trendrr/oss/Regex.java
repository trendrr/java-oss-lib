/**
 *  ___          _   _      _  _ 
 * |   \ _  _ __| |_(_)_ _ | \| |
 * | |) | || (_-<  _| | ' \| .` |
 * |___/ \_,_/__/\__|_|_||_|_|\_|
 *
 * Copyright (C) 2006 Dustin Norlander
 */
package com.trendrr.oss;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author dustin
 *
 */
public class Regex {

	
	/**
	 * 
	 */
	public Regex() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Matches the first instancec of the regex and returns it, else returns null
	 * 
	 * @param input
	 * @param regex
	 * @param ignoreCase
	 * @param group
	 * @return
	 */
	public static String matchFirst(String input, String regex, boolean ignoreCase, Integer group) {
	
		if (input == null || regex == null)
			return null;
		try {
			
			Pattern pattern = Pattern.compile(regex);
			if (ignoreCase) {
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			}
			Matcher matcher = pattern.matcher(input);
			if (matcher.find()) {
				if (group == null) {
					return matcher.group();	
				} else {
					return matcher.group(group);	
				}
			}
		} catch (Exception x) {
//			log.info("Caught", x);
		}
		return null;
	}
	
	public static String matchFirst(String input, String regex, boolean ignoreCase) {
		return matchFirst(input, regex, ignoreCase, null);
	}
	
	public static boolean contains(String input, String regex, boolean ignoreCase) {
		return (matchFirst(input, regex, ignoreCase) != null);
	}
	
	/**
	 * ps
	 * @param input
	 * @param ignoreCase
	 * @param regex
	 * @return
	 */
	public static boolean contains(String input, String ...regex) {
		for (String r : regex) {
			if (contains(input, r, false))
				return true;
		}
		return false;
	}
	public static boolean containsIgnoreCase(String input, String ...regex) {
		for (String r : regex) {
			if (contains(input, r, true))
				return true;
		}
		return false;
	}
	
	
	/**
	 * returns true if the regex matches the entire input string
	 * @param input
	 * @param regex
	 * @param ignoreCase
	 * @return
	 */
	public static boolean matches(String input, String regex, boolean ignoreCase) {
		String m = matchFirst(input, regex, ignoreCase);
		if (m == null)
			return false;
		return (m.length() == input.length());
	}
	
	
	public static String matchLast(String input, String regex, boolean ignoreCase) {
		List<String> tmp = matchAll(input, regex, ignoreCase);
		if (tmp == null || tmp.isEmpty())
			return null;
		return tmp.get(tmp.size()-1);
	}
	public static List<String> matchAll(String input, String regex, boolean ignoreCase) {
		if (input == null || regex == null)
			return null;
		
		try {
			Vector<String> results = new Vector<String> ();
			Pattern pattern = Pattern.compile(regex);
			if (ignoreCase) {
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			}
			Matcher matcher = pattern.matcher(input);
			while (matcher.find()) {
				results.add(matcher.group());
			}
			return results;
		} catch (Exception x) {
//			log.info("Caught", x);
		}
		return null;

	}
}
