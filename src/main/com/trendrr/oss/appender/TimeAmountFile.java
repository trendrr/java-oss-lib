/**
 * 
 */
package com.trendrr.oss.appender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.FileHelper;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.TimeAmount;
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
	private FileWriter writer = null;
	private Date lastWrite = new Date();
	
	public TimeAmountFile(TimeAmount ta, long epoch, String dir, long maxBytes) throws Exception {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		this.epoch = epoch;
		this.timeAmount = ta;
		this.maxBytes = maxBytes;
		boolean exists = true;
		while(exists) {
			//guarentee uniqueness.
			String filename = dir + epoch +"_" + ta.abbreviation() + "__" + StringHelper.getRandomString(5);
			filename = FileHelper.toSystemDependantFilename(filename);
			FileHelper.createDirectories(filename);
			this.file = new File(filename);
			exists = !file.createNewFile();
		}
		this.writer = new FileWriter(this.file, true);
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
		this.epoch = TypeCast.cast(Long.class, tmp[0]);
		this.timeAmount = TimeAmount.instance(tmp[1]);
		this.file = file;
		this.writer = new FileWriter(this.file, true);
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
	 * Append to this file.  note that 
	 * @param str
	 * @throws FileClosedException
	 * @throws IOException
	 */
	public synchronized void append(String str) throws FileClosedException, IOException {
		if (stale) {
			throw new FileClosedException();
		}
		//check that we havent gone over the max bytes
		if (maxBytes > 0 && curBytes >= maxBytes) {
			this.setStale();
			throw new FileClosedException();
		}
		this.lastWrite = new Date();
		this.writer.append(str);
		this.writer.flush();
	}
	
	public synchronized void setStale() {
		stale = true;
		try {
			this.writer.flush();
			this.writer.close();
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
