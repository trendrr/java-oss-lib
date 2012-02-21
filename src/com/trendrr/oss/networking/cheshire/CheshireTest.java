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
 * @created Feb 21, 2012
 * 
 */
public class CheshireTest implements CheshireApiCallback{

	protected Log log = LogFactory.getLog(CheshireTest.class);
	
	
	public static void main(String ...strings) throws TrendrrException, IOException {
		CheshireClient client = new CheshireClient("localhost", 8008);
		client.connect();
		CheshireTest callback = new CheshireTest();
		Date start = new Date();
		for (int i=0 ; i < 1000000; i++) {
			DynMap params = new DynMap();
			params.put("timeframe", "hourly");
			params.put("namespace", "testing_1");
			params.put("unique", StringHelper.randomString(1));
			params.put("val", 1);
			
			
			client.apiCall("/v1/increment", Verb.POST, 
					params, callback);
			
		}
		
		
		Sleep.seconds(5);
		
		System.out.println("Completed in : " + (new Date().getTime() - start.getTime() - 5000));
		DynMap params = new DynMap();
		params.put("timeframe", "hourly");
		params.put("namespace", "testing_1");
		System.out.println(client.apiCall("/v1/top_elements", Verb.GET, 
				params).toJSONString());
		
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
		// TODO Auto-generated method stub
		
	}
}
