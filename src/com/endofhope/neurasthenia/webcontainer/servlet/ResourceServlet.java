/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.webcontainer.MimeManager;

/**
 * 
 * @author endofhope
 *
 */
public class ResourceServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		
		ServletContext sc = getServletContext();
		String requestUri = req.getRequestURI();
		String contextPath = req.getContextPath();
		URL resourceURL = null;
		if("/".equals(contextPath)){
			resourceURL = sc.getResource(requestUri);
		}else{
			resourceURL = sc.getResource(requestUri.substring(contextPath.length()));
		}
		String resourcePath = resourceURL.getPath();
		if(resourcePath == null || "".equals(resourcePath)){
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found for "+requestUri);
		}else{
			File targetFile = new File(resourcePath);
			if(targetFile.exists()){
				if(targetFile.isFile()){
					String fileName = targetFile.getName();
					String fileExtension = null;
					int indexOfLastPoint = fileName.lastIndexOf(".");
					if(indexOfLastPoint > 1){
						fileExtension = fileName.substring(indexOfLastPoint+1);
					}
					String mimeType = MimeManager.getInstance().getMimeType(fileExtension);

					resp.setContentType(mimeType);
					int contentLength = (int)targetFile.length();
					resp.setContentLength(contentLength);

					FileInputStream fis = new FileInputStream(targetFile);
					ServletOutputStream sos = resp.getOutputStream();
					byte[] buf = new byte[512];
					while(true){
						int readSize = fis.read(buf);
						if(readSize <= 0){
							break;
						}
						sos.write(buf, 0, readSize);
					}
					fis.close();
				}else{
					boolean foundFlag = false;
					File[] children = targetFile.listFiles();
					if(children != null){
						for(File child : children){
							String childName = child.getName();
							List<String> representFileList = ConfigManager.getInstance().getWebContainerInfo().getRepresentFileList();
							for(String representFile : representFileList){
								if(childName.equals(representFile)){
									String fileName = child.getName();
									String fileExtension = null;
									int indexOfLastPoint = fileName.lastIndexOf(".");
									if(indexOfLastPoint > 1){
										fileExtension = fileName.substring(indexOfLastPoint+1);
									}
									String mimeType = MimeManager.getInstance().getMimeType(fileExtension);

									resp.setContentType(mimeType);
									int contentLength = (int)child.length();
									resp.setContentLength(contentLength);

									FileInputStream fis = new FileInputStream(child);
									ServletOutputStream sos = resp.getOutputStream();
									byte[] buf = new byte[512];
									while(true){
										int readSize = fis.read(buf);
										if(readSize <= 0){
											break;
										}
										sos.write(buf, 0, readSize);
									}
									fis.close();
									foundFlag = true;
									break;
								}
							}
						}
					}
					if(!foundFlag){
						resp.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found for "+requestUri);
					}
				}
			}else{
				if(requestUri.endsWith("/")){
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found for "+requestUri);
				}else{
					resp.sendRedirect(requestUri + "/");
				}
			}
		}
	}
}
