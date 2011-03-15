/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;

/**
 * @author Dustin Norlander
 * @created Jan 26, 2011
 * 
 */
public class RequestBuilder {

	protected Log log = LogFactory.getLog(RequestBuilder.class);

	StrestRequest request;
	
	public static void main(String...strings) {
		RequestBuilder b = new RequestBuilder();
		try {
			b.url("http://www.trendrr.com/api/blah.json?poop=none");
			b.url("www.trendrr.com");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RequestBuilder() {
		request = new StrestRequest();
	}
	
	public RequestBuilder(StrestRequest request) {
		this.request = request;
	}
	
	/**
	 * sets the host and the uri. 
	 * 
	 * this is assumed to be a properly formed url
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public RequestBuilder url(String url) throws MalformedURLException {
		try {
			URL u = new URL(url);
			String host = u.getHost();
			request.setHeader(StrestHeaders.Names.HOST, host);
			String uri = url.substring(url.indexOf(host) + host.length());
			request.setUri(uri);
		} catch (Exception x) {
			MalformedURLException m = new MalformedURLException("Unable to parse: " + url);
			m.initCause(x);
			throw m;
		}
		return this;
	}
	
	public RequestBuilder uri(String uri) {
		request.setUri(uri);
		return this;
	}
	
	
	
	/**
	 * adds params to the uri.
	 * @param params
	 * @return
	 */
	public RequestBuilder params(DynMap params) {
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
	
	/**
	 * sets a custom transaction Id.  a unique txn id is 
	 * already set, so this call is not manditory.
	 * @param id
	 * @return
	 */
	public RequestBuilder txnId(String id) {
		request.setHeader(StrestHeaders.Names.STREST_TXN_ID, id);
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
		request.setHeader(StrestHeaders.Names.STREST_TXN_ACCEPT, val);
		return this;
	}
	
	public RequestBuilder method(String method) {
		request.setMethod(method);
		return this;
	}
	
	public RequestBuilder header(String header, Object value) {
		request.setHeader(header, value);
		return this;
	}
	
	public RequestBuilder content(String contentType, byte[] bytes) {
		request.setContent(contentType, bytes);
		return this;
	}
	
	public StrestRequest getRequest() {
		return this.request;
	}
}
