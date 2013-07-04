/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
/**
 * 
 * @author endofhope
 *
 */
public class ServletConfigImpl implements ServletConfig {

	private String servletName;
	private ServletContextImpl servletContextImpl;
	
	protected ServletConfigImpl(String servletName, ServletContextImpl servletContextImpl){
		this.servletName = servletName;
		this.servletContextImpl = servletContextImpl;
		servletConfigInitParamMap = new HashMap<String, String>();
	}
	
	protected void addServletConfigInitParam(String paramName, String paramValue){
		servletConfigInitParamMap.put(paramName, paramValue);
	}
	
	private Map<String, String> servletConfigInitParamMap;
	
	@Override
	public String getInitParameter(String name) {
		return servletConfigInitParamMap.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getInitParameterNames() {
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(servletConfigInitParamMap.keySet());
		return nameVector.elements();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContextImpl;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

}
