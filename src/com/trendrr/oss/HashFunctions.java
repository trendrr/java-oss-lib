/**
 * 
 */
package com.trendrr.oss;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.LazyInit;


/**
 * @author Dustin Norlander
 * @created Mar 22, 2012
 * 
 */
public class HashFunctions {

	protected static Log log = LogFactory.getLog(HashFunctions.class);

	/**
	 *  The MurmurHash3 algorithm was created by Austin Appleby.  This java port was authored by
	 *  Yonik Seeley and is placed into the public domain.  The author hereby disclaims copyright
	 *  to this source code.
	 *  <p>
	 *  This produces exactly the same hash values as the final C++
	 *  version of MurmurHash3 and is thus suitable for producing the same hash values across
	 *  platforms.
	 *  <p>
	 *  The 32 bit x86 version of this hash should be the fastest variant for relatively short keys like ids.
	 *  <p>
	 *  Note - The x86 and x64 versions do _not_ produce the same results, as the
	 *  algorithms are optimized for their respective platforms.
	 *  <p>
	 *  See http://github.com/yonik/java_util for future updates to this file.
	 *  
	 *  
	 *  Returns the MurmurHash3_x86_32 hash.
	 *  
	 */
	public static int murmurhash3(byte[] data, int seed) {
		final int c1 = 0xcc9e2d51;
		final int c2 = 0x1b873593;

		int h1 = seed;
		int len = data.length;
		int roundedEnd = (len & 0xfffffffc);  // round down to 4 byte block

		for (int i=0; i<roundedEnd; i+=4) {
			// little endian load order
			int k1 = (data[i] & 0xff) | ((data[i+1] & 0xff) << 8) | ((data[i+2] & 0xff) << 16) | (data[i+3] << 24);
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
			k1 *= c2;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19);  // ROTL32(h1,13);
			h1 = h1*5+0xe6546b64;
		}

		// tail
		int k1 = 0;

		switch(len & 0x03) {
		case 3:
			k1 = (data[roundedEnd + 2] & 0xff) << 16;
			// fallthrough
		case 2:
			k1 |= (data[roundedEnd + 1] & 0xff) << 8;
			// fallthrough
		case 1:
			k1 |= (data[roundedEnd] & 0xff);
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
			k1 *= c2;
			h1 ^= k1;
		}

		// finalization
		h1 ^= len;

		// fmix(h1);
		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;
		return h1;
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
    	return StringHelper.toHex(sha1(bytes));
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
