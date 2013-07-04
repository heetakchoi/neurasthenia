/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.util.List;
import java.util.Map;

import com.endofhope.neurasthenia.Constants;

/**
 * 
 * @author endofhope
 *
 */
public class PanzerRequest {
	
	private Map<String, List<String>> headerMap;
	public void setHeaderMap(Map<String, List<String>> headerMap){
		this.headerMap = headerMap;
	}
	public Map<String, List<String>> getHeaderMap(){
		return headerMap;
	}

	private String method;
	public void setMethod(String method){
		this.method = method;
	}
	public String getMethod(){
		return method;
	}
	
	private String requestUri;
	public void setRequestUri(String requestUri){
		this.requestUri = requestUri;
	}
	public String getRequestUri(){
		return requestUri;
	}
	
	private String contextPath;
	public void setContextPath(String contextPath){
		this.contextPath = contextPath;
	}
	public String getContextPath(){
		return contextPath;
	}
	
	private String servletName;
	public void setServletName(String servletName){
		this.servletName = servletName;
	}
	public String getServletName(){
		return servletName;
	}
	
	private String queryString;
	public void setQueryString(String queryString){
		this.queryString = queryString;
	}
	public String getQueryString(){
		return queryString;
	}
	
	private String contentType;
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	public String getContentType(){
		return contentType;
	}
	
	private String charset;
	public void setCharset(String charset){
		if(charset == null){
			charset = Constants.DEFAULT_HTTP_CHARSET;
		}
		this.charset = charset;
	}
	public String getCharset(){
		return charset;
	}

	private boolean blockingMode;
	public void setBlockingMode(boolean blockingMode){
		this.blockingMode = blockingMode;
	}
	public boolean isBlockingMode(){
		return blockingMode;
	}
	
	private byte[] bodyBytes;
	public void setBodyBytes(byte[] bodyBytes){
		this.bodyBytes = bodyBytes;
	}
	public byte[] getBodyBytes(){
		return bodyBytes;
	}
	
	private int contentLength;
	public void setContentLength(int contentLength){
		this.contentLength = contentLength;
	}
	public int getContentLength(){
		return contentLength;
	}
	
	private String localAddr;
	public void setLocalAddr(String localAddr){
		this.localAddr = localAddr;
	}
	public String getLocalAddr(){
		return localAddr;
	}
	private int localPort;
	public void setLocalPort(int localPort){
		this.localPort = localPort;
	}
	public int getLocalPort(){
		return localPort;
	}
	private String localName;
	public void setLocalName(String localName){
		this.localName = localName;
	}
	public String getLocalName(){
		return localName;
	}
	private String remoteAddr;
	public void setRemoteAddr(String remoteAddr){
		this.remoteAddr = remoteAddr;
	}
	public String getRemoteAddr(){
		return remoteAddr;
	}
	private int remotePort;
	public void setRemotePort(int remotePort){
		this.remotePort = remotePort;
	}
	public int getRemotePort(){
		return remotePort;
	}
	private String remoteHost;
	public void setRemoteHost(String remoteHost){
		this.remoteHost = remoteHost;
	}
	public String getRemoteHost(){
		return remoteHost;
	}
	private String httpVersion;
	public void setHttpVersion(String httpVersion){
		this.httpVersion = httpVersion;
	}
	public String getHttpVersion(){
		return httpVersion;
	}
	private String acceptCharset;
	public void setAcceptCharset(String acceptCharset){
		this.acceptCharset = acceptCharset;
	}
	public String getAcceptCharset(){
		return acceptCharset;
	}
	private String characterEncoding;
	public String getCharacterEncoding(){
		// 사용자 우선
		// acceptCharset 시도
		// 전체 charset 시도
		// 다 없으면 default
//		if(characterEncoding == null){
//			characterEncoding = acceptCharset; 
//		}
		if(characterEncoding == null){
			characterEncoding = encoding;
		}
		if(characterEncoding == null){
			characterEncoding = Constants.DEFAULT_HTTP_CHARSET;
		}
		return characterEncoding;
	}
	private String encoding;
	public void setEncoding(String encoding){
		this.encoding = encoding;
	}
}
