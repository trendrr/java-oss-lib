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
		System.out.println(html);
		//check that we read the whole html content
		Assert.assertTrue(html.endsWith("</html>"));
	}
	
//	@Test
	public void testRequest() throws TrendrrException, IOException{
		HttpRequest request = new HttpRequest();
		request.setUrl(GET_url);
		request.setMethod("GET");

//		request.setUrl("https://tools.questionmarket.com/verveindex/trendrr_ping.pl");
//		request.setMethod("POST");
//		request.setContent("application/json", "this is a test".getBytes());

		HttpResponse response = Http.request(request);
		String result = new String(response.getContent());
		Assert.assertEquals("{ \"status\":\"ok\" }", result);
	}

}
