/**
 * 
 */
package com.trendrr.oss;

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
	
	
	/**
	 * loads a file from the cache.  All exceptions are logged as warnings, and null is returned.
	 * @param filename filename to load
	 * @param timeoutPeriod number of milliseconds between reload of the file.
	 * @return
	 */
	public byte[] getFileBytes(String filename, long timeoutPeriod) {
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
