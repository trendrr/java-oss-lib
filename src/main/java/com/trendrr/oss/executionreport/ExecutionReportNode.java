/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReportNode implements Comparable<ExecutionReportNode>{

	protected static Log log = LogFactory.getLog(ExecutionReportNode.class);
	
	protected ExecutionReportNode parent = null;
	protected Set<ExecutionReportNode> children = new HashSet<ExecutionReportNode>();
	protected String name = null;
	protected String fullname = null;
	protected long val = 0l;
	protected long millis = 0l;
	
	public ExecutionReportNode(ExecutionReportNode parent, String name) {
		this.parent = parent;
		this.name = name;
		this.init();
	}
	
	protected void init() {
		if (parent == null) {
			fullname = name;
		} else {
			fullname = parent.getFullname() + "." + name;
			parent.getChildren().add(this);
		}
	}
	
	public ExecutionReportNode getParent() {
		return parent;
	}

	public Set<ExecutionReportNode> getChildren() {
		return children;
	}

	public String getName() {
		return name;
	}

	public String getFullname() {
		return fullname;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExecutionReportNode o) {
		return this.fullname.compareTo(o.fullname);
	}
	
	public long getVal() {
		return val;
	}

	public void setVal(long val) {
		this.val = val;
	}

	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}
	public String toString() {
		return fullname + " | val:" + val + " | millis:" + millis ;
	}
}
