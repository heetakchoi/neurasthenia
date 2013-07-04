/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author endofhope
 *
 */
public class MessageOutputStream extends OutputStream{
	
	private static final Logger logger = Logger.getLogger("servlet");
	
	public MessageOutputStream(){
		byteArrayOutputStream = new ByteArrayOutputStream();
	}
	
	public byte[] getWritten(){
		return byteArrayOutputStream.toByteArray();
	}
	
	private ByteArrayOutputStream byteArrayOutputStream;
	
	public static final int BUFFER_SIZE = 8192;
	private ByteBuffer byteBuffer;
	
	private int bufferSize = MessageOutputStream.BUFFER_SIZE;
	public void setBufferSize(int bufferSize){
		this.bufferSize = bufferSize;
	}
	public int getBufferSize(){
		return bufferSize;
	}
	private void initBuffer(){
		if(byteBuffer == null){
			byteBuffer = ByteBuffer.allocate(bufferSize);
		}
	}
	@Override
	public void write(int b){
		initBuffer();
		if(byteBuffer.remaining()<1){
			flush();
		}
		byteBuffer.put((byte)b);
	}
	@Override
	public void write(byte[] bytes) throws IOException{
		write(bytes, 0, bytes.length);
	}
	@Override
	public void write(byte[] bytes, int offset, int length){
		initBuffer();
		int remainingSize = byteBuffer.remaining();
		if(remainingSize >= length){
			// 보낼 것이 버퍼 남은 것 보다 작으면 그냥 보낸다.
			byteBuffer.put(bytes, offset, length);
		}else{
			// 기존 버퍼로는 어차피 넘치므로 flush 한다.
			flush();
			if(length <= bufferSize){
				// 이제 남은 것이 한번에 보낼 크기라면 보낸다.
				byteBuffer.put(bytes, offset, length);
			}else{
				// 한번에 보낼 크기가 아니다
				// 따라서 버퍼 크기만큼 잘라서 보낸다.
				int quotient = length / bufferSize;
				for(int i=0; i<quotient; i++){
					byteBuffer.put(bytes, offset + (bufferSize *i), bufferSize);
					flush();
				}
				int remains = length % bufferSize;
				byteBuffer.put(bytes, offset + bufferSize*quotient, remains);
			}
		}
	}
	@Override
	public void flush(){
		initBuffer();
		byteBuffer.flip();
		
		if(byteBuffer.hasRemaining()){
			byte[] dataBytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(dataBytes);
			try {
				byteArrayOutputStream.write(dataBytes);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Fail to write to byteArrayOutputStream", e);
			}
//			MessageImpl messageImpl = new MessageImpl(inMessage.getMessageId(), inMessage.getMessageType(), inMessage.getPhysicalConnectionKey(), null, dataBytes);
//			try {
//				messageQueue.put(messageImpl);
//				logger.log(Level.FINEST, "http response {0}", StringUtil.makeUTF8(dataBytes));
//			} catch (InterruptedException e) {
//				logger.log(Level.WARNING, "messageQueue interrupted at MessageOutputStream", e);
//			}
		}
		byteBuffer.clear();
	}
	
	
	
	@Override
	public void close(){
		initBuffer();
		flush();
		byteBuffer.clear();
	}
}
