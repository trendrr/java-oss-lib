/**
 * 
 */
package com.trendrr.oss;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

import com.trendrr.oss.concurrent.LazyInit;


/**
 * @author dustin
 *
 */
public class StringHelper {
	
	
	public static void main(String...strings) {
		
	}
	
	/**
	 * joins a list into a string.  similar to methods available IN ANY OTHER LANGUAGE
	 * @param lst
	 * @param delim
	 * @return
	 */
	public static String join(List lst, String delim) {
		StringBuilder builder = new StringBuilder();
		for (Object obj : lst) {
			if (obj != null)
				builder.append(obj.toString());
			builder.append(delim);
		}
		
		builder.delete(builder.length()-delim.length(), builder.length());
		return builder.toString();
	}
	
	public static String underscoreSpaces(String input) {
		if (input == null)
			return null;
		String tmp = input.trim().replaceAll("\\s+", "_");
		tmp = tmp.replaceAll("_+", "_");
			return tmp;
	} 	 
	
	public static String reverse(String input) {
		StringBuffer buf = new StringBuffer(input);
		return buf.reverse().toString();
	}

	/**
	 * capitalizes the first letter.
	 * @param input
	 * @return
	 */
	public static String capitalize(String input) {
		if (input == null || input.isEmpty())
			return input;
		
		 return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
	
	/**
	 * lower cases the first letter
	 * @param input
	 * @return
	 */
	public static String uncapitalize(String input) {
		if (input == null || input.isEmpty())
			return input;
		return input.substring(0, 1).toLowerCase() + input.substring(1);
	}
	
	public static String capitalizeWords(String input) {
		String [] tmp = input.split("\\s+");
		String retVal = "";
		
		for (String word : tmp) {
			retVal += capitalize(word) + " ";
		}
		return retVal.trim();
	}
	
	public static String singularize(String input) {
		String str = input;
		if (str.endsWith("s")) 
			str = str.substring(0, str.length() -1);
		return str;
	}
	
	/**
	 * Does a simple string compare, ignoring case and punctuation.
	 * @param str1 
	 * @param str2
	 * @return
	 */
	public static boolean like(String str1, String str2) {
		String tmp1 = prepareStringSearch(str1);
		String tmp2 = prepareStringSearch(str1);
		return tmp1.equals(tmp2);
	}
	
	/**
	 * Does a simple string search to see if str1 contains str2.
	 * Ignores case, punctuation
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	private static String prepareStringSearch(String str) {
//		add a space at the beginning to make stripping 'a', 'the' ect easier.
		String tmp = " " + str.toLowerCase();
		tmp = tmp.replaceAll("\\s\\&\\s", " and ");
		tmp = tmp.replaceAll("[\\,\\-]", " ");
		tmp = tmp.replaceAll("[^a-z0-9\\s]", "");
		tmp = tmp.replaceAll("\\sthe\\s", " ");
		tmp = tmp.replaceAll("\\sa\\s", " ");
		tmp = tmp.replaceAll("\\sto\\s", " ");
		tmp = tmp.trim().replaceAll("\\s+", " ");
		return tmp;
	}
	
	/**
	 * Checks if str1 contains str2.
	 * ignores case and punctuation and words 'the' 'a' 'to'.
	 * 
	 * if wordBoundaries is true then it only returns true on word matches:
	 * ie. 
	 * 
	 * wordBoundaries = true : "jon johnson" matches "jon" or "johnson"
	 * wordBoundaries = false : matches all from above plus "john" "hnson" ect..
	 * @param str1
	 * @param str2
	 * @param wordBoundaries 
	 * @return
	 */
	public static boolean contains(String str1, String str2, boolean wordBoundaries) {	
		String tmp1 = (wordBoundaries?" ":"") + prepareStringSearch(str1) + (wordBoundaries?" ":"");
		String tmp2 = (wordBoundaries?" ":"") + prepareStringSearch(str2) + (wordBoundaries?" ":"");
		return tmp1.indexOf(tmp2) != -1;
	}
	
	public static final char[] CHARACTERS = {	//indexes
		'A','B','C','D','E','F','G','H', 	//0-7
		'I','J','K','L','M','N','O','P', 	//8-15
		'Q','R','S','T','U','V','W','X', 	//16-23
		'Y','Z','a','b','c','d','e','f', 	//24-31
		'g','h','i','j','k','l','m','n', 	//32-39
		'o','p','q','r','s','t','u','v', 	//40-47
		'w','x','y','z','0','1','2','3', 	//48-55
		'4','5','6','7','8','9' 			//56-61
	};
	
	/**
	 * Generates a random string of length length,
	 * containing a-z, A-Z, 0-9
	 * 
	 */
	public static String getRandomString(int length) {
		Random generator = new Random();
		char str[] = new char[length];
		for (int i=0; i < length; i++) {
			str[i] = CHARACTERS[generator.nextInt(CHARACTERS.length)];
		}
		return new String (str);
	}
	
	public static String toHex(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i=0;i<bytes.length;i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
			    // could use a for loop, but we're only dealing with a single byte
			    hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	/**
	 * randomizes a string ordering.  probably could be faster
	 * @param str
	 * @return
	 */
	public static String shuffle(String str) {
	    if (str.length()<=1)
	        return str;
	 
	    int split=str.length()/2;
	 
	    String temp1=shuffle(str.substring(0,split));
	    String temp2=shuffle(str.substring(split));
	 
	    if (Math.random() > 0.5) 
	        return temp1 + temp2;
	    else
	        return temp2 + temp1;
    }
	/**
	 * Generates a random string of length length,
	 * containing a-z, A-Z, 0-9
	 * 
	 */
	public static String randomString(int length) {
		return getRandomString(length);
	}
	
	public static String getRandomNumberString(int length) {
		Random generator = new Random();
		char str[] = new char[length];
		for (int i=0; i < length; i++) {
			str[i] = CHARACTERS[51 + generator.nextInt(10)];
		}
		return new String (str);
		
	}
	
	/**
	 * escapes the string for use in javascript
	 * @param input
	 * @return
	 */
	public static String javascriptEscape(String input) {
		if (input == null)
			return null;
		String tmp = input.replaceAll("'", "\\'");
		tmp = tmp.replaceAll("\"", "\\\"");
		return tmp;
	}

	
	public static String camelize(String word) {
        return camelize(word, false);
    }


	/**
	 * camel cases the passed in string.  spaces and underscores are removed and trigger a capitalization.
	 * @param word
	 * @return
	 */
    public static String camelize(String word, boolean lowercaseFirstLetter) {
    	if (word == null)
    		return null;
       String[] words = word.split("[\\s_]+");
       StringBuffer buf = new StringBuffer();
       
       if (lowercaseFirstLetter) {
    	   buf.append(words[0].substring(0, 1).toLowerCase() + words[0].substring(1));
       } else {
    	   buf.append(capitalize(words[0]));
       }
       for (int i=1; i < words.length; i++) {
    	   buf.append(capitalize(words[i]));
       }
       return buf.toString();
    }

    /**
     * uncamelizes the input string.
     * does not lowercase anything, just adds a space infront of any capitals
     * @param input
     * @return
     */
    public static String uncamelize(String input) {
    	StringBuilder buf = new StringBuilder();
    	for (char c : input.toCharArray()) {
    		if (Character.isUpperCase(c))
    			buf.append(' ');
    		buf.append(c);
    	}
    	return buf.toString().trim();
    }
    
    /**
     * returns true if the input contains one or more of the delimiters.
     * @param input
     * @param delimiters
     * @return
     */
    public static boolean contains(String input, String ... delimiters) {
    	if (input == null || delimiters == null)
    		return false;
    	
    	for (String s : delimiters) {
    		if (input.contains(s))
    			return true;
    	}
    	return false;
    }
    
    /**
     * returns true if the input is equal to one of the passed in strs.
     * @param input
     * @param delimiters
     * @return
     */
    public static boolean equals(String input, boolean ignoreCase, String ... str) {
    	if (input == null || str == null)
    		return false;
    	
    	for (String s : str) {
    		if (ignoreCase) {
    			if (input.equalsIgnoreCase(s)) {
    				return true;
    			}
    		} else {
    			if (input.equals(s))
        			return true;
    		}
    	}
    	return false;
    }
    
    
    public static String toPrettyString(Map map) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{");
    	for (Object key: map.keySet()) {
    		buf.append(key.toString());
    		buf.append(" : ");
    		
    		Object val = map.get(key);
    		if (val == null) {
    			buf.append("null");
    		} else if (val instanceof String[]) {
    			buf.append(toPrettyString((String[])val));
    		} else {
    			buf.append(val.toString());
    		}
    		buf.append(", ");
    	}
    	if (buf.length() > 1) {
    		buf.deleteCharAt(buf.length()-1);
    		buf.deleteCharAt(buf.length()-1);
    	}
    	buf.append("}");
    	return buf.toString();
    }
    
    public static String toPrettyString(String[] array) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("[");
    	for (String st: array) {
    		buf.append(st);
    		buf.append(",");
    	}
    	if (buf.length() > 1);
    		buf.deleteCharAt(buf.length()-1);
    	buf.append("]");
    	return buf.toString();
    }
    
    /**
     * trims the specified number of chars off the END of the string. If num > input.length then 
     * a zero length string is returned.
     * @param input
     * @param num
     * @return
     */
    public static String trimChars(String input, int num) {
    	if (input == null) 
    		return null;
    	if (num >= input.length())
    		return "";
    	return input.substring(0, input.length() - num);
    	
    }
    
    /**
     * will trim off the front and back of the string.
     * 
     * if trim = " " then it is the same as str.trim()
     * 
     * @param input
     * @param trim
     * @return
     */
    public static String trim(String input, String trim) {
    	if (input == null || trim == null || trim.isEmpty())
    		return input;
    	
    	String tmp = input;
    	while(tmp.startsWith(trim)) {
    		tmp = tmp.substring(trim.length());
    	}
    	while(tmp.endsWith(trim)) {
    		tmp = tmp.substring(0, tmp.length()-trim.length());
    	}
    	return tmp;
    	
    }
    
    /**
     * will return a string with max nmber of characters
     * @param input
     * @param max
     * @return
     */
    public static String maxChars(String input, int max) {
    	if (input == null) 
    		return null;
    	if (max >= input.length())
    		return input;
    	return input.substring(0, max);
    	
    }
    
    
    /**
     * removes all instances of the inputed strings. strings are matches not regexes.
     * @param input
     * @param strings
     * @return
     */
    public static String removeAll(String input, String ...toRemove ) {
    	String ret = input;
    	for (String str : toRemove) {
    		ret = ret.replace(str, "");
    	}
    	return ret;
    }
    
    /**
     * removes all the requested characters. 
     * Implemented to be optimally fast. 
     * 
     * @param input
     * @param toRemove
     * @return
     */
    public static String removeAll(String input, char ...toRemove ) {
    	if (input == null)
    		return null;
    	
    	Set<Character> toRem = new HashSet<Character>();
    	for (char c : toRemove) {
    		toRem.add(c);
    	}
    	StringBuilder build = new StringBuilder();
    	StringCharacterIterator iter = new StringCharacterIterator(input);
    	for(char c = iter.first(); c != StringCharacterIterator.DONE; c = iter.next()) {
    		if (toRem.contains(c))
    			continue;
    		build.append(c);
    	}
    	return build.toString();
    }
    
    /**
     * returns true if input equals one of the passed in strings.
     * @param input
     * @param val
     * @return
     */
    public static boolean matches(String input, String ...vals) {
    	if (input == null) 
    		return false;
    	for (String val : vals) {
    		if (input.equals(val))
    			return true;
    	}
    	return false;
    }
    
    /**
     * splits the string based on the given delimiters. 
     * 
     * 
     * @param input
     * @param delimiters
     * @return
     */
    public static List<String> split(String input, String ...delimiters) {
		if (input == null)
			return null;
		String str = input;
		List<String> list = new ArrayList<String>();
		
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
     * Just strips any non allowed characters.
     * 
     * currently allow letters, digits and 
     * 
     * $ - _ . + ! * ' ( ) , { } | \ ^ ~ [ ] ` " > < # % ; / ? & =
     * 
     * @param url
     * @return
     */
    public static String sanitizeUrl(String url) {
    	StringBuilder str = new StringBuilder();
    	CharacterIterator ci = new StringCharacterIterator(url);

		for (char ch = ci.first(); ch != CharacterIterator.DONE; ch = ci.next()) {
			if (Character.isLetterOrDigit(ch)) {
				str.append(ch);
				continue;
			}
			//we could do this with a regex, but would be unreadable, and I believe this
			//is faster.
			switch (ch) {
				case '$' :
					str.append(ch);
					continue;
				case '-' :
					str.append(ch);
					continue;
				case '_' :
					str.append(ch);
					continue;
				case '.' :
					str.append(ch);
					continue;
				case '+' :
					str.append(ch);
					continue;
				case '!' :
					str.append(ch);
					continue;
				case '*' :
					str.append(ch);
					continue;
				case '\'' :
					str.append(ch);
					continue;
				case '(' :
					str.append(ch);
					continue;
				case ')' :
					str.append(ch);
					continue;
				case ',' :
					str.append(ch);
					continue;
				case '{' :
					str.append(ch);
					continue;
				case '}' :
					str.append(ch);
					continue;
				case '|' :
					str.append(ch);
					continue;
				case '\\' :
					str.append(ch);
					continue;
				case '^' :
					str.append(ch);
					continue;
				case '~' :
					str.append(ch);
					continue;
					
				case '[' :
					str.append(ch);
					continue;
					
				case ']' :
					str.append(ch);
					continue;
				case '`' :
					str.append(ch);
					continue;
				case '\"' :
					str.append(ch);
					continue;
				case '<' :
					str.append(ch);
					continue;
				case '>' :
					str.append(ch);
					continue;
				case '#' :
					str.append(ch);
					continue;
				case '%' :
					str.append(ch);
					continue;
				case ';' :
					str.append(ch);
					continue;
				case '/' :
					str.append(ch);
					continue;
				case '?' :
					str.append(ch);
					continue;						
			}
		}
		return str.toString();
    }
    
    /**
     * returns a 20 byte sha1 hash 
     * @param bytes
     * @return
     */
    public static byte[] sha1(byte[] bytes) {
    	try {
	    	MessageDigest sha = MessageDigest.getInstance("SHA-1");
	    	byte[] result =  sha.digest( bytes );
	    	return result;
    	} catch (Exception x) {
    		
    	}
    	return null;
    }
    
    public static String sha1Hex(byte[] bytes) {
    	return toHex(sha1(bytes));
    }
    
    private static LazyInit lock = new LazyInit();
    private static SecureRandom prng = null;
    /**
     * Generates a cryptographically secure ID.
     * 
     * Should reasonably be unique, though that is not guarenteed.
     * 
     * result is 20 bytes long.
     * 
     * 
     */
    public static byte[] secureId() {
    	try {
	    	if (lock.start()) {
		    	try {
		    		//initialize the secure random only once
		    		//as it is a lengthy op.
		    		prng = SecureRandom.getInstance("SHA1PRNG");
		    	} finally {
		    		lock.end();
		    	}
	    	} 
	    	int numBytes = 64;
	    	
	    	//upper 8 bytes is the timestamp, to attempt uniqueness
	    	Long millis = new Date().getTime();
	    	
	    	byte[] randBytes = new byte[numBytes - 8];
	    	prng.nextBytes(randBytes);
	    	ByteBuffer buf = ByteBuffer.allocate(numBytes);
	    	buf.putLong(millis);
	    	buf.put(randBytes);
	    	 //get its digest
	    	return sha1(buf.array());
    	} catch (Exception x) {
    		x.printStackTrace();
    	}
    	return null;
    }
}
