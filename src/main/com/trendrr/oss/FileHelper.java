/**
 * 
 */
package com.trendrr.oss;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import java.util.zip.*;

/**
 * @author dustin
 *
 */
public class FileHelper {

	public static boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
	public static boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	public static boolean MAC = System.getProperty("os.name").toLowerCase().contains("mac");
	
	
	public static double bytesToGigs(long bytes) {
		return bytes / 1073741824l;
	}
	public static double bytesToMegs(long bytes) {
		return bytes / 1048576l;
	}
	
	/**
	 * converts from gigabytes to bytes
	 * @return
	 */
	public static long gigsToBytes(int gigs) {
		return gigs * 1073741824l;
	}
	
	public static long megsToBytes(int megs) {
		return megs * 1048576l;
	}
	
	/**
	 * Gets a filestream for the passed in filename.
	 * will try to get it from within a jar if available. 
	 * else will load relative to cwd.
	 * 
	 * @param filename
	 * @return
	 */
	public static InputStream fileStream(String filename) throws Exception {
		filename = toSystemDependantFilename(filename);
		//try to load it from the jar.	
		InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
    	if (stream == null) {
    		//else load from the filesystem.
    		stream = new FileInputStream(filename);
    	}
		return stream;
	}
	
	
	public static String toSystemDependantFilename(String filename) {
		return filename.replace('/', File.separatorChar);
	}
	
	public static String unzip(String filename,String zip_extension,String file_extension){
		FileInputStream instream;
		try {

			instream = new FileInputStream(filename + "." + file_extension + "." + zip_extension);
			GZIPInputStream ginstream =new GZIPInputStream(instream);

			FileOutputStream outstream = new FileOutputStream(filename + "." + file_extension);

			byte[] buf = new byte[1024];
			int len=0;

			while ((len = ginstream.read(buf)) > 0) 
			{

				outstream.write(buf, 0, len);
			}

			ginstream.close();
			outstream.close();
			return filename + "." + file_extension;
		}catch(EOFException e){
			System.out.println("File is ended");
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Errors");
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * zips the file and save as filename +".zip".
	 * 
	 * returns the filename of the newly created zip
	 * @param filename
	 */
	public static String zip(String filename) throws Exception{
		int BUFFER = 2048;
		String fname = toSystemDependantFilename(filename);
		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(fname + ".zip");
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		//out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[BUFFER];
		// get a list of files from current directory
		File in = new File(fname);
		FileInputStream fi = new FileInputStream(in);
		origin = new BufferedInputStream(fi, BUFFER);
		ZipEntry entry = new ZipEntry(in.getName());
		out.putNextEntry(entry);
		int count;
		while((count = origin.read(data, 0, 
				BUFFER)) != -1) {
			out.write(data, 0, count);
		}
		origin.close();

		out.close();
		return fname + ".zip";
	}
	
	/**
	 * cleans up any illegal characters for windows 
	 * @param filename
	 * @return
	 */
	public static String toWindowsFilename(String filename) {
		return StringHelper.removeAll(filename, ':', '\"', '|', '<', '>', '?', '*');
	}
	
	public static String getAbsoluteFilename(File file) {
		return file.getPath();
	}
	
	/**
	 * returns a list of the files within this directory
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static List<File> listDirectory(File dir, boolean recur) throws Exception {
		File[] fileList = dir.listFiles();
		List<File> files = new ArrayList<File>();
		if (fileList == null)
			return files;
		
		for (File file : fileList) {
			if (file.isDirectory()) {
				if (recur) {
					files.addAll(listDirectory(file, recur));
				}
			} else {
				files.add(file);
			}
		}
		return files;
	}
	
	/**
	 * same as listDirectory but only returns the simple filename (not complete path).
	 * @param dir
	 * @param recure
	 * @return
	 * @throws Exception
	 */
	public static List<String> listFilenames(String directory, boolean recur) throws Exception {
		List<File> files = listDirectory(new File(directory), recur);
		List<String> filenames = new ArrayList<String>();
	    
		for (File file : files) {
			String filename = file.getName();
			filenames.add(filename);
		}
	    return filenames;
	}
	
	/**
	 * returns filenames within this directory. only returns files, not directories
	 * @param directory
	 * @return
	 * @throws Exception
	 */
	public static List<String> listDirectory(String directory, boolean recur) throws Exception {
		
		if (directory == null || directory.isEmpty()) {
			directory = "./";
		}
		
		List<File> files = listDirectory(new File(directory), recur);
		List<String> filenames = new ArrayList<String>();
	    
		for (File file : files) {
			String filename = getAbsoluteFilename(file);
			filenames.add(filename);
		}
	    return filenames;
	}
	
	/**
	 * Creates directories if necessarry
	 * @param filename
	 */
	public static void createDirectories(String filename) throws Exception {
		if (filename.indexOf(File.separator) == -1) {
			return;
		}
		
		File file = new File(filename.substring(0, filename.lastIndexOf(File.separator)));
		if (!file.exists())
			file.mkdirs();
	}
	
	public static File createNewFile(String filename) throws Exception {
		filename = toSystemDependantFilename(filename);
		createDirectories(filename);
		File file = new File(filename);
		file.createNewFile();
		return file;
	}
	
	public static void saveBytes(String filename, byte[] bytes) throws Exception {
		File file = FileHelper.createNewFile(filename);
		FileOutputStream os = new FileOutputStream(filename);
		os.write(bytes);
		os.close();
	}
	
	public static void saveString(String filename, String text) throws Exception {
		saveBytes(filename, text.getBytes("utf-8"));
	}
	
	public static byte[] loadBytes(String filename) throws Exception {
		InputStream in = FileHelper.fileStream(filename);
		byte [] bytes = new byte[in.available()];
		in.read(bytes);
		in.close();
		return bytes;
	}
	
	public static String loadString(String filename) throws Exception {
		byte[] bytes = loadBytes(filename);
		return new String(bytes, "utf-8");
	}
	
	public static Properties loadProperties(String filename) throws Exception {
		InputStream filestream = null;
		try {
			filestream = fileStream(filename);
			Properties properties = new Properties();
	        properties.load(filestream);	
	        return properties;
		} catch (Exception x) {
			throw x;
		} finally {
			try {
				filestream.close();
			} catch (Exception x) {
				
			}
		}
	}
	
	/**
	 * loads a properties file as a dynmap.
	 * 
	 * will load the files in cascading order.  skipping any that result in null.
	 * 
	 * returns null on error.
	 * 
	 * @param filename
	 * @return
	 */
	public static DynMap loadPropertiesAsMap(String filename) {
		try {
			Properties prop = loadProperties(filename);
			DynMap rets = new DynMap();
			for (String key : prop.stringPropertyNames()) {
				rets.put(key, prop.getProperty(key));
			}
			return rets;
		} catch (Exception x) {
//			log.info("Caught", x);
		} 
		return null;
	}
}
