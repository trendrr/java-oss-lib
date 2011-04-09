/**
 * 
 */
package com.trendrr.oss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dustin Norlander
 * @created Apr 6, 2011
 * 
 */
public class ListHelper {

	protected Log log = LogFactory.getLog(ListHelper.class);
	
	/**
	 * checks if the lists are equivelent, i.e. all items in both lists are the same
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static boolean equivalent(Object l1, Object l2) {
		try {
			if (l1 == l2)
				return true;
			
			if (l1 == null || l2 == null)
				return false;
			
			
			List list1 = TypeCast.toList(l1);
			List list2 = TypeCast.toList(l2);
			System.out.println("LIST1 : " + list1);
			System.out.println("LIST2 :" + list2);
			System.out.println("END");
			return list1.containsAll(list2);
		} catch (Exception x) {
		} 
		return false;
	}
	
	public static String delimit(Collection input, String delimiter) {
		StringBuffer buf = new StringBuffer();
		boolean isStart = true;
		for (Object str : input) {
			if (!isStart) {
				buf.append(delimiter);
			}
			buf.append(str);
			isStart = false;
		}
		return buf.toString();
	}
	
	/**
	 * Wrapper around TypeCast.getTypedList
	 * 
	 * Will not return an empty list, only null.  Always returns a new List.
	 * @param <T>
	 * @param cls
	 * @param obj
	 * @param delimiters
	 * @return
	 */
	public static <T> List<T> toTypedList(Class<T> cls, Object obj, String ... delimiters) {
		return TypeCast.toTypedList(cls, obj, delimiters);
	}
	
	/**
	 * returns TypeCasted value from the list, else null. Should never throw exception.
	 * @param <T>
	 * @param cls
	 * @param index
	 * @param list
	 * @return
	 */
	public static <T> T getIndex(Class<T> cls, int index, Object list) {
		Object obj = getIndex(index, list);
		if (obj == null)
			return null;
		return TypeCast.cast(cls, obj);
	}
	
	/**
	 * convient way to get values out of a list, collection, or array.
	 * 
	 * will never throw exceptions, will just return null if its out of bounds.
	 * 
	 * @param index
	 * @param list Collection, List, or Array
	 * @return
	 */
	public static Object getIndex(int index, Object list) {
		if (index < 0 || list == null)
			return null;
		
		if (list instanceof List) {
			//check bounds
			List l = (List)list;
			if (l.size() <= index)
				return null;
			return ((List)list).get(index);
		}
		
		if (list instanceof Collection) {
			Collection col = (Collection)list;
			if (col.size() <= index)
				return null;
			int i = 0;
			for (Object val : col) {
				if (i == index)
					return val;
				i++;
			}
			return null;
		}
		
		if (list instanceof Object[]) {
			Object[] array = (Object[])list;
			if (array.length <= index)
				return null;
			return array[index];
		}
		return null;
	}
	
	/**
	 * If no delimiters are supplied, one-element list containing <code>input</code>
	 * @param input
	 * @param delimiters
	 * @return
	 */
	public static List<String> split(String input, String ...delimiters) {
		if (input == null)
			return null;
		String str = input;
		List<String> list = new ArrayList<String>();
		
		if(delimiters==null || delimiters.length<1){
			list.add(input);
			return list;
		}
		
		String delim = delimiters[0];
		
		for (int i=1; i < delimiters.length; i++) {
			str = str.replaceAll(delimiters[i], delim);
		}
		String[] tmp = str.split(delim);
		for (int i=0; i < tmp.length; i++) {
			list.add(tmp[i].trim());
		}
		return list;
	}
	
	/**
	 * Removes any duplicate elements from the collection
	 * 
	 * @param vals
	 */
	public static void uniquify(Collection vals) {
		Set st = new HashSet();
		
		st.addAll(vals);
		vals.clear();
		vals.addAll(st);
		return;
	}
	
	/**
	 * will return true if the passed in object is some kind of interateble set.
	 * array, collection, enumeration
	 * @param obj
	 * @return
	 */
	public static boolean isCollection(Object obj) {
		if (obj instanceof Enumeration)
			return true;
		if (obj instanceof Collection)
			return true;
		if (obj instanceof Object[])
			return true;
		return false;
	}
}
