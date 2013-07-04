package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterChainImpl implements FilterChain{
	private List<Filter> filterList;
	private Servlet targetServlet;
	
	public FilterChainImpl(List<Filter> filterList, Servlet targetServlet){
		this.filterList = filterList;
		this.targetServlet = targetServlet;
		index = 0;
		length = filterList.size();
	}
	private int index;
	private int length;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		if(index < length){
			Filter currentFilter = filterList.get(index);
			index ++;
			currentFilter.doFilter(req, resp, this);
		}
	}
}
