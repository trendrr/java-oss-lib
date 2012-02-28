/**
 * 
 */
package com.trendrr.oss.networking.cheshire;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Feb 23, 2012
 * 
 */
public class CheshirePingTest implements CheshireApiCallback{

	protected Log log = LogFactory.getLog(CheshirePingTest.class);
	
	public static void main(String ...strings) throws TrendrrException, IOException {
		CheshireClient client = new CheshireClient("localhost", 8010);
		client.connect();
		CheshirePingTest callback = new CheshirePingTest();
		Date start = new Date();
		for (int i=0 ; i < 100000; i++) {			
			client.apiCall("/ping", Verb.GET, 
					new DynMap());
		}
		
		
		Sleep.seconds(5);
		
		System.out.println("Completed in : " + (new Date().getTime() - start.getTime() - 5000));
		
	}


	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.cheshire.CheshireApiCallback#response(com.trendrr.oss.DynMap)
	 */
	@Override
	public void response(DynMap response) {
		System.out.println(response.toJSONString());
	}


	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.cheshire.CheshireApiCallback#txnComplete(java.lang.String)
	 */
	@Override
	public void txnComplete(String txnId) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.cheshire.CheshireApiCallback#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable x) {
		x.printStackTrace();
	}
}
