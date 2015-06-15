/**
 * 
 */
package com.trendrr.oss;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrClosedException;
import com.trendrr.oss.exceptions.TrendrrInitializationException;
import com.trendrr.oss.exceptions.TrendrrTimeoutException;


/**
 * A simple threadsafe, fixed size, round robin pool of objects.
 * 
 * @author Dustin Norlander
 * @created Jul 26, 2013
 * 
 */
public class Pool<T> {

	protected static Log log = LogFactory.getLog(Pool.class);
	
	/**
	 * interface used to create new items in the pool.  This will be called lazily when a new item is needed
	 * @author Dustin Norlander
	 * @created Jul 26, 2013
	 *
	 */
	public static interface Creator<T> {
		
		/**
		 * creates a new object.  This should return a fully initialized object or an exception, never null.
		 * @return
		 * @throws Exception
		 */
		public T create() throws Exception;
		//clean up this object, it was returned as bad, or the pool is closed
		public void cleanup(T obj);
	}
	
	/**
	 * Internal class to handle refreshing a connection
	 * 
	 * 
	 * @author Dustin Norlander
	 * @created Jul 26, 2013
	 *
	 */
	private static class Refresher {
		Date lastLookup = null;
	}
	
	private Creator<T> creator;
	private LinkedBlockingQueue queue;
	private int size;
	
	/**
	 * The min amount of time before we retry to create a new object.
	 */
	private int minRefreshMillis = 1000*5;
	
	
	private ReentrantReadWriteLock closeLock = new ReentrantReadWriteLock();
	private boolean closed = false;
	
	
	
	public Pool(Creator<T> creator, int size) {
		this.creator = creator;
		this.queue = new LinkedBlockingQueue(size);
		for (int i=0; i < size; i++) {
			this.queue.offer(new Refresher());
		}
		this.size = size;
	}
	
	/**
	 * Gets the total pool size
	 * @return
	 */
	public int getSize() {
		return this.size;
	}
	
	public void close() {
		try {
			this.closeLock.writeLock().lock();
			if (this.closed) {
				return;
			}
			
			Object o = this.queue.poll();
			while(o != null) {
				if (!(o instanceof Refresher)) {
					T v = (T)o;
					this.creator.cleanup(v);
				}
				o = this.queue.poll();
			}
		} finally {
			this.closeLock.writeLock().unlock();
		}
	}
	
	/**
	 * Return an item to the pool
	 * @param obj
	 */
	public void returnGood(T obj) {
		try {
			this.closeLock.readLock().lock();
			
			if (this.closed) {
				this.creator.cleanup(obj);
				return;
			}
			this.queue.offer(obj);
			
		} finally {
			this.closeLock.readLock().unlock();
		}

	}
	
	public void returnBroken(T obj) {
		try {
			this.closeLock.readLock().lock();
			this.creator.cleanup(obj);
			if (this.closed) {
				return;
			}
			this.queue.offer(new Refresher());
		} finally {
			this.closeLock.readLock().unlock();
		}
		
	}
	
	public T borrow(long timeoutMillis) throws TrendrrTimeoutException, TrendrrInitializationException, TrendrrClosedException {
		try {
			this.closeLock.readLock().lock();
			if (this.closed) {
				throw new TrendrrClosedException("pool is closed, try the beach");
			}
			Object v = null;
			try {
				v = this.queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
			} catch (InterruptedException x) {
				//not exactly the right exception, but this case should be exceedingly rare.
				throw new TrendrrTimeoutException(x);
			}
			
			if (v == null) {
				throw new TrendrrTimeoutException("Pool error unable to get item within :" + timeoutMillis + " millis");
			}
			if (v instanceof Refresher) {
				//refresh the item
				Refresher r = (Refresher)v;
				
				if (r.lastLookup != null && 
						r.lastLookup.getTime() > new Date().getTime()-this.minRefreshMillis) {
					//do a little sleep and try again.
					long sleep = Math.min(timeoutMillis, this.minRefreshMillis);
					Sleep.millis(sleep);
					if (r.lastLookup.getTime() > new Date().getTime()-this.minRefreshMillis) {
						
						this.queue.offer(r);
						throw new TrendrrTimeoutException("Pool error unable to get item within :" + timeoutMillis + " millis");
					}
				} 
				//we should be good now to create a new object.
				try {
					T val = this.creator.create();
					return val;
				} catch (Exception x) {
					r.lastLookup = new Date();
					this.queue.offer(r);
					throw new TrendrrInitializationException(x);
				}
			}
			return (T)v;
		} finally {
			this.closeLock.readLock().unlock();
		}
	}	
}
