/**
 * 
 */
package com.trendrr.json.stream;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class JSONStreamParser {

	protected static Log log = LogFactory.getLog(JSONStreamParser.class);
	
	protected StringBuilder builder = null;
	protected int openBrackets = 0;
	protected boolean isQuote = false;
	protected char previous = ' ';
	protected long numRead = 0;
	protected long maxBufferedChars; //max size to write before we assume this is an invalid stream.
	
	/**
	 * creates a new stream parser.  max buffer is the largest allowable buffer for a single json packet.
	 * @param maxBuffer
	 */
	public JSONStreamParser(long maxBuffer) {
		this.maxBufferedChars = maxBuffer;
	}
	
	/**
	 * creates a new stream parser with a 4mb max buffer
	 */
	public JSONStreamParser() {
		this(FileHelper.megsToBytes(4));
	}
	
	public synchronized void reset() {
		builder = null;
		openBrackets = 0;
		isQuote = false;
		previous = ' ';
		numRead = 0;
	}
	
	/**
	 * parses the passed in string.  Will return an empty list if no complete json packet is available.  else will 
	 * return all the complete packets possible.
	 * @param string
	 * @return
	 * @throws TrendrrParseException
	 */
	public synchronized List<DynMap> addString(String string) throws TrendrrParseException {
		List<DynMap> retList = new ArrayList<DynMap>();
		StringCharacterIterator iter = new StringCharacterIterator(string);
		for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			DynMap res = this.addChar(c);
			if (res != null)
				retList.add(res);
		}
		return retList;
	}
	
	/**
	 * Add a char to the stream.  will return the parsed DynMap when ready, otherwise null. 
	 * throws parse exception if this is not a parseable json stream.
	 * @param nextChar
	 * @return
	 */
	public synchronized DynMap addChar(char nextChar) throws TrendrrParseException {
		if (builder == null) {
			if (nextChar == '{') {
				builder = new StringBuilder("{");
				previous = '{';
				openBrackets = 1;
			} else if (!Character.isWhitespace(nextChar)) {
				throw new TrendrrParseException("unable to parse json string, didn't start with {");
			}
			return null;
		}
		
		builder.append(nextChar);
		if (!isQuote) {
			if (nextChar == '{') {
				openBrackets++;
			} else if (nextChar == '}') {
				openBrackets--;
			}
		}
		
		if (nextChar == '"' && previous != '\\') {
			isQuote = !isQuote;
		}
		previous = nextChar;
		numRead++;
		if (numRead > this.maxBufferedChars) {
			throw new TrendrrParseException("Read " + this.maxBufferedChars + " chars without a valid json dict");
		}
		if (openBrackets == 0) {
			DynMap dm = DynMapFactory.instanceFromJSON(builder.toString());
			if (dm == null) {
				throw new TrendrrParseException("unable to parse json string");
			}
			this.reset();
			return dm;
		}
		return null;
	}
}
