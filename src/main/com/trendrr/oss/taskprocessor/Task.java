/**
 * 
 */
package com.trendrr.oss.taskprocessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;


/**
 * @author Dustin Norlander
 * @created Sep 24, 2012
 * 
 */
public class Task {

	protected static Log log = LogFactory.getLog(Task.class);
	protected static AtomicLong ids = new AtomicLong();
	
	public static enum ASYNCH {
		FAIL_ON_TIMEOUNT,
		CONTINUE_ON_TIMEOUT
	}
	
	//date when it was submitted to process.
	protected Date started = null;
	
	protected String id = Long.toString(ids.incrementAndGet());
	protected List<TaskFilter> filters = new LinkedList<TaskFilter>(); //these have not been run.
	protected List<TaskFilter> filtersExecuted = new LinkedList<TaskFilter>(); //these have been run.
	protected TaskProcessor processor;
	protected TaskCallback callback;
	
	public TaskCallback getCallback() {
		return callback;
	}

	public void setCallback(TaskCallback callback) {
		this.callback = callback;
	}

	/**
	 * is this task set as asynch?  if true this task is currently waiting for some asynch op to complete before it
	 * continues its execution.
	 * @return
	 */
	public boolean isAsynch() {
		return asynch;
	}

	public void setAsynch(boolean asynch) {
		this.asynch = asynch;
	}

	protected DynMap content = new DynMap();
	boolean asynch = false;
	
	public static Task instance(Class ...filterClasses) throws Exception {
		Task t = new Task();
		for (Class c : filterClasses) {
			t.addFilter(c);
		}
		return t;
	}
	
	public static Task instance(String ...filterClassNames) throws Exception {
		Task t = new Task();
		for (String c : filterClassNames) {
			t.addFilter(c);
		}
		return t;
	}
	
	public static Task instance(TaskFilter ...filters) {
		Task t = new Task();
		for (TaskFilter c : filters) {
			t.addFilter(c);
		}
		return t;
	}
	
	public String getId() {
		return this.id;
	}
	
	/**
	 * adds a filter by its full classname
	 * @param className
	 */
	public void addFilter(String className) throws Exception{
		TaskFilter f = Reflection.defaultInstance(TaskFilter.class, className);
		this.addFilter(f);
	}
	
	public void addFilter(Class cls) throws Exception {
		Object obj = Reflection.defaultInstance(cls);
		if (obj instanceof TaskFilter) {
			this.addFilter((TaskFilter)obj);
		} else {
			log.error("Class : " + cls + " is not a TaskFilter!");
		}
	}
	
	public void addFilter(TaskFilter filter) {
		this.filters.add(filter);
	}
	
	
	/**
	 * called when the task is first submitted. this is called when 
	 * getSubmitted == null  
	 * 
	 */
	public void submitted() {
		this.started = new Date();
	}
	
	/**
	 * returns the time when this task was submitted
	 */
	public Date getSubmitted() {
		return this.started;
	}
	
	/**
	 * This would be set in the TaskFilter.  sets this task to be asynchronous.
	 * 
	 * After the current filter returns execution of filters will stop until processor.resume(id) is called, or
	 * the timeout happens.
	 * @param timeout
	 */
	public void asynch(ASYNCH asynch, long timeout) {
		this.asynch = true;
		this.getProcessor().setAsynch(this, asynch, timeout);
	}
	
	public void resume() {
		//resume from asynch call. 
		this.getProcessor().resumeAsynch(this.getId());
	}
	
	/**
	 * returns the next filter to be run. it is moved from the 
	 * unexecuted to executed lists.
	 * @return
	 */
	public TaskFilter popFilter() {
		if (this.filters.isEmpty())
			return null;
		TaskFilter f = filters.remove(0);
		this.filtersExecuted.add(f);
		return f;
	}
	
	public List<TaskFilter> getFiltersExecuted() {
		return filtersExecuted;
	}
	
	public List<TaskFilter> getFiltersUnexecuted() {
		return filters;
	}

	public void setFiltersUnexecuted(List<TaskFilter> filters) {
		this.filters = filters;
	}

	public TaskProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(TaskProcessor processor) {
		this.processor = processor;
	}

	public DynMap getContent() {
		return content;
	}

	public void setContent(DynMap content) {
		this.content = content;
	}

	
}