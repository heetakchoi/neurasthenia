/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.gather;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.connection.PhysicalConnectionManager;
import com.endofhope.neurasthenia.gather.HttpBufferPack.Status;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.message.MessageImpl;

/**
 * 
 * @author endofhope
 *
 */
public class HttpGather extends AbstractGather{
	
	private static final Logger logger = Logger.getLogger("http.gather");

	public HttpGather(Server server, String id, String serviceType, int port,
			int readSelectTimeout, int readBufferSize,
			BlockingQueue<Message> messageQueue) {
		super(server, id, serviceType, port, readSelectTimeout, readBufferSize,
				messageQueue);
	}

	@Override
	public int onReceive(SelectionKey selectionKey) throws IOException {
		readBuffer.clear();
		SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
		int readSize = socketChannel.read(readBuffer);
		if(readSize < 0){
			selectionKey.cancel();
			return readSize;
		}
		readBuffer.flip();
		
		HttpBufferPack httpBufferPack = (HttpBufferPack)selectionKey.attachment();
		if(httpBufferPack == null){
			httpBufferPack = new HttpBufferPack();
			selectionKey.attach(httpBufferPack);
		}
		Status status = Status.INIT;
		while(readBuffer.remaining()> 0){
			byte oneByte = readBuffer.get();
			status = httpBufferPack.getStatus();
			if(Status.INIT == status){
				if(Constants.isCr(oneByte) || Constants.isLf(oneByte)){
					// 무시한다.
				}else{
					httpBufferPack.addByte(oneByte);
					httpBufferPack.setStatus(Status.METHOD);
				}
			}else if(Status.METHOD == status){
				if(Constants.isCr(oneByte)){
					httpBufferPack.setMethodBytes(httpBufferPack.getPacked());
					httpBufferPack.setStatus(Status.METHOD_CR);
				}else if(Constants.isLf(oneByte)){
					throw new IllegalStateException("Method 가 끝날 때 CR 이 와야하지만 LF 가 먼저 왔다.");
				}else{
					httpBufferPack.addByte(oneByte);
				}
			}else if(Status.METHOD_CR == status){
				if(Constants.isLf(oneByte)){
					httpBufferPack.setStatus(Status.METHOD_CRLF);
				}else{
					throw new IllegalStateException("Method 가 끝날 때 CR 이후엔 LF 가 와야만 한다.");
				}
			}else if(Status.METHOD_CRLF == status){
				if(Constants.isCr(oneByte)){
					// header 가 없는 경우이다.
					logger.log(Level.WARNING, "header 가 없는 경우가 발생하였다. 상태를 header_crlfcr 로 변경한다.");
					httpBufferPack.setStatus(Status.HEADER_CRLFCR);
				}else if(Constants.isLf(oneByte)){
					throw new IllegalStateException("Method 가 끝난 후 LF 는 올 수 없다.");
				}else{
					httpBufferPack.addByte(oneByte);
					httpBufferPack.setStatus(Status.HEADER);
				}
			}else if(Status.HEADER == status){
				if(Constants.isCr(oneByte)){
					httpBufferPack.addHeaderBytes(httpBufferPack.getPacked());
					httpBufferPack.setStatus(Status.HEADER_CR);
				}else if(Constants.isLf(oneByte)){
					throw new IllegalStateException("Header 가 끝날 때 CR 이 와야 하지만 LF 가 먼저 왔다.");
				}else{
					httpBufferPack.addByte(oneByte);
				}
			}else if(Status.HEADER_CR == status){
				if(Constants.isLf(oneByte)){
					httpBufferPack.setStatus(Status.HEADER_CRLF);
				}else{
					throw new IllegalStateException("header 가 끝날 때 CR 이후엔 LF 가 와야 한다.");
				}
			}else if(Status.HEADER_CRLF == status){
				if(Constants.isCr(oneByte)){
					// header 가 모두 끝났다.
					httpBufferPack.setStatus(Status.HEADER_CRLFCR);
				}else if(Constants.isLf(oneByte)){
					throw new IllegalStateException("header_crlf 다음에 lf 는 올 수 없다.");
				}else{
					httpBufferPack.addByte(oneByte);
					httpBufferPack.setStatus(Status.HEADER);
				}
			}else if(Status.HEADER_CRLFCR == status){
				if(Constants.isLf(oneByte)){
					if(httpBufferPack.isContentLengthFlag()){
						httpBufferPack.setStatus(Status.BODY);
					}else{
						httpBufferPack.setStatus(Status.TERMINUS);
						status = Status.TERMINUS;
					}
				}else{
					throw new IllegalStateException("header_crlfcr 다음에는 LF 만 올 수 있다.");
				}
			}else if(Status.BODY == status){
				if(httpBufferPack.isContentLengthFlag()){
					httpBufferPack.addByte(oneByte);
					httpBufferPack.increateReadBodySize();
					if(httpBufferPack.getContentLength() <= httpBufferPack.getReadBodySize()){
						httpBufferPack.setBodyBytes(httpBufferPack.getPacked());
						httpBufferPack.setStatus(Status.TERMINUS);
						status = Status.TERMINUS;
					}
				}else{
					// body 가 없다
					httpBufferPack.setStatus(Status.TERMINUS);
					status = Status.TERMINUS;
				}
			}
			if(Status.TERMINUS == status){
				selectionKey.attach(null);
				Message message = new MessageImpl(
						server.getUnique(), 
						Message.MSG_TYPE_HTTP, 
						PhysicalConnectionManager.createPhysicalConnectionKey((SocketChannel)selectionKey.channel()), 
						httpBufferPack, null);
				logger.log(Level.FINER, "http gather put msg id:{0}", message.getMessageId());
				try {
					messageQueue.put(message);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "gather fail to put message", e);
				}
				if( readBuffer.remaining() > 0){
					HttpBufferPack neoHttpBufferPack = new HttpBufferPack();
					httpBufferPack = neoHttpBufferPack;
					selectionKey.attach(httpBufferPack);
				}
			}
		}
		return readSize;
	}
}
