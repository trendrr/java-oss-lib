/**
 * 
 */
package com.trendrr.oss.strest;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.strest.models.StrestResponse;


/**
 * @author Dustin Norlander
 * @created Apr 13, 2011
 * 
 */
public class ExampleMain {

	protected static Log log = LogFactory.getLog(ExampleMain.class);

	public static void main(String...strings) throws Exception{
		
		StrestClient client = new StrestClient("localhost", 8009);
		//EXAMPLE Blocking request.
		client.connect();
		while(true) {
			try {
					System.out.println("Connecting ...");
					Date start = new Date();

					System.out.println("Connected in: " + (new Date().getTime() - start.getTime()));
					
					System.out.println("Sending request");
					StrestResponse response = client.sendRequest(new RequestBuilder().uri("/ping").method("GET").getRequest());
					System.out.println("***********************************");
					System.out.println(new String(response.toString()));
					System.out.println("***********************************");
					
					System.out.println("Closing ...");
//					client.close();
					System.out.println("sleeping ...");
				
				} catch (Exception x) {
					x.printStackTrace();
				}
				
			Sleep.seconds(5);
		}
//		System.out.println("NOW TEST THE FIREHOSE");
//		//EXAMPLE Asynch Firehose request
//		client.sendRequest(new RequestBuilder().uri("/firehose").method("GET").getRequest(), new StrestRequestCallback() {
//			@Override
//			public void txnComplete(String txnId) {
//				System.out.println("TRANSACTION " + txnId + " COMPLETE!");
//			}
//			@Override
//			public void response(StrestResponse response) {
//				System.out.println("***********************************");
//				System.out.println(new String(response.getContent()));
//				System.out.println("***********************************");
//			}
//			@Override
//			public void error(Throwable x) {
//			
//			}
//		});
		
//		Sleep.seconds(30);
	}
}
