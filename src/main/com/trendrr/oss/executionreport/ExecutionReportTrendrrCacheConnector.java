/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.TimeAmount;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.TypeCast;
import com.trendrr.oss.cache.TrendrrCache;


/**
 * A connector for a TrendrrCache 
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 31, 2012
 * 
 */
public class ExecutionReportTrendrrCacheConnector extends
		AbstractExecutionReportDBConnector {

	protected static Log log = LogFactory
			.getLog(ExecutionReportTrendrrCacheConnector.class);
	
	private TrendrrCache cache = null;
	private String namespace = "execution_report";
	
	public ExecutionReportTrendrrCacheConnector(TrendrrCache cache) {
		this.cache = cache;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#inc(com.trendrr.oss.executionreport.ExecutionReportPointId, long, long)
	 */
	@Override
	protected void inc(ExecutionReportPointId id, long val, long millis) {
		
		Date expire = this.getExpire(id.getTimeAmount(), id.getTimestamp());
		
		try {
			System.out.println("SAVIGN: " + id.toString() + ".val");
			System.out.println("SAVIGN: " + id.toString() + ".millis");
			
			this.cache.inc(this.namespace, id.toString() + ".val", TypeCast.cast(Integer.class, val), expire);
			this.cache.inc(this.namespace, id.toString() + ".millis", TypeCast.cast(Integer.class, millis), expire);
		} catch (Exception e) {
			log.error("caught", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#load(java.util.List)
	 */
	@Override
	protected List<ExecutionReportPoint> load(List<ExecutionReportPointId> ids) {
		
		HashMap<String, ExecutionReportPointId> idMap = new HashMap<String,ExecutionReportPointId>();

		for (ExecutionReportPointId id : ids) {
			try {
				String k1 = id.toString() + ".val";
				String k2 = id.toString() + ".millis";
				idMap.put(k1, id);
				idMap.put(k2, id);
				System.out.println(k1 + "\n" + k2);
			} catch (Exception e) {
				log.error("caught",e);
			}
		}
		Map<String, Object> vals = cache.getMulti(this.namespace, idMap.keySet());
		System.out.println("LOADED: " + vals);
		HashMap<ExecutionReportPointId, ExecutionReportPoint> points = new HashMap<ExecutionReportPointId, ExecutionReportPoint>();
		
		for (String id : idMap.keySet()) {
//			System.out.println("GOT ID: " + id);
			ExecutionReportPointId exId = idMap.get(id);
			ExecutionReportPoint point = points.get(exId);
			if (point == null) {
				point = new ExecutionReportPoint();
				point.setId(exId);
				points.put(exId, point);
			}
//			System.out.println("GOT POINT: " + point.toString());
			if (id.endsWith("val")) {
				point.setVal(TypeCast.cast(Long.class, vals.get(id), 0l));
			} else if (id.endsWith("millis")){
				point.setMillis(TypeCast.cast(Long.class, vals.get(id), 0l));
			}
//			System.out.println("AFTER VAL SET: " + point.toString());
			
		}
		
		return new ArrayList<ExecutionReportPoint>(points.values());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#saveChildList(java.lang.String, java.util.Collection, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public void saveChildList(String parentFullname,
			Collection<String> childrenFullnames, Date date, TimeAmount timeamount) {
		String id = "children-" + parentFullname + "-" + timeamount.abbreviation() + "-" + timeamount.toTrendrrEpoch(date);
		System.out.println("SAVING CHILDREN: " + id + "\n" + childrenFullnames);
		
		this.cache.addToSet(this.namespace, id, childrenFullnames, this.getExpire(timeamount, date));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#findChildren(java.lang.String, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public List<String> findChildren(String parentFullname, Date date,
			TimeAmount timeframe) {
		String id = "children-" + parentFullname + "-" + timeframe.abbreviation() + "-" + timeframe.toTrendrrEpoch(date);
		System.out.println("LOADING CHILDREN: " + id);
		Set<String> res = this.cache.getSet(this.namespace, id);
		ArrayList<String> children = new ArrayList<String>();
		if (res != null) {
			children.addAll(res);
			Collections.sort(children);
		}
		return children;
	}
	
	protected Date getExpire(TimeAmount frame, Date date) {
		Date expireMin = Timeframe.HOURS.add(date, 24);
		Date expire = frame.add(date, 25);
		if (expire.before(expireMin)) {
			expire = expireMin;
		}
		return expire;
	}
}
