/**
 * 
 */
package com.trendrr.oss.networking.cheshire;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AlreadyConnectedException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrDisconnectedException;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.networking.strest.RequestBuilder;
import com.trendrr.oss.networking.strest.StrestClient;
import com.trendrr.oss.networking.strest.StrestRequest;
import com.trendrr.oss.networking.strest.StrestResponse;


/**
 * 
 * This handles connections to a cheshire server. 
 * 
 * This will maintain a single connection to the server.  since the STREST protocol is 
 * asynch, a single connection can handle all your requests.  this client is threadsafe and is
 * expected to be shared across all your threads.
 * 
 * The connection to the server will automatically reconnect if the connection is broken.
 * 
 * @author Dustin Norlander
 * @created Apr 6, 2011
 * 
 */
public class CheshireClient implements CheshireApiCaller{

	protected Log log = LogFactory.getLog(CheshireClient.class);
	
	protected DynMap paramsForEveryRequest = new DynMap();
	private String host = "strest.trendrr.com";
	private int port = 80;
	protected StrestClient strest = null;
	
	protected int maxReconnectAttempts = -1;
	protected int reconnectWaitSeconds = 5;
	protected boolean keepalive = false;
	
	protected Timer timer = null; //timer for keepalive pings
	
	public synchronized boolean isKeepalive() {
		return keepalive;
	}
	
	/**
	 * setting this to true will keep the connection open.  
	 * 
	 * @param keepalive
	 */
	public synchronized void setKeepalive(boolean keepalive) {
		if (this.keepalive == keepalive) {
			return;
		}
		this.keepalive = keepalive;
		if (this.keepalive) {
			//start the timer.
			final CheshireClient self = this;
			this.timer = new Timer(true);
			this.timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						self.ping();
					} catch (TrendrrDisconnectedException x) {
						//make one reconnect attempt. 
						try {
							self.connect();
						} catch (IOException e) {
							
						}
					} catch (TrendrrException x) {
						log.error("Caught", x);
					}
				}
			}, 1000*1, 1000*30);
		} else {
			timer.cancel();
		}
	}

	public static void main(String ...strings) throws Exception {
		
		CheshireClient client = new CheshireClient("strest.trendrr.com", 80);
		client.connect();
		
//		System.out.println(client.apiCall("/helper/whats_my_ip", HttpMethod.GET, null));
		
		DynMap params = new DynMap();
		//start tracking
//		
//		params.put("project_id", "4cd057cd3e3b63fd8b10cf2e");
//		params.put("datasource_id", "twitter_search");
//		params.put("query", "sherlock");
//		
//		DynMap result = client.apiCall("/datasets/create", HttpMethod.POST, params);
//		System.out.println(result.toJSONString());
		
		
		
		//datasource list.
//		DynMap result = client.apiCall("/datasources/list", HttpMethod.GET, null);
//		System.out.println(result.toJSONString());
		
//		params.put("query", "foursquare");
//		DynMap result = client.apiCall("/datasources/find", HttpMethod.GET, params);
//		System.out.println(Datasource.get(client, "twitter_search"));
//		System.out.println(result.toJSONString());
		
//		 '/srm/datasource',
//         'GET',
//         {
//           'project_id': project_id, 
//           'datasource_id': 'twitter_user_stats',
//           'update_stats' : True,
//           'date' : date
//          }
		params.put("username", "tvcharts");
		params.put("secret", "ve_vant_ze_money_lebowski");
		params.put("project_id","4d22002cbb7463fd5d1fb459");
		params.put("datasource_id", "twitter_user_stats");
		DynMap result = client.apiCall("/srm/datasource", Verb.GET, params);
		System.out.println(result.toJSONString());
	}
	
	public CheshireClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.strest = new StrestClient(host, port);
	}
	
	/**
	 * specialized constructor.  
	 * 
	 * The params from the passed in map will be include in every request
	 * It is only useful in specialized cases, we suggest you use the authToken constructor.
	 * @param paramsForEveryRequest
	 */
	public CheshireClient(String host, int port, Map paramsForEveryRequest) {
		this(host, port);
		this.paramsForEveryRequest.putAll(paramsForEveryRequest);
	}
	
	public void connect() throws IOException{
		this.strest.connect();
	}
	
	public void setApiHost(String host, int port) throws AlreadyConnectedException {
		if (this.strest.isConnected()) {
			throw new AlreadyConnectedException();
		}
		this.host = host;
		this.port = port;
		this.strest = new StrestClient(host, port);
	}
	public void close() {
		this.strest.close();
	}
	
	/**
	 * Does an asynchronous api call.  This method returns immediately. the Response or error is sent to the callback.
	 * 
	 * 
	 * @param endPoint
	 * @param method
	 * @param params
	 * @param callback
	 */
	public void apiCall(String endPoint, Verb method, Map params, CheshireApiCallback callback) {
		if (!this.strest.isConnected()) {
			this.attemptReconnect();
		}
		StrestRequest request = this.createRequest(endPoint, method, params);
		strest.sendRequest(request, new CallbackWrapper(callback));
	}
	
	/**
	 * Does a synchronous ping.  will throw an exception.  This method will *NOT* trigger a reconnect attempt. 
	 * @throws Exception
	 */
	public void ping() throws TrendrrException {
		strest.sendRequest( this.createRequest("/ping", Verb.GET, null));
	}
	
	/**
	 * A synchronous call.  blocks until response is available.  Please note that this does *NOT* block concurrent api calls, so you can continue to 
	 * make calls in other threads.
	 * 
	 * If the maxReconnectAttempts is non-zero (-1 is infinit reconnect attempts), then this will attempt to reconnect and send on any io problems. 
	 * 
	 * @param endPoint
	 * @param method
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public DynMap apiCall(String endPoint, Verb method, Map params) throws TrendrrException {
		StrestRequest request = this.createRequest(endPoint, method, params);
		StrestResponse response = this.sendWithReconnect(request);
		String res;
		try {
			res = new String(response.getContent(), "utf8");
		} catch (UnsupportedEncodingException e) {
			throw new TrendrrException("WHAT bad encoding!!?", e);
		}
		return DynMapFactory.instanceFromJSON(res);
	}
	
	private StrestRequest createRequest(String endPoint, Verb method, Map params) {
		RequestBuilder builder = RequestBuilder.instance();
		builder.method(method.toString());
		
		builder.uri(endPoint + "?" + this.paramsForEveryRequest.toURLString());
		
		if (params != null) {
			DynMap pms = null;
			if (params instanceof DynMap){
				pms = (DynMap)params;
			} else {
				pms = DynMap.instance(params);
			}
			
			if (method == Verb.POST) {
				builder.paramsPOST(pms);
			} else {
				builder.paramsGET(pms);
			}
		}
		return builder.getRequest();
	}
	
	/**
	 * Called if we get an exception when making a request, repeatedly attempts to reconnect
	 * Returns true if successful, false otherwise
	 */
	protected synchronized boolean attemptReconnect(){
		if(this.strest.isConnected())
			return true;
		
		if (this.maxReconnectAttempts == 0) {
			return false;
		}
		int attempts = 0;

		while(true) {
			try {
				log.warn("Attempting to reconnect to trendrr api");
				this.connect();
				//if we get to this point then we have succeeded??
				return true;
			} catch (IOException e) {
				 log.info("Caught", e);
			} finally {
				attempts++;
			}
			if (this.maxReconnectAttempts != -1 && attempts >= this.maxReconnectAttempts) {
				return false;
			}
			Sleep.seconds(this.reconnectWaitSeconds);
		}
	}
	
	protected StrestResponse sendWithReconnect(StrestRequest req) throws TrendrrDisconnectedException{
		StrestResponse response = null;
		try {
//			log.info("Sending request ");
//			log.info(req);
//			log.info("**************************");
			response = this.strest.sendRequest(req);
		} catch (TrendrrException e) {
//			log.info("Caught", e);
			//we are evidently not connected, so update that
			if(this.attemptReconnect()){
				//able to reconnect, so try one more time
				try{
//					log.info("Sending reconnect request ");
//					log.info(req);
//					log.info("************************");
					response = this.strest.sendRequest(req);
					
				}catch (TrendrrException e1){
					//disconnected again? this is some craziness, fail
					throw new TrendrrDisconnectedException(e1);
				}
			}else{
				throw new TrendrrDisconnectedException(e);
			}
		}
		return response;
	}
}
