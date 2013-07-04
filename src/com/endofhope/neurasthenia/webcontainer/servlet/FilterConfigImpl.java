package com.endofhope.neurasthenia.webcontainer.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class FilterConfigImpl implements FilterConfig{

	private String filterName;
	private ServletContextImpl servletContextImpl;
	
	protected FilterConfigImpl(String filterName, ServletContextImpl servletContextImpl){
		this.filterName = filterName;
		this.servletContextImpl = servletContextImpl;
		filterConfigInitParamMap = new HashMap<String, String>();
	}
	
	protected void addFilterConfigInitParam(String paramName, String paramValue){
		filterConfigInitParamMap.put(paramName, paramValue);
	}
	
	private Map<String, String> filterConfigInitParamMap;
	
	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public String getInitParameter(String paramName) {
		return filterConfigInitParamMap.get(paramName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getInitParameterNames() {
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(filterConfigInitParamMap.keySet());
		return nameVector.elements();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContextImpl;
	}

}
