/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	protected Log log = LogFactory
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
		
		Date expire = this.getExpire(id.getTimeframe(), id.getTimestamp());
		
		try {
			this.cache.inc(this.namespace, id.toIdString() + ".val", TypeCast.cast(Integer.class, val), expire);
			this.cache.inc(this.namespace, id.toIdString() + ".millis", TypeCast.cast(Integer.class, millis), expire);
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
				String k1 = id.toIdString() + ".val";
				String k2 = id.toIdString() + ".millis";
				idMap.put(k1, id);
				idMap.put(k2, id);
			} catch (Exception e) {
				log.error("caught",e);
			}
		}
		Map<String, Object> vals = cache.getMulti(this.namespace, idMap.keySet());
		
		HashMap<ExecutionReportPointId, ExecutionReportPoint> points = new HashMap<ExecutionReportPointId, ExecutionReportPoint>();
		
		for (String id : vals.keySet()) {
			ExecutionReportPointId exId = idMap.get(id);
			ExecutionReportPoint point = points.get(exId);
			if (point == null) {
				point = new ExecutionReportPoint();
				points.put(exId, point);
				point.setId(exId);
			}
			
			if (id.endsWith("val")) {
				point.setVal(TypeCast.cast(Long.class, vals.get(id)));
			} else if (id.endsWith("millis")){
				point.setMillis(TypeCast.cast(Long.class, vals.get(id)));
			}
		}
		
		return new ArrayList<ExecutionReportPoint>(points.values());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#saveChildList(java.lang.String, java.util.Collection, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public void saveChildList(String parentFullname,
			Collection<String> childrenFullnames, Date date, Timeframe timeframe) {
		String id = "children-" + parentFullname + "-" + timeframe.toString() + "-" + timeframe.toTrendrrEpoch(date);
		this.cache.addToSet(this.namespace, id, childrenFullnames, this.getExpire(timeframe, date));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#findChildren(java.lang.String, java.util.Date, com.trendrr.oss.Timeframe)
	 */
	@Override
	public List<String> findChildren(String parentFullname, Date date,
			Timeframe timeframe) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Date getExpire(Timeframe frame, Date date) {
		Date expireMin = Timeframe.HOURS.add(date, 24);
		Date expire = frame.add(date, 25);
		if (expire.before(expireMin)) {
			expire = expireMin;
		}
		return expire;
	}
}
