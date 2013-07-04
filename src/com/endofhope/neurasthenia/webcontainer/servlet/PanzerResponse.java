/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.endofhope.neurasthenia.Constants;

/**
 * 
 * @author endofhope
 *
 */
public class PanzerResponse {
	
	public PanzerResponse(){
		headerMap = new HashMap<String, List<String>>();
	}

	private Map<String, List<String>> headerMap;
	public Map<String, List<String>> getHeaderMap(){
		return headerMap;
	}
	public void addHeader(String name, String value){
		List<String> valueList = headerMap.get(name);
		if(valueList == null){
			valueList = new ArrayList<String>();
		}
		valueList.add(value);
		headerMap.put(name, valueList);
	}
	
	public void setHeader(String name, String value){
		List<String> valueList = new ArrayList<String>();
		headerMap.put(name, valueList);
	}
	
	/*
	 * 예약된 response header
	 * 	content-type
	 * 	content-length
	 * 	transfer-coding;
	 */
	
	private String contentType;
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return contentType;
	}
	
	private int contentLength;
	public void setContentLength(int contentLength){
		this.contentLength = contentLength;
	}
	public int getContentLength() {
		return contentLength;
	}
	
	private String transferCoding;
	public void setTransferCoding(String transferCoding){
		this.transferCoding = transferCoding;
	}
	public String getTransferCoding(){
		return transferCoding;
	}
	
	private String characterEncoding;
	public void setCharacterEncoding(String charset) {
		this.characterEncoding = charset;
	}
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
	private boolean allowKeepAlive;
	public void setAllowKeepAlive(boolean allowKeepAlive){
		this.allowKeepAlive = allowKeepAlive;
	}
	public boolean isAllowKeepAlive(){
		return allowKeepAlive;
	}
	private int httpVersion;
	public void setHttpVersion(int httpVersion){
		this.httpVersion = httpVersion;
	}
	public int getHttpVersion(){
		return httpVersion;
	}
	private int statusCode;
	public void setStatusCode(int statusCode){
		this.statusCode = statusCode;
	}
	public int getStatusCode(){
		return statusCode;
	}
	private String statusMessage;
	public void setStatusMessage(String statusMessage){
		this.statusMessage = statusMessage;
	}
	public String getStatusMessage(){
		return statusMessage;
	}
	@SuppressWarnings("unused")
	private String acceptCharset;
	public void setAcceptCharset(String acceptCharset){
		this.acceptCharset = acceptCharset;
	}
	private String encoding;
	public void setEncoding(String encoding){
		this.encoding = encoding;
	}
}
