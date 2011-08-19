/**
 * 
 */
package com.trendrr.oss.networking;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.exceptions.TrendrrException;


/**
 * 
 * A private class that allows us to do synchronous requests on the channel reader
 * 
 * @author Dustin Norlander
 * @created Mar 10, 2011
 * 
 */
class SynchronousReadCallback implements ByteReadCallback, StringReadCallback{

	protected Log log = LogFactory.getLog(SynchronousReadCallback.class);

	TrendrrException exception;
	String stringResult;
	byte[] byteResult;
	Semaphore lock = new Semaphore(1, true);
	
	public SynchronousReadCallback() {
		try {
			//take the only semaphore
			lock.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void awaitResponse() {
		try {
			//try to aquire a semaphore, none is available so we wait.
			lock.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		this.exception = ex;
		this.lock.release();		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.StringReadCallback#stringResult(java.lang.String)
	 */
	@Override
	public void stringResult(String result) {
		this.stringResult = result;
		this.lock.release();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ByteReadCallback#byteResult(byte[])
	 */
	@Override
	public void byteResult(byte[] result) {
		this.byteResult = result;
		this.lock.release();
	}
}
