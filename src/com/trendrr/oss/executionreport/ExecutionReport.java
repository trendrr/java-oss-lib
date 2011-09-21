/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.IncrementMap;
import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReport extends TimerTask {

	protected Log log = LogFactory.getLog(ExecutionReport.class);
	
	protected String name;
	
	protected AtomicReference<ExecutionReportConfig> config = new AtomicReference<ExecutionReportConfig>();
	
	
	protected AtomicReference<Date> lastSerialization = new AtomicReference<Date>(new Date());
	private class Vals {
		IncrementMap vals = new IncrementMap();
		IncrementMap millis = new IncrementMap();
	}
	
	protected AtomicReference<Vals> vals = new AtomicReference<Vals>(new Vals());
	
	protected static AtomicReference<Timer> timer = new AtomicReference<Timer>();

	protected static AtomicReference<ExecutionReportConfig> defaultConfig = new AtomicReference<ExecutionReportConfig>(new ExecutionReportConfig());
	public static void setDefaultConfig(ExecutionReportConfig config) {
		defaultConfig.set(config);
	}
	public static ExecutionReportConfig getDefaultConfig() {
		return defaultConfig.get();
	}
	
	protected static AtomicReference<String> jvmInstanceId = new AtomicReference<String>();
	
	public static String getJvmInstanceId() {
		return jvmInstanceId.get();
	}

	/**
	 * This will add an additional level that allows you to view executions for specific jvms
	 * 
	 * This is null by default, but we suggest setting it to the IP address via
	 * 
	 * ExecutionReport.setJvmInstanceId(WhatsMyIp.getIP());
	 * 
	 * 
	 * @param jvmInstanceId
	 */
	public static void setJvmInstanceId(String jvmInstanceId) {
		ExecutionReport.jvmInstanceId.set(cleanup(jvmInstanceId));
	}

	
	
	protected static ConcurrentHashMap<String, ExecutionReport> reports = new ConcurrentHashMap<String, ExecutionReport>();
	
	public static ExecutionReport instance(String name) {
		return instance(name, null);
	}
	
	public static ExecutionReport instance(String name, ExecutionReportConfig config) {
		ExecutionReport report = reports.putIfAbsent(name, new ExecutionReport(name, config));
		if (report == null) {
			report = reports.get(name);
			report.start();
		}
		return report;
	}
	
	/**
	 * Create a new execution report
	 * @param name name of this top level report.
	 */
	protected ExecutionReport(String name, ExecutionReportConfig config) {
		this.name = cleanup(name);
		if (config != null) {
			this.config.set(config);
		} else {
			this.config.set(getDefaultConfig());
		}
	}
	
	/**
	 * cleans up a key.
	 * 
	 * will replace . with | and trim whitespace
	 * @param name
	 * @return
	 */
	public static String cleanup(String name) {
		return name.replace('.', '|').trim();
	}
	
	/**
	 * Increments the amount.  uses start to calculate the number of millis this execution took
	 * @param key
	 * @param amount
	 * @param start
	 */
	public void inc(String key, long amount, Date start) {
		if (start != null) {
			this.inc(key, amount, new Date().getTime() - start.getTime());
		} else {
			this.inc(key, amount, 0l);
		}
	}
	
	/**
	 * increments the val and millis
	 * @param key
	 * @param amount
	 * @param millis
	 */
	public void inc(String key, long amount, long millis) {
		Vals v = vals.get();
		v.vals.inc(key, amount);
		v.millis.inc(key, millis);
		
	}
	
	/**
	 * increments the value and uses 0 for the millis
	 * @param key
	 * @param amount
	 */
	public void inc(String key, long amount) {
		inc(key, amount, 0l);
	}
	
	/**
	 * increments the value by 1 and uses start to calculation the millis
	 * @param key
	 * @param start
	 */
	public void inc(String key, Date start) {
		inc(key, 1l, start);
	}
	
	public void inc(String key) {
		inc(key, 1l, 0l);
	}
	/**
	 * clears any values
	 */
	public void clear() {
		vals.set(new Vals());
	}
	
	/**
	 * this runs on the timer as long as flushMillis is set.
	 */
	public synchronized void flush() {

		try {
			Vals v = vals.getAndSet(new Vals());
			Date end = this.lastSerialization.get();
			
			ExecutionReportNodeTree tree = new ExecutionReportNodeTree(this.getName());
			
			for (String k : v.vals.keySet()) {
				Long val = v.vals.get(k);
				if (val == null)
					continue;
				Long millis = v.millis.get(k);
				if (millis == null)
					millis = 0l;
				tree.addNode(k, val, millis);
			}
	
			List<ExecutionReportPoint> points = new ArrayList<ExecutionReportPoint>();
			HashMap<String, Set<String>> children = new HashMap<String, Set<String>>();
			
			for (ExecutionReportNode node: tree.getNodes()) {
				//handle the children
				
				String parentFullname = node.getParent().getFullname();
				if (!children.containsKey(parentFullname)) {
					children.put(parentFullname, new TreeSet<String>());
				}
				children.get(parentFullname).add(node.getFullname());

				if (node.getMillis() == 0 && node.getVal() == 0) {
					continue ; //no data
				}
				
				{
					ExecutionReportPoint point = new ExecutionReportPoint();
					point.setTimestamp(end);
					point.setMillis(node.getMillis());
					point.setVal(node.getVal());
					point.setFullname(node.getFullname());
					points.add(point);
				}
				
				String instanceId = getJvmInstanceId();
				if (instanceId != null) {
					ExecutionReportPoint point = new ExecutionReportPoint();
					point.setTimestamp(end);
					point.setMillis(node.getMillis());
					point.setVal(node.getVal());
					point.setFullname(node.getFullname() + "." + instanceId);
					points.add(point);
					
					//need to add as a child.
					if (!children.containsKey(node.getFullname())) {
						children.put(node.getFullname(), new TreeSet<String>());
					}
					children.get(node.getFullname()).add(node.getFullname() + "." + instanceId);
				}
			}
			children.put("execution_reports", new TreeSet<String>());
			children.get("execution_reports").add(this.name);
			
			for (String p : children.keySet()) {
				this.getConfig().getSerializer().saveChildren(p, children.get(p));
			}
			this.getConfig().getSerializer().save(this, points);
		} catch (Exception x) {
			log.error("CAught", x);
		}
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		this.flush();
	}
	
	public void setConfig(ExecutionReportConfig config) {
		this.config.set(config);
	}
	/**
	 * gets the current config object, or the default if a custom one is not set.
	 * @return
	 */
	public ExecutionReportConfig getConfig() {
		ExecutionReportConfig config = this.config.get();
		if (config == null)
			return getDefaultConfig();
		return config;
	}

	
	/**
	 * starts the timer.  this is called automatically the first time the report is accessed.
	 * 
	 * If the report is already scheduled this will cancel it and restart. You should call this method if you wish to update the 
	 * flush millis in the config
	 * 
	 */
	public void start() {
		if (timer.get() == null) {
			timer.compareAndSet(null, new Timer(true));
		}
		
//		this.cancel(); //cancel in case this has been previously scheduled
		
		long millis = this.getConfig().getFlushMillis();
		if (millis < 1)
			return;
		timer.get().schedule(this, millis, millis);
	}
}
