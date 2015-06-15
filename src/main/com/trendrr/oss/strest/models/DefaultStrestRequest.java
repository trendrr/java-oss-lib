/**
 * 
 */
package com.trendrr.oss.strest.models;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.models.StrestHeader.ContentEncoding;
import com.trendrr.oss.strest.models.StrestHeader.Method;
import com.trendrr.oss.strest.models.StrestHeader.Name;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created Jul 29, 2013
 * 
 */
public class DefaultStrestRequest implements StrestRequest {

	protected static Log log = LogFactory.getLog(DefaultStrestRequest.class);

	protected float protocolVersion = StrestHeader.STREST_VERSION;
	protected String protocolName = StrestHeader.STREST_PROTOCOL;
	
	protected String txnId;
	
	protected ShardRequest shard;
	
	protected Method method;
	
	protected String uri;
	
	protected TxnAccept accept = TxnAccept.SINGLE; 

	protected DynMap params = new DynMap();
	
	protected InputStream content;
	protected int contentLength = 0;
	protected StrestHeader.ContentEncoding contentEncoding = StrestHeader.ContentEncoding.STRING;
	
	
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
	 * @see com.trendrr.oss.strest.models.StrestRequest#setShardRequest(com.trendrr.oss.strest.models.ShardRequest)
	 */
	@Override
	public void setShardRequest(ShardRequest shard) {
		this.shard = shard;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#getShardRequest()
	 */
	@Override
	public ShardRequest getShardRequest() {
		return this.shard;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#setMethod(com.trendrr.oss.strest.models.StrestHeader.Method)
	 */
	@Override
	public void setMethod(Method method) {
		this.method = method;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#getMethod()
	 */
	@Override
	public Method getMethod() {
		return this.method;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#setUri(java.lang.String)
	 */
	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#getUri()
	 */
	@Override
	public String getUri() {
		return this.uri;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#setTxnAccept(com.trendrr.oss.strest.models.StrestHeader.TxnAccept)
	 */
	@Override
	public void setTxnAccept(TxnAccept accept) {
		this.accept = accept;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#getTxnAccept()
	 */
	@Override
	public TxnAccept getTxnAccept() {
		return this.accept;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#setParams(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setParams(DynMap params) {
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestRequest#getParams()
	 */
	@Override
	public DynMap getParams() {
		return this.params;
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
	
	public void setContent(String content) throws Exception {
		this.setContent(ContentEncoding.STRING, content.getBytes("utf8"));
	}
	
	public void setContent(ContentEncoding contentEncoding, byte[] content) throws Exception {
		this.content = new ByteArrayInputStream(content);
		this.contentEncoding = contentEncoding;
		this.contentLength = content.length;
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
	 * @see com.trendrr.oss.DynMapConvertable#toDynMap()
	 */
	@Override
	public DynMap toDynMap() {
		log.warn("ToDynMap in DefaultStrestRequest not implimented. todo");
		return null;
	}
}
