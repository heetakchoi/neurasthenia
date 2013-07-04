/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author endofhope
 *
 */
public class PhysicalConnection {
	
	private static final Logger logger = Logger.getLogger("connection.physical");

	private PhysicalConnectionKey physicalConnectionKey;
	private SocketChannel socketChannel;
	private long acceptTime;
	private long lastAccessTime;
	protected PhysicalConnection(SocketChannel socketChannel){
		physicalConnectionKey = new PhysicalConnectionKey(socketChannel);
		this.socketChannel = socketChannel;
		acceptTime = System.currentTimeMillis();
		lastAccessTime = acceptTime;
	}
	public PhysicalConnectionKey getPhysicalConnectionKey(){
		return physicalConnectionKey;
	}
	/**
	 * socket channel 을 빼내와서 쓸 때에는 반드시 socketChannel 에 lock 을 걸고
	 * 다 쓴 후에는 반드시 lock 을 풀어 주어야 한다.
	 * 그렇지 않으면 다른 쓰레드에서 접근할 때 동시성 문제가 발생할 수 있다.
	 * @return
	 */
	public SocketChannel getSocketChannel(){
		lastAccessTime = System.currentTimeMillis();
		return socketChannel;
	}
	public long getAcceptTime(){
		return acceptTime;
	}
	public long getLastAccessTime(){
		return lastAccessTime;
	}
	public void close(){
		if(socketChannel != null){
			try {
				socketChannel.close();
				socketChannel.socket().close();
			} catch (IOException e) {
				logger.log(Level.WARNING, "try physical connection but socket channel close fail", e);
			}
		}
	}
}
