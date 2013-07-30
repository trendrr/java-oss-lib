/**
 * 
 */
package com.trendrr.oss.networking.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Regex;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.exceptions.TrendrrParseException;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;
import com.trendrr.oss.strest.models.StrestHeader.TxnStatus;
import com.trendrr.oss.strest.models.StrestPacketBase;
import com.trendrr.oss.strest.models.StrestResponse;
import com.trendrr.oss.strest.models.StrestHeader.Name;
import com.trendrr.oss.strest.models.json.StrestJsonResponse;


/**
 * @author Dustin Norlander
 * @created Jun 20, 2012
 * @deprecate dont use, this shit never really worked
 */
@Deprecated
public class HttpResponse implements StrestResponse {

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
	
	

	public void setContent(DynMap content) {
		try {
			this.setContent("application/json", content.toJSONString().getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		
	}

	public void setContent(String contentType, byte[] bytes) {
		this.addHeader("Content-Type", contentType);
		this.content = bytes;
	}

	public byte[] getContentBytes() {
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

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setContent(java.lang.String, long, java.io.InputStream)
	 */
	@Override
	public void setContent(ContentEncoding contentEncoding, int contentLength,
			InputStream stream) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContent()
	 */
	@Override
	public InputStream getContent() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentEncoding()
	 */
	@Override
	public ContentEncoding getContentEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentLength()
	 */
	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.DynMapConvertable#toDynMap()
	 */
	@Override
	public DynMap toDynMap() {
		try {
			StrestJsonResponse response = new StrestJsonResponse(this);
			return response.toDynMap();
		} catch (Exception x) {
			log.error("caught", x);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#setTxnStatus(com.trendrr.oss.strest.models.StrestHeader.TxnStatus)
	 */
	@Override
	public void setTxnStatus(TxnStatus status) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#getTxnStatus()
	 */
	@Override
	public TxnStatus getTxnStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}
