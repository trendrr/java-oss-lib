/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Sep 20, 2011
 * 
 */
public class ExecutionReportNodeTree {

	protected Log log = LogFactory.getLog(ExecutionReportNodeTree.class);
	
	protected HashMap<String, ExecutionReportNode> nodes = new HashMap<String, ExecutionReportNode>();
	protected ExecutionReportNode parent = null;
	
	public static void main(String ...nodes) {
		ExecutionReportNodeTree tree = new ExecutionReportNodeTree("exe");
		tree.addNode("parent.child1.child2.child3", 0, 0);
		System.out.println(tree);
	}
	public ExecutionReportNodeTree(String reportName) {
		parent = new ExecutionReportNode(null, reportName);
	}
	public ExecutionReportNode addNode(String fullname, long val, long millis) {
		ExecutionReportNode me = null;
		int ind = fullname.lastIndexOf('.');
		if (ind == -1) {
			me = new ExecutionReportNode(this.parent, fullname);
		} else {
			String parentname = fullname.substring(0, ind);
			String name = fullname.substring(ind+1);
			me = new ExecutionReportNode(this.addNode(parentname, 0l, 0l), name);
		}
		if (!nodes.containsKey(me.getFullname())) {
			nodes.put(me.getFullname(), me);
		}
		me = nodes.get(me.getFullname());
		
		if (val != 0 || millis != 0) {
			me.setMillis(millis);
			me.setVal(val);
		}
		
		return me;
	}
	
	public Collection<ExecutionReportNode> getNodes() {
		return nodes.values();
	}
	
	public String toString() {
		return nodes.keySet().toString();
	}
}
