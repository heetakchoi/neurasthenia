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
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.message.MessageImpl;

/**
 * 
 * @author endofhope
 *
 */
public class EchoGather extends AbstractGather{
	
	private static final Logger logger = Logger.getLogger("gather");

	public EchoGather(Server server, String id, String serviceType, int port,
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
		
		BufferPack bufferPack = (BufferPack)selectionKey.attachment();
		if(bufferPack == null){
			bufferPack = new HttpBufferPack();
			selectionKey.attach(bufferPack);
		}
		while(readBuffer.remaining()> 0){
			byte oneByte = readBuffer.get();
			if(oneByte == Constants.LF){
				selectionKey.attach(null);
				Message message = new MessageImpl(
						server.getUnique(), 
						Message.MSG_TYPE_ECHO, 
						PhysicalConnectionManager.createPhysicalConnectionKey((SocketChannel)selectionKey.channel()), 
						bufferPack, bufferPack.getPacked());
				try {
					messageQueue.put(message);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "gather fail to put message", e);
				}
				if( readBuffer.remaining() > 0){
					BufferPack neoBufferPack = new HttpBufferPack();
					bufferPack = neoBufferPack;
					selectionKey.attach(bufferPack);
				}
			}else{
				bufferPack.addByte(oneByte);
			}
		}
		return readSize;
	}
}
