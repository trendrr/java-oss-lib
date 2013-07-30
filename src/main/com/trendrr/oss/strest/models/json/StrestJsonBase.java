/**
 * 
 */
package com.trendrr.oss.strest.models.json;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;
import com.trendrr.oss.strest.models.StrestPacketBase;
import com.trendrr.oss.strest.models.StrestHeader.Method;
import com.trendrr.oss.strest.models.StrestHeader.Name;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public abstract class StrestJsonBase implements StrestPacketBase {

	protected static Log log = LogFactory.getLog(StrestJsonBase.class);

	DynMap map = new DynMap();
	
	public StrestJsonBase(DynMap map) {
		this.map = map;
	}
	
	public StrestJsonBase() {
		
	}
	/**
	 * gets the DynMap this packet is based on.
	 * @return
	 */
	public DynMap getMap() {
		return this.map;
	}

	
//	public void addHeader(String header, String value) {
//		this.map.putWithDot("strest." + header.toLowerCase(), value);
//		
//	}
	
	public void addHeader(StrestHeader.Name header, String value) {
		this.map.putWithDot("strest." + header.getJsonName(), value);
	}

	public String getHeader(StrestHeader.Name header) {
		return this.map.getString("strest." + header.getJsonName());
	}
		
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setStrestProtocolVersion(java.lang.String)
	 */
	@Override
	public void setProtocol(String protocol, float version) {
		this.map.putWithDot("strest.v", version);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getStrestProtocolVersion()
	 */
	@Override
	public float getProtocolVersion() {
		return this.map.getDouble("strest.v", 0d).floatValue();
	}

	public String getProtocolName() {
		return "strest";
	}
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setTxnId(java.lang.String)
	 */
	@Override
	public void setTxnId(String id) {
		this.addHeader(StrestHeader.Name.TXN_ID, id);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getTxnId()
	 */
	@Override
	public String getTxnId() {
		return this.getHeader(Name.TXN_ID);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		try {
			return this.map.toJSONString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		return this.map.toJSONString();
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		this.map = null;
	}
	
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setContent(java.lang.String, long, java.io.InputStream)
	 */
	@Override
	public void setContent(ContentEncoding contentEncoding, int contentLength,
			InputStream stream) throws Exception {
		this.map.putWithDot("content_encoding", contentEncoding);
		
		byte[] bytes = new byte[(int)contentLength];
		DataInputStream dataIs = new DataInputStream(stream);
		dataIs.readFully(bytes);
		if (contentEncoding == ContentEncoding.STRING) {
			this.map.put("content", new String(bytes, "utf8"));
			return;
		}
		throw new TrendrrException("Json request only supports string encoding atm");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContent()
	 */
	@Override
	public InputStream getContent() throws Exception {
		return new ByteArrayInputStream(this.map.getString("content", "").getBytes("utf8"));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentEncoding()
	 */
	@Override
	public ContentEncoding getContentEncoding() {
		return ContentEncoding.STRING;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentLength()
	 */
	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}
}
