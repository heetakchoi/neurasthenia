/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.endofhope.neurasthenia.Neurasthenia;


/**
 * 
 * @author endofhope
 *
 */
public class Bootstrap {

	public static void main(String[] args)  throws ClassNotFoundException, 
		SecurityException, IllegalArgumentException, InstantiationException, 
		IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.action();
	}
	
	private void action() throws ClassNotFoundException, InstantiationException, 
	IllegalAccessException, SecurityException, NoSuchMethodException, 
	IllegalArgumentException, InvocationTargetException{
		String homeDirString = System.getProperty("home.dir", ".");
		File homeFile = new File(homeDirString);
		try {
			homeDirString = homeFile.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(homeDirString.endsWith(".")){
			homeDirString = homeDirString.substring(0, homeDirString.length()-2);
		}
		String libString = new StringBuilder(homeDirString).append(File.separator).append("lib").toString();
		File libFile = new File(libString);
		List<URL> resourceList = new ArrayList<URL>();
		addJars(libFile, resourceList);
		URL[] resourceURLArray = new URL[resourceList.size()];
		resourceList.toArray(resourceURLArray);
		URLClassLoader rootClassLoader = new URLClassLoader(resourceURLArray);
		Thread.currentThread().setContextClassLoader(rootClassLoader);
		
		Class oneClass = Class.forName("com.endofhope.neurasthenia.Neurasthenia", true, rootClassLoader);
		Object oneObject = oneClass.newInstance();
		Method bootMethod = oneClass.getMethod("startup");
		bootMethod.invoke(oneObject);
	}
	
	private void addJars(File parentDirectory, List<URL> resourceList){
		if(parentDirectory.exists() && parentDirectory.isDirectory()){
			File[] subFiles = parentDirectory.listFiles();
			if(subFiles != null){
				for(File subFile : subFiles){
					if(subFile.isDirectory()){
						addJars(subFile, resourceList);
					}else{
						try {
							if(subFile.getName().endsWith(".jar")){
								resourceList.add(subFile.toURI().toURL());
							}
						} catch (MalformedURLException e) {
							System.out.printf("%s is not valid URL format", subFile.getAbsolutePath());
						}
					}
				}
			}
		}
	}
}
