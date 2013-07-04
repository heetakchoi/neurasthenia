/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.comet.TopicManager;
import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.connection.PhysicalConnection;
import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.gather.HttpBufferPack;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.util.StringUtil;
import com.endofhope.neurasthenia.webcontainer.WebContextManager;
import com.endofhope.neurasthenia.webcontainer.servlet.FilterChainImpl;
import com.endofhope.neurasthenia.webcontainer.servlet.HttpServletRequestImpl;
import com.endofhope.neurasthenia.webcontainer.servlet.HttpServletResponseImpl;
import com.endofhope.neurasthenia.webcontainer.servlet.PanzerRequest;
import com.endofhope.neurasthenia.webcontainer.servlet.PanzerResponse;
import com.endofhope.neurasthenia.webcontainer.servlet.ServletContextImpl;
import com.endofhope.neurasthenia.webcontainer.servlet.ServletManager;
import com.endofhope.neurasthenia.webcontainer.servlet.SystemSender;

/**
 * 
 * @author endofhope
 *
 */
public class HttpWorker implements Runnable{
	
	private static final Logger logger = Logger.getLogger("http.worker");
	
	private Message message;
	private Server server;
	protected HttpWorker(Message message, Server server){
		this.message = message;
		this.server = server;
	}
	@Override
	public void run() {

		logger.log(Level.FINER, "http worker work {0}", message.getMessageId());
		processHttp(message);

	}
	private void processHttp(Message inMessage) {

		HttpBufferPack httpBufferPack = (HttpBufferPack)inMessage.getBufferPack();

		PanzerRequest panzerRequest = new PanzerRequest();
		PanzerResponse panzerResponse = new PanzerResponse();
		
		try {
			setupPanzerRequest(httpBufferPack, panzerRequest);
			setupPanzerResponse(panzerRequest, panzerResponse);
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "setup panzer", t);
		}
		
		// HttpServletResponseImpl 을 생성한다.
		HttpServletResponseImpl httpServletResponseImpl = 
			new HttpServletResponseImpl(panzerResponse, server.getMessageQueue("scatter_queue"), inMessage);
		
		// ContextPath 를 보고 servlet context 찾기를 시도한다.
		WebContextManager webContextManager = server.getWebContainer().getWebContextManager();
		ServletContextImpl servletContextImpl = webContextManager.getServletContext(panzerRequest.getContextPath());

		if(servletContextImpl == null){
			String requestUri = panzerRequest.getRequestUri();
			if(requestUri.endsWith("/")){
				SystemSender.sendError(httpServletResponseImpl, HttpServletResponse.SC_NOT_FOUND, "Not found");
			}else{
				SystemSender.send302(httpServletResponseImpl, requestUri+"/");
			}
			return;
		}
		
		// Context 에 topic manager 를 박아넣자.
		TopicManager topicManager = (TopicManager)servletContextImpl.getAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME);
		if(topicManager == null){
			topicManager = server.getWebContainer().getTopicManager();
			servletContextImpl.setAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME, topicManager);
		}
		
		// HttpServletRequestImpl 을 생성한다.
		HttpServletRequestImpl httpServletRequestImpl = new HttpServletRequestImpl(panzerRequest, servletContextImpl);
		
		// comet 연결에 쓰도록 PhysicalConnectionKey 를 하나 박아넣자.
		httpServletRequestImpl.setAttribute(Constants.TOPIC_PHYSICAL_CONNECTION_KEY_ATTRIBUTE_NAME, message.getPhysicalConnectionKey());
		
		ServletManager servletManager = servletContextImpl.getServletManager();
		String servletName = panzerRequest.getServletName();
		String filterServletPath = "/"+servletName;
		httpServletRequestImpl.setServletPath(filterServletPath);
		Servlet targetServlet = servletManager.getServlet(filterServletPath);

		try {
			// FIXME filter 처리
			List<Filter> filterList = servletManager.getFilterList(filterServletPath);
			FilterChainImpl filterChainImpl = new FilterChainImpl(filterList, targetServlet);
			if(filterList.size() > 0){
				filterChainImpl.doFilter(httpServletRequestImpl, httpServletResponseImpl);
			}
			if(targetServlet == null){
				SystemSender.sendError(httpServletResponseImpl, HttpServletResponse.SC_NOT_FOUND, "Not found");
				return;
			}
			targetServlet.service(httpServletRequestImpl, httpServletResponseImpl);
		} catch (ServletException e) {
			logger.log(Level.WARNING, "servlet execute error", e);
			try {
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorHead(e.getMessage()));
				e.printStackTrace(httpServletResponseImpl.getWriter());
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorTail());
			} catch (IOException e1) {
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "servlet io error", e);
			try {
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorHead(e.getMessage()));
				e.printStackTrace(httpServletResponseImpl.getWriter());
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorTail());
			} catch (IOException e1) {
			}
		}catch (Throwable t){
			logger.log(Level.WARNING, "servlet error", t);
			try {
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorHead(t.getMessage()));
				t.printStackTrace(httpServletResponseImpl.getWriter());
				httpServletResponseImpl.getWriter().write(StringUtil.getPrettyErrorTail());
			} catch (IOException e1) {
			}
		} finally {
			try {
				httpServletResponseImpl.postService();
			} catch (IOException e) {
				logger.log(Level.WARNING, "postService io error", e);
			}
		}
	}

	private void setupPanzerRequest(HttpBufferPack httpBufferPack, 
			PanzerRequest panzerRequest) throws Throwable{

		List<byte[]> headerBytesList = httpBufferPack.getHeaderBytesList();

		String requestLine = new String(httpBufferPack.getMethodBytes());
		int indexOfBlank = requestLine.indexOf(" ");
		String method = requestLine.substring(0, indexOfBlank);

		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		for(byte[] headerBytes : headerBytesList){
			String oneHeaderLine = new String(headerBytes);
			int colonIndex = oneHeaderLine.indexOf(":");
			String head = oneHeaderLine.substring(0, colonIndex).trim();
			String lowerCaseHead = head.toLowerCase();
			String tail = oneHeaderLine.substring(colonIndex+1).trim();
			StringTokenizer headerST = new StringTokenizer(tail, ";");
			List<String> valueList = headerMap.get(lowerCaseHead);
			if(valueList == null){
				valueList = new ArrayList<String>();
			}
			while(headerST.hasMoreTokens()){
				valueList.add(headerST.nextToken().trim());
			}
			headerMap.put(lowerCaseHead, valueList);
		}
		
		List<String> contentLengthList = headerMap.get("content-length");
		if(contentLengthList != null && contentLengthList.size() > 0){
			try{
				panzerRequest.setContentLength(Integer.parseInt(contentLengthList.get(0).trim()));
			}catch(NumberFormatException e){
				panzerRequest.setContentLength(0);
			}
		}else{
			panzerRequest.setContentLength(0);
		}
		
		panzerRequest.setHeaderMap(headerMap);
		
		// requestLine 을 가져와서 method/requestUri 를 결정하자.
		int indexOfFirstBlank = requestLine.indexOf(" ");
		int indexOfSecondBlank = requestLine.indexOf(" ", indexOfFirstBlank+1);
		String requestUri = requestLine.substring(indexOfFirstBlank+1, indexOfSecondBlank);
		panzerRequest.setMethod(method);
		panzerRequest.setRequestUri(requestUri);
		
		// scheme 을 설정한다.
		String httpVersion = requestLine.substring(indexOfSecondBlank+1);
		panzerRequest.setHttpVersion(httpVersion);
		
		// contextPath 를 결정하자.
		String contextPath = "/";
		int contextPathLength = 0;
		int indexOfSecondSlash = requestUri.indexOf("/", 1);
		if(indexOfSecondSlash > 0){
			contextPath = requestUri.substring(0, indexOfSecondSlash);
			contextPathLength = contextPath.length();
		}
		panzerRequest.setContextPath(contextPath);
		
		// servlet 이름을 결정하자.
		// 이를 위해 queryString 을 떼어내야한다.
		String queryString = null;
		String servletName = null;
		int indexOfQuestionMark = requestUri.indexOf("?");
		if(indexOfQuestionMark > 0){
			servletName = requestUri.substring(contextPathLength+1, indexOfQuestionMark);
			queryString = requestUri.substring(indexOfQuestionMark+1);
		}else{
			servletName = requestUri.substring(contextPathLength+1);
		}
		panzerRequest.setServletName(servletName);
		panzerRequest.setQueryString(queryString);
		
		// contentType 을 결정하자.
		// charset 도 존재하면 세팅한다.
		String contentType = null;
		String charset = null;
		List<String> contentTypeList = headerMap.get("content-type");
		if(contentTypeList != null && contentTypeList.size() > 0){
			contentType = contentTypeList.get(0).trim();
			Iterator<String> contextTypeIter = contentTypeList.iterator();
			// 처음은 contextType 이므로 그냥 버린다.
			contextTypeIter.next();
			while(contextTypeIter.hasNext()){
				String charsetCandidate = contextTypeIter.next();
				int indexOfCharsetEqual = charsetCandidate.indexOf("charset=");
				if(indexOfCharsetEqual > 0){
					charset = charsetCandidate.substring(indexOfCharsetEqual + 8).trim();
				}
			}
		}
		
		panzerRequest.setContentType(contentType);
		panzerRequest.setCharset(charset);
		
		// blocking 인지 non-blocking 인지 설정한다.
		// non-blocking 이면 body 까지 다 읽어 들인 상태이다.
		// blocking 이면 body 는 읽지 않은 상태이다.
		panzerRequest.setBlockingMode(false);
		// non-blocking 이면 읽어들인 body 를 panzerRequest 로 넘겨준다.
		panzerRequest.setBodyBytes(httpBufferPack.getBodyBytes());
		
		// content-length 를 설정한다. (body 는 읽어야 되지 않겠는가)
		panzerRequest.setContentLength(httpBufferPack.getContentLength());
		
		// localAddr, localPort, localName 를 설정한다.
		PhysicalConnectionKey physicalConnectionKey = message.getPhysicalConnectionKey();
		PhysicalConnection physicalConnection = server.getPhysicalConnectionManager().getConnection(physicalConnectionKey);
		if(physicalConnection == null){
			server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
			return;
		}
		SocketChannel socketChannel = physicalConnection.getSocketChannel(); 
		Socket socket = socketChannel.socket();
		
		InetAddress localAddress = socket.getLocalAddress();
		String localAddr = localAddress.getHostAddress();
		int localPort = socket.getLocalPort();
		String localName = localAddress.getHostName();
		
		panzerRequest.setLocalAddr(localAddr);
		panzerRequest.setLocalPort(localPort);
		panzerRequest.setLocalName(localName);
		
		// remoteAddr, remotePort, remoteName 을 설정한다.
		InetAddress remoteAddress = socket.getInetAddress();
		String remoteAddr = remoteAddress.getHostAddress();
		int remotePort = socket.getPort();
		String remoteHost = remoteAddress.getHostAddress();
		
		panzerRequest.setRemoteAddr(remoteAddr);
		panzerRequest.setRemotePort(remotePort);
		panzerRequest.setRemoteHost(remoteHost);
	}
	
	private void setupPanzerResponse(PanzerRequest panzerRequest, 
			PanzerResponse panzerResponse) throws Throwable{
		Map<String, List<String>> headerMap = panzerRequest.getHeaderMap();
		// keepAlive 가능한가, 이것부터는 panzerResponse 에 넣어 HttpServletResponseImpl 에 전달한다.
		List<String> connectionList = headerMap.get("connection");
		if(connectionList != null && connectionList.size() > 0){
			String connectionString = connectionList.get(0);
			if("keep-alive".equalsIgnoreCase(connectionString)){
				panzerResponse.setAllowKeepAlive(true);
			}else if("close".equalsIgnoreCase(connectionString)){
				panzerResponse.setAllowKeepAlive(false);
			}
		}
		
		// chunked 가능한지 transfer-coding 을 설정한다.
		List<String> transferCodingList = headerMap.get("transfer-coding");
		if(transferCodingList != null && transferCodingList.size() > 0){
			String transferCodingString = transferCodingList.get(0);
			panzerResponse.setTransferCoding(transferCodingString);
		}
		
		// acceptCharset 을 설정하자.
		List<String> acceptCharsetList = headerMap.get("accept-charset");
		if(acceptCharsetList != null && acceptCharsetList.size() > 0){
			String acceptCharset = acceptCharsetList.get(0);
			int indexOfPause = acceptCharset.indexOf(",");
			if(indexOfPause > 0){
				acceptCharset = acceptCharset.substring(0, indexOfPause).trim();
			}
			panzerResponse.setAcceptCharset(acceptCharset);
			panzerRequest.setAcceptCharset(acceptCharset);
		}
		
		// encoding 을 설정하자
		String encoding = server.getWebContainer().getWebContextManager().getEncoding();
		panzerRequest.setEncoding(encoding);
		panzerResponse.setEncoding(encoding);
		
		// http version 을 response 에 설정한다.
		// 무조건 1.1 로 설정한다.
		panzerResponse.setHttpVersion(Constants.HTTP_VERSION_11);
	}
}
