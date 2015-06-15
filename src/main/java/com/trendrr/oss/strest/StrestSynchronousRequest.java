/**
 * 
 */
package com.trendrr.oss.strest;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrTimeoutException;
import com.trendrr.oss.strest.models.StrestResponse;



/**
 * 
 * Allows us to do synchronous requests
 * 
 * @author Dustin Norlander
 * @created Mar 15, 2011
 * 
 */
class StrestSynchronousRequest implements StrestRequestCallback{

	protected static Log log = LogFactory.getLog(StrestSynchronousRequest.class);

	Semaphore lock = new Semaphore(1, true);
	StrestResponse response;
	Throwable error;
	
	public StrestSynchronousRequest() {
		try {
			//take the only semaphore
			lock.acquire(1);
		} catch (InterruptedException e) {
			log.error("Caught", e);
		}
	}
	
	public StrestResponse awaitResponse(long timeoutMillis) throws Throwable {
		try {
			//try to aquire a semaphore, none is available so we wait.
			if (timeoutMillis <= 0) {
				lock.acquire(1);
			} else {
				if (!lock.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
					throw new TrendrrTimeoutException("Waited for " + timeoutMillis + " millis for a response");
				}
			}
		} catch (InterruptedException e) {
			log.error("Caught", e);
			throw e;
		}
		if (this.error != null) {
			throw this.error;
		}
		
		return this.response;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestCallback#messageRecieved(com.trendrr.oss.networking.strest.StrestResponse)
	 */
	@Override
	public void response(StrestResponse response) {
		this.response = response;
		//release the single semaphore.
		lock.release(1);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestCallback#txnComplete()
	 */
	@Override
	public void txnComplete(String txnId) {
		//do nothing.  txn should always be complete!
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.strest.StrestCallback#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable x) {
		this.error = x;
		//release the single semaphore.
		lock.release(1);
	}
}
