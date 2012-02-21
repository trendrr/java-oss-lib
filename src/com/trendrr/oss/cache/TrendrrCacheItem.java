/**
 * 
 */
package com.trendrr.oss.cache;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.exceptions.TrendrrParseException;


/**
 * 
 * Simple class that allows us to store aribitrary dynmap as metadata to the a byte array.
 * 
 * @author Dustin Norlander
 * @created Jan 9, 2012
 * 
 */
public class TrendrrCacheItem {

	protected Log log = LogFactory.getLog(TrendrrCacheItem.class);
	
	public static TrendrrCacheItem instance(DynMap metadata, byte[] bytes) {
		TrendrrCacheItem item = new TrendrrCacheItem();
		item.setContentBytes(bytes);
		item.setMetadata(metadata);
		return item;
	}
	
	private byte[] bytes = new byte[0];
	
	private DynMap metadata = new DynMap();
	
	public byte[] getContentBytes() {
		return bytes;
	}

	public void setContentBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public DynMap getMetadata() {
		return metadata;
	}

	public void setMetadata(DynMap metadata) {
		this.metadata = metadata;
	}

	
	
	
	public byte[] serialize() {
		try {
			int length = 0;
			byte[] meta = new byte[0];
			if (metadata != null && metadata.size() > 0) {
				meta = metadata.toJSONString().getBytes("utf8");
				length = meta.length;
			}
			byte[] len = intToByteArray(length);
			
			return this.concatAll(len, meta, this.bytes);
		} catch (UnsupportedEncodingException e) {
			log.warn("Gah, how'd you get an encodeing exception!?", e);
		}
		return null;
	}
	
	/**
	 * will deserialize the byte array
	 * @param vals
	 * @throws UnsupportedEncodingException 
	 */
	public static TrendrrCacheItem deserialize(byte[] bytes) throws TrendrrParseException {
		int length = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
		byte[] metabytes = Arrays.copyOfRange(bytes, 4, 4+length);
		DynMap metadata;
		try {
			metadata = DynMap.instance(new String(metabytes, "utf8"));
		} catch (UnsupportedEncodingException e) {
			throw new TrendrrParseException(e);
		}
		byte[] b = Arrays.copyOfRange(bytes, 4+length, bytes.length);
		
		TrendrrCacheItem item = new TrendrrCacheItem();
		item.setContentBytes(b);
		item.setMetadata(metadata);
		return item;
	}
	
	protected byte[] concatAll(byte[] first, byte[]... rest) {
		  int totalLength = first.length;
		  for (byte[] array : rest) {
		    totalLength += array.length;
		  }
		  byte[] result = Arrays.copyOf(first, totalLength);
		  int offset = first.length;
		  for (byte[] array : rest) {
		    System.arraycopy(array, 0, result, offset, array.length);
		    offset += array.length;
		  }
		  return result;
	}
	
	private static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	
	private static int byteArrayToInt(byte [] b) {
	        return (b[0] << 24)
	                + ((b[1] & 0xFF) << 16)
	                + ((b[2] & 0xFF) << 8)
	                + (b[3] & 0xFF);
	}
}
