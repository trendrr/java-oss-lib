/**
 * 
 */
package com.trendrr.oss.tests.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created May 18, 2012
 * 
 */
public class EchoClass {

	protected static Log log = LogFactory.getLog(EchoClass.class);
	
	
	public int stringLength(String input) {
		return input.length();
	}
	
	public List<Object> inputToList(Object in1, Object in2) {
		ArrayList<Object> retList = new ArrayList<Object>();
		retList.add(in1);
		retList.add(in2);
		return retList;
	}
}
