/**
 * 
 */
package com.trendrr.oss.strest.models;

import java.io.InputStream;

import com.trendrr.oss.DynMapConvertable;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public interface StrestPacketBase extends DynMapConvertable {

	
	public void setProtocol(String protocolName, float version);
	public float getProtocolVersion();
	public String getProtocolName();
	
	public void setTxnId(String id);
	public String getTxnId();
	
	public void setContent(StrestHeader.ContentEncoding contentEncoding, int contentLength, InputStream stream) throws Exception;
	
	public InputStream getContent() throws Exception;
	public ContentEncoding getContentEncoding();
	public int getContentLength();
	
	
	/**
	 * this packet is done with, clean up anything that needs it.
	 */
	public void cleanup();
	
	@Deprecated
	public byte[] toByteArray();
//	
//	public Map<String, Object> toMap();
}
