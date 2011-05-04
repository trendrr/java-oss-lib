/**
 * 
 */
package com.trendrr.oss;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.ReinitObject;


/**
 * 
 * A threadsafe file cache.
 * 
 * 
 * @author Dustin Norlander
 * @created Apr 7, 2011
 * 
 */
public class FileCache {

	protected Log log = LogFactory.getLog(FileCache.class);
	
	private static ConcurrentHashMap<String, ReinitObject<byte[]>> cache = new ConcurrentHashMap<String, ReinitObject<byte[]>> ();

	//absolute max number of items to cache.
	private int maxItems = 200;

	/**
	 * max number of items to cache. 
	 * 
	 * this is a hard limit.  
	 * @return
	 */
	public int getMaxItems() {
		return maxItems;
	}


	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}


	/**
	 * gets the file as a utf8 string or null;
	 * @param filename
	 * @param timeoutPeriod
	 * @return
	 */
	public String getFileString(String filename, long timeoutPeriod) {
		byte[] bytes = this.getFileBytes(filename, timeoutPeriod);
		if (bytes == null)
			return null;
		try {
			return new String(bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	
	/**
	 * loads a file from the cache.  All exceptions are logged as warnings, and null is returned.
	 * @param filename filename to load
	 * @param timeoutPeriod number of milliseconds between reload of the file.
	 * @return
	 */
	public byte[] getFileBytes(String filename, long timeoutPeriod) {
		if (cache.size() > this.maxItems) {
			//TODO: this could cause a threadsafety problem below
			//this should be smarter, but for now this is only a 
			//catch for a potential memory leak
			cache.clear();
		}
		
		final String f = filename;
		
		//fancy timeout cache. 
		cache.putIfAbsent(filename, new ReinitObject<byte[]>(timeoutPeriod) {
			@Override
			public byte[] init() {
				try {
					System.out.println("LOADING FROM FILE SYSTEM!");
					return FileHelper.loadBytes(f);
				} catch (Exception e) {
					log.warn("Caught", e);
				}
				return null;
			}
		});
		return cache.get(filename).get();
	}
}
