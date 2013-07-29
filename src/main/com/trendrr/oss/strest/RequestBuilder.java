/**
 * 
 */
package com.trendrr.oss.strest;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.models.StrestHeader;
import com.trendrr.oss.strest.models.StrestRequest;
import com.trendrr.oss.strest.models.StrestHeader.TxnAccept;
import com.trendrr.oss.strest.models.json.StrestJsonRequest;

/**
 * @author Dustin Norlander
 * @created Jan 26, 2011
 * 
 */
public class RequestBuilder {

	protected static Log log = LogFactory.getLog(RequestBuilder.class);

	StrestRequest request;
	
	public static void main(String...strings) {

	}
	
	public static RequestBuilder instance() {
		return new RequestBuilder();
	}
	public static RequestBuilder instance(StrestRequest request) {
		return new RequestBuilder(request);
	}
	
	public RequestBuilder() {
		this(null);
	}
	
	public RequestBuilder(StrestRequest request) {
		if (request == null) {
			this.request = new StrestJsonRequest();
		} else {
			this.request = request;
		}
	}
	
	public RequestBuilder uri(String uri) {
		request.setUri(uri);
		return this;
	}
	
	
	public RequestBuilder params(Map params) {
		request.setParams(DynMap.instance(params));
		return this;
	}
	
	/**
	 * adds params to the uri. 
	 * @param params
	 * @return
	 */
	public RequestBuilder paramsGET(DynMap params) {
		String encodedParams = params.toURLString();
		if (encodedParams == null || encodedParams.isEmpty()) {
			return this;
		}
		String uri = request.getUri();
		if (!uri.contains("?")) {
			uri = uri + "?";
		} else {
			uri = uri + "&";
		}
		request.setUri(uri + encodedParams);
		return this;
	}
	
//	/**
//	 * adds params to the content section and sets the Content-Type to
//	 * 
//	 * @param params
//	 * @return
//	 */
//	public RequestBuilder paramsPOST(DynMap params) {
//		String encodedParams = params.toURLString();
//		log.info(encodedParams);
//		if (encodedParams == null || encodedParams.isEmpty()) {
//			return this;
//		}
//		return this.contentUTF8("application/x-www-form-urlencoded", encodedParams);
//	}
//	
//	/**
//	 * Adds params to content section as json string, with mime type set to json
//	 * @param params
//	 * @return
//	 */
//	public RequestBuilder paramsJSONPOST(DynMap params){
//		String json = params.toJSONString();
//		log.info(json);
//		if(json == null || json.isEmpty()){
//			return this;
//		}
//		return this.contentUTF8("application/json", json);
//	}
//	
//	/**
//	 * encodes the text as utf8 and swallows and logs a warning for any character encoding exceptions
//	 * @param mimeType
//	 * @param content
//	 * @return
//	 */
//	public RequestBuilder contentUTF8(String mimeType, String content) {
//		try {
//			this.content(mimeType, content.getBytes("utf8"));
//		} catch (UnsupportedEncodingException e) {
//			log.warn("Swallowed", e);
//		}
//		return this;
//	}
	
	/**
	 * sets a custom transaction Id.  a unique txn id is 
	 * already set, so this call is not manditory.
	 * @param id
	 * @return
	 */
	public RequestBuilder txnId(String id) {
		request.setTxnId(id);
		return this;
	}
	
	/**
	 * what kind of transactions to accept.
	 * 
	 * 'single' or multi
	 * 
	 * @param val
	 * @return
	 */
	public RequestBuilder txnAccept(String val) {
		request.setTxnAccept(TxnAccept.instance(val));
		return this;
	}
	
	public RequestBuilder method(String method) {
		request.setMethod(StrestHeader.Method.instance(method));
		return this;
	}
	
//	public RequestBuilder header(String header, String value) {
//		request.addHeader(header, value);
//		return this;
//	}
		
	public StrestRequest getRequest() {
		return this.request;
	}
}
