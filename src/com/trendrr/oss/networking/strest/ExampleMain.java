/**
 * 
 */
package com.trendrr.oss.networking.strest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;


/**
 * @author Dustin Norlander
 * @created Apr 13, 2011
 * 
 */
public class ExampleMain {

	protected Log log = LogFactory.getLog(ExampleMain.class);

	public static void main(String...strings) throws Exception{
		StrestClient client = new StrestClient("localhost", 8000);
		client.connect();
		
		//EXAMPLE Blocking request.
		StrestResponse response = client.sendRequest(new RequestBuilder().uri("/hello/world").method("GET").getRequest());
		System.out.println("***********************************");
		System.out.println(new String(response.getContent()));
		System.out.println("***********************************");
		
		System.out.println("NOW TEST THE FIREHOSE");
		//EXAMPLE Asynch Firehose request
		client.sendRequest(new RequestBuilder().uri("/firehose").method("GET").getRequest(), new StrestRequestCallback() {
			@Override
			public void txnComplete(String txnId) {
				System.out.println("TRANSACTION " + txnId + " COMPLETE!");
			}
			@Override
			public void response(StrestResponse response) {
				System.out.println("***********************************");
				System.out.println(new String(response.getContent()));
				System.out.println("***********************************");
			}
			@Override
			public void error(Throwable x) {
			
			}
		});
		
		Sleep.seconds(30);
	}
}
