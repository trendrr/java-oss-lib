/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
import com.trendrr.oss.exceptions.TrendrrParseException;
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
		
//		String test = "11\n{ \"status\":\"OK\" }\n0";
//		byte[] a = readLine('\n',new ByteArrayInputStream(test.getBytes()));
//		System.out.println("a is: "+a.toString()+" of length "+ a.length);
		
		
		HttpRequest request = new HttpRequest();
		request.setUrl("https://www.google.com/#hl=en&output=search&sclient=psy-ab&q=test&oq=test&aq=f&aqi=g4");
		request.setMethod("GET");

//		request.setUrl("https://tools.questionmarket.com/verveindex/trendrr_ping.pl");
//		request.setMethod("POST");
//		request.setContent("application/json", "this is a test".getBytes());

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
		
		Socket socket = null;
		try {
			if (request.isSSL()) {
				System.out.println("SSL!");
				//uhh, what the hell do we do here?
			   if (port == 80) {
				   port = 443;
			   }
	
			    SocketFactory socketFactory = SSLSocketFactory.getDefault();
			    socket = socketFactory.createSocket(host, port);
			    return doRequest(request, socket);
			} else {
				socket = new Socket(host, port);
				return doRequest(request, socket);
			}
		} catch (IOException x) {
			throw new TrendrrIOException(x);
		} catch (Exception x) {
			throw new TrendrrException(x);
		} finally {
			try {
				if (socket != null) 
				{
					socket.close();
				}
			} catch (Exception x) {}
		}
	}
	
	
	private static HttpResponse doRequest(HttpRequest request, Socket socket) throws IOException, TrendrrParseException {
		 //TODO: socket timeouts
	    
	    
	    // Create streams to securely send and receive data to the server
	    InputStream in = socket.getInputStream();
	    OutputStream out = socket.getOutputStream();
	    out.write(request.toByteArray());
	    
	    // Read from in and write to out...
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String t;
		StringBuilder contentBuilder = new StringBuilder();
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		
		//file
		String filepath = "/home/markg/Documents/mark/httptestdoc";
		BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
		
		
		
		
//		s.getInputStream().r
		StringBuilder headerBuilder = new StringBuilder();
		byte[] dum1;
		while(!(t = readLine(in)).isEmpty()) {
			System.out.println("t is--"+t+"--end");
			System.out.println(t.length());
			headerBuilder.append(t).append("\r\n");
		}
		String headers = headerBuilder.toString();
		System.out.println("headers: \n"+headers);
		
		HttpResponse response = HttpResponse.parse(headers);
		
		byte[] content = null;
		
		
		if (response.getHeader("Content-Length") != null) {
			content = new byte[getContentLength(response)];
			in.read(content);
			outstream.write(content, 0, content.length);
//			contentBuilder.append(content);
		} else {
			String chunked = response.getHeader("Transfer-Encoding");
			if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
				//TODO: handle chunked encoding!
		
				
				int length = 1;
				String lengthstr = "";
				int ctr = 0;
				
				while(!(lengthstr = readLine(in)).equals("0")){ 
					System.out.println("line:"+lengthstr);
					if(lengthstr.isEmpty()){
						System.out.println("lengthstr is empty, skipping");
//						System.out.println("content: "+new String(content));
						continue;
					}else {
						ctr++;
						length = Integer.parseInt(lengthstr,16);
						System.out.println("length: "+length);
						
						content = new byte[length];
						int numread;
						int total=0;
						
						while(total < length && 
							 (numread = in.read(content,0,length)) != -1){
							System.out.println("written: "+numread+" ctr="+ctr);
//							System.out.println("content: "+new String(content));
							outstream.write(content, 0, numread);
							total+=numread;
						}
//						bw.write(content);
//						contentBuilder.append(content);
					}
					
					
				}
				
			}
		}
		outstream.close();
		br.close();
	    in.close();
	    out.close();
	    bw.close();
//	    System.out.println(contentBuilder.toString());
	    response.setContent(outstream.toByteArray());
//	    response.setContent(contentBuilder.toString().getBytes());
		return response;
	}
	
	private static String readLine(InputStream in) throws IOException{
		byte current = 'a';
		byte[] temp = new byte[1000];//is this large enough to handle any header content?
		
		int offset=-1;
		while((char)current != '\n' && (char)current != '\r'){
			offset++;
			in.read(temp, offset, 1);
//			System.out.println("result at: "+offset+"="+(char)temp[offset]);
			current = temp[offset];
		}
		
		byte[] result = new byte[offset-1];
		for(int i=0; i<result.length; i++){
			result[i]=temp[i];
		}
		
		return new String(result);
	}
	
	private static int getContentLength(HttpResponse response) {
		int length = TypeCast.cast(Integer.class, response.getHeader("Content-Length"), 0);
		return length;
	}
	
	
	public static String get(String url) throws TrendrrNetworkingException {
		return get(url, null);
	}
	/**
	 * Shortcut to do a simple GET request.  returns the content on 200, else throws an exception
	 * @param url
	 * @param params
	 * @return
	 * @throws TrendrrNetworkingException
	 */
	public static String get(String url, DynMap params) throws TrendrrNetworkingException {
		try {
			if (!url.contains("?")) {
				url += "?";
			}
			if (params != null) {
				url += params.toURLString();
			}
			HttpRequest request = new HttpRequest();
			request.setUrl(url);
			request.setMethod("GET");
			HttpResponse response = request(request);
			
			if (response.getStatusCode() == 200) {
				return new String(response.getContent(), "utf8");
			}
			//TODO: some kind of http exception.
			throw new TrendrrNetworkingException("Error from response") {
			};
		} catch (TrendrrNetworkingException e) { 
			throw e;
		}catch (Exception x) {
			throw new TrendrrIOException(x);
		}
	}
	
	public static String post(String url, DynMap params) throws TrendrrNetworkingException {
		try {
			//TODO: update for using request.	
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
