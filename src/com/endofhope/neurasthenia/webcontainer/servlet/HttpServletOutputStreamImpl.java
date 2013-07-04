/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.message.MessageImpl;

/**
 * 
 * @author endofhope
 *
 */
public class HttpServletOutputStreamImpl extends ServletOutputStream {
	
	private static final Logger logger = Logger.getLogger("servlet");
	
	private MessageOutputStream messageOutputStream;
	private HttpServletResponseImpl httpServletResponseImpl;
	
	public HttpServletOutputStreamImpl(HttpServletResponseImpl httpServletResponseImpl, BlockingQueue<Message> messageQueue, Message inMessage){
		messageOutputStream = new MessageOutputStream();
		this.httpServletResponseImpl = httpServletResponseImpl;
		this.messageQueue = messageQueue;
		this.inMessage = inMessage;
	}
	
	private BlockingQueue<Message> messageQueue;
	private Message inMessage;

	private byte[] buffer;
	private int dataIndex = 0;
	
	protected void postService() throws IOException{
		// 일단 쌓인 것을 보내고
		flush();
		boolean isCometSupport = httpServletResponseImpl.isCometSupport();
		if(chunkedFlag){
			// chunked 모드인 경우
			if(!isCometSupport){
				// 일반적인 servlet 인 경우
				// 끝을 알리기 위해 size 0 인 것을 보낸다.
				sendCloseChunk();
				// footer (지금은 내용 없이) 를 보낸다.
				sendBlankFooter();
				internalFlush();
				intermediateFlush = true;
				dataIndex = 0;
			}else{
				// comet 연결이 된 경우 연결이 종료되었다는 표지를 알리지 않는다.
			}
		}else{
			//  쌓인 것이 있다면 보낸다.
			internalFlush();
			// content-length 가 존재할 것이다. (없으면 HTTP/1.1 미만 연결이라고 간주하여 끊어 끝을 표시한다.)
			if(endCloseFlag){
				internalClose();
			}
		}
		if(!isCometSupport){
			// comet 연결이 아니라면 selector 를 반납한다.
			// comet 연결이라면 계속 쓸 것이 있다.
		}
		// 여기서 message queue 에 넣는다.
		// keep-alive 라면 message 타입을 유지하고
		// 그렇지 않다면 (close 라면) MSG_TYPE_HTTP_CLOSE 를 주자
		int messageType = Message.MSG_TYPE_HTTP;
		if(!httpServletResponseImpl.isAllowKeepAlive()){
			messageType = Message.MSG_TYPE_HTTP_CLOSE;
		}
		MessageImpl outMessage = new MessageImpl(
				inMessage.getMessageId(), messageType, 
				inMessage.getPhysicalConnectionKey(), null,
				messageOutputStream.getWritten());
		try {
			messageQueue.put(outMessage);
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "messageQueue interrupted", e);
		}
	}
	
	private boolean chunkedFlag = false;
	private boolean endCloseFlag = false;
	
	@Override
	public void flush() throws IOException{
		// flush 가 처음 불린 상태라면 response-line, header 를 보내야 한다.
		if(!intermediateFlush){
			// chunked 가 가능한지 확인한다.
			if(httpServletResponseImpl.isAllowKeepAlive()){
				// content-length 가 넘어왔는지 확인한다.
				if(httpServletResponseImpl.getContentLength() > 0){
					chunkedFlag = false;
				}else{
					chunkedFlag = true;
				}
			}else{
				if(httpServletResponseImpl.getContentLength() > 0){
					chunkedFlag = false;
				}
			}
			// chunked 라고 설정되었더라도 HTTP/1.1 이 아니면 chunked mode 로 보낼 수 없다.
			// end-close 모드로 설정한다.
			if(httpServletResponseImpl.getHttpVersion() != Constants.HTTP_VERSION_11){
				chunkedFlag = false;
				endCloseFlag = true;
			}
			httpServletResponseImpl.setupHeader();
			internalSend(httpServletResponseImpl.getHeaderBytes());
		}
		// 이제 body 를 보내야 한다.
		sendBody(chunkedFlag);
		internalFlush();
		dataIndex = 0;
		if(!intermediateFlush){
			intermediateFlush = true;
		}
	}
	
	private void sendCloseChunk() throws IOException{
		internalSend("0".getBytes());
		internalSend(Constants.CRLF);
	}
	
	private void sendBlankFooter() throws IOException{
		internalSend(Constants.CRLF);
	}
	
	protected void sendBody(boolean chunked) throws IOException {
		if(dataIndex > 0){
			// 보낼 것이 있을때만 보낸다.
			if (chunked) {
				String hexSize = String.format("%x", dataIndex);
				String hexSizeStr = hexSize + Constants.CRLFStr;
				internalSend(hexSizeStr.getBytes());
				internalSend(buffer, 0, dataIndex);
				internalSend(Constants.CRLF);
			} else {
				internalSend(buffer, 0, dataIndex);
			}
		}
	}
	
	protected void internalFlush() throws IOException{
		messageOutputStream.flush();
	}
	protected void internalSend(byte[] bytes) throws IOException{
		messageOutputStream.write(bytes);
	}
	protected void internalSend(byte[] bytes, int offset, int length) throws IOException{
		messageOutputStream.write(bytes, offset, length);
	}
	
	private boolean intermediateFlush = false;
	
	public void internalClose() throws IOException{
		messageOutputStream.close();
	}
	@Override
	public void close() throws IOException{
		internalFlush();
	}

	@Override
	public void write(int b) throws IOException {
		initBuffer();
		int expectedEnd = dataIndex +  1;
		if(expectedEnd < (bufferSize -1)){
			dataIndex ++;
			buffer[dataIndex] = (byte)b;
		}else if(expectedEnd == (bufferSize -1)){
			dataIndex ++;
			buffer[dataIndex] = (byte)b;
			flush();
		}else{
			flush();
			dataIndex ++;
			buffer[dataIndex] = (byte)b;
		}
	}
	
	@Override
	public void write(byte[] bytes) throws IOException{
		initBuffer();
		write(bytes, 0, bytes.length);
	}
	
	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException{
		
//		byte[] dummy = new byte[length];
//		System.arraycopy(bytes, offset, dummy, 0, length);
//		System.out.println(new String(dummy));
		
		initBuffer();
		int expectedIndex = dataIndex + length;
		if(expectedIndex < (bufferSize -1)){
			System.arraycopy(bytes, offset, buffer, dataIndex, length);
			dataIndex = dataIndex + length;
		}else{
			flush();
			int quotient = length / bufferSize;
			for(int i=0; i<quotient; i++){
				System.arraycopy(bytes, offset + bufferSize * i, buffer, 0, bufferSize);
				dataIndex = bufferSize;
				flush();
			}
			int remains = length % bufferSize;
			System.arraycopy(bytes, offset + bufferSize * quotient, buffer, 0, remains);
			dataIndex = remains;
		}
	}
	
	public static final int BODY_BUFFER_SIZE = 8192;
	private int bufferSize = HttpServletOutputStreamImpl.BODY_BUFFER_SIZE;
	public void setBodyBufferSize(int bodyBufferSize){
		this.bufferSize = bodyBufferSize;
		initBuffer();
	}
	public int getBodyBufferSize(){
		return bufferSize;
	}
	private void initBuffer(){
		if(buffer == null){
			buffer = new byte[bufferSize];
		}
	}

}
