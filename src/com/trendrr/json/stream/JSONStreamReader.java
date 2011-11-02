/**
 * 
 */
package com.trendrr.json.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrIOException;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * Provides a way to read streams of json dictionaries.
 * 
 * will start a dictionary on the first '{' and finish the 
 * dict on the corresponding
 * '}'
 * 
 * It will ignore any characters between dicts, so any dilimiter can be used.
 * each dict must be valid, an exception will be thrown on the first parse error.
 * 
 * 
 * @author Dustin Norlander
 * @created Nov 2, 2011
 * 
 */
public class JSONStreamReader {

	protected Log log = LogFactory.getLog(JSONStreamReader.class);
	
	Reader reader;
	
	long maxBufferedChars = 2048; //max size to write before we assume this is an invalid stream.
	
	public JSONStreamReader(InputStream stream) {
		this(new InputStreamReader(stream));
	}
	
	public JSONStreamReader(Reader reader) {
		this.reader =new BufferedReader(reader);
	}
	
	public static void main(String ...strings) throws TrendrrException {
		String json = "blah blah{ \"key\" : \"value\"}{ \"key1\" : \"valu\\\"e1}\"}";
		JSONStreamReader reader = new JSONStreamReader(new StringReader(json));
		DynMap mp;
		while((mp = reader.readNext()) != null) {
			System.out.println(mp.toJSONString());
		}
	}
	
	
	
	/**
	 * returns null on eof. throws exception or returns parsed object.
	 * @return
	 * @throws IOException
	 */
	public DynMap readNext() throws TrendrrException {
		StringBuilder json = new StringBuilder("{");
		try {
			long numRead = 0;
			int openBrackets = 1;
			boolean isQuote = false;
			char previous = '{';
			
			//read until the first open bracket
			int current = this.reader.read();
			while(current != '{' && current != -1) {
				numRead++;
				if (numRead > this.maxBufferedChars) {
					throw new TrendrrParseException("Read " + this.maxBufferedChars + " chars without a valid json dict");
				}
				current = this.reader.read();
			}
			
			do {
				current = this.reader.read();
				if (current == -1) {
					return null;
				}
				char c = (char)current;
				json.append(c);
				if (!isQuote) {
					if (c == '{') {
						openBrackets++;
					} else if (c == '}') {
						openBrackets--;
					}
				}
				if (c == '"' && previous != '\\') {
					isQuote = !isQuote;
				}
				
				previous = c;
				numRead++;
				if (numRead > this.maxBufferedChars) {
					throw new TrendrrParseException("Read " + this.maxBufferedChars + " chars without a valid json dict");
				}
			} while(openBrackets != 0);
			
			DynMap dm = DynMapFactory.instanceFromJSON(json.toString());
			if (dm == null) {
				throw new TrendrrParseException("unable to parse json string");
			}
			return dm;
		} catch (IOException x) {
			throw new TrendrrIOException(x);
		}
	}
	
	
}
