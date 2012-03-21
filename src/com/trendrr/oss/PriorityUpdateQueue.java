/**
 * 
 */
package com.trendrr.oss;

import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * A priority queue with an efficient 'update' function. 
 * 
 * update from code posted: http://stackoverflow.com/questions/714796/priorityqueue-heap-update
 * 
 * Synchronized.
 * 
 * @author Dustin Norlander
 * @created Feb 15, 2012
 * 
 */
public class PriorityUpdateQueue <T>{

	protected Log log = LogFactory.getLog(PriorityUpdateQueue.class);
	private List<T> heap = new ArrayList<T>();

	
	/**
	 * TODO: this implementation is broken for T is a Map since hashcode for that 
	 * is apparently contingent on the mappings in the map..
	 * investigate..
	 */

	private HashMap<T, Integer> indexes = new HashMap<T, Integer>();
	
	private Comparator<T> comparator;

	/**
	 * Pass in an appropriate comparator.  
	 * @param comparator
	 */
    public PriorityUpdateQueue(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public synchronized int getSize() {
    	return heap.size();
    }
    
    /**
     * drains and discards elements until the size of the heap is <= size
     * @param size
     */
    public void drainToSize(int size) {
    	while(this.size() > size) {
    		this.pop();
    	}
    }
    
    /**
     * push an item into the queue.
     * 
     * note that you can NOT add the same element twice.  Sameness meaning exactly the same reference. Same by comparable works fine.
     * @param obj
     */
    public synchronized void push(T obj) {
    	if (this.indexes.containsKey(obj)) {
    		log.warn("Already contains: " + obj);
    		return;
    	}
    	
        heap.add(obj);
        indexes.put(obj, heap.size()-1);
        pushUp(heap.size()-1);
    }

    /**
     * pops the top of the heap and returns it. returns null on empty.
     * @return
     */
    public synchronized T pop() {
        if (heap.size() > 0) {
            swap(0, heap.size()-1);
            T result = heap.remove(heap.size()-1);
            this.indexes.remove(result);
            pushDownByIndex(0);
            return result;
        } else {
            return null;
        }
    }

    /**
     * returns the head of the queue without removing it.
     * @return
     */
    public synchronized T peek() {
    	if (heap.size() < 1)
    		return null;
        return heap.get(0);
    }

    /**
     * item has been updated, so move in the heap.  update is meant to be fast.  usually only a few compares.
     * @param obj
     */
    public synchronized void update(T obj) {
    	int i = this.indexes.get(obj);
    	T parent = this.heap.get(this.parent(i));
    	
    	if (i > 0 && this.isGreaterOrEqual(obj, parent)) {
    		this.pushUp(i);
    	} else {
    		this.pushDownByIndex(i);
    	}
    }
    
    /**
     * inserts the item if it does not already exist, else updates it if it does.
     * @param obj
     * @returns true if the item was pushed, false if it was updated.
     */
    public synchronized boolean pushOrUpdate(T obj) {
    	if (this.indexes.containsKey(obj)) {
    		this.update(obj);
    		return false;
    	} else {
    		this.push(obj);
    		return true;
    	}
    }
    
    protected Object get(int index) {
        return heap.get(index);
    }

    public synchronized int size() {
        return heap.size();
    }

    

    protected int parent(int i) {
        return (i - 1) / 2;
    }

    protected int left(int i) {
        return 2 * i + 1;
    }

    protected int right(int i) {
        return 2 * i + 2;
    }

    protected void swap(int i, int j) {
        T tmp = heap.get(i);
        T tmpj = heap.get(j);
                
        this.indexes.put(tmpj, i);
        this.indexes.put(tmp, j);
        heap.set(i, tmpj);
        heap.set(j, tmp);
    }
    
   

    /**
     * push the object lower in the heap.
     * @param obj
     */
    private void pushDown(T obj) {
    	int i = this.indexes.get(obj);
    	this.pushDownByIndex(i);
    }
    private void pushDownByIndex(int i) {
    	 int left = left(i);
         int right = right(i);
         int largest = i;

         if (left < heap.size() && !isGreaterOrEqual(this.heap.get(largest), this.heap.get(left))) {
        	 largest = left;
         }
         if (right < heap.size() && !isGreaterOrEqual(this.heap.get(largest), this.heap.get(right))) {
        	 largest = right;
         }

         if (largest != i) {
        	 swap(largest, i);
             pushDownByIndex(largest);
         }
    }

    private boolean isGreaterOrEqual(T i, T j) {
    	return this.comparator.compare(i, j) >= 0;
    }
    
    private void pushUp(int i) {
//    	System.out.println("PUSH UP: " + i);
//    	
//    	System.out.println(parent(i));
//    	System.out.println(this.heap);
        while (i > 0 && !isGreaterOrEqual(this.heap.get(parent(i)), this.heap.get(i))) {
            swap(parent(i), i);
            i = parent(i);
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer("Heap:\n");
        int rowStart = 0;
        int rowSize = 1;
        for (int i = 0; i < heap.size(); i++) {
            if (i == rowStart+rowSize) {
                s.append('\n');
                rowStart = i;
                rowSize *= 2;
            }
            s.append(get(i));
            s.append(" ");
        }
        return s.toString();
    }

    public static void main(String[] args){
	
    	PriorityUpdateQueue<DynMap> h = new PriorityUpdateQueue<DynMap>(new Comparator<DynMap>() {
			@Override
			public int compare(DynMap o1, DynMap o2) {
				return o1.getInteger("val").compareTo(o2.getInteger("val"));
			}
		});
        
        DynMap item = new DynMap();
        item.put("name","testing");
        item.put("val",1000);
        h.push(item);
        item.put("val",50);
        h.update(item);
        Date start = new Date();
        
        for (int i = 0; i < 10; i++) {
        	
        	DynMap it = new DynMap();
        	int v = (int)(100 * Math.random());
        	it.put("name", "" + v);
        	it.put("val", v);
            h.push(it);
            if (i > 100000) {
            	h.pop();
            }
            if (i % 10000 == 0) {
            	System.out.println(i);
            }
        }
        System.out.println("Added 1 million in : " + (new java.util.Date().getTime()-start.getTime()));
        System.out.println("**************** DONE ADDING *************");
        
        item.put("val",50);
        h.update(item);
        start = new Date();
        while (h.size() > 0) {
//            System.out.println(h.pop());
        	h.pop();
        }
        System.out.println("Removed 1 million in : " + (new java.util.Date().getTime()-start.getTime()));

    }
	
}
