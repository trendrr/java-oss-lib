/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.StringHelper;


/**
 * 
 * represents a parent point and all the immediate children
 * 
 * @author Dustin Norlander
 * @created Nov 7, 2011
 * 
 */
public class ExecutionReportChildPoints extends ExecutionReportPoint{

	protected Log log = LogFactory.getLog(ExecutionReportChildPoints.class);
	
	
	TreeMap<String, ExecutionReportPoint> points = new TreeMap<String,ExecutionReportPoint>();
	
	TreeSet<String> childKeys = new TreeSet<String>();
	public void setChildPoints(Collection<ExecutionReportPoint> points) {
		for (ExecutionReportPoint p : points) {
			this.addChildPoint(p);
		}
	}
	
	public void addChildPoint(ExecutionReportPoint p) {
		String k = this.getKey(p.getFullname());
		this.points.put(k, p);
		this.childKeys.add(k);
	}
	
	public ExecutionReportPoint getChildPoint(String name) {
		return points.get(this.getKey(name));
	}
	
	private String getKey(String key) {
		return  StringHelper.trim(key, this.getFullname() + ".");
	}
	
	/**
	 * returns the child keys in sorted order.
	 * @return
	 */
	public Collection<String> getChildKeys() {
		return this.childKeys;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n");
		for (ExecutionReportPoint p : this.points.values()) {
			str.append("+++++ ");
			str.append(p.toString());
			str.append("\n");
		}
		return str.toString();
		
	}
	
	/**
	 * converts to a map.  useful so we can cast to a dynmap if needed
	 * @return
	 */
	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> mp = super.toMap();
		List<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
		for (ExecutionReportPoint p : this.points.values()) {
			children.add(p.toMap());
		}
		mp.put("children", children);
		return mp;
	}
}
