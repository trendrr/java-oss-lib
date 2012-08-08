/**
 * 
 */
package com.trendrr.oss.strest.models.json;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;

import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestRequest;
import com.trendrr.oss.strest.models.StrestHeader.Method;
import com.trendrr.oss.strest.models.StrestHeader.Name;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonRequest extends StrestJsonBase implements StrestRequest {

	/**
	 * @param map
	 */
	public StrestJsonRequest(DynMap map) {
		super(map);
		this.setProtocol(StrestHeader.STREST_PROTOCOL, StrestHeader.STREST_VERSION);
	}

	public StrestJsonRequest() {
		super();
		this.setProtocol(StrestHeader.STREST_PROTOCOL, StrestHeader.STREST_VERSION);
	}
	
	protected static Log log = LogFactory.getLog(StrestJsonRequest.class);

		
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setMethod(com.trendrr.strest.server.v2.StrestHeader.Method)
	 */
	@Override
	public void setMethod(Method method) {
		this.map.putWithDot("strest.method", method.toString());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getMethod()
	 */
	@Override
	public Method getMethod() {
		return StrestHeader.Method.instance(this.map.getString("strest.method"));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setUri(java.lang.String)
	 */
	@Override
	public void setUri(String uri) {
		this.map.putWithDot("strest.uri", uri);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getUri()
	 */
	@Override
	public String getUri() {
		return this.map.getString("strest.uri");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setTxnAccept(com.trendrr.strest.server.v2.StrestHeader.TxnAccept)
	 */
	@Override
	public void setTxnAccept(TxnAccept accept) {
		this.addHeader(Name.TXN_ACCEPT, accept.getJson());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getTxnAccept()
	 */
	@Override
	public TxnAccept getTxnAccept() {
		TxnAccept accept = TxnAccept.instance(this.getHeader(Name.TXN_ACCEPT));
		if (accept == null)
			return TxnAccept.SINGLE;
		return accept;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setParams(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setParams(DynMap params) {
		this.map.putWithDot("strest.params", params);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getParams()
	 */
	@Override
	public DynMap getParams() {
		return this.map.getMap("strest.params");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		super.cleanup();
	}
}
