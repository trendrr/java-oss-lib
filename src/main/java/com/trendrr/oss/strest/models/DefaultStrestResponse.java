/**
 * 
 */
package com.trendrr.oss.strest.models;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;
import com.trendrr.oss.strest.models.StrestHeader.Method;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;
import com.trendrr.oss.strest.models.StrestHeader.TxnStatus;
import com.trendrr.oss.strest.models.json.StrestJsonResponse;



/**
 * @author Dustin Norlander
 * @created Jul 29, 2013
 * 
 */
public class DefaultStrestResponse implements StrestResponse {

	protected static Log log = LogFactory.getLog(DefaultStrestResponse.class);

	protected float protocolVersion = StrestHeader.STREST_VERSION;
	protected String protocolName = StrestHeader.STREST_PROTOCOL;
	
	protected String txnId;
	
	protected TxnStatus txnStatus;
	
	protected int statusCode = 200;
	protected String statusMessage;
	
	
	protected InputStream content;
	protected int contentLength = 0;
	protected StrestHeader.ContentEncoding contentEncoding;
	
	protected DynMap params = new DynMap();
	
	public synchronized DynMap getParams() {
		return params;
	}

	public synchronized void setParams(DynMap params) {
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setProtocol(java.lang.String, float)
	 */
	@Override
	public void setProtocol(String protocolName, float version) {
		this.protocolVersion = version;
		this.protocolName = protocolName;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getProtocolVersion()
	 */
	@Override
	public float getProtocolVersion() {
		return this.protocolVersion;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getProtocolName()
	 */
	@Override
	public String getProtocolName() {
		return this.protocolName;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setTxnId(java.lang.String)
	 */
	@Override
	public void setTxnId(String id) {
		this.txnId = id;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getTxnId()
	 */
	@Override
	public String getTxnId() {
		return this.txnId;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setContent(java.lang.String, long, java.io.InputStream)
	 */
	@Override
	public void setContent(ContentEncoding contentEncoding, int contentLength,
			InputStream content) throws Exception {
		this.content = content;
		this.contentEncoding = contentEncoding;
		this.contentLength = contentLength;
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContent()
	 */
	@Override
	public InputStream getContent() {
		return this.content;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentEncoding()
	 */
	@Override
	public ContentEncoding getContentEncoding() {
		return this.contentEncoding;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContentLength()
	 */
	@Override
	public int getContentLength() {
		return this.contentLength;
	}

	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		this.statusCode = code;
		this.statusMessage = message;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		return this.statusCode;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#setTxnStatus(com.trendrr.oss.strest.models.StrestHeader.TxnStatus)
	 */
	@Override
	public void setTxnStatus(TxnStatus status) {
		this.txnStatus = status;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestResponse#getTxnStatus()
	 */
	@Override
	public TxnStatus getTxnStatus() {
		return this.txnStatus;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.DynMapConvertable#toDynMap()
	 */
	@Override
	public DynMap toDynMap() {
		try {
			StrestJsonResponse response = new StrestJsonResponse(this);
			DynMap mp = response.toDynMap();
			if (this.params != null) {
				mp.putAll(this.getParams());
			}
			return mp;
		} catch (Exception x) {
			log.error("caught", x);
		}
		return null;
	}
}
