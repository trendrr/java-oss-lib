/**
 * 
 */
package com.trendrr.oss;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author dustin
 *
 */
public class Reflection {
	
	protected static Log log = LogFactory.getLog(Reflection.class);
	
	public static void setter(Object obj, String name, Object input) {
		try {
			obj.getClass().getMethod("set" + StringHelper.capitalize(name), input.getClass()).invoke(obj, input);
		} catch (Exception x) {
//			log.info("Caught", x);
		}
	}
	
	/**
	 * this does a attempt to find the method you are looking for, it will look at every
	 * method in the class and run any that match the name against the inputs.  This approach is 
	 * slightly slower then the execute method, which suffers from requiring the inputs to match the class 
	 * of the inputs exactly.
	 * 
	 * 
	 * @param obj
	 * @param method
	 * @param input
	 */
	public static Object exec(Object obj, String method, Object ... inputs) throws Exception{
		Exception lastException = null;
		
		for (Method m : obj.getClass().getMethods()) {
			if (m.getName().equals(method)) {
				try {
					return m.invoke(obj, inputs);
				} catch (Exception x) {
					lastException = x;
				}
			}
		}
		
		if (lastException != null)
			throw lastException;
		throw new java.lang.NoSuchMethodException("No method found with name: " + method);
	}
	
	/**
	 * This will execute the requested method on the specified object.  note, this does not search superclasses
	 * @param obj
	 * @param method
	 * @param inputs
	 * @return
	 * @throws Exception
	 */
	public static Object execute(Object obj, String method, Object ... inputs) throws Exception{
		if (inputs.length == 1)  {
			exec(obj, method, inputs[0]);
			return null;
		}
			
		try {
			Class[] classes = new Class[inputs.length];
			int i=0;
			for (Object o : inputs) {
				classes[i] = o.getClass();
				i++;
			}
			return obj.getClass().getMethod(method, classes).invoke(obj, inputs);
		} catch (java.lang.reflect.InvocationTargetException ex) { 
			if (ex.getCause() != null && ex.getCause() instanceof Exception) {
				throw (Exception)ex.getCause();
			}
			throw ex;
		} catch (Exception x) {
			throw x;
		}
	}
	
	/**
	 * accesses a getter.  will return null if the getter is not found, else returns whatever the
	 * data is.
	 * @param obj
	 * @param name
	 * @return
	 */
	public static Object getter(Object obj, String name) {
		log.warn("Trying to get getter: " + name + " for " + obj);
		try {
			if (!name.startsWith("get")) {
				name =  "get" + StringHelper.capitalize(name);
			}
			return obj.getClass().getMethod(name).invoke(obj);
		} catch (java.lang.NoClassDefFoundError ex) {
        	log.warn("Caught and swallowed (likely an unresolved dependancy) while trying to use getter: " + name, ex);
        } catch (NoSuchMethodException x) {
//			x.printStackTrace();
		} catch (Throwable x) {
			log.info("Caught and swalled (trying to use getter: " + name + ")", x);
		}
		return null;
	}
	
	public static boolean hasGetter(Object obj, String name) {
		try {
			obj.getClass().getMethod("get" + StringHelper.capitalize(name));
			return true;
		} catch (NoSuchMethodException x) {
			
		}
		return false;
	}
	
	public static boolean hasMethod(Object obj, String name, Class ...params) {
		try {
			obj.getClass().getMethod(name, params);
			return true;
		} catch (NoSuchMethodException x) {
			
		}
		return false;
	}
	
	/**
	 * gets an instance of the specified class.  cls is assumed to be the base class.  
	 * @param cls
	 * @param className
	 * @param params
	 * @return
	 */
	public static <T> T instance(Class<T> cls, String className, Object ... params ) {
		try {
			Class[] paramTypes = new Class[params.length];
			for (int i=0; i < params.length; i++) {
				paramTypes[i] = params[i].getClass();
			}
			Class newcls = Class.forName(className);
			Constructor<T> cstr = newcls.getConstructor(paramTypes);
			T instance = (T)cstr.newInstance(params);
			return instance;
		} catch (Throwable x) {
//			log.info("Caught Pie", x);
//			x.printStackTrace();
		}
		return null;
		
	}
	
	public static <T> T instance(Class<T> cls, Object ... params ) {
		try {
			Class[] paramTypes = new Class[params.length];
			for (int i=0; i < params.length; i++) {
				paramTypes[i] = params[i].getClass();
			}
			
			Constructor<T> cstr = cls.getConstructor(paramTypes);
			T instance = (T)cstr.newInstance(params);
			return instance;
		} catch (Throwable x) {
//			log.info("Caught Pie", x);
//			x.printStackTrace();
		}
		return null;
		
	}
	
	public static <T> List<T> instances(Class<T> cls, String packageName, boolean recur, Object ... params) {
		try {
//			log.info("Looking at package : " + packageName);
			List<Class> classes = findClasses(packageName, recur);
			List<T> instances = new ArrayList<T>();
			for (Class c : classes) {
//				log.info("got class: " + c + " classessize: " + classes.size());
				if (cls.isAssignableFrom(c)) {
					T inst = (T)instance(c, params);
					if (inst != null) {
						instances.add(inst);
					} else {
//						log.info("Unable to instantiate: " + c);
					}
				}
			}
//			log.info("returning instances: " + instances);
			return instances;
		} catch (Throwable x) {
//			log.info("Caught", x);
		}
		return null;
	}
	
	/**
	 * returns a defualt instance of the passedc in classname.
	 * cls is assumed to be the base class. 
	 * 
	 * @param <T>
	 * @param cls
	 * @param className
	 * @return
	 * @throws Exception
	 */
	public static <T> T defaultInstance(Class<T> cls, String className) throws Exception {
		try {
		Class newcls = Class.forName(className);
		Constructor<T> cstr = newcls.getConstructor();
		T instance = (T)cstr.newInstance();
		return instance;
		} catch (Exception x) {
			throw x;
		} catch (Throwable x) {
			throw new Exception(x);
		}
	}
	
	/**
	 * searches the given packagename for any implementations of class t and returns new instances for each.
	 * @param <T>
	 * @param cls
	 * @param packageName
	 * @param recure
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> defaultInstances(Class<T> cls, String packageName, boolean recur) throws Exception {
		try {
			List<Class> classes = findClasses(packageName, recur);
			List<T> instances = new ArrayList<T>();
			for (Class c : classes) {
				if (c.isInterface() || Modifier.isAbstract( c.getModifiers() )) {
					//skipping
					continue;
				}
				if (cls.isAssignableFrom(c)) {
					instances.add((T)defaultInstance(c));
				}
			}
			return instances;
		} catch (Exception x) {
			throw x;
		} catch (Throwable x) {
			throw new Exception(x);
		}
	}
	
	public static <T> T defaultInstance(Class<T> cls) throws Exception {
		try {
			Constructor<T> cstr = cls.getConstructor();
			T instance = (T)cstr.newInstance();
			return instance;
		} catch (Exception x) {
			throw x;
		} catch (Throwable x) {
			throw new Exception(x);
		}
	}
	
	public static Object defaultInstance(String className) throws Exception {
		try {
			Class newcls = Class.forName(className);
			Constructor cstr = newcls.getConstructor();
			return cstr.newInstance();
		} catch (Exception x) {
			throw x;
		} catch (Throwable x) {
			throw new Exception(x);
		}
	}
	
	/**
	 * returns all the classes of type cls (or some subclass) within the specified package.
	 * @param cls
	 * @param pckgname
	 * @param recur
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List<Class> findClassesOfType(Class cls, String pckgname, boolean recur) throws ClassNotFoundException {
		List<Class> classes = findClasses(pckgname, recur);
		List<Class> retList = new ArrayList<Class>();
		for (Class c : classes) {
			if (cls.isAssignableFrom(c)) {
				retList.add(c);
			}
		}
		return retList;
	}
	
	/**
	 * Gets a list of all the classes in the given package. 
	 * 
	 * 
	 * references:
	 * http://forums.sun.com/thread.jspa?threadID=341935
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip113.html?page=2
	 * 
	 * @param pckgname package name ex: 'com.trendrr.common'
	 * @param recur should list sub packages as well?
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List<Class> findClasses(String pckgname, boolean recur) throws ClassNotFoundException {
		  // This will hold a list of directories matching the pckgname. 
        //There may be more than one if a package is split over multiple jars/paths
        ArrayList<Class> classes = new ArrayList<Class>();
        ArrayList<File> directories = new ArrayList<File>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")){
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e:Collections.list(jar.entries())){
 
                        if (e.getName().startsWith(pckgname.replace('.', '/')) 
                            && e.getName().endsWith(".class") && !e.getName().contains("$")){
                            String className = 
                                    e.getName().replace("/",".").substring(0,e.getName().length() - 6);
                            try {
                            	classes.add(Class.forName(className));
                            } catch (java.lang.NoClassDefFoundError ex) {
                            	log.warn("Caught and swallowed (likely an unresolved dependancy) while trying to add: " + className, ex);
                            }
                        }
                    }
                }else
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying " +
                    "to get all resources for " + pckgname);
        }
         
        for (File directory : directories) {
        	directoryClasses(classes, directory, pckgname, recur);
        }
        return classes;

	}
	
	private static void directoryClasses(ArrayList<Class> classes, File dir, String pckgname, boolean recur) throws ClassNotFoundException{
//		log.debug("looking at file " + dir.getAbsolutePath());
		if (dir.exists()) {
			// Get the list of the files contained in the package
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				String filename = files[i].getName();
				// we are only interested in .class files
				if (filename.endsWith(".class")) {
					// removes the .class extension
					String className = pckgname + '.'
							+ filename.substring(0, filename.length() - 6);
					try {
						classes.add(Class.forName(className));
					} catch (NoClassDefFoundError ex) {
						log.warn("Caught and swallowed (likely an unresolved dependancy) while trying to add: " + className, ex);
					}
				}
				if (files[i].isDirectory() && recur) {
					directoryClasses(classes, files[i], pckgname + '.' + filename, recur);
				}
			}
		} 
	}
}
