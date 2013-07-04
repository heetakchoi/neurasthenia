/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.endofhope.neurasthenia.LifeCycle;
import com.endofhope.neurasthenia.webcontainer.WebContextManager;

/**
 * 
 * @author endofhope
 *
 */
public class ServletContextImpl implements ServletContext, LifeCycle{
	
	private static Logger logger = Logger.getLogger("servlet");

	private WebContextManager webContextManager;
	private String directory;
	private String contextName;
	private String contextPath;
	private int version;
	
	private Map<String, String> initParamMap;
	protected Map<String, String> getInitParamMap(){
		return initParamMap;
	}
	
	public int getVersion(){
		return version;
	}
	
	public ServletContextImpl(WebContextManager webContextManager, String directory, String contextName, String contextPath, int version){
		this.webContextManager = webContextManager;
		this.directory = directory;
		this.contextName = contextName;
		this.contextPath = contextPath;
		this.version = version;
		attributeMap = new ConcurrentHashMap<String, Object>();
		initParamMap = new HashMap<String, String>();
		servletManager = new ServletManager(this);
	}
	
	public WebContextManager getWebContextManager(){
		return webContextManager;
	}
	
	protected String getRealContextPath(){
		return directory;
	}
	
	private ServletManager servletManager;
	public ServletManager getServletManager(){
		return servletManager;
	}
	
	private Map<String, Object> attributeMap;
	@Override
	public void setAttribute(String name, Object object) {
		attributeMap.put(name, object);
	}
	@Override
	public Object getAttribute(String name) {
		return attributeMap.get(name);
	}
	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getAttributeNames() {
		Set<String> nameSet = attributeMap.keySet();
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(nameSet);
		return nameVector.elements();
	}
	

	@Override
	public ServletContext getContext(String uripath) {
		return webContextManager.getServletContext(uripath);
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getInitParameter(String name) {
		return initParamMap.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getInitParameterNames() {
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(initParamMap.keySet());
		return nameVector.elements();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public String getMimeType(String file) {
		// TODO Implement
		throw new UnsupportedOperationException("Until not supported");
	}

	@Override
	public int getMinorVersion() {
		return 5;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		// TODO Implement
		throw new UnsupportedOperationException("Until not supported");
	}

	@Override
	public String getRealPath(String path) {
		return directory + path;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Implement
		throw new UnsupportedOperationException("Until not supported");
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		String absolutePath = getRealPath(path);
		return new URL("file", "localhost", absolutePath);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(getResource(path).getFile());
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "resource not found", e);
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "resource is not well formed", e);
		}
		return fis;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set getResourcePaths(String path) {
		// TODO Implement
		throw new UnsupportedOperationException();
	}

	public static final String serverInfo = "Communicator Servlet Container";
	@Override
	public String getServerInfo() {
		return serverInfo;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return servletManager.getServlet(name);
	}

	@Override
	public String getServletContextName() {
		return contextName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getServletNames() {
		Set<String> keySet = servletManager.getServletMap().keySet();
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(keySet);
		return nameVector.elements();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getServlets() {
		Vector<Servlet> servletVector = new Vector<Servlet>();
		servletVector.addAll(servletManager.getServletMap().values());
		return servletVector.elements();
	}

	@Override
	public void log(String msg) {
		logger.log(Level.INFO, msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		logger.log(Level.SEVERE, msg, exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		logger.log(Level.SEVERE, message, throwable);
	}
	
	@Override
	public void boot(){
		running = true;
		servletManager.boot();
	}
	@Override
	public void down() {
		servletManager.down();
		running = false;
	}
	private volatile boolean running;
	@Override
	public boolean isRunning(){
		return running;
	}
}
