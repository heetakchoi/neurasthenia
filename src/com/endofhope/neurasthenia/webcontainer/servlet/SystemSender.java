/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.util.StringUtil;

/**
 * 
 * @author endofhope
 *
 */
public class SystemSender {
	private static Logger logger = Logger.getLogger("servlet");
	
	public static void send302(HttpServletResponseImpl httpServletResponseImpl, String location){
		int statusCode = 302;
		String statusMessage = "Moved Temporarily";
		try{
			httpServletResponseImpl.setStatus(statusCode, statusMessage);
			httpServletResponseImpl.setLocation(location);
			httpServletResponseImpl.setupHeader();
			
		}catch(Throwable t){
			logger.log(Level.WARNING, "send error", t);
		}finally{
			try {
				httpServletResponseImpl.postService();
			} catch (IOException e) {
				logger.log(Level.WARNING, "SEND error", e);
			}			
		}
	}
	
	public static void sendError(HttpServletResponseImpl httpServletResponseImpl, int statusCode, String statusMessage){
		try{
			httpServletResponseImpl.setStatus(statusCode, statusMessage);
			httpServletResponseImpl.setContentType("text/html");
			String characterEncoding = httpServletResponseImpl.getCharacterEncoding();
			byte[] bodyBytes = null;
			try {
				bodyBytes = statusMessage.getBytes(characterEncoding);
			} catch (UnsupportedEncodingException e) {
				bodyBytes = statusMessage.getBytes();
			}
			int contentLength = bodyBytes.length;
			httpServletResponseImpl.setContentLength(contentLength);
			httpServletResponseImpl.setupHeader();
			
			if(contentLength > 0){
				try {
					httpServletResponseImpl.getWriter().write(StringUtil.makeUTF8(bodyBytes));
					httpServletResponseImpl.flushBuffer();
				} catch (IOException e) {
					logger.log(Level.WARNING, "send error", e);
				}
			}
		}catch(Throwable t){
			logger.log(Level.WARNING, "send error", t);
		}finally{
			try {
				httpServletResponseImpl.postService();
			} catch (IOException e) {
				logger.log(Level.WARNING, "SEND error", e);
			}			
		}
	}
}
