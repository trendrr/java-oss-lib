/**
 * 
 */
package com.trendrr.oss.strest.cheshire;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AlreadyConnectedException;
import java.util.Date;
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
import com.trendrr.oss.exceptions.TrendrrTimeoutException;
import com.trendrr.oss.strest.RequestBuilder;
import com.trendrr.oss.strest.StrestClient;
import com.trendrr.oss.strest.models.StrestRequest;
import com.trendrr.oss.strest.models.StrestResponse;
import com.trendrr.oss.strest.models.json.StrestJsonBase;


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

	protected static Log log = LogFactory.getLog(CheshireClient.class);
	
	protected DynMap paramsForEveryRequest = new DynMap();
	private String host = "strest.trendrr.com";

	private int port = 80;
	protected StrestClient strest = null;
	
	protected int maxReconnectAttempts = -1;
	protected int reconnectWaitSeconds = 5;
	protected boolean keepalive = false;
	
	protected Date lastSuccessfulPing = null;
	
	protected Timer timer = null; //timer for keepalive pings
	
	public synchronized boolean isKeepalive() {
		return keepalive;
	}
	
	/**
	 * the date of the last successful ping.  could be null
	 * @return
	 */
	public Date getLastSuccessfulPing() {
		return lastSuccessfulPing;
	}
	
	public synchronized void setLastSuccessfullPing(Date d) {
		this.lastSuccessfulPing = d;
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
						self.lastSuccessfulPing = new Date();
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

	/**
	 * Maximum number of queued writes.  once this limit is reached exceptions will be thrown on write.
	 * @return
	 */
	public int getMaxQueuedWrites() {
		return this.strest.getMaxQueuedWrites();
	}

	public void setMaxQueuedWrites(int maxQueuedWrites) {
		this.strest.setMaxQueuedWrites(maxQueuedWrites);
	}
	
	public static void main(String ...strings) throws Exception {
		
		CheshireClient client = new CheshireClient("strest.trendrr.com", 80);
		client.connect();

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
		
		StrestClient old = null;
		if (this.strest != null) {
			old = this.strest;
		}
		
		this.strest = new StrestClient(host, port);
		if (old != null) {
			this.strest.setMaxQueuedWrites(old.getMaxQueuedWrites());
			this.strest.setMaxWaitingForResponse(old.getMaxWaitingForResponse());
			this.strest.setWaitOnMaxQueuedWrites(old.isWaitOnMaxQueuedWrites());
		}
		
		
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
		strest.sendRequest(request, new CallbackWrapper(this,callback));
	}
	
	/**
	 * Does a synchronous ping.  will throw an exception.  This method will *NOT* trigger a reconnect attempt. 
	 * @throws Exception
	 */
	public void ping() throws TrendrrException {
		strest.sendRequest( this.createRequest("/ping", Verb.GET, null));
		this.setLastSuccessfullPing(new Date());
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
	@Override
	public DynMap apiCall(String endPoint, Verb method, Map params, long timeoutMillis) throws TrendrrTimeoutException, TrendrrException {
		StrestRequest request = this.createRequest(endPoint, method, params);
		StrestResponse response = this.sendWithReconnect(request, timeoutMillis);
		return ((StrestJsonBase)response).getMap();
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
			builder.params(params);
		}
		return builder.getRequest();
	}
	
	/**
	 * Called if we get an exception when making a request, repeatedly attempts to reconnect
	 * Returns true if successful, false otherwise
	 */
	synchronized boolean attemptReconnect(){
		if(this.strest.isConnected()) {
			return true;
		}
		
		if (this.maxReconnectAttempts == 0) {
			return false;
		}
		int attempts = 0;

		while(true) {
			try {
				log.warn("Attempting to reconnect to trendrr api: " + this.host);
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
	
	protected StrestResponse sendWithReconnect(StrestRequest req, long timeoutMillis) throws TrendrrTimeoutException, TrendrrDisconnectedException{
		StrestResponse response = null;
		try {
//			log.info("Sending request ");
//			log.info(req);
//			log.info("**************************");
			response = this.strest.sendRequest(req, timeoutMillis);
		} catch (TrendrrTimeoutException e) {
			throw e;
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
	
	public int getMaxReconnectAttempts() {
		return maxReconnectAttempts;
	}

	public void setMaxReconnectAttempts(int maxReconnectAttempts) {
		this.maxReconnectAttempts = maxReconnectAttempts;
	}


	public int getReconnectWaitSeconds() {
		return reconnectWaitSeconds;
	}

	public void setReconnectWaitSeconds(int reconnectWaitSeconds) {
		this.reconnectWaitSeconds = reconnectWaitSeconds;
	}
	
	/**
	 * wait or exception when max queued writes is reached? default to false
	 * @return
	 */
	public boolean isWaitOnMaxQueuedWrites() {
		return this.strest.isWaitOnMaxQueuedWrites();
	}

	public void setWaitOnMaxQueuedWrites(boolean waitOnMaxQueuedWrites) {
		this.strest.setWaitOnMaxQueuedWrites(waitOnMaxQueuedWrites);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
