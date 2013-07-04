/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;

import java.net.Socket;
import java.nio.channels.SocketChannel;
/**
 * 
 * @author endofhope
 *
 */
public class PhysicalConnectionKey {
	private String remoteAddress;
	private int remotePort;
	private int localPort;

	private String baseExpression;
	
	protected PhysicalConnectionKey(SocketChannel socketChannel){
		Socket socket = socketChannel.socket();
		remoteAddress = socket.getInetAddress().getHostAddress();
		remotePort = socket.getPort();
		localPort = socket.getLocalPort();
		
		StringBuilder sb = new StringBuilder()
		.append("RA:").append(remoteAddress).append(" ")
		.append("RP:").append(remotePort).append(" ")
		.append("LP:").append(localPort).append(" ");
		baseExpression = sb.toString();
	}
	
	@Override
	public boolean equals(Object other){
		boolean isEqual = false;
		if(other instanceof PhysicalConnectionKey){
			PhysicalConnectionKey otherPhysicalConnectionKey = (PhysicalConnectionKey)other;
			if(remoteAddress.equals(otherPhysicalConnectionKey.remoteAddress)
					&& remotePort == otherPhysicalConnectionKey.remotePort
					&& localPort == otherPhysicalConnectionKey.localPort){
				isEqual = true;
			}
		}
		return isEqual;
	}
	@Override
	public int hashCode(){
		return baseExpression.hashCode();
	}
	@Override
	public String toString(){
		return baseExpression;
	}
}
