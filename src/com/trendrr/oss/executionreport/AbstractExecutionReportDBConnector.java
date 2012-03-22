/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.concurrent.PeriodicLock;


/**
 * 
 * An abstract ExecutionReportSerializer useful for implementing on Key Value type storage.
 * 
 * 
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public abstract class AbstractExecutionReportDBConnector implements
		ExecutionReportSerializer {

	protected static Log log = LogFactory
			.getLog(AbstractExecutionReportDBConnector.class);

		
	public AbstractExecutionReportDBConnector() {
//		timeframes.add(Timeframe.MINUTES);
//		timeframes.add(Timeframe.HOURS);
//		timeframes.add(Timeframe.DAYS);
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#save(com.trendrr.oss.executionreport.ExecutionReport, java.util.List)
	 */
	@Override
	public void save(ExecutionReport report, List<ExecutionReportPoint> points) {
		if (points == null || points.isEmpty()) {
			return;
		}
		System.out.println("**** SAVING: " + report.getName());
		Date ts = new Date();
		for (ExecutionReportPoint p : points) {
			System.out.println(p);
			Date t = p.getTimestamp();
			if (t == null) {
				t = ts;
			}
			for (Timeframe f : report.getConfig().getTimeframes()) {
				try {
					ExecutionReportPointId id = ExecutionReportPointId.instance(
							p.getFullname(), p.getTimestamp(), f);
					
					this.inc(id, p.getVal(), p.getMillis());
				} catch (Exception x) {
					log.error("Caught", x);
				}
			}
			
			
		}
		System.out.println("**** DONE SAVING: " + report.getName());
	}
	
	/**
	 * saves, database implementations should just override this method and can be satisfied.
	 * @param id
	 * @param val
	 * @param millis
	 */
	protected abstract void inc(ExecutionReportPointId id, long val, long millis);
	
	/**
	 * load points based on id.  any db implimentation should implement this method.
	 * @param ids
	 * @return
	 */
	protected abstract List<ExecutionReportPoint> load(List<ExecutionReportPointId> ids);
	
	protected int maxItems = 500; 
//	protected ConcurrentHashMap<String, Collection<String>> childrenCache = new ConcurrentHashMap<String,Collection<String>>();
	
	public abstract void saveChildList(String parentFullname,
			Collection<String> childrenFullnames,Date date, Timeframe timeframe);
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#saveChildren(java.lang.String, java.util.Collection)
	 */
	@Override
	public void saveChildren(String parentFullname,
			Collection<String> childrenFullnames, Date date, Timeframe timeframe) {
//		//just to avoid a leak
//		if (childrenCache.size() > this.maxItems) {
//			childrenCache.clear();
//		}
//		
//		Collection<String> cached = childrenCache.get(parentFullname);
//		if (cached != null) {
//			if (cached.size() == childrenFullnames.size() && cached.containsAll(childrenFullnames)) {
//				//collections are equal, no need to save
//				return;
//			}
//		}
//		this.childrenCache.put(parentFullname, childrenFullnames);
		this.saveChildList(parentFullname, childrenFullnames, date, timeframe);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#findChildren(java.lang.String)
	 */
	@Override
	public abstract List<String> findChildren(String parentFullname, Date date, Timeframe timeframe);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#load(java.lang.String, java.util.Date, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public List<ExecutionReportPoint> load(String fullname, Date start,
			Date end, Timeframe timeframe) {
		int startTE = timeframe.toTrendrrEpoch(start).intValue();
		int endTE = timeframe.toTrendrrEpoch(end).intValue();
		List<ExecutionReportPointId> ids = new ArrayList<ExecutionReportPointId> ();
		for (int te = startTE; te <= endTE; te++) {
			Date timestamp = timeframe.fromTrendrrEpoch(te);
			try {
				ExecutionReportPointId id = ExecutionReportPointId.instance(fullname, timestamp, timeframe);
				ids.add(id);
			} catch (Exception x) {
				log.error("Caught", x);
			}
			
		}
		return this.load(ids);
	}
	
	@Override
	public List<ExecutionReportChildPoints> loadChildren(String fullname, Date start, Date end, Timeframe timeframe){
		try {
			int startTE = timeframe.toTrendrrEpoch(start).intValue();
			int endTE = timeframe.toTrendrrEpoch(end).intValue();
			
			//load the parents.
			HashMap<String, ExecutionReportChildPoints> pointsByParentId = new HashMap<String, ExecutionReportChildPoints>();
			HashMap<String, ExecutionReportChildPoints> pointsByChildId = new HashMap<String, ExecutionReportChildPoints>();
			TreeSet<String> childrenNames = new TreeSet<String>();
			childrenNames.addAll(this.findChildren(fullname, end, timeframe));
			childrenNames.addAll(this.findChildren(fullname, start, timeframe));
			
			
			List<ExecutionReportPointId> ids = new ArrayList<ExecutionReportPointId> ();
			for (int te = startTE; te <= endTE; te++) {
				Date timestamp = timeframe.fromTrendrrEpoch(te);
				try {
					ExecutionReportPointId id = ExecutionReportPointId.instance(fullname, timestamp, timeframe);
					ids.add(id);
					ExecutionReportChildPoints point = new ExecutionReportChildPoints();
					pointsByParentId.put(id.toIdString(), point);
					for (String n : childrenNames) {
						ExecutionReportPointId cId = ExecutionReportPointId.instance(n, timestamp, timeframe);
						ids.add(cId);
						pointsByChildId.put(cId.toIdString(), point);
					}
				} catch (Exception x) {
					log.error("Caught", x);
				}
				
			}
			
			
			List<ExecutionReportPoint> points = this.load(ids);
			for (ExecutionReportPoint p : points) {
				String id = p.getId().toIdString();
				ExecutionReportChildPoints ps = pointsByParentId.get(id);
				if (ps != null) {
					ps.setFullname(p.getFullname());
					ps.setId(p.getId());
					ps.setMillis(p.getMillis());
					ps.setTimestamp(p.getTimestamp());
					ps.setVal(p.getVal());
				} else {
					pointsByChildId.get(id).addChildPoint(p);
				}
			}
			
			List<ExecutionReportChildPoints> pts = new ArrayList<ExecutionReportChildPoints>();
			pts.addAll(pointsByParentId.values());
			return pts;
		} catch (Exception x) {
			log.error("Caught", x);
		}
		return new ArrayList<ExecutionReportChildPoints>();
	}
	
}
