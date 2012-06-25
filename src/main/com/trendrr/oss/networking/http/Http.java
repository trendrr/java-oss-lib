/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Regex;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrIOException;
import com.trendrr.oss.exceptions.TrendrrNetworkingException;
import com.trendrr.oss.networking.SocketChannelWrapper;


/**
 * Simple http class.
 * 
 * This makes it much easier to deal with headers, and funky requests.  Apache httpclient is unusable...
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
//		request.setUrl("https://www.google.com/#hl=en&output=search&sclient=psy-ab&q=test&oq=test&aq=f&aqi=g4");
//		request.setMethod("GET");

		request.setUrl("https://tools.questionmarket.com/verveindex/trendrr_ping.pl");
		request.setMethod("POST");
		request.setContent("application/json", "this is a test".getBytes());

		HttpResponse response = request(request);
		System.out.println(new String(response.getContent()));
		
	}
	
	
	public static HttpResponse request(HttpRequest request) throws TrendrrException {
		String host = request.getHost();
		int port = 80;
		if (host.contains(":")) {
			String tmp[] = host.split("\\:");
			host = tmp[0];
			port = TypeCast.cast(Integer.class, tmp[1]);
		}
		
		try {
			if (request.isSSL()) {
				System.out.println("SSL!");
				//uhh, what the hell do we do here?
			   if (port == 80) {
				   port = 443;
			   }
	
			    SocketFactory socketFactory = SSLSocketFactory.getDefault();
			    Socket socket = socketFactory.createSocket(host, port);
			    //TODO: socket timeouts
			    
			    
			    // Create streams to securely send and receive data to the server
			    InputStream in = socket.getInputStream();
			    OutputStream out = socket.getOutputStream();
			    out.write(request.toByteArray());
			    
			    // Read from in and write to out...
			    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				String t;
				
//				s.getInputStream().r
				StringBuilder headerBuilder = new StringBuilder();
				while(!(t = br.readLine()).isEmpty()) {
					headerBuilder.append(t).append("\r\n");
				}
				String headers = headerBuilder.toString();
				System.out.println(headers);
				
				HttpResponse response = HttpResponse.parse(headers);
				
				byte[] content = null;
				
				if (response.getHeader("Content-Length") != null) {
					content = new byte[getContentLength(response)];
					in.read(content);
				} else {
					content = new byte[100];
					String chunked = response.getHeader("Transfer-Encoding");
					if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
						//TODO: handle chunked encoding!
				
						String b;
//						StringBuilder contentBuffer = new StringBuilder(); 
						int length = 1;
						String chunk = "";
						int offset = 0;
						int ctr = 0;
						while(!chunk.equals("0")){
							ctr++;
							chunk = br.readLine();
							System.out.println("line: "+chunk);
							length = Integer.parseInt(chunk,16);
							System.out.println("length: "+length+", offset "+offset);
							
							content = new byte[length];
							int numread;
							int total=0;
							while(total < length && 
								 (numread = in.read(content, 0, content.length-total)) != -1){
								System.out.println("written: "+numread+" ctr="+ctr);
								outstream.write(content, 0, numread);
								total+=numread;
							}
							br.readLine();//clear trailing line break
							
//							System.out.println("written: "+in.read(content));
//							outstream.write(content);
////							System.out.println(br.read(charbuf, offset, length));
//							offset = length;
////							System.out.println(content.length);
//							br.readLine();//call to just skip through the content line
						}
						
//						String status = Regex.matchFirst(contentBuffer.toString(), "\\{.+\\}", true);
//						content = status.getBytes();
					}
				}
				
				br.close();
			    outstream.flush();
			    // Close the socket
			    in.close();
			    out.close();
			    response.setContent(outstream.toByteArray());
				return response;
			
			} else {
	
				
				
				SocketChannel channel = SocketChannel.open();
				
				channel.connect(new InetSocketAddress(host, port));
				
				SocketChannelWrapper wrapper = new SocketChannelWrapper(channel);

				System.out.println(new String(request.toByteArray(), "utf8"));
				wrapper.write(request.toByteArray());
				
				String headers = wrapper.readUntil("\r\n\r\n", Charset.forName("utf8"), true);
				
				HttpResponse response = HttpResponse.parse(headers);
				
				byte[] content = null;
				
				if (response.getHeader("Content-Length") != null) {
					content = wrapper.readBytes(getContentLength(response));
				} else {
					String chunked = response.getHeader("Transfer-Encoding");
					if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
						//TODO: handle chunked encoding!
//						int length = 1;
//						StringBuilder contentBuffer = new StringBuilder(); 
//						while(length>0){
//							String w = wrapper.readUntil("\n", Charset.forName("utf8"), true);
//							length = w.length();
//							contentBuffer.append(w);
//						}
//						String status = Regex.matchFirst(contentBuffer.toString(), "\\{.+\\}", true);
//						content = status.getBytes();
					}
				}
				
				response.setContent(content);
				return response;
				
			}
		} catch (IOException x) {
			throw new TrendrrIOException(x);
		} catch (Exception x) {
			throw new TrendrrException(x);
		}
	}
	
	private static int getContentLength(HttpResponse response) {
		int length = TypeCast.cast(Integer.class, response.getHeader("Content-Length"), 0);
		return length;
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
