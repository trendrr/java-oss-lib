/**
 * 
 */
package com.trendrr.oss.messaging.channel;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.trendrr.oss.DynMap;


/**
 * A request handler that envokes methods on the object.
 * 
 * @author Dustin Norlander
 * @created May 18, 2012
 * 
 */
public class ChannelMethodRequestHandler implements ChannelRequestHandler {

	protected static Log log = LogFactory.getLog(ChannelMethodRequestHandler.class);
	

	Object object;
	HashMap<String, Method> methods = new HashMap<String, Method>();
	
	/**
	 * this object contains all the methods you want to be able to execute. 
	 * 
	 * Be extremely careful with overloaded methods.  if overloading
	 * make sure to use different # of params.
	 * 
	 * @param object
	 */
	public ChannelMethodRequestHandler(Object object) {
		this.object = object;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.trendrr.topx2.channel.ChannelRequestHandler#handleRequest(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object handleRequest(String endpoint, Object... inputs)
			throws Exception {
		
		if (this.object == null) {
			throw new Exception("Object is null! (" + this + ") enpoint: " + endpoint + " inputs: " + inputs + "");
		}
		
		
		Method m = this.methods.get(endpoint+ "_" + inputs.length);
		if (m == null) {
			//search for the method to add to our map.
			for (Method method : this.object.getClass().getMethods()) {
				if (method.getName().equals(endpoint)) {
					if (method.getParameterTypes().length == inputs.length) {
						this.methods.put(endpoint+ "_" + inputs.length, method);
						m = method;
					}
				}
			}
		}
		if (m == null) {
			throw new Exception("Method: " + endpoint + " with " + inputs.length + " not found!");
		}
		return m.invoke(this.object, inputs);
	}
}
