package com.trendrr.oss.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import junit.framework.Assert;

import org.junit.Test;

import com.trendrr.oss.TypeCast;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrIOException;
import com.trendrr.oss.networking.SocketChannelWrapper;
import com.trendrr.oss.networking.http.Http;
import com.trendrr.oss.networking.http.HttpRequest;
import com.trendrr.oss.networking.http.HttpResponse;

public class HttpTests {

	public static final String GET_url = "https://www.youtube.com/watch?v=jgMutAOEQ5I&feature=g-vrec";

	@Test
	public void GETTest() throws TrendrrException, UnsupportedEncodingException{
		HttpRequest request = new HttpRequest();
		request.setUrl(GET_url);
		request.setMethod("GET");
		HttpResponse response = Http.request(request);
		String html = new String(response.getContent(), "utf8").trim();
		//check that we read the whole html content
		Assert.assertTrue(html.endsWith("</html>"));
	}
	
	@Test
	public void testRequest() throws TrendrrException, IOException{
		HttpRequest request = new HttpRequest();
		request.setUrl(GET_url);
		request.setMethod("GET");

//		request.setUrl("https://tools.questionmarket.com/verveindex/trendrr_ping.pl");
//		request.setMethod("POST");
//		request.setContent("application/json", "this is a test".getBytes());

		HttpResponse response = request(request);
		String result = new String(response.getContent());
		Assert.assertEquals("{ \"status\":\"ok\" }", result);
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
			    
			    
			   //HEY MAKE SURE TO CLOSE THE STREAMS YOU JUST COMMENTED OUT!!!
			    
			    // Create streams to securely send and receive data to the server
			    InputStream in = socket.getInputStream();
			    OutputStream out = socket.getOutputStream();
			    out.write(request.toByteArray());
			    
			    // Read from in and write to out...
			    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				String t;
				
				 //test inputstream, read from file
				
				
				
			    String filepath = "/home/markg/Documents/mark/httptestdoc";
				in = new FileInputStream(filepath);
			    br = new BufferedReader(new FileReader(filepath));
			    
//			    String inst = "11\n{ \"status\":\"ok\" }\n0\n";
//			    in = new ByteArrayInputStream(inst.getBytes());
//			    br = new BufferedReader(new StringReader(inst));
				
//				s.getInputStream().r
				StringBuilder headerBuilder = new StringBuilder();
				while(!(t = br.readLine()).isEmpty()) {
					System.out.println(t);
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
//					content = new byte[100];
					String chunked = response.getHeader("Transfer-Encoding");
					if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
						//TODO: handle chunked encoding!
				
						String b;
						int length = 1;
						String chunk = "";
						int offset = 0;
						int ctr = 0;
						
						content = null;
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
								System.out.println("wrote: "+new String(content));
								outstream.write(content, 0, numread);
								total+=numread;
							}
							br.readLine();//clear trailing line break
						}
					}
				}
				
				br.close();
			    outstream.flush();
			    // Close the socket
			    in.close();
			    out.close();
			    
//			    response.setContent(outstream.toByteArray());
			    
			    
//			    OutputStream o = new FileOutputStream(filepath);
//			    outstream.writeTo(o);
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
}
