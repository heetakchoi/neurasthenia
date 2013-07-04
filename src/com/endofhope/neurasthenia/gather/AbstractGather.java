/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.gather;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.connection.PhysicalConnectionManager;
import com.endofhope.neurasthenia.message.Message;

/**
 * 
 * @author endofhope
 *
 */
public abstract class AbstractGather implements Gather{
	
	private static final Logger logger = Logger.getLogger("gather");
	
	protected Server server;
	protected String id;
	protected String serviceType;
	protected int port;
	protected int readSelectTimeout;
	protected int readBufferSize;
	protected BlockingQueue<Message> messageQueue;
	
	public AbstractGather(
			Server server, 
			String id, String serviceType, 
			int port,
			int readSelectTimeout,
			int readBufferSize,
			BlockingQueue<Message> messageQueue){
		this.server = server;
		this.id = id;
		this.serviceType = serviceType;
		this.port = port;
		this.readSelectTimeout = readSelectTimeout;
		this.readBufferSize = readBufferSize;
		this.messageQueue = messageQueue;
	}
	@Override
	public Server getServer() {
		return server;
	}
	@Override
	public String getId() {
		return id;
	}
	@Override
	public String getServiceType() {
		return serviceType;
	}
	@Override
	public int getPort() {
		return port;
	}
	@Override
	public int getReadSelectTimeout(){
		return readSelectTimeout;
	}
	@Override
	public int getReadBufferSize(){
		return readBufferSize;
	}
	@Override
	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	private volatile boolean running;
	@Override
	public void boot() {
		running = true;
		readBuffer = ByteBuffer.allocate(readBufferSize);
		selectorThread = new SelectorThread();
		selectorThread.setName("gather");
		selectorThread.start();
		logger.log(Level.INFO, "gather {0} {1} {2} booted", new Object[]{id, serviceType, port});
	}
	protected SelectorThread selectorThread;
	protected ByteBuffer readBuffer;
	protected Selector selector;
	class SelectorThread extends Thread{
		@Override
		public void run(){
			try {
				selector = Selector.open();
				ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
				ServerSocket serverSocket = serverSocketChannel.socket();
				serverSocket.bind(new InetSocketAddress(port));
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				running = false;
				logger.log(Level.SEVERE, "select initialize fail", e);
			}
			PhysicalConnectionManager physicalConnectionManager = server.getPhysicalConnectionManager();
			while(running){
				try {
					selector.select((long)readSelectTimeout * 1000L);
					if(!running){
						selector.close();
						break;
					}
					Set<SelectionKey> selectionKeySet = selector.selectedKeys();
					Iterator<SelectionKey> selectionKeyIter = selectionKeySet.iterator();
					while(selectionKeyIter.hasNext()){
						SelectionKey selectionKey = selectionKeyIter.next();
						selectionKeyIter.remove();
						if(!selectionKey.isValid()){
							continue;
						}
						try{
							if(selectionKey.isAcceptable()){
								ServerSocketChannel oneServerSocketChannel = (ServerSocketChannel)selectionKey.channel();
								SocketChannel socketChannel = oneServerSocketChannel.accept();
								socketChannel.configureBlocking(false);
								socketChannel.register(selector, SelectionKey.OP_READ);
								physicalConnectionManager.register(socketChannel);
							}
							if(selectionKey.isReadable()){
								SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
								PhysicalConnectionKey physicalConnectionKey = PhysicalConnectionManager.createPhysicalConnectionKey(socketChannel);
								try{
									int readSize = onReceive(selectionKey);
									if(readSize < 0){
										cleanUp(selectionKey, physicalConnectionManager, physicalConnectionKey);
									}
								}catch(IOException ioe){
									cleanUp(selectionKey, physicalConnectionManager, physicalConnectionKey);
								}
							}
						}catch(CancelledKeyException e){
							logger.log(Level.WARNING, "key canceled", e);
						}
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "error at select", e);
				}
			}
		}
	}
	private void cleanUp(SelectionKey selectionKey, 
			PhysicalConnectionManager physicalConnectionManager, 
			PhysicalConnectionKey physicalConnectionKey){
		selectionKey.cancel();
		physicalConnectionManager.closePhysicalConnection(physicalConnectionKey);
		server.getLogicalConnectionManager().removeLogicalConnectionByPhysicalConnectionKey(physicalConnectionKey);
		logger.log(Level.FINER, "gather {0} socket close", id);
	}
	@Override
	public void down() {
		running = false;
		try {
			selector.close();
		} catch (IOException e) {
			logger.log(Level.FINEST, "gather selector close fail", e);
		}
		logger.log(Level.INFO, "{0} {1} {2} downed ", new Object[]{id, serviceType, port});
	}
	@Override
	public boolean isRunning() {
		return running;
	}
	@Override
	public abstract int onReceive(SelectionKey selectionKey) throws IOException;
}
