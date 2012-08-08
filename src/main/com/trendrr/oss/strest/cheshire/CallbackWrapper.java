/**
 * 
 */
package com.trendrr.oss.strest.cheshire;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.StrestRequestCallback;
import com.trendrr.oss.strest.models.StrestResponse;
import com.trendrr.oss.strest.models.json.StrestJsonBase;



/**
 * @author Dustin Norlander
 * @created Apr 6, 2011
 * 
 */
class CallbackWrapper implements StrestRequestCallback{

	protected static Log log = LogFactory.getLog(CallbackWrapper.class);
	CheshireApiCallback cb = null;
	CallbackWrapper(CheshireApiCallback cb) {
		this.cb = cb;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestRequestCallback#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable arg0) {
		this.cb.error(arg0);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestRequestCallback#response(com.trendrr.oss.networking.strest.StrestResponse)
	 */
	@Override
	public void response(StrestResponse response) {
		cb.response(((StrestJsonBase)response).getMap());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestRequestCallback#txnComplete(java.lang.String)
	 */
	@Override
	public void txnComplete(String arg0) {
		// TODO Auto-generated method stub
		
	}
}
