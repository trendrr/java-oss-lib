/**
 * 
 */
package com.trendrr.oss;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.concurrent.TrendrrLock;


/**
 * A simple singleton that gets your current IP address.
 * 
 * Usage:
 * 
 * WhatsMyIp.getIP()
 * 
 * on first invocation will reach out to a webservice to get your public ip (currently uses jsonip.com)
 * 
 * @author Dustin Norlander
 * @created Sep 21, 2011
 * 
 */
public class WhatsMyIp {

	protected static Log log = LogFactory.getLog(WhatsMyIp.class);
	
	protected LazyInit lock = new LazyInit();
	protected int timeout = 20*1000;
	protected String ip = null;
	
	public static void main(String ...strings) throws IOException {

		System.out.println(WhatsMyIp.getIP());
		
		System.out.println(WhatsMyIp.getIP());
		System.out.println(WhatsMyIp.getIP());
		System.out.println(WhatsMyIp.getIP());
		System.out.println(WhatsMyIp.getIP());
	}
	
	private static WhatsMyIp instance = new WhatsMyIp();
	public static WhatsMyIp instance() {
		return instance;
	}
	
	public static String getIP() {
		return instance.getIPAddress();
	}
	
	protected WhatsMyIp() {
		
	}
	
	/**
	 * gets your current IP address, currently uses jsonip.com
	 * 
	 * this will do one request and cache the result.
	 * 
	 * @return your ip address as a string, or null
	 */
	public String getIPAddress() {
		if (lock.start()) {
			try {
				DynMap mp = DynMap.instance(this.download("http://jsonip.com"));
				this.ip = mp.getString("ip");
			} catch (Exception x) {
				log.warn("Caught", x);
			} finally {
				lock.end();
			}
		}
		return this.ip;
	}
	
	
	protected String download(String url) throws IOException {
		  HttpURLConnection sourceConnection = null;
		  BufferedInputStream inputStream = null;
		    URL sourceURL = new URL(url);
		    try {
		    	
		      sourceConnection = (HttpURLConnection)sourceURL.openConnection();
		      sourceConnection.setConnectTimeout(timeout);
		      sourceConnection.setReadTimeout(timeout);
		    }
		    catch (MalformedURLException exc) {
		      throw new RuntimeException("Configured URL caused a MalformedURLException: ", exc);
		    }
		    sourceConnection.connect();
		    inputStream = new BufferedInputStream(sourceConnection.getInputStream());
		    InputStreamReader r = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(r);
			StringBuilder builder = new StringBuilder();
			String str = "";
			while (str != null) {
				str = reader.readLine();
				if (str != null)
					builder.append(str);
				
			}
		    
		    return builder.toString();
	}
	
}
