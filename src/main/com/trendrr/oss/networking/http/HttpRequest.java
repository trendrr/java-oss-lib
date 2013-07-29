/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestPacketBase;
import com.trendrr.oss.strest.models.StrestHeader.Method;
import com.trendrr.oss.strest.models.StrestHeader.Name;


/**
 * @author Dustin Norlander
 * @created Jun 20, 2012
 * 
 */
public class HttpRequest implements StrestPacketBase {

	protected static Log log = LogFactory.getLog(HttpRequest.class);
	
	protected Map<String,String> headers = new HashMap<String,String>();

	protected String protocol = "HTTP";
	protected float protocolVersion = 1.1f;
	protected Method method = Method.GET;
	protected byte[] content;
	protected String path = "/";
	protected String host;
	protected boolean isSSL = false;
	
	public boolean isSSL() {
		return isSSL;
	}
	public void setSSL(boolean isSSL) {
		this.isSSL = isSSL;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void setMethod(String method) {
		this.method = Method.instance(method);
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public static void main(String ...strings) throws UnsupportedEncodingException {
		HttpRequest test = new HttpRequest();
		test.setUrl("http://strest.trendrr.com?test=test2");
		test.addHeader("Etag", "3f80f-1b6-3e1cb03b");
		test.setContent("text", "THIS IS SOME CONTENT".getBytes());
		System.out.println(new String(test.toByteArray(), "utf8"));
		
		
		
	}
	/**
	 * sets the uri and host
	 * @param url
	 */
	public void setUrl(String url) {
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		URI uri = URI.create(url);
		if (uri.getPort() <= 0) {
			this.host = uri.getHost();
		} else {
			this.host = uri.getHost() + ":" + uri.getPort();
		}
		this.path = uri.getPath();
		if (this.path.isEmpty()) {
			this.path = "/";
		}
		
		if (uri.getQuery() != null) {
			this.path += "?" + uri.getQuery();
		}
		if (uri.getScheme().equals("https")) {
			this.isSSL = true;
		}
	}
	
	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void addHeader(Name header, String value) {
		this.addHeader(header.getHttpName(), value);
	}

	public String getHeader(Name header) {
		return this.getHeader(header.getHttpName());
	}

	public String getHeader(String header) {
		return this.headers.get(header);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#setProtocol(java.lang.String, float)
	 */
	@Override
	public void setProtocol(String protocolName, float version) {
		this.protocol = protocolName;
		this.protocolVersion = version;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#getProtocolVersion()
	 */
	@Override
	public float getProtocolVersion() {
		return this.protocolVersion;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#getProtocolName()
	 */
	@Override
	public String getProtocolName() {
		return this.protocol;
	}

	/**
	 * For strest only.  you can ignore this...
	 */
	@Override
	public void setTxnId(String id) {
		this.addHeader(Name.TXN_ID, id);
	}

	/**
	 * for strest only. you can ignore this..
	 */
	@Override
	public String getTxnId() {
		return this.getHeader(Name.TXN_ID);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#setContent(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setContent(DynMap content) {
		try {
			this.setContent("application/json", content.toJSONString().getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#setContent(java.lang.String, byte[])
	 */
	@Override
	public void setContent(String contentType, byte[] bytes) {
		this.addHeader("Content-Type", contentType);
		this.content = bytes;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#getContent()
	 */
	@Override
	public byte[] getContent() {
		return this.content;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		this.content = null;
		this.headers = null;
	}

	public String getHeaderAsString() {
		StringBuilder str = new StringBuilder();
		//Status Line
		str.append(this.method.getHttp())
			.append(" ")
			.append(this.path)
			.append(" ")
			.append(this.protocol)
			.append("/")
			.append(this.protocolVersion)
			.append(StrestHeader.HTTP_LINE_ENDING);
		
		str.append("Host: ")
			.append(this.host)
			.append(StrestHeader.HTTP_LINE_ENDING);

		
		for (String h : this.headers.keySet()) {
			str.append(h)
				.append(": ")
				.append(this.getHeader(h))
				.append(StrestHeader.HTTP_LINE_ENDING);
		}
		
		if (this.content != null) {
			//content length
			str.append("Content-Length: ")
			.append(this.content.length)
			.append(StrestHeader.HTTP_LINE_ENDING);
		}
		str.append(StrestHeader.HTTP_LINE_ENDING);
		return str.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		byte[] headers;
		try {
			headers = this.getHeaderAsString().getBytes("utf8");
			if (this.content == null || this.content.length == 0) 
				return headers;
			
			ByteBuffer buf = ByteBuffer.allocate(headers.length + this.content.length);
			buf.put(headers);
			buf.put(this.content);
			return buf.array();
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
	
//	public void addHeader(String
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#toMap()
	 */
	@Override
	public Map<String, Object> toMap() {
		log.warn("toMap not implemented: " + this);
		return null;
	}
}
