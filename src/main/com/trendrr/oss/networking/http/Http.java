/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Regex;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrIOException;
import com.trendrr.oss.exceptions.TrendrrNetworkingException;
import com.trendrr.oss.networking.SocketChannelWrapper;


/**
 * Simple http class.
 * 
 * 
 * @author Dustin Norlander
 * @created Jun 13, 2012
 * 
 */
public class Http {

	protected static Log log = LogFactory.getLog(Http.class);
	
	public static void main(String ...strings) throws Exception {
		HttpRequest request = new HttpRequest();
		request.setUrl("http://google.com");
		request.setMethod("POST");
		request.setContent("application/json", "this is something something".getBytes());
		request(request);
//		System.out.println(new String(result));
		
	}
	
	
	public static HttpResponse request(HttpRequest request) throws TrendrrException {
		String host = request.getHost();
		int port = 80;
		if (host.contains(":")) {
			String tmp[] = host.split("\\:");
			host = tmp[0];
			port = TypeCast.cast(Integer.class, tmp[1]);
		}
		
		if (request.isSSL()) {
			//uhh, what the hell do we do here?
			
			
		} else {
			try {
//				Socket s = new Socket(host, port);
//				
//				OutputStream os = s.getOutputStream();
//				os.write(request.toByteArray());
//				os.flush();
//				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
//				String t;
//				
//				s.getInputStream().r
//				while((t = br.readLine()) != null) System.out.println(t);
//				br.close();
				
				
				
				
				SocketChannel channel = SocketChannel.open();
				channel.connect(new InetSocketAddress(host, port));
				SocketChannelWrapper wrapper = new SocketChannelWrapper(channel);
				System.out.println(new String(request.toByteArray(), "utf8"));
				wrapper.write(request.toByteArray());
				
				String headers = wrapper.readUntil("\r\n\r\n", Charset.forName("utf8"), true);
				String tmp = Regex.matchFirst(headers, "Content\\-Length\\:\\s+[0-9]+", true);
				int length = 0;
				if (tmp != null) {
					length = TypeCast.cast(Integer.class, tmp.replaceAll("[^0-9]", ""), 0);
				}
				byte[] contentbytes = wrapper.readBytes(length);
				wrapper.close();
				HttpResponse response = HttpResponse.parse(headers);
				response.setContent(contentbytes);
				return response;
//			} catch (TrendrrException x) {
//				throw x;
			} catch (IOException x) {
				throw new TrendrrIOException(x);
			}
		}
		throw new TrendrrException("Uhh, somethings wrong");
	}
	
	public static String get(String url, DynMap params) throws TrendrrNetworkingException {
		try {
			if (!url.contains("?")) {
				url += "?";
			}
			if (params != null) {
				url += params.toURLString();
			}
			
			URL u = new URL(url);
	        URLConnection c = u.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                c.getInputStream()));
	        String inputLine;
	        StringBuilder response = new StringBuilder();
	        while ((inputLine = in.readLine()) != null) {
	        	response.append(inputLine);
	        }
	        in.close();
			return response.toString();
		} catch (Exception x) {
			throw new TrendrrIOException(x);
		}
	}
	
	public static String post(String url, DynMap params) throws TrendrrNetworkingException {
		try {
						
			URL u = new URL(url);
	        URLConnection c = u.openConnection();
	        c.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
	        wr.write(params.toURLString());
	        wr.flush();
	        
	        
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                c.getInputStream()));
	        String inputLine;
	        StringBuilder response = new StringBuilder();
	        while ((inputLine = in.readLine()) != null) {
	        	response.append(inputLine);
	        }
	        in.close();
			return response.toString();
		} catch (Exception x) {
			throw new TrendrrIOException(x);
		}
		
	}
	
	
	
}
