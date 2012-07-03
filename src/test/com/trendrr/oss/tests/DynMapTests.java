/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;

import com.trendrr.json.simple.JSONObject;
import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;



/**
 * @author Dustin Norlander
 * @created Dec 30, 2010
 * 
 */
public class DynMapTests {

	protected Logger log = Logger.getLogger(DynMapTests.class.getCanonicalName());
	
	@Test
	public void test1() {
		DynMap mp = DynMapFactory.instance("{ \"key\" : 90 }");
		log.info(mp.toJSONString());
		Assert.assertTrue(mp.get(Integer.class, "key") == 90);
		
		Assert.assertTrue(mp.get(String.class, "key").equals("90"));
		
		mp = DynMapFactory.instance("{ \"key\" : 90," +
				"\"map1\" : {" +
					"\"key2\" : \"today\"" +
				"}," +
				"\"list\" : [ 1,2,3]" +
			" }");
		
		
		
		
		
		Assert.assertEquals(mp.get(String.class, "map1.key2"), "today");
		mp.remove("map1");
		Assert.assertEquals("key=90&list=1&list=2&list=3", mp.toURLString());
	}
	
	@Test
	public void getDotTest() {
		DynMap mp = new DynMap();
		mp.putWithDot("facebook.val", 12);
		Assert.assertNull(mp.getInteger("facebook"));
	}
	
	@Test
	public void putDotTest() {
		DynMap mp = new DynMap();
		mp.putWithDot("map1.map2.map3.val", 0);
		
		mp.putWithDot("map1.map2.val", 10);
		
		Assert.assertNotNull(mp.getMap("map1"));
	}
	@Test
	public void keysetwithDottest(){
		DynMap mp = new DynMap();
		mp.put("date", new Date());
		//mp.putWithDot("user.name", "sourabh");
		mp.putWithDot("user.name.first ","Anakin");
		mp.putWithDot("user.name.last", "Skywalker");
		mp.putWithDot("user.screenname", "darthvader");
		
		Set<String> ks = mp.keySetWithDot();
		Assert.assertEquals(ks.size(), 4);
		Iterator it = ks.iterator();
		while(it.hasNext()){
			Assert.assertNull(mp.get(it.next()));
			
		}
		
		
		
	}
	@Test
	public void putNull(){
		DynMap mp = new DynMap();
		mp.putWithDot("alpha."+null, 23);
		System.out.println(mp);
		Assert.assertEquals(mp.getInteger("alpha."+null).intValue(), 23);
		System.out.println(mp.getMap("alpha").getInteger("null"));
	}
	
	@Test
	public void outputTest() {
		DynMap mp = new DynMap();
		
		HashSet<String> test = new HashSet<String>();
		List<String> test2 = new ArrayList<String>();
		
		for (int i=0; i < 3; i++){
			test.add("str" + i);
			test2.add("2str" + i);
		}
		
		mp.put("list", test2);
		mp.put("set", test);
		Assert.assertEquals("{\"set\":[\"str2\",\"str1\",\"str0\"],\"list\":[\"2str0\",\"2str1\",\"2str2\"]}", mp.toJSONString());
	}
	
	@Test
	public void extendTest() {
		DynMap mp1 = new DynMap();
		
		mp1.putWithDot("map1.map2.map3.val", 0);
		mp1.putWithDot("map1.map2.map3.keep", 66);
		mp1.putWithDot("map1.mapz.sub.devil", 666);
		
		
		DynMap mp2 = new DynMap();
		mp2.putWithDot("map1.map2.map3.val", 10);
		mp1.putWithDot("map1.mapz", 777);
		
		mp1.extend(mp2);
//		System.out.println(mp1.toJSONString());
		
		//no exception!
		mp1.extend(null);
		
		Assert.assertEquals((int)mp1.getInteger("map1.map2.map3.val"), 10);
		Assert.assertEquals((int)mp1.getInteger("map1.mapz"), 777);
		Assert.assertEquals((int)mp1.getInteger("map1.map2.map3.keep"), 66);
		Assert.assertNull(mp1.get("map1.mapz.sub.devil"));
	}
	
	@Test
	public void nullTest() {
		DynMap mp = new DynMap();
		mp.put("null", null);
		Assert.assertEquals("{\"null\":null}", mp.toJSONString());
	}
	
	@Test 
	public void removeTest() {
		DynMap mp1 = new DynMap();
		
		mp1.putWithDot("map1.map2.map3.val", 0);
		mp1.putWithDot("map1.map2.map3.keep", 66);
		mp1.putWithDot("map1.mapz.sub.devil", 666);
		
		Assert.assertTrue((Integer)mp1.remove("map1.mapz.sub.devil") == 666);
		System.out.println(mp1.toJSONString());
		mp1.remove("map1");
		Assert.assertTrue(mp1.size() == 0);
		
	}
	
	@Test
	public void removeWithDotTest2() {
		//Test submaps are parsed to dynmap.
		DynMap mp = DynMapFactory.instance("{ \"key\" : 90," +
				"\"map1\" : {" +
					"\"key2\" : \"today\"" +
				"}," +
				"\"list\" : [ 1,2,3]" +
			" }");
		
		mp.remove("map1.key2");
		
		Assert.assertNull(mp.get("map1.key2"));
		
		//now test that other map implementations are readded as dynmaps
		
		DynMap mp2 = new DynMap();
		JSONObject json = new JSONObject();
		json.put("id_str","abdefg");
		
		mp2.put("user",json);
		System.out.println(mp2.get(DynMap.class,"user"));
		System.out.println("removing: "+(mp2.remove("user.id_str")));
		Assert.assertNull(mp2.getString("user.id_str"));
		System.out.println("mp2 after remove: "+mp2);
	}
	
	@Test
	public void comparableTest() {
		List<DynMap> maps = new ArrayList<DynMap>();
		DynMap mp1 = new DynMap();
		mp1.put("int", 0);
		mp1.put("string", "zebra");
		maps.add(mp1);
		
		DynMap mp2 = new DynMap();
		mp2.put("int", 1);
		mp2.put("string", "yabba");
		maps.add(mp2);
		

		DynMap mp3 = new DynMap();
		mp3.put("int", 2);
		mp3.put("string", "xylene");
		maps.add(mp3);
		
		Collections.sort(maps, DynMapFactory.comparator(Integer.class, "int"));
//		System.out.println(maps);
		Assert.assertEquals(maps.get(0),mp1);
	
		Collections.sort(maps, DynMapFactory.comparator(String.class, "string"));
//		System.out.println(maps);
		Assert.assertEquals(maps.get(0),mp3);
	}
	
}
