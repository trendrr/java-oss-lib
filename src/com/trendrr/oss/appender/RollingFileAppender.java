/**
 * 
 */
package com.trendrr.oss.appender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.FileHelper;
import com.trendrr.oss.StringHelper;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.concurrent.Sleep;


/**
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created Aug 26, 2011
 * 
 */
public class RollingFileAppender {

	protected Log log = LogFactory.getLog(RollingFileAppender.class);
	
	protected String filename = null;
	protected String fileExtension = "";
	
	protected Timeframe timeframe = null;
	protected Integer timeframeAmount = 1;
	
	protected int maxFiles = 10;
	
	protected File current = null;
	protected Long currentTE = null;
	protected String currentFilename = null;
	protected FileWriter writer = null;
	protected LazyInit init = new LazyInit();
	
	protected AtomicReference<RollingFileCallback> callback = new AtomicReference<RollingFileCallback>();
	protected AtomicReference<Date> date = new AtomicReference<Date>();
	

	protected RollingFileThread thread = null;
	
	protected static boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
	
	public static void main(String ...str) throws Exception {
		RollingFileAppender appender = new RollingFileAppender(Timeframe.SECONDS, 10, 10, "/home/dustin/Desktop/appenderFiles/testing.log", null, true);
		appender.init();
		
		Date start = new Date();
		for (int i=0; i < 1000000; i++) {
			appender.append(i + " THIS IS SOMETHING!\n");
		}
		System.out.println("Wrote 1 million in " + (new Date().getTime() - start.getTime()));
	}
	/**
	 * creates a new appender with no callback and threaded.
	 * 
	 * @param timeframe
	 * @param timeframeAmount
	 * @param maxFiles
	 * @param filename
	 */
	public RollingFileAppender(Timeframe timeframe, int timeframeAmount, int maxFiles, String filename) {
		this(timeframe, timeframeAmount, maxFiles, filename, null, false);
	}
	
	/**
	 * Creates a new rolling file appender
	 * 
	 * @param timeframe Minute, hours, days, ect
	 * @param timeframeAmount how many {timeframe} to keep in one file (ex: 6 HOURS of data per file)
	 * @param maxFiles how many files to store before deleting the old ones.
	 * @param filename The filename to save as (the timestamp is appended to the end). suggestion: my_log.log
	 * @param callback If you want to do additional processing on the log files on creation, deletion or rollover.
	 * @param threaded This will queue writes in a separate thread, so you're app will not be slowed down by the appender.  if the io gets severly behind you will get exceptions sent to your callback (or logged if no callback is supplied)
	 */
	public RollingFileAppender(Timeframe timeframe, int timeframeAmount, int maxFiles, String filename, RollingFileCallback callback, boolean threaded) {
		this.timeframe = timeframe;
		this.timeframeAmount = timeframeAmount;
		this.maxFiles = maxFiles;
		this.filename = FileHelper.toSystemDependantFilename(filename);
		this.callback.set(callback);
		
		//get the file extension
		int ind = this.filename.lastIndexOf('.');
		if (ind != -1) {
			this.fileExtension = this.filename.substring(ind);
			this.filename = this.filename.substring(0, ind);
		}
		
	}
	protected long toTE(Date date) {
		if (this.timeframeAmount == 1)
			this.timeframe.toTrendrrEpoch(date);

		long te = this.timeframe.toTrendrrEpoch(date).longValue();
		return ((long)(te/this.timeframeAmount)) * this.timeframeAmount;
	}
	
	protected String toFilename(long te) {
		return filename + "__" + te + this.fileExtension;
	}
	
	protected long minTE() {
		System.out.println(this.currentTE);
		return ((long)((this.currentTE - (this.maxFiles*this.timeframeAmount))/this.timeframeAmount)) * this.timeframeAmount;
	}
	
	/**
	 * initializes the appender.  This will be called automatically on the first invocation of append. 
	 * 
	 * Subsequent calls to init are simply ignored.
	 */
	public void init() {
		if (this.init.start()) {
			try {
				//cleans up any old files, and creates the new one.
				
				String directory = System.getProperty("user.dir");
				if (filename.contains(File.separator)) {
					directory = filename.substring(0, filename.lastIndexOf(File.separator));
					if (!directory.startsWith(File.separator)) {
						directory = System.getProperty("user.dir") + File.separator + directory;
					}
				}
				this.currentTE = this.toTE(getDate());
				for (File f : FileHelper.listDirectory(new File(directory),false)) {
					
					String fn = f.getAbsolutePath();
					if (!fn.endsWith(this.fileExtension) || !fn.startsWith(this.filename)) {
						continue;
					}
					
					String tmp = fn; 
					
					tmp = StringHelper.trim(tmp, this.fileExtension);
					if (!tmp.contains("__")) {
						continue;
					}
					
					tmp = tmp.replace(this.filename + "__", ""); 
					
					long te = TypeCast.cast(Long.class, tmp);
					if (te < this.minTE()) {
						System.out.println("Current TE: " + this.currentTE + " min:  " + this.minTE());
						System.out.println("DELETE: " + fn);
						this.delete(f);
					}
				}
				this.newFile();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				init.end();
			}
		}
	}
	
	protected void delete(File f) {
		RollingFileCallback cb = this.getCallback();
		
		if (cb != null) {
			cb.beforeDelete(f.getAbsolutePath());
		}
		f.delete();
	}
	/**
	 * gets the current file
	 * @return
	 * @throws Exception 
	 */
	public synchronized File getFile() throws Exception {
		this.init();
		if (this.toTE(getDate()) != this.currentTE) {
			this.newFile();
		} 
		return current;
	}
	
	private void newFile() throws Exception {
		
		String oldFilename = this.currentFilename;
		
		this.currentTE = this.toTE(getDate());
		
		this.currentFilename = this.toFilename(this.currentTE);
		this.current = FileHelper.createNewFile(this.currentFilename);
		if (this.writer != null) {
			this.writer.close();
		}
		this.writer = new FileWriter(this.current);
		this.delete(new File(this.toFilename(this.minTE())));
		
		if (LINUX) {
			//add link to most recent file.
			try {
				new File(this.filename + this.fileExtension).delete();
			} catch (Exception x) {
				log.error("Caught", x);
			}
			try {
				Runtime rt=Runtime.getRuntime();
				Process result=null;
				String exe=new String("ln"+" "+this.currentFilename+" "+this.filename + this.fileExtension);
				result=rt.exec(exe);
			} catch (Exception x) {
				log.error("Caught", x);
			}
		}
		
		RollingFileCallback cb = this.getCallback();
		
		if (cb != null) {
			cb.onRollover(oldFilename, this.currentFilename);
		}
	}
	
	public synchronized void append(String str) throws Exception {
		this.append(new Date(), str);
	}
	
	public synchronized void append(Date date, String str) throws Exception {
		if (str == null)
			return;
		if (this.thread != null) {
			this.thread.append(date, str);
		} else {
			this.doAppend(date, str);
		}
	}
	
	void doAppend(Date date, String str) throws Exception {
		this.date.set(date);
		this.getFile();
		this.writer.append(str);
		this.writer.flush();
	}
	
	public RollingFileCallback getCallback() {
		return callback.get();
	}
	public void setCallback(RollingFileCallback callback) {
		this.callback.set(callback);
	}
	
	/**
	 * gets the date of the most recent append, or NOW
	 * @return
	 */
	protected Date getDate() {
		Date d = this.date.get();
		if (d == null)
			return new Date();
		return d;
	}
}
