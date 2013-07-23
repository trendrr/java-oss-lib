/**
 * 
 */
package com.trendrr.oss.appender;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.FileHelper;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.appender.exceptions.FileClosedException;
import com.trendrr.oss.exceptions.TrendrrIOException;


/**
 * @author Dustin Norlander
 * @created Jun 17, 2013
 * 
 */
public class TimeAmountFile {

	protected static Log log = LogFactory.getLog(TimeAmountFile.class);
	
	private File file;
	private long epoch;
	private TimeAmount timeAmount;
	
	private boolean stale = false;
	private boolean callbackreturn = false;
	private long maxBytes;
	private long curBytes = 0;
	private boolean gzip = false;
	
	private OutputStream os = null;
	
//	private FileWriter writer = null;
	private Date lastWrite = new Date();
	
	public TimeAmountFile(TimeAmount ta, long epoch, String dir, long maxBytes, boolean gzip) throws Exception {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		this.epoch = epoch;
		this.timeAmount = ta;
		this.maxBytes = maxBytes;
		this.gzip = gzip;
		
		boolean exists = true;
		while(exists) {
			//guarentee uniqueness.
			//we use current millisecond in the hour + a 5 character random string
			//this should be sufficiently unique in most cases.
			Date start = ta.fromTrendrrEpoch(ta.toTrendrrEpoch(new Date()));
			long millis = new Date().getTime()-start.getTime();
			String filename = dir + epoch +"_" + ta.abbreviation() + "__" + millis + StringHelper.getRandomString(5);
			if (this.gzip) {
				filename += ".gz";
			}
			filename = FileHelper.toSystemDependantFilename(filename);
			FileHelper.createDirectories(filename);
			this.file = new File(filename);
			exists = !file.createNewFile();
		}
		this.os = new BufferedOutputStream(new FileOutputStream(file, true));
		
		if (this.gzip) {
			this.os = new GZIPOutputStream(this.os);
		}
	}
	
	/**
	 * Creates from previously created file.
	 * @param file
	 * @throws Exception
	 */
	public TimeAmountFile(File file, long maxBytes) throws Exception {
		String filename = file.getName();
		String tmp[] = filename.split("_");
		
		if (tmp.length < 3) {
			throw new TrendrrIOException("File : " + filename + " is not a TimeAmountFile");
		}
		
		if (filename.endsWith(".gz")) {
			this.gzip = true;
		}
		this.epoch = TypeCast.cast(Long.class, tmp[0]);
		this.timeAmount = TimeAmount.instance(tmp[1]);
		this.file = file;
		
		
		this.os = new BufferedOutputStream(new FileOutputStream(file, true));
		if (this.gzip) {
			this.os = new GZIPOutputStream(this.os);
		}
		
		
//		this.writer = new FileWriter(this.file, true);
		this.maxBytes = maxBytes;
	}
	
	
	/**
	 * stales the file.
	 * @param callback
	 * @return
	 */
	// Moved The delete code to callback level for deleting after the uploading
	protected synchronized void stale(TimeAmountFileCallback callback) {
		if (this.callbackreturn)
			return; //do nothing..
		this.callbackreturn = true;
		callback.staleFile(this);
	}
	
	/**
	 * Append to this file.  The string will be utf8 encoded.
	 * @param str
	 * @throws FileClosedException
	 * @throws IOException
	 */
	public synchronized void append(String str) throws FileClosedException, IOException {
		this.append(str.getBytes("utf8"));
	}
	
	/**
	 * Append to this file.  The string will be utf8 encoded.
	 * @param str
	 * @throws FileClosedException
	 * @throws IOException
	 */
	public synchronized void append(byte[] bytes) throws FileClosedException, IOException {
		if (stale) {
			throw new FileClosedException();
		}
		//check that we havent gone over the max bytes
		if (maxBytes > 0 && curBytes >= maxBytes) {
			
			this.setStale();
			throw new FileClosedException();
		}
		
		//Just count the chars for speed.  
		this.curBytes += bytes.length;
//		System.out.println(this.curBytes + " " + maxBytes);
		this.lastWrite = new Date();
		this.os.write(bytes);
		this.os.flush();
		
//		this.writer.append(str);
//		this.writer.flush();
	}
	
	
	public synchronized void setStale() {
		stale = true;
		try {
			this.os.flush();
			this.os.close();
//			
//			this.writer.flush();
//			this.writer.close();
		} catch (Exception x) {
			log.error("Caught", x);
		}
	}

	public synchronized File getFile() {
		return file;
	}

	public synchronized long getEpoch() {
		return epoch;
	}
	
	public synchronized TimeAmount getTimeAmount() {
		return timeAmount;
	}

	public synchronized long getMaxBytes() {
		return maxBytes;
	}

	public synchronized long getCurBytes() {
		return curBytes;
	}

	public synchronized Date getLastWrite() {
		return lastWrite;
	}
	

}
