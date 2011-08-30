/**
 * 
 */
package com.trendrr.oss;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	String filename = null;
	String fileExtension = "";
	
	Timeframe timeframe = null;
	Integer timeframeAmount = 1;
	
	int maxFiles = 10;
	
	File current = null;
	Long currentTE = null;
	FileWriter writer = null;
	LazyInit init = new LazyInit();
	
	public static void main(String ...str) throws Exception {
		RollingFileAppender appender = new RollingFileAppender(Timeframe.SECONDS, 10, 10, "/home/dustin/Desktop/appenderFiles/testing.log");
		appender.init();
		
		Date start = new Date();
		for (int i=0; i < 1000000; i++) {
			appender.append(i + " THIS IS SOMETHING!\n");
			Sleep.seconds(1);
		}
		System.out.println("Wrote in " + (new Date().getTime() - start.getTime()));
	}
	
	public RollingFileAppender(Timeframe timeframe, int timeframeAmount, int maxFiles, String filename) {
		this.timeframe = timeframe;
		this.timeframeAmount = timeframeAmount;
		this.maxFiles = maxFiles;
		this.filename = FileHelper.toSystemDependantFilename(filename);
		
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
				String directory = filename.substring(0, filename.lastIndexOf(File.separator));
				this.newFile();
				
				System.out.println(directory);
				for (File f : FileHelper.listDirectory(new File(directory),false)) {
					
					String fn = f.getAbsolutePath();
					if (!fn.endsWith(this.fileExtension) || !fn.startsWith(this.filename)) {
						continue;
					}
					
					String tmp = fn; 
					
					tmp = StringHelper.trim(tmp, this.fileExtension);
					tmp = tmp.replace(this.filename + "__", ""); 
					
					long te = TypeCast.cast(Long.class, tmp);
					if (te < this.minTE()) {
						System.out.println("Current TE: " + this.currentTE + " min:  " + this.minTE());
						System.out.println("DELETE: " + fn);
						f.delete();
					}
					
					
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				init.end();
			}
		}
	}
	
	
	/**
	 * gets the current file
	 * @return
	 * @throws Exception 
	 */
	public synchronized File getFile() throws Exception {
		this.init();
		if (this.toTE(new Date()) != this.currentTE) {
			this.newFile();
		} 
		return current;
	}
	
	private void newFile() throws Exception {
		this.currentTE = this.toTE(new Date());
		this.current = FileHelper.createNewFile(this.toFilename(this.currentTE));
		if (this.writer != null) {
			this.writer.close();
		}
		this.writer = new FileWriter(this.current);
		
		new File(this.toFilename(this.minTE())).delete();
	}
	
	public synchronized void append(String str) throws Exception {
		if (str == null)
			return;
		this.getFile();
		this.writer.append(str);
		this.writer.flush();
	}
	
	/**
	 * deletes any old 
	 */
	public void cleanup() {
		
		
	}
}
