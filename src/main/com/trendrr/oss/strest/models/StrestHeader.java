/**
 * 
 */
package com.trendrr.oss.strest.models;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestHeader {

	protected static Log log = LogFactory.getLog(StrestHeader.class);
	
	public static String STREST_PROTOCOL = "strest";
	public static float STREST_VERSION = 2.0f;
	
	public static final String HTTP_LINE_ENDING = "\r\n";
	
	private static AtomicLong txn = new AtomicLong(0l);
	public static String generateTxnId() {
		return Long.toHexString(txn.incrementAndGet());
	}
	
	
	public enum Name {
		
		TXN_ID("Strest-Txn-Id", "txn.id"),
		TXN_ACCEPT("Strest-Txn-Accept", "txn.accept"),
		TXN_STATUS("Strest-Txn-Status", "txn.status"),
		CONTENT_TYPE("Content-Type", "content-type"),
		USER_AGENT("User-Agent", "user-agent");

		protected String http;
		protected String json;
		
		Name(String httpHeader, String jsonHeader) {
			this.http = httpHeader;
			this.json = jsonHeader;
		}
		
		public String getHttpName() {
			return http;
		}
		
		public String getJsonName() {
			return this.json;
		}
		
	}
	
	public enum TxnStatus {
		CONTINUE("continue", "continue", (byte)1),
		COMPLETED("completed", "completed", (byte)0);
		protected String http;
		protected String json;
		protected byte binary;
		
		public static TxnStatus instance(String str) {
			if (str == null)
				return null;
			return TxnStatus.valueOf(str.toUpperCase());
		}
		
		public static TxnStatus instance (byte binary) {
			for (TxnStatus t : TxnStatus.values()) {
				if (t.getBinary() == binary) {
					return t;
				}
			}
			return null;
		}
		
		TxnStatus(String http, String json, byte binary) {
			this.http = http;
			this.json = json;
			this.binary = binary;
		}
		
		public String getHttp() {
			return http;
		}
		
		public String getJson() {
			return this.json;
		}
		
		public byte getBinary() {
			return this.binary;
		}
		
	}
	
	
	public enum TxnAccept {
		MULTI("multi", "multi", (byte)1),
		SINGLE("single", "single", (byte)0);
		protected String http;
		protected String json;
		protected byte binary;
		
		
		public static TxnAccept instance(String str) {
			if (str == null)
				return null;
			return TxnAccept.valueOf(str.toUpperCase());
		}
		
		TxnAccept(String http, String json, byte binary) {
			this.http = http;
			this.json = json;
			this.binary = binary;
		}
		
		public String getHttp() {
			return http;
		}
		
		public String getJson() {
			return this.json;
		}
		
		public byte getBinary() {
			return this.binary;
		}
	}
	
	public enum Method {
		GET((byte)0),
		POST((byte)1),
		PUT((byte)2),
		DELETE((byte)3);
		
		protected byte binary;
		
		public static Method instance(String method) {
			if (method == null)
				return null;
			return Method.valueOf(method.toUpperCase());
		}
		
		private Method(byte binary) {
			this.binary = binary;
		}
		
		public String getHttp() {
			return this.toString();
		}
		
		public String getJson() {
			return this.toString();
		}
		
		public byte getBinary() {
			return this.binary;
		}
	}
	
	public enum ParamEncoding {
		JSON((byte)0),
		MSGPACK((byte)1);
		protected byte binary;
		private ParamEncoding(byte binary) {
			this.binary = binary;
		}
		
		public byte getBinary() {
			return this.binary;
		}
	}
	
	public enum ContentEncoding {
		STRING((byte)0),
		BYTES((byte)1);
		protected byte binary;
		
		public static ContentEncoding instance (byte binary) {
			for (ContentEncoding t : ContentEncoding.values()) {
				if (t.getBinary() == binary) {
					return t;
				}
			}
			return null;
		}
		
		private ContentEncoding(byte binary) {
			this.binary = binary;
		}
		
		public byte getBinary() {
			return this.binary;
		}
	}
	
}
