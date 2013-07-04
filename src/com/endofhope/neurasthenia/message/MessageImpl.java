/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.message;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.gather.BufferPack;
import com.endofhope.neurasthenia.gather.HttpBufferPack;
import com.endofhope.neurasthenia.gather.StompBufferPack;
/**
 * 
 * @author endofhope
 *
 */
public class MessageImpl implements Message{

	private String messageId;
	private int messageType;
	private PhysicalConnectionKey physicalConnectionKey;
	private BufferPack bufferPack;
	private byte[] data;
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("msg detail\n")
		.append("message id              [").append(messageId).append("]").append(Constants.cLF)
		.append("message type            [").append(messageType).append("]").append(Constants.cLF)
		.append("physical connection key [").append(physicalConnectionKey).append("]").append(Constants.cLF)
		.append("data                    [").append(data).append("]").append(Constants.cLF);
		if(bufferPack instanceof StompBufferPack){
			sb.append("STOMP MSG").append(Constants.cLF);
			sb.append(bufferPack.toString());
		}else if(bufferPack instanceof HttpBufferPack){
			sb.append("HTTP MSG").append(Constants.cLF);
			sb.append(bufferPack.toString());
		}
		return sb.toString();
	}
	
	public MessageImpl(
			String messageId, int messageType, 
			PhysicalConnectionKey physicalConnectionKey, 
			BufferPack bufferPack, byte[] data){
		this.messageId = messageId;
		this.messageType = messageType;
		this.physicalConnectionKey = physicalConnectionKey;
		this.bufferPack = bufferPack;
		this.data = data;
	}
	@Override
	public String getMessageId() {
		return messageId;
	}
	@Override
	public int getMessageType() {
		return messageType;
	}
	@Override
	public PhysicalConnectionKey getPhysicalConnectionKey() {
		return physicalConnectionKey;
	}
	@Override
	public BufferPack getBufferPack() {
		return bufferPack;
	}
	@Override
	public byte[] getData() {
		return data;
	}
}
