/**
 * 
 */
package com.trendrr.oss.appender;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.exceptions.TrendrrIOException;



/**
 * @author Dustin Norlander
 * @created Oct 3, 2011
 * 
 */
class RollingFileThread implements Runnable{

	protected Log log = LogFactory.getLog(RollingFileThread.class);
	
	public RollingFileThread(RollingFileAppender cacheAppender){
		this.appender = cacheAppender;
		this.queue = new LinkedBlockingQueue<AppendItem>();
	}
	
	private RollingFileAppender appender;
	private class AppendItem {
		Date d;
		String c;
		public AppendItem(Date d, String c) {
			this.d = d;
			this.c = c;
		}
	}
	
	private LinkedBlockingQueue<AppendItem> queue;
	
	private int maxQueueSize = 200;
	private AppendItem eofSignal = new AppendItem(null, "eof"); //we use a special queue item to let us know we are done. (Maybe slightly hacky, but it works)
	
	private LazyInit lazyInit = new LazyInit();
	
	private AtomicBoolean stopped = new AtomicBoolean(false);
	
	private Thread t = null;
	
	private void exception(Exception e) {
		RollingFileCallback cb = this.appender.getCallback();
		if (cb != null) {
			cb.onError(e);
		} else {
			log.error("CAught", e);
		}
	}
	
	@Override
	public void run() {
		while(true){
			if (this.stopped.get() && this.queue.isEmpty()) {
				return;
			}
			
			AppendItem next = null;
			try {
				next = this.queue.take();
			} catch (InterruptedException e) {
				if (this.stopped.get())
					return;
				this.exception(e);
				continue;
			}
			if (this.eofSignal.equals(next)) {
				return;
			}
			
			try {
				this.appender.doAppend(next.d, next.c);
			} catch (Exception e) {
				this.exception(e);
			}
		}

	}
	
	private void startThreadIfNeeded() {
		if (lazyInit.start()) {
			try {
				this.t = new Thread(this);
				this.t.setDaemon(true);
				this.t.start();
			} finally {
				lazyInit.end();
			}
		}
	}
	
	public void append(Date d, String s){
		if (this.stopped.get()) {
			Exception e = new TrendrrIOException("This file appendar is no longer active.  Dropping this task: \n\n" + s);
			this.exception(e);
			return;
		}
		
		if (this.queue.size() > this.maxQueueSize) {
			Exception e = new TrendrrIOException("Error file queue size is growing faster then I can write to disk.  Dropping this task: \n\n" + s);
			this.exception(e);
			return;
		}
		
		this.startThreadIfNeeded();
		try {
			if (d == null)
				d = new Date();
			this.queue.put(new AppendItem(d, s));
		} catch (InterruptedException e) {
			this.exception(e);
		}
	}
	
	public void close() {
		this.stopped.set(true);
		this.queue.add(this.eofSignal);
	}
}
