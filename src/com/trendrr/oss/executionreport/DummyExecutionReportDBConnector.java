/**
 * 
 */
package com.trendrr.oss.executionreport;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Timeframe;


/**
 * @author Dustin Norlander
 * @created Sep 21, 2011
 * 
 */
public class DummyExecutionReportDBConnector extends
		AbstractExecutionReportDBConnector {

	protected Log log = LogFactory
			.getLog(DummyExecutionReportDBConnector.class);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.ExecutionReportConnector#save(com.trendrr.oss.executionreport.ExecutionReport, java.util.List)
	 */
	@Override
	public void save(ExecutionReport report, List<ExecutionReportPoint> points) {
		System.out.println("**** SAVING: " + report.getName());
		Date ts = new Date();
		for (ExecutionReportPoint p : points) {
			System.out.println(p);
		}
		System.out.println("**** DONE SAVING: " + report.getName());
	}
	

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#saveChildren(java.lang.String, java.util.Collection)
	 */
	@Override
	public void saveChildList(String parentFullname,
			Collection<String> childrenFullnames) {
		
		System.out.println("SAVING CHILDREN: " + parentFullname + " : " + childrenFullnames);

	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#findChildren(java.lang.String)
	 */
	@Override
	public List<String> findChildren(String parentFullname) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#inc(com.trendrr.oss.executionreport.ExecutionReportPointId, long, long)
	 */
	@Override
	protected void inc(ExecutionReportPointId id, long val, long millis) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.trendrr.oss.executionreport.AbstractExecutionReportDBConnector#load(java.util.List)
	 */
	@Override
	protected List<ExecutionReportPoint> load(List<ExecutionReportPointId> ids) {
		// TODO Auto-generated method stub
		return null;
	}
}
