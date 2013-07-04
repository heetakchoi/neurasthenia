/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.message.Message;

/**
 * 
 * @author endofhope
 *
 */
public class HttpServletResponseImpl implements HttpServletResponse {
	
	private HttpServletOutputStreamImpl httpServletOutputStreamImpl;
	private PanzerResponse panzerResponse;
	private PrintWriter printWriter;
	
	public HttpServletResponseImpl(
			PanzerResponse panzerResponse,
			BlockingQueue<Message> messageQueue,
			Message inMessage){ 
		this.panzerResponse = panzerResponse;
		httpServletOutputStreamImpl = new HttpServletOutputStreamImpl(this, messageQueue, inMessage);
		OutputStreamWriter osw = null;
		try{
			osw = new OutputStreamWriter(httpServletOutputStreamImpl, panzerResponse.getCharacterEncoding());
		}catch(UnsupportedEncodingException e){
			osw = new OutputStreamWriter(httpServletOutputStreamImpl);
		}
		printWriter = new PrintWriter(osw);
		setStatus(HttpServletResponse.SC_OK, "OK");
		allowKeepAlive = panzerResponse.isAllowKeepAlive();
	}
	private boolean postServiceFlag;
	public void postService() throws IOException{
		if(!postServiceFlag){
			postServiceFlag = true;
			printWriter.flush();
			httpServletOutputStreamImpl.postService();
		}
	}
	
	private boolean cometSupport;
	public void setCometSupport(boolean cometSupport){
		this.cometSupport = cometSupport;
	}
	public boolean isCometSupport(){
		return cometSupport;
	}
	
	private boolean allowKeepAlive;
	protected void setLocation(String location){
		List<String> locationHeaderList = new ArrayList<String>();
		locationHeaderList.add(location);
		panzerResponse.getHeaderMap().put("Location", locationHeaderList);
	}
	protected void setupHeader(){
		String characterEncoding = panzerResponse.getCharacterEncoding();
		// headerMap 에 content-type 설정이 없는 경우 추가한다.
		// 둘 다 없을 경우 text/html
		// 그리고 ;charset= 에 characterEncoding 을 추가한다.
		Map<String, List<String>> headerMap = panzerResponse.getHeaderMap();
		List<String> contentTypeList = headerMap.get("Content-Type");
		if(contentTypeList == null || contentTypeList.size() < 1){
			String contentType = getContentType();
			if(contentType == null){
				contentType = "text/html;charset="+characterEncoding;
			}else{
				int charsetIndex = contentType.indexOf(";charset=");
				if(charsetIndex < 0){
					if(contentType.contains("htm")){
						contentType = contentType.concat(";charset=").concat(characterEncoding);
					}
				}
			}
			contentTypeList = new ArrayList<String>(1);
			contentTypeList.add(contentType);
			setContentType(contentType);
		}
		headerMap.put("Content-Type", contentTypeList);
		
		// content-length 는 getContentLength 값이 의미있는 값 ( >0) 이 존재할 때만 headerMap 에 추가한다.
		int contentLength = panzerResponse.getContentLength();
		if(contentLength > 0){
			List<String> contentLengthList = new ArrayList<String>(1);
			contentLengthList.add(""+contentLength);
			headerMap.put("Content-Length", contentLengthList);
			setContentLength(contentLength);
//			allowKeepAlive = false;
		}
		
		// chunked 가 true 이면 transfer-coding 에 chunked 표시를 추가한다.
		// chunked 인 경우에는 content-length 를 넣지 않는다.
		if(allowKeepAlive && contentLength <=0){
			List<String> transferCodingList = new ArrayList<String>(1);
			transferCodingList.add("chunked");
			headerMap.put("Transfer-Encoding", transferCodingList);
			headerMap.remove("Content-Length");
		}
		
		// HTTP/1.1 연결이면 keep-alive 가 가능하다고 header 에 넣자
		if(panzerResponse.getHttpVersion() == Constants.HTTP_VERSION_11){
			List<String> connectionList = new ArrayList<String>(1);
			connectionList.add("Keep-Alive");
			headerMap.put("Connection", connectionList);
		}
	}
	
	protected byte[] getHeaderBytes(){
		StringBuilder sb = new StringBuilder();
		int httpVersion = panzerResponse.getHttpVersion();
		String httpVersionString = null;
		if(httpVersion == 11){
			httpVersionString = "HTTP/1.1";
		}else if(httpVersion == 10){
			httpVersionString = "HTTP/1.0";
		}else{
			httpVersionString = "HTTP/0.9";
		}
		sb.append(httpVersionString).append(Constants.SPStr)
		.append(panzerResponse.getStatusCode()).append(Constants.SPStr)
		.append(panzerResponse.getStatusMessage()).append(Constants.CRLFStr);
		Map<String, List<String>> headerMap = panzerResponse.getHeaderMap();
		Set<String> headerKeySet = headerMap.keySet();
		Iterator<String> headerKeyIter = headerKeySet.iterator();
		while(headerKeyIter.hasNext()){
			String headerName = headerKeyIter.next();
			StringBuilder headerSB = new StringBuilder();
			headerSB.append(headerName).append(": ");
			List<String> valueList = headerMap.get(headerName);
			for(String oneValue : valueList){
				headerSB.append(oneValue).append(",");
			}
			sb.append(headerSB.substring(0, headerSB.length() -1)).append(Constants.CRLFStr);
		}
		sb.append(Constants.CRLFStr);
		return sb.toString().getBytes();
	}

	public boolean isAllowKeepAlive(){
		return allowKeepAlive;
	}
	
	@Override
	public void setContentLength(int len) {
		panzerResponse.setContentLength(len);
	}	
	public int getContentLength(){
		return panzerResponse.getContentLength();
	}
	
	@Override
	public void setCharacterEncoding(String charset) {
		panzerResponse.setCharacterEncoding(charset);
	}
	@Override
	public String getCharacterEncoding() {
		return panzerResponse.getCharacterEncoding();
	}

	@Override
	public void setContentType(String type) {
		panzerResponse.setContentType(type);
	}
	@Override
	public String getContentType() {
		return panzerResponse.getContentType();
	}
	public int getHttpVersion(){
		return panzerResponse.getHttpVersion();
	}
	
	@Override
	public void setBufferSize(int size) {
		httpServletOutputStreamImpl.setBodyBufferSize(size);
	}
	@Override
	public int getBufferSize() {
		return httpServletOutputStreamImpl.getBodyBufferSize();
	}
	
	@Override
	public void flushBuffer() throws IOException {
		httpServletOutputStreamImpl.flush();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return httpServletOutputStreamImpl;
	}
	@Override
	public PrintWriter getWriter() throws IOException {
		return printWriter;
	}	
	
	/**************************************************************************/
	// header
	@Override
	public void addDateHeader(String name, long date) {
		Date oneDate = new Date(date);
		SimpleDateFormat sdf = new SimpleDateFormat();
		panzerResponse.addHeader(name, sdf.format(oneDate));
	}
	@Override
	public void addHeader(String name, String value) {
		panzerResponse.addHeader(name, value);
	}
	@Override
	public void addIntHeader(String name, int value) {
		panzerResponse.addHeader(name, ""+value);
	}
	@Override
	public boolean containsHeader(String name) {
		Set<String> keySet = panzerResponse.getHeaderMap().keySet();
		return (keySet.contains(name));
	}
	@Override
	public void setDateHeader(String name, long date) {
		Date oneDate = new Date(date);
		SimpleDateFormat sdf = new SimpleDateFormat();
		panzerResponse.setHeader(name, sdf.format(oneDate));
	}
	@Override
	public void setHeader(String name, String value) {
		panzerResponse.setHeader(name, value);
	}
	@Override
	public void setIntHeader(String name, int value) {
		panzerResponse.setHeader(name, ""+value);
	}
	/**************************************************************************/
	// status
	@Override
	public void setStatus(int sc) {
		panzerResponse.setStatusCode(sc);
	}
	@Override
	public void setStatus(int sc, String sm) {
		panzerResponse.setStatusCode(sc);
		panzerResponse.setStatusMessage(sm);
	}	
	/**************************************************************************/
	// error
	@Override
	public void sendError(int sc, String msg) throws IOException {
		SystemSender.sendError(this, sc, msg);
	}
	@Override
	public void sendError(int sc) throws IOException {
		SystemSender.sendError(this, sc, "ERROR\n");
	}	
	/**************************************************************************/
	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub
	}
	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		SystemSender.send302(this, location);
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		
	}
}
