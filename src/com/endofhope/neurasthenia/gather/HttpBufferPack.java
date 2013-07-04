/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.gather;

import java.util.ArrayList;
import java.util.List;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.util.StringUtil;

/**
 * 
 * @author endofhope
 *
 */
public class HttpBufferPack extends BufferPack{
	
	protected HttpBufferPack(){
		super();
		headerBytesList = new ArrayList<byte[]>();
	}
	
	private byte[] methodBytes;
	private List<byte[]> headerBytesList;
	private byte[] bodyBytes;
	
	protected void setMethodBytes(byte[] methodBytes){
		this.methodBytes = methodBytes;
	}
	protected void addHeaderBytes(byte[] headerBytes){
		headerBytesList.add(headerBytes);
		if(!contentLengthFlag){
			String header = StringUtil.makeUTF8(headerBytes).trim();
			String lowerCaseHeader = header.toLowerCase();
			if(lowerCaseHeader.startsWith("content-length")){
				int indexOfColon = header.indexOf(":");
				contentLength = Integer.parseInt(header.substring(indexOfColon+1).trim());
				contentLengthFlag = true;
			}
		}
	}
	protected void setBodyBytes(byte[] bodyBytes){
		this.bodyBytes = bodyBytes;
	}
	
	public byte[] getMethodBytes(){
		return methodBytes;
	}
	public List<byte[]> getHeaderBytesList(){
		return headerBytesList;
	}
	public byte[] getBodyBytes(){
		return bodyBytes;
	}
	
	private boolean contentLengthFlag;
	private int contentLength;
	public boolean isContentLengthFlag(){
		return contentLengthFlag;
	}
	public int getContentLength(){
		return contentLength;
	}
	
	private int readBodySize = 0;
	protected void increateReadBodySize(){
		readBodySize ++;
	}
	public int getReadBodySize(){
		return readBodySize;
	}
	
	public enum Status {
		INIT, METHOD, METHOD_CR, METHOD_CRLF, HEADER, HEADER_CR, HEADER_CRLF,
		HEADER_CRLFCR, HEADER_CRLFCRLF, BODY, TERMINUS
	}
	private Status status = Status.INIT;
	protected void setStatus(Status status){
		this.status = status;
	}
	protected Status getStatus(){
		return status;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP-BUFFERPACK-DETAIL").append(Constants.cLF);
		sb.append("method : ").append(StringUtil.makeUTF8(methodBytes)).append(Constants.cLF);
		if(headerBytesList != null){
			for(byte[] header : headerBytesList){
				sb.append(StringUtil.makeUTF8(header)).append(Constants.cLF);
			}
		}else{
			sb.append("no header").append(Constants.cLF);
		}
		if(bodyBytes != null){
			sb.append(StringUtil.makeUTF8(bodyBytes));
		}else{
			sb.append("no body");
		}
		sb.append(Constants.cLF);
		return sb.toString();
	}
}
