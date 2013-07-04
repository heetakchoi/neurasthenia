/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.IOException;

import javax.servlet.ServletInputStream;
/**
 * 
 * @author endofhope
 *
 */
public class HttpServletInputStreamImpl extends ServletInputStream{
	
	public HttpServletInputStreamImpl(PanzerRequest panzerRequest){
		blockingMode = panzerRequest.isBlockingMode();
		// blocking 모드라면 이제부터 body 를 읽어 내야 하고
		// non-blocking 모드라면 이미 body 까지 다 들어온 상태이다.
		// TODO 일단 non-blocking mode 만 지원한다.
		if(!blockingMode){
			// non-blocking 모드에서는 모든 body 가 이미 bodyBytes 로 넘어와 있다.
			readSize = 0;
			bodyBytes = panzerRequest.getBodyBytes();
			bodySize = panzerRequest.getContentLength();
		}
	}
	
	private boolean blockingMode;
	private int readSize;
	private int bodySize;
	private byte[] bodyBytes;
	
	@Override
	public int read() throws IOException {
		if(!blockingMode){
			if(bodyBytes != null){
				if(readSize < bodySize){
					return bodyBytes[readSize];
				}
			}
		}
		return -1;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int copiedSize = -1;
		if(!blockingMode){
			if(bodyBytes != null){
				int remainSize = bodySize - readSize;
				if(remainSize > 0){
					if(len < remainSize){
						System.arraycopy(bodyBytes, readSize, b, 0, len);
						readSize = readSize + len;
						copiedSize = len;
					}else{
						System.arraycopy(bodyBytes, readSize, b, 0, remainSize);
						readSize = bodySize;
						copiedSize = remainSize;
					}
				}else{
					return -1;
				}
			}
		}
		return copiedSize;
	}
}
