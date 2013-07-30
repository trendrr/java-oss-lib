/**
 * 
 */
package com.trendrr.oss.strest.models.json;

import java.io.DataInputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;
import com.trendrr.oss.strest.models.StrestResponse;
import com.trendrr.oss.strest.models.StrestHeader.TxnStatus;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonResponse extends StrestJsonBase implements StrestResponse {

	/**
	 * @param map
	 */
	public StrestJsonResponse(DynMap map) {
		super(map);
	}

	public StrestJsonResponse() {
		super();
	}
	
	
	/**
	 * creates a new json response from the passed in response.
	 * @param response
	 */
	public StrestJsonResponse(StrestResponse response) throws Exception {
		super();
		
		if (response.getContentEncoding() == ContentEncoding.JSON && response.getContentLength() > 0) {
			byte[] json = new byte[response.getContentLength()];
			DataInputStream dataIs = new DataInputStream(response.getContent());
			dataIs.readFully(json);
			this.map.putAll(DynMap.instance(new String(json, "utf8")));
		} else {
			this.setContent(response.getContentEncoding(), response.getContentLength(), response.getContent());	
		}
		this.setProtocol(response.getProtocolName(), response.getProtocolVersion());
		this.setStatus(response.getStatusCode(), response.getStatusMessage());
		this.setTxnId(response.getTxnId());
		this.setTxnStatus(response.getTxnStatus());
	}
	
	protected static Log log = LogFactory.getLog(StrestJsonResponse.class);

	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		this.map.putWithDot("status.code", code);
		this.map.putWithDot("status.message", message);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		return this.map.getInteger("status.code");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return this.map.getString("status.message");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setTxnStatus(com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus)
	 */
	@Override
	public void setTxnStatus(TxnStatus status) {
		this.addHeader(StrestHeader.Name.TXN_STATUS, status.getJson());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getTxnStatus()
	 */
	@Override
	public TxnStatus getTxnStatus() {
		return TxnStatus.instance(this.getHeader(StrestHeader.Name.TXN_STATUS));
	}
	
//	/* (non-Javadoc)
//	 * @see com.trendrr.oss.strest.models.StrestPacketBase#toMap()
//	 */
//	@Override
//	public Map<String, Object> toMap() {
//		return this.map;
//	}
}
