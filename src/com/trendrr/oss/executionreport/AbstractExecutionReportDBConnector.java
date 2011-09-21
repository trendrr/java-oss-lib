/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.concurrent.PeriodicLock;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public abstract class AbstractExecutionReportDBConnector implements
		ExecutionReportSerializer {

	protected Log log = LogFactory
			.getLog(AbstractExecutionReportDBConnector.class);

	protected List<Timeframe> timeframes = new ArrayList<Timeframe>();
	
	public AbstractExecutionReportDBConnector() {
		timeframes.add(Timeframe.MINUTES);
		timeframes.add(Timeframe.HOURS);
		timeframes.add(Timeframe.DAYS);
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#save(com.trendrr.oss.executionreport.ExecutionReport, java.util.List)
	 */
	@Override
	public void save(ExecutionReport report, List<ExecutionReportPoint> points) {
		System.out.println("**** SAVING: " + report.getName());
		Date ts = new Date();
		for (ExecutionReportPoint p : points) {
			System.out.println(p);
			Date t = p.getTimestamp();
			if (t == null) {
				t = ts;
			}
			for (Timeframe f : this.timeframes) {
				try {
					byte[] id = this.toId(f, t, p.getFullname());
					this.inc(id, f, t, p.getVal(), p.getMillis());
				} catch (Exception x) {
					log.error("Caught", x);
				}
			}
			
			
		}
		System.out.println("**** DONE SAVING: " + report.getName());
	}

	protected byte[] toId(Timeframe frame, Date timestamp, String key) throws Exception {
		StringBuilder id = new StringBuilder();
		id.append(frame.toTrendrrEpoch(timestamp));
		id.append("::");
		id.append(frame);
		id.append("::");
		id.append(key);
		return StringHelper.sha1(id.toString().getBytes("utf8"));
	}
	
	/**
	 * saves, database implementations should just override this method and can be satisfied.
	 * @param id
	 * @param val
	 * @param millis
	 */
	protected abstract void inc(byte[] id, Timeframe frame, Date timestamp, long val, long millis);
	
	/**
	 * load points based on id.  any db implimentation should implement this method.
	 * @param ids
	 * @return
	 */
	protected abstract List<ExecutionReportPoint> load(List<byte[]> ids);
	
	protected int maxItems = 500; 
	protected ConcurrentHashMap<String, Collection<String>> childrenCache = new ConcurrentHashMap<String,Collection<String>>();
	
	public abstract void saveChildList(String parentFullname,
			Collection<String> childrenFullnames);
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#saveChildren(java.lang.String, java.util.Collection)
	 */
	@Override
	public void saveChildren(String parentFullname,
			Collection<String> childrenFullnames) {
		//just to avoid a leak
		if (childrenCache.size() > this.maxItems) {
			childrenCache.clear();
		}
		
		Collection<String> cached = childrenCache.get(parentFullname);
		if (cached != null) {
			if (cached.size() == childrenFullnames.size() && cached.containsAll(childrenFullnames)) {
				//collections are equal, no need to save
				return;
			}
		}
		this.childrenCache.put(parentFullname, childrenFullnames);
		this.saveChildList(parentFullname, childrenFullnames);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#findChildren(java.lang.String)
	 */
	@Override
	public abstract List<String> findChildren(String parentFullname);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#load(java.lang.String, java.util.Date, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public List<ExecutionReportPoint> load(String fullname, Date start,
			Date end, Timeframe timeframe) {
		int startTE = timeframe.toTrendrrEpoch(start).intValue();
		int endTE = timeframe.toTrendrrEpoch(end).intValue();
		List<byte[]> ids = new ArrayList<byte[]> ();
		for (int te = startTE; te <= endTE; te++) {
			Date timestamp = timeframe.fromTrendrrEpoch(te);
			try {
				byte[] id = this.toId(timeframe, timestamp, fullname);
				ids.add(id);
			} catch (Exception x) {
				log.error("Caught", x);
			}
			
		}
		return this.load(ids);
	}
}
