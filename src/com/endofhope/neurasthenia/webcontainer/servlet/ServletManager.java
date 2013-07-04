/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.endofhope.neurasthenia.LifeCycle;
import com.endofhope.neurasthenia.webcontainer.WebContextManager;
/**
 * 
 * @author endofhope
 *
 */
public class ServletManager implements LifeCycle{
	
	private static Logger logger = Logger.getLogger("servlet");

	private ServletContextImpl servletContextImpl;
	private URLClassLoader urlCL;
	private Map<String, FilterInfo> filterInfoMap;
	private Map<String, ServletInfo> servletInfoMap;
	private List<UrlPattern> servletUrlPatternList;
	private List<UrlPattern> filterUrlPatternList;
	private Map<String, String> filterServletNameMap;
	protected ServletManager(ServletContextImpl servletContextImpl){
		this.servletContextImpl = servletContextImpl;
		servletCache = new HashMap<String, Servlet>();
		filterCache = new HashMap<String, Filter>();
		init();
	}
	class ServletInfo{
		private String servletName;
		private String servletClass;
		private Map<String, String> initParamMap;
		private ServletInfo(String servletName, String servletClass){
			this.servletName = servletName;
			this.servletClass = servletClass;
			initParamMap = new HashMap<String, String>();
		}
		protected String getServletName(){
			return servletName;
		}
		private String getServletClass(){
			return servletClass;
		}
		private Map<String, String> getInitParamMap(){
			return initParamMap;
		}
	}
	class FilterInfo{
		private String filterName;
		private String filterClass;
		private Map<String, String> initParamMap;
		private FilterInfo(String filterName, String filterClass){
			this.filterName = filterName;
			this.filterClass = filterClass;
			initParamMap = new HashMap<String, String>();
		}
		protected String getFilterName(){
			return filterName;
		}
		protected String getFilterClass(){
			return filterClass;
		}
		protected Map<String, String> getInitParamMap(){
			return initParamMap;
		}
	}
	protected List<String> getFilterNameList(String servletPath){
		List<String> filterNameList = new ArrayList<String>();
		for(UrlPattern urlPattern : filterUrlPatternList){
			if(urlPattern.isMatched(servletPath)){
				filterNameList.add(urlPattern.getTargetName());
			}
		}
		return filterNameList;
	}
	protected String getServletName(String servletPath){
		String servletName = null;
		for(UrlPattern urlPattern : servletUrlPatternList){
			if(urlPattern.isMatched(servletPath)){
				servletName = urlPattern.getTargetName();
				break;
			}
		}
		return servletName;
	}
	public String getServletClass(String servletName){
		return servletInfoMap.get(servletName).getServletClass();
	}

	private static final String RESOURCE_SERVLET = "__RESOURCE_SERVLET";
	private static final String JSP_SERVLET = "__JSP_SERVLET";

	public List<Filter> getFilterList(String filterServletPath){
		List<String> filterNameList = getFilterNameList(filterServletPath);
		List<Filter> filterList = new ArrayList<Filter>();
		for(String filterName : filterNameList){
			filterList.add(filterCache.get(filterName));
		}
		return filterList;
	}
	public Servlet getServlet(String servletPath){
		String servletName = null;
		if(servletPath != null 
				&&(servletPath.endsWith(".jsp")
						|| servletPath.endsWith(".JSP")
						|| servletPath.endsWith(".jspx")
						|| servletPath.endsWith(".JSPX"))){
			servletName = ServletManager.JSP_SERVLET;
		}else{
			servletName = getServletName(servletPath);
		}
		if(servletName == null){
			servletName = ServletManager.RESOURCE_SERVLET;
		}
		return servletCache.get(servletName);
	}
	
	protected Map<String, Servlet> getServletMap(){
		return servletCache;
	}
	
	private Map<String, Servlet> servletCache;
	private Map<String, Filter> filterCache;
	
	protected Map<String, Filter> getFilterMap(){
		return filterCache;
	}
	
	@SuppressWarnings("unchecked")
	private void init(){
		
		List<URL> urlList = new ArrayList<URL>();
		String webinfPath = servletContextImpl.getRealContextPath() + File.separator + "WEB-INF" + File.separator; 
		File classes = new File(webinfPath + "classes");
		File lib = new File(webinfPath + "lib");

		WebContextManager webContextManager = servletContextImpl.getWebContextManager();
		String commonClasspath = webContextManager.getCommonClassPath();
		if(commonClasspath != null && commonClasspath.length() > 1){
			StringTokenizer st = new StringTokenizer(commonClasspath.trim());
			while(st.hasMoreTokens()){
				String oneCommonClasspath = st.nextToken();
				File commonClasspathFile = new File(oneCommonClasspath);
				if(commonClasspathFile.exists()){
					try {
						urlList.add(commonClasspathFile.toURI().toURL());
					} catch (MalformedURLException e) {
						logger.log(Level.WARNING, "common classpath invalid", e);
					}
				}
			}
		}
		if(classes.exists()){
			try {
				urlList.add(classes.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, "WEB-INF/classes invalid", e);
			}
		}
		if(lib.exists()){
			try {
				FileFilter ff = new FileFilter(){
					@Override
					public boolean accept(File pathname) {
						boolean result = false;
						if(pathname.getName().endsWith(".jar")){
							result = true;
						}
						return result;
					}
				};
				File[] jarList = lib.listFiles(ff);
				for(File file : jarList){
					urlList.add(file.toURI().toURL());
//					sb.append(":").append(file.getAbsolutePath());
				}
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, "WEB-INF/lib invalid", e);
			}
		}
		URL[] urls = new URL[urlList.size()];
		for(int i=0; i<urls.length; i++){
			urls[i] = urlList.get(i);
		}
		urlCL = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		
		servletInfoMap = new HashMap<String, ServletInfo>();
		servletInfoMap.put(
				ServletManager.RESOURCE_SERVLET, 
				new ServletInfo(
						ServletManager.RESOURCE_SERVLET, 
						"com.endofhope.neurasthenia.webcontainer.servlet.ResourceServlet"));
		ServletInfo jspServletInfo = new ServletInfo(
				ServletManager.JSP_SERVLET,
				"org.apache.jasper.servlet.JspServlet");
		servletInfoMap.put(ServletManager.JSP_SERVLET, jspServletInfo);	
		
		Thread.currentThread().setContextClassLoader(urlCL);
		
		SAXBuilder saxb = new SAXBuilder();
		try {
			Document doc = saxb.build(new File(webinfPath + "web.xml"));
			Element webAppElement = doc.getRootElement();
			
			List<Element> contextParamElementList = webAppElement.getChildren("context-param");
			for(Element contextParamElement : contextParamElementList){
				servletContextImpl.getInitParamMap().put(
						contextParamElement.getChildTextTrim("param-name"), 
						contextParamElement.getChildTextTrim("param-value"));
			}
			
			List<Element> servletElementList = webAppElement.getChildren("servlet");
			for(Element servletElement : servletElementList){
				Element servletNameElement = servletElement.getChild("servlet-name");
				Element servletClassElement = servletElement.getChild("servlet-class");
				String servletName = servletNameElement.getTextTrim();
				String servletClass = servletClassElement.getTextTrim();
				ServletInfo servletInfo = new ServletInfo(servletName, servletClass); 
				List<Element> initParamElementList = servletElement.getChildren("init-param");
				for(Element initParamElement : initParamElementList){
					servletInfo.getInitParamMap().put(
							initParamElement.getChildTextTrim("param-name"), 
							initParamElement.getChildTextTrim("param-value"));
				}
				servletInfoMap.put(servletName, servletInfo);
			}
			
			filterInfoMap = new HashMap<String, FilterInfo>();
			List<Element> filterElementList = webAppElement.getChildren("filter");
			for(Element filterElement : filterElementList){
				Element filterNameElement = filterElement.getChild("filter-name");
				Element filterClassElement = filterElement.getChild("filter-class");
				String filterName = filterNameElement.getTextTrim();
				String filterClass = filterClassElement.getTextTrim();
				FilterInfo filterInfo = new FilterInfo(filterName, filterClass);
				List<Element> initParamElementList = filterElement.getChildren("init-param");
				for(Element initParamElement : initParamElementList){
					filterInfo.getInitParamMap().put(
							initParamElement.getChildTextTrim("param-name"), 
							initParamElement.getChildTextTrim("param-value"));
				}
				filterInfoMap.put(filterName, filterInfo);
			}
			
			servletUrlPatternList = new ArrayList<UrlPattern>();
			List<Element> servletMappingList = webAppElement.getChildren("servlet-mapping");
			for(Element servletMappingElement : servletMappingList){
				Element servletNameElement = servletMappingElement.getChild("servlet-name");
				Element urlPatternElement = servletMappingElement.getChild("url-pattern");
				String servletName = servletNameElement.getTextTrim();
				String urlPatternString = urlPatternElement.getTextTrim();
				UrlPattern urlPattern = new UrlPattern(urlPatternString, servletName);
				servletUrlPatternList.add(urlPattern);
			}
			filterUrlPatternList = new ArrayList<UrlPattern>();
			filterServletNameMap = new HashMap<String, String>();
			List<Element> filterMappingList = webAppElement.getChildren("filter-mapping");
			for(Element filterMappingElement : filterMappingList){
				Element filterNameElement = filterMappingElement.getChild("filter-name");
				Element servletNameElement = filterMappingElement.getChild("servlet-name");
				Element urlPatternElement = filterMappingElement.getChild("url-pattern");
				if(servletNameElement != null){
					filterServletNameMap.put(filterNameElement.getTextTrim(), servletNameElement.getTextTrim());
				}else if(urlPatternElement != null){
					UrlPattern urlPattern = new UrlPattern(urlPatternElement.getTextTrim(), filterNameElement.getTextTrim());
					filterUrlPatternList.add(urlPattern);
				}
			}
		} catch (JDOMException e) {
			logger.log(Level.WARNING, "WEB-INF/web.xml is invalid", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "WEB-INF/web.xml is invalid", e);
		}		
	}
	
	@Override
	public void boot(){
		running = true;
		
		Set<String> servletNameSet = servletInfoMap.keySet();
		Iterator<String> servletNameIter = servletNameSet.iterator();
		Class<?> servletClass = null;
		while(servletNameIter.hasNext()){
			String servletName = servletNameIter.next();
			
			ServletConfigImpl servletConfigImpl = new ServletConfigImpl(servletName, servletContextImpl);
			ServletInfo servletInfo = servletInfoMap.get(servletName);
			String servletClassName = servletInfo.getServletClass();
			Map<String, String> initParamMap = servletInfo.getInitParamMap();
			Set<String> keySet = initParamMap.keySet();
			Iterator<String> keyIter = keySet.iterator();
			while(keyIter.hasNext()){
				String key = keyIter.next();
				servletConfigImpl.addServletConfigInitParam(key, initParamMap.get(key));
			}
			try {
				servletClass = urlCL.loadClass(servletClassName);
				Servlet cachedServlet = (Servlet)servletClass.newInstance();
				cachedServlet.init(servletConfigImpl);
				servletCache.put(servletName, cachedServlet);
				logger.log(Level.FINE, "servelt {0}, {1} initialized", 
						new Object[]{servletName, cachedServlet});
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, "servlet class ["+servletName+"] not found", e);
			} catch (InstantiationException e) {
				logger.log(Level.WARNING, "servlet class ["+servletName+"] is not valid servlet", e);
			} catch (IllegalAccessException e) {
				logger.log(Level.WARNING, "servlet class ["+servletName+"] can not accessible", e);
			} catch (ServletException e) {
				logger.log(Level.WARNING, "servlet ["+servletName+"] exception", e);
			}
		}
		
		Set<String> filterNameSet = filterInfoMap.keySet();
		Iterator<String> filterNameIter = filterNameSet.iterator();
		Class<?> filterClass = null;
		while(filterNameIter.hasNext()){
			String filterName = filterNameIter.next();
			
			FilterConfigImpl filterConfigImpl = new FilterConfigImpl(filterName, servletContextImpl);
			FilterInfo filterInfo = filterInfoMap.get(filterName);
			String filterClassName = filterInfo.getFilterClass();
			Map<String, String> initParamMap = filterInfo.getInitParamMap();
			Set<String> keySet = initParamMap.keySet();
			Iterator<String> keyIter = keySet.iterator();
			while(keyIter.hasNext()){
				String key = keyIter.next();
				filterConfigImpl.addFilterConfigInitParam(key, initParamMap.get(key));
			}
			try {
				filterClass = urlCL.loadClass(filterClassName);
				Filter cachedFilter = (Filter)filterClass.newInstance();
				cachedFilter.init(filterConfigImpl);
				filterCache.put(filterName, cachedFilter);
				logger.log(Level.FINE, "filter {0}, {1} initialized", 
						new Object[]{filterName, cachedFilter});
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, "filter class ["+filterName+"] not found", e);
			} catch (InstantiationException e) {
				logger.log(Level.WARNING, "filter class ["+filterName+"] is not valid servlet", e);
			} catch (IllegalAccessException e) {
				logger.log(Level.WARNING, "filter class ["+filterName+"] can not accessible", e);
			} catch (ServletException e) {
				logger.log(Level.WARNING, "filter ["+filterName+"] exception", e);
			}
		}
	}

	@Override
	public void down() {
		running = false;
		
		Set<String> servletKeySet = servletCache.keySet();
		Iterator<String> servletKeyIter = servletKeySet.iterator();
		while(servletKeyIter.hasNext()){
			servletCache.get(servletKeyIter.next()).destroy();
		}
		servletCache.clear();
		
		Set<String> filterKeySet = filterCache.keySet();
		Iterator<String> filterKeyIter = filterKeySet.iterator();
		while(filterKeyIter.hasNext()){
			filterCache.get(filterKeyIter.next()).destroy();
		}
		filterCache.clear();
		
	}
	private volatile boolean running;
	@Override
	public boolean isRunning(){
		return running;
	}
}
