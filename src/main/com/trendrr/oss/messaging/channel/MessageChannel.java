/**
 * 
 */
package com.trendrr.oss.messaging.channel;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.SafeBox;
import com.trendrr.oss.tests.messaging.EchoClass;


/**
 * A synchronous channel.  multiple threads can submit tasks to the channel.  All requests are handled in
 * order by the ChannelRequestHandler.  The ChannelRequestHandler operates in a single dedicated thread.
 * 
 * Note that throughput goes up as the number of threads submitting tasks goes up.  The wait/notify cycle 
 * is full of latency, so using this for 1to1 thread communication is slow.
 * 
 * 
 * @author Dustin Norlander
 * @created May 16, 2012
 * 
 */
public class MessageChannel implements Runnable{

	protected static Log log = LogFactory.getLog(MessageChannel.class);
	
	ArrayBlockingQueue<ChannelRequest> requests = new ArrayBlockingQueue<ChannelRequest>(200);

	protected String name;
	protected ChannelRequestHandler handler;
	
	private static ConcurrentHashMap<String, MessageChannel> channels = new ConcurrentHashMap<String, MessageChannel>();
	
	public static void main (String ...strings) throws Exception{
		
		EchoClass echo = new EchoClass();
		ChannelMethodRequestHandler handler = new ChannelMethodRequestHandler(echo);
		MessageChannel channel = MessageChannel.create("test", handler);
		int num = 500000;
		//warm up.
		for (int i=0; i < 100; i++) {
			Object val = channel.request("stringLength", "1234");
			List<Object> vals = (List<Object>)channel.request("inputToList", "one", "two");
		}
		
		
		Date start = new Date();
		for (int i=0; i < num; i++) {
			Object val = channel.request("stringLength", "1234");
			List<Object> vals = (List<Object>)channel.request("inputToList", "one", "two");
		}
		long millis = (new Date().getTime()-start.getTime());
		System.out.println("MESSAGE CHANNEL COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		start = new Date();
		for (int i=0; i < num; i++) {
			Object val = handler.handleRequest("stringLength", "1234");
			List<Object> vals = (List<Object>)handler.handleRequest("inputToList", "one", "two");
		}
		
		System.out.println("HANDLER COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		
		
		start = new Date();
		for (int i=0; i < num; i++) {
			Object val = echo.stringLength("1234");
			List<Object> vals = echo.inputToList( "one", "two");
		}
		
		System.out.println("COMPLETED " + num + " IN " + (new Date().getTime()-start.getTime()));
		
		
		channel.stop();
		
	}
	
	
	/**
	 * creates a new channel with the given name.  If a channel already exists with that name, it is stopped and this channel will
	 * replace it.
	 * @param name
	 * @param handler
	 * @return
	 */
	public static MessageChannel create(String name, ChannelRequestHandler handler) {
		MessageChannel c = new MessageChannel(name, handler);
		c.start();
		return c;	
	}
	
	/**
	 * returns the requested channel.
	 * @param name
	 * @return
	 */
	public static MessageChannel get(String name) {
		return channels.get(name);
	}
	
	protected MessageChannel(String name, ChannelRequestHandler handler) {
		this.handler = handler;
		this.name = name;
	}
	
	protected void start() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
		MessageChannel c = channels.put(name, this);
		if (c != null) {
			c.stop();
		}
			
	}
	
	/**
	 * stops the channel.  it will not be available after this call.
	 */
	public void stop() {
		channels.remove(this.name);
		try {
			requests.put(new ChannelStopRequest());
		} catch (InterruptedException e) {
			log.warn("Caught", e);
		}
	}
	
	public Object request(String endpoint, Object ...inputs) throws Exception {
		return this.request(new ChannelRequest(endpoint, inputs));
	}
	
	/**
	 * does a request to the channel.  will wait for a response.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object request(ChannelRequest request) throws Exception{
		
		this.requests.put(request);
		ChannelResponse response = request.awaitResponse();	
		if (response.getError() != null) {
			throw response.getError();
		}
		return response.getResult();
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		ChannelRequest request = new ChannelStopRequest();
		while(true) {
			try {
				request = this.requests.take();
				if (request instanceof ChannelStopRequest) {
					log.warn("Channel: " + this.name + " stopping");
					//TODO: a channel stop callback?
					return;
				}
				Object result = this.handler.handleRequest(request.getEndpoint(), request.getInputs());
				request.setResponse(new ChannelResponse(result, null));
				
			} catch (Exception e) {
				request.setResponse(new ChannelResponse(null, e));
			}
		}
	}
}
