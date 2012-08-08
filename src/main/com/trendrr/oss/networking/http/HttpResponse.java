/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Regex;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.exceptions.TrendrrParseException;
import com.trendrr.oss.networking.strest.models.StrestPacketBase;
import com.trendrr.oss.networking.strest.models.StrestHeader.Name;


/**
 * @author Dustin Norlander
 * @created Jun 20, 2012
 * 
 */
public class HttpResponse implements StrestPacketBase {

	protected static Log log = LogFactory.getLog(HttpResponse.class);

	protected String protocol = "HTTP";
	protected float protocolVersion = 1.1f;

	
	protected Map<String,String> headers = new HashMap<String,String>();

	protected byte[] content;
	
	public static HttpResponse parse(String headers) throws TrendrrParseException{
		try {
			String [] lines = headers.split("\n");
			
			HttpResponse response = new HttpResponse();
			int code = 0;
			for (String ln : lines) {
				if (code == 0) {
					//status line
					code = TypeCast.cast(Integer.class, Regex.matchFirst(ln, "[0-9]{3}", false));
					response.setStatusCode(code);
					response.setStatusMessage(ln.split("[0-9]{3}")[1].trim());
					System.out.println("CODE: " + code);
					System.out.println("STATUS: " + response.getStatusMessage());
					//TODO: parse the protocol (WHO really cares though?)
					continue;
				}
				
				int ind = ln.indexOf(':');
				String header = ln.substring(0, ind);
				String val = ln.substring(ind+1, ln.length()).trim();
				response.addHeader(header, val);
			}
			return response;
		} catch (Exception x) {
			throw new TrendrrParseException("Bad http headers\n***********\n" +headers + "\n**********", x);
		}
	}
	
	int statusCode = 200;
	String statusMessage = "OK";
	
	
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#addHeader(com.trendrr.oss.networking.strest.v2.models.StrestHeader.Name, java.lang.String)
	 */
	@Override
	public void addHeader(Name header, String value) {
		this.addHeader(header.getHttpName(), value);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#getHeader(com.trendrr.oss.networking.strest.v2.models.StrestHeader.Name)
	 */
	@Override
	public String getHeader(Name header) {
		return this.getHeader(header.getHttpName());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#getHeader(java.lang.String)
	 */
	@Override
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
		return content;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.v2.models.StrestPacketBase#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
