/**
 * 
 */
package com.trendrr.oss.appender;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.appender.exceptions.FileClosedException;
import com.trendrr.oss.exceptions.TrendrrIOException;


/**
 * This is a slightly different implementation then RollingFileAppender.
 * 
 * It is not meant to keep a rolling number of files available, but instead
 * is meant to write to time based files, then it calls a callback once those files are
 * considered stale (have not been written to in 5 minutes).  
 * 
 * This is useful for backing up event based items into a secondary storage where items need to be grouped
 * by time, but they might not arrive in cronological order.
 * 
 * methodology:
 * 
 * 1. files get uploaded if they have not been written to for five minutes
 * 2. files have a maxbytes param, will upload once that file size is achieved.
 * 3. filename in the form {epoch}_{timeamount}__{randomstring}
 * 
 * @author Dustin Norlander
 * @created Jun 17, 2013
 * 
 */
public class TimeAmountFileAppender {

	protected static Log log = LogFactory.getLog(TimeAmountFileAppender.class);
	
	TimeAmount amount;
	
	LoadingCache<Long, TimeAmountFile> cache;
	
	
	/**
	 * If the timeamount of the file has not been updated in 
	 * this amount of time, we stale the file.
	 */
	TimeAmount staleFileCheck = TimeAmount.instance(Timeframe.MINUTES, 5);
	
	TimeAmountFileCallback callback;
	
	long maxBytes;
	String directory;
	
	public TimeAmountFileAppender(TimeAmountFileCallback callback, TimeAmount amount, TimeAmount staleFileCheck, String dir, long maxBytes) {
		this.amount = amount;
		this.callback = callback;
		this.directory = dir;
		this.maxBytes = maxBytes;
		
		this.cache = CacheBuilder.newBuilder()
	       .maximumSize(1000)
	       .expireAfterWrite(staleFileCheck.getAmount(), staleFileCheck.getTimeframe().getTimeUnit())
	       .removalListener(
	    		   new RemovalListener<Long, TimeAmountFile>() {
						@Override
						public void onRemoval(RemovalNotification<Long, TimeAmountFile> rn) {
							staleFile(rn.getValue());
						}
	    		   })
	       .build(
	    		   new CacheLoader<Long, TimeAmountFile>() {
	    			   public TimeAmountFile load(Long epoch) throws Exception {
	    				   System.out.println("LOADIGN EPOCH: " + epoch);
	    				   return newFile(epoch);
	    			   }
	    		   });
		
		//now load any files already in the directory.
		File f[] = new File(dir).listFiles();
		if (f != null) {
			for (File file : f) {
				try {
					TimeAmountFile taf = new TimeAmountFile(file, maxBytes);
					this.cache.put(taf.getEpoch(), taf);
				} catch (Exception x) {
					log.error("Caught", x);
				}
			}
		}	
	}
	
	protected void staleFile(TimeAmountFile f) {
		f.stale(callback);
	}

	protected TimeAmountFile newFile(Long epoch) throws Exception {
		return new TimeAmountFile(this.amount, epoch, this.directory, this.maxBytes);
	}
	
	public void appendLine(Date date, String str) throws Exception {
		this.append(new Date(), str + "\n");
	}
		
	public synchronized void append(Date date, String str) throws Exception {
		long epoch = this.amount.toTrendrrEpoch(date).longValue();
		//Try to get the file max 5 times.
		for (int i=0; i < 5; i++) {
			TimeAmountFile file = null;
			try {
				file = this.cache.get(epoch);
				file.append(str);
				return;
			} catch (FileClosedException x) {
				this.staleFile(file);
				this.cache.invalidate(epoch);
				//try again..
			}
		}
		throw new TrendrrIOException("Unable to get file for epoch: " + epoch);
	}
}
