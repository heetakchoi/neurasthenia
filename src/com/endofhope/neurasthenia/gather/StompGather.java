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
import com.endofhope.neurasthenia.gather.StompBufferPack.Status;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.message.MessageImpl;

/**
 * 
 * @author endofhope
 *
 */
public class StompGather extends AbstractGather{
	
	private static final Logger logger = Logger.getLogger("gather.stomp");

	public StompGather(Server server, String id, String serviceType, int port,
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
		
		StompBufferPack stompBufferPack = (StompBufferPack)selectionKey.attachment();
		if(stompBufferPack == null){
			stompBufferPack = new StompBufferPack();
			selectionKey.attach(stompBufferPack);
		}
		Status status = stompBufferPack.getStatus();
		
		while(readBuffer.remaining() > 0){

			byte oneByte = readBuffer.get();
//			char oneChar = (char)oneByte;
//			System.out.print(oneChar);
			status = stompBufferPack.getStatus();
			if(status == Status.INIT){
				if(Constants.isLf(oneByte)){
					continue;
				}
				stompBufferPack.addByte(oneByte);
				stompBufferPack.setStatus(Status.METHOD);
			}else if(status == Status.METHOD){
				if(Constants.isLf(oneByte)){
					byte[] packed = stompBufferPack.getPacked();
					stompBufferPack.setMethodBytes(packed);
					stompBufferPack.setStatus(Status.HEADER);
				}else if(Constants.isTerminus(oneByte)){
					byte[] packed = stompBufferPack.getPacked();
					stompBufferPack.setMethodBytes(packed);
					stompBufferPack.setStatus(Status.TERMINUS);
				}else{
					stompBufferPack.addByte(oneByte);
				}
			}else if(status == Status.HEADER){
				if(Constants.isLf(oneByte)){
					stompBufferPack.addHeaderBytes(stompBufferPack.getPacked());
					stompBufferPack.setStatus(Status.HEADER_LF);
				}else if(Constants.isTerminus(oneByte)){
					stompBufferPack.addHeaderBytes(stompBufferPack.getPacked());
					stompBufferPack.setStatus(Status.TERMINUS);
				}else{
					stompBufferPack.addByte(oneByte);
				}
			}else if(status == Status.HEADER_LF){
				if(Constants.isLf(oneByte)){
					stompBufferPack.setStatus(Status.BODY);
				}else if(Constants.isTerminus(oneByte)){
					stompBufferPack.setStatus(Status.TERMINUS);
				}else{
					stompBufferPack.addByte(oneByte);
					stompBufferPack.setStatus(Status.HEADER);
				}
			}else if(status == Status.BODY){
				stompBufferPack.increateReadBodySize();
				if(Constants.isTerminus(oneByte)){
					stompBufferPack.setBodyBytes(stompBufferPack.getPacked());
					stompBufferPack.setStatus(Status.TERMINUS);
				}else{
					stompBufferPack.addByte(oneByte);
					if(stompBufferPack.isContentLengthFlag()){
						if(stompBufferPack.getContentLength() < stompBufferPack.getReadBodySize()){
							stompBufferPack.setBodyBytes(stompBufferPack.getPacked());
							stompBufferPack.setStatus(Status.TERMINUS);
						}
					}
				}
			}
			if(stompBufferPack.getStatus() == Status.TERMINUS){
				selectionKey.attach(null);
				Message message = new MessageImpl(
						server.getUnique(), 
						Message.MSG_TYPE_STOMP, 
						PhysicalConnectionManager.createPhysicalConnectionKey((SocketChannel)selectionKey.channel()), 
						stompBufferPack, null);
				logger.log(Level.INFO, "stomp gather put msg {0}", message.getMessageId());
				logger.log(Level.FINEST, message.toString());
				try {
					messageQueue.put(message);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "gather fail to put message", e);
				}
				if( readBuffer.remaining() > 0){
					StompBufferPack neoStompBufferPack = new StompBufferPack();
					stompBufferPack = neoStompBufferPack;
					selectionKey.attach(stompBufferPack);
				}
			}
		}
		return readSize;
	}
}
