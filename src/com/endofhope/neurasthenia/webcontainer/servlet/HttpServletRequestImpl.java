/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.webcontainer.session.DummySession;

/**
 * 
 * @author endofhope
 *
 */
public class HttpServletRequestImpl implements HttpServletRequest{
	
	private static Logger logger = Logger.getLogger("servlet");
	
	private PanzerRequest panzerRequest;
	private HttpServletInputStreamImpl httpServletInputStreamImpl;
	private Map<String, List<String>> headerMap;
	private ServletContextImpl servletContextImpl;
	
	public HttpServletRequestImpl(
			PanzerRequest panzerRequest, 
			ServletContextImpl servletContextImpl){
		this.panzerRequest = panzerRequest;
		headerMap = panzerRequest.getHeaderMap();
		httpServletInputStreamImpl = new HttpServletInputStreamImpl(panzerRequest);
		this.servletContextImpl = servletContextImpl;
		
		attributeMap = new HashMap<String, Object>();
		parameterMap = new HashMap<String, List<String>>();
		characterEncoding = panzerRequest.getCharset();
		initBodyInfo();
	}
	/**************************************************************************/
	// charset
	private String characterEncoding;
	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		characterEncoding = env;
	}
	// contentLength
	@Override
	public int getContentLength() {
		return panzerRequest.getContentLength();
	}
	// contentType
	@Override
	public String getContentType() {
		return panzerRequest.getContentType();
	}
	// local (Addr|Port|Name)
	@Override
	public String getLocalAddr() {
		return panzerRequest.getLocalAddr();
	}
	@Override
	public int getLocalPort() {
		return panzerRequest.getLocalPort();
	}
	@Override
	public String getLocalName() {
		return panzerRequest.getLocalName();
	}
	// Remote (Addr|Port|Host)
	@Override
	public String getRemoteAddr() {
		return panzerRequest.getRemoteAddr();
	}
	@Override
	public int getRemotePort() {
		return panzerRequest.getRemotePort();
	}
	@Override
	public String getRemoteHost() {
		return panzerRequest.getRemoteHost();
	}
	// protocol HTTP/1.1
	@Override
	public String getProtocol() {
		return panzerRequest.getHttpVersion();
	}
	// scheme
	@Override
	public String getScheme() {
		String scheme = null;
		String httpVersion = panzerRequest.getHttpVersion();
		if(httpVersion != null){
			if(httpVersion.startsWith("HTTP")){
				scheme = "HTTP";
			}
		}
		return scheme;
	}
	/**************************************************************************/
	// servlet inputstream
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return httpServletInputStreamImpl;
	}
	// reader
	private InputStreamReader inputStreamReader;
	@Override
	public BufferedReader getReader() throws IOException {
		if(inputStreamReader == null){
			inputStreamReader = new InputStreamReader(httpServletInputStreamImpl);
		}
		return new BufferedReader(inputStreamReader);
	}
	/**************************************************************************/
	// attribute
	private Map<String, Object> attributeMap;
	@Override
	public Object getAttribute(String name) {
		return attributeMap.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getAttributeNames() {
		Set<String> keySet = attributeMap.keySet();
		Vector<String> attributeVector = new Vector<String>();
		attributeVector.addAll(keySet);
		return attributeVector.elements();
	}
	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}
	/**************************************************************************/
	private boolean isGetMethod;
	private boolean isPostMethod;
	private boolean isUrlEncoded;
	private boolean isMultipart;
	private boolean isBodyParsed;
	private void initBodyInfo(){
		String method = panzerRequest.getMethod();
		String contentType = getContentType();
		if("GET".equalsIgnoreCase(method)){
			isGetMethod = true;
		}else if("POST".equalsIgnoreCase(method)){
			isPostMethod = true;
			if("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)){
				isUrlEncoded = true;
			}else if("multipart/form-data".equalsIgnoreCase(contentType)){
				isMultipart = true;
			}
		}else{
			throw new UnsupportedOperationException("Only GET/POST is supported");
		}
	}
	
	Map<String, List<String>> parameterMap;
	private void parseBody(){
		if(!isBodyParsed){
			String charset = panzerRequest.getCharacterEncoding();
			if(isGetMethod){
				String queryString = panzerRequest.getQueryString();
				if(queryString != null){
					StringTokenizer st = new StringTokenizer(queryString, "&");
					while(st.hasMoreTokens()){
						String param = st.nextToken();
						int indexOfEqual = param.indexOf("=");
						if(indexOfEqual > 0){
							String key = param.substring(0, indexOfEqual);
							try {
								key = URLDecoder.decode(key, charset);
							} catch (UnsupportedEncodingException e) {
								logger.log(Level.WARNING, "Can't be possible", e);
							}
							List<String> valueList = parameterMap.get(key);
							if(valueList == null){
								valueList = new ArrayList<String>();
							}
							String value = null;
							if(indexOfEqual < param.length()){
								value = param.substring(indexOfEqual +1);
							}
							try {
								String decoded = URLDecoder.decode(value, charset);
								valueList.add(decoded);
							} catch (UnsupportedEncodingException e) {
								logger.log(Level.WARNING, "Can't be possible", e);
							}
							parameterMap.put(key, valueList);
						}
					}
				}
			}else if(isPostMethod){
				if(isUrlEncoded){
					int contentLength = getContentLength();
					byte[] bodyBytes = new byte[contentLength];
					try {
						httpServletInputStreamImpl.read(bodyBytes);
					} catch (IOException e) {
						logger.log(Level.WARNING, "parse body error", e);
					}
					String bodyString = null;
//					try {
//						bodyString = URLDecoder.decode(new String(bodyBytes), charset);
//					} catch (UnsupportedEncodingException e) {
//						try {
//							logger.log(Level.WARNING, "charset is not valid, try "+Constants.DEFAULT_HTTP_CHARSET, e);
//							bodyString = URLDecoder.decode(new String(bodyBytes), Constants.DEFAULT_HTTP_CHARSET);
//						} catch (UnsupportedEncodingException e1) {
//							logger.log(Level.WARNING, "body encoding is not acceptable", e1);
//						}
//					}
					try {
						bodyString = new String(bodyBytes, charset);
					} catch (UnsupportedEncodingException e) {
						logger.log(Level.WARNING, "charset is not valid, try "+Constants.DEFAULT_HTTP_CHARSET, e);
						try {
							bodyString = new String(bodyBytes, Constants.DEFAULT_HTTP_CHARSET);
						} catch (UnsupportedEncodingException e1) {
							bodyString = new String(bodyBytes);
						}
					}
					StringTokenizer st = new StringTokenizer(bodyString, "&");
					while(st.hasMoreTokens()){
						String pair = st.nextToken();
						int indexOfEqual = pair.indexOf("=");
						String parameterName = null;
						String parameterValue = null;
						if(indexOfEqual > 0){
							parameterName = pair.substring(0, indexOfEqual);
							try {
								parameterName = URLDecoder.decode(parameterName, charset);
							} catch (UnsupportedEncodingException e) {
								logger.log(Level.WARNING, "charset is not valid, try "+Constants.DEFAULT_HTTP_CHARSET, e);
								try {
									parameterName = URLDecoder.decode(parameterName, Constants.DEFAULT_HTTP_CHARSET);
								} catch (UnsupportedEncodingException e1) {
									logger.log(Level.WARNING, "charset is not valid", e);
								}
							}
						}else{
							continue;
						}
						if(indexOfEqual < pair.length()){
							parameterValue = pair.substring(indexOfEqual +1);
							try {
								parameterValue = URLDecoder.decode(parameterValue, charset);
							} catch (UnsupportedEncodingException e) {
								logger.log(Level.WARNING, "charset is not valid, try "+Constants.DEFAULT_HTTP_CHARSET, e);
								try {
									parameterValue = URLDecoder.decode(parameterValue, Constants.DEFAULT_HTTP_CHARSET);
								} catch (UnsupportedEncodingException e1) {
									logger.log(Level.WARNING, "charset is not valid", e);
								}
							}
						}

						List<String> valueList = parameterMap.get(parameterName);
						if(valueList == null){
							valueList = new ArrayList<String>();
						}
						valueList.add(parameterValue);
						parameterMap.put(parameterName, valueList);
					}
				}else if(isMultipart){
					throw new UnsupportedOperationException("Multipart body is not implemented yet");
				}
			}
			isBodyParsed = true;
		}
	}
	
	@Override
	public String getParameter(String name) {
		parseBody();
		String value = null;
		List<String> valueList = parameterMap.get(name);
		if(valueList != null && valueList.size() > 0){
			value = valueList.get(0);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getParameterMap() {
		parseBody();
		return parameterMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getParameterNames() {
		parseBody();
		Set<String> keySet = parameterMap.keySet();
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(keySet);
		return nameVector.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		parseBody();
		String[] valueArray = null;
		List<String> valueList = parameterMap.get(name);
		if(valueList != null && valueList.size() > 0){
			valueArray = new String[valueList.size()];
			valueList.toArray(valueArray);
		}
		return valueArray;
	}
	/**************************************************************************/
	// header
	@Override
	public long getDateHeader(String name) {
		// TODO
		throw new UnsupportedOperationException();
	}
	@Override
	public String getHeader(String name) {
		String value = null;
		List<String> valueList = headerMap.get(name);
		if(valueList != null && valueList.size() > 0){
			value = valueList.get(0);
		}
		return value;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getHeaderNames() {
		Set<String> keySet = headerMap.keySet();
		Vector<String> nameVector = new Vector<String>();
		nameVector.addAll(keySet);
		return nameVector.elements();
	}
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getHeaders(String name) {
		Vector<String> valueVector = new Vector<String>();
		List<String> valueList = headerMap.get(name.toLowerCase());
		if(valueList != null){
			valueVector.addAll(valueList);
		}
		return valueVector.elements();
	}
	@Override
	public int getIntHeader(String name) {
		int intValue = -1;
		List<String> valueList = headerMap.get(name);
		if(valueList != null && valueList.size() > 0){
			String value = valueList.get(0).trim();
			try{
				intValue = Integer.parseInt(value);
			}catch(NumberFormatException e){
				logger.log(Level.WARNING, "value is not integer", e);
			}
		}
		return intValue;
	}
	/**************************************************************************/
	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return servletContextImpl.getRealContextPath() + File.separator + path;
	}
	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getContextPath() {
		return servletContextImpl.getContextPath();
	}
	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getMethod() {
		return panzerRequest.getMethod();
	}
	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getQueryString() {
		return panzerRequest.getQueryString();
	}
	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getRequestURI() {
		return panzerRequest.getRequestUri();
	}
	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}
	private String servletPath;
	public void setServletPath(String servletPath){
		this.servletPath = servletPath;
	}
	@Override
	public String getServletPath() {
		return servletPath;
	}
	@Override
	public HttpSession getSession(boolean create) {
		return new DummySession();
	}
	@Override
	public HttpSession getSession() {
		return getSession(true);
	}
	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}
}
