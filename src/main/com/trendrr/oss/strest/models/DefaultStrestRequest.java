/**
 * 
 */
package com.trendrr.oss.strest.models;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
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

	protected float protocolVersion = 0f;
	protected String protocolName;
	
	protected String txnId;
	
	protected ShardRequest shard;
	
	protected Method method;
	
	protected String uri;
	
	protected TxnAccept accept = TxnAccept.SINGLE; 

	protected DynMap params = new DynMap();
	
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
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setContent(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setContent(DynMap content) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#setContent(java.lang.String, byte[])
	 */
	@Override
	public void setContent(String contentType, byte[] bytes) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#getContent()
	 */
	@Override
	public Object getContent() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see com.trendrr.oss.strest.models.StrestPacketBase#toMap()
	 */
	@Override
	public Map<String, Object> toMap() {
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
}
