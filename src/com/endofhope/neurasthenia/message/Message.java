/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.message;

import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.gather.BufferPack;
/**
 * 
 * @author endofhope
 *
 */
public interface Message {
	
	public static final int MSG_TYPE_STOMP = 1;
	public static final int MSG_TYPE_HTTP = 2;
	public static final int MSG_TYPE_HTTP_CLOSE = 3;
	public static final int MSG_TYPE_APN = 4;
	public static final int MSG_TYPE_STOMP_CLOSE = 5;
	public static final int MSG_TYPE_BYPASS = 6;
	public static final int MSG_TYPE_ECHO = 7;
	
	public String getMessageId();
	public int getMessageType();
	public PhysicalConnectionKey getPhysicalConnectionKey();
	public BufferPack getBufferPack();
	public byte[] getData();
}
