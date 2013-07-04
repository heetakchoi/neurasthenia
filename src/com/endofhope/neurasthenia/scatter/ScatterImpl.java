/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.scatter;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.connection.PhysicalConnection;
import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.util.StringUtil;

/**
 * 
 * @author endofhope
 *
 */
public class ScatterImpl extends AbstractScatter{
	
	private static final Logger logger = Logger.getLogger("scatter");
	
	public static final long WRITE_TRY_TOTAL_TIME = 1000L;
	public static final int WRITE_TRY = 5;
	public static final long WRITE_TRY_UNIT_TIME = ScatterImpl.WRITE_TRY_TOTAL_TIME / (long)ScatterImpl.WRITE_TRY;

	public ScatterImpl(Server server, String id, String serviceType,
			BlockingQueue<Message> messageQueue) {
		super(server, id, serviceType, messageQueue);
	}

	@Override
	public Runnable createWorker(Message message) {
		Runnable worker = new ScatterWorker(message);
		return worker;
	}
	class ScatterWorker implements Runnable{
		private Message message;
		private ScatterWorker(Message message){
			this.message = message;
		}
		@Override
		public void run() {
			PhysicalConnectionKey physicalConnectionKey = message.getPhysicalConnectionKey();
			PhysicalConnection physicalConnection = server.getPhysicalConnectionManager().getConnection(physicalConnectionKey);
			if(physicalConnection == null){
				server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
				return;
			}
			SocketChannel socketChannel = physicalConnection.getSocketChannel();
			byte[] dataBytes = message.getData();
			
			logger.log(Level.FINER, "scatter scatter id:{0}", message.getMessageId());
			
			ByteBuffer byteBuffer = ByteBuffer.wrap(dataBytes);
			if(socketChannel != null && socketChannel.isOpen()){
				Selector writeSelector = server.getSelectorManager().getSelector();
				try {
					synchronized(socketChannel){
						if(writeSelector == null){
							logger.log(Level.WARNING, "write fail, cause acquire selector fail");
						}else{
							socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
							int retryRemainNumber = ScatterImpl.WRITE_TRY;
							while(byteBuffer.hasRemaining()){	
								writeSelector.select(ScatterImpl.WRITE_TRY_UNIT_TIME);
								retryRemainNumber --;
								if(retryRemainNumber <= 0){
									throw new IllegalStateException("retry number exceed");
								}
								Set<SelectionKey> selectionKeySet = writeSelector.selectedKeys();
								Iterator<SelectionKey> selectionKeyIter = selectionKeySet.iterator();
								while(selectionKeyIter.hasNext()){
									SelectionKey selectionKey = selectionKeyIter.next();
									selectionKeyIter.remove();
									if(!selectionKey.isValid()){
										selectionKey.cancel();
										continue;
									}
									if(selectionKey.isWritable()){
										int writtenSize = socketChannel.write(byteBuffer);
										if(writtenSize > 0){
											if("true".equals(System.getProperty("scatter.log"))){
												int orgPosition = byteBuffer.position();
												byteBuffer.position(orgPosition - writtenSize);
												byte[] writtenBytes = new byte[writtenSize];
												byteBuffer.get(writtenBytes);
												logger.log(Level.FINEST, "scatter to {0} msg\n{1}",
														new Object[]{physicalConnectionKey,
														StringUtil.makeUTF8(writtenBytes)});
											}
										}
									}
								}
							}
						}
					}
				} catch (IOException e) {
					logger.log(Level.WARNING, "Write error", e);
					server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
					try {
						writeSelector.close();
					} catch (IOException e1) {
						logger.log(Level.SEVERE, "writeSelector close fail", e1);
					} finally {
						writeSelector = null;
					}
				} catch (Throwable t){
					logger.log(Level.WARNING, "Unknown error", t);
					server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
					try {
						writeSelector.close();
					} catch (IOException e1) {
						logger.log(Level.SEVERE, "writeSelector close fail", e1);
					} finally {
						writeSelector = null;
					}
				} finally {
					server.getSelectorManager().backSelector(writeSelector);
					if(message.getMessageType() == Message.MSG_TYPE_HTTP_CLOSE
							|| message.getMessageType() == Message.MSG_TYPE_STOMP_CLOSE
					){
						// 끊어버린다.
						server.getPhysicalConnectionManager().closePhysicalConnection(physicalConnectionKey);
						server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
					}
				}
			}else{
				logger.log(Level.WARNING, "closed connection");
			}
		}
	}
}
