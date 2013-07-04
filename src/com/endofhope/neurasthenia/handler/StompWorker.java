/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.gather.StompBufferPack;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.util.StringUtil;

/**
 * 
 * @author endofhope
 *
 */
public 	class StompWorker implements Runnable{

	private static final Logger logger = Logger.getLogger("stomp.worker");

	private Message message;
	private Server server;
	protected StompWorker(Message message, Server server){
		this.message = message;
		this.server = server;
	}
	@Override
	public void run() {
		List<Message> outMessageList = processStomp(message);
		try {
			if(outMessageList != null){
				for(Message outMessage : outMessageList){
					server.getMessageQueue(ConfigManager.getInstance()
							.getScatterInfoList().get(0)
							.getMessageQueueId()).put(outMessage);
				}
			}
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "add scatter queue fail", e);
		}
	}
	private List<Message> processStomp(Message inMessage){
		logger.log(Level.FINEST, "stomp handle msg {0}", inMessage.toString());

		StompBufferPack stompBufferPack = (StompBufferPack)message.getBufferPack();
		String method = StringUtil.makeUTF8(stompBufferPack.getMethodBytes()).trim();
		List<byte[]> headerBytesList = stompBufferPack.getHeaderBytesList();
		Map<String, String> headerMap = new HashMap<String, String>();
		for(byte[] headerBytes : headerBytesList){
			String header = StringUtil.makeUTF8(headerBytes);
			int indexOfColon = header.indexOf(":");
			if(indexOfColon > 0){
				headerMap.put(
						header.substring(0, indexOfColon).trim(), 
						header.substring(indexOfColon+1));
			}
		}
		List<Message> outMessageList = new ArrayList<Message>();
		if("CONNECT".equals(method)){
			
		}else if("SUBSCRIBE".equals(method)){
			
		}else if("SEND".equals(method)){
			
		}
		return outMessageList;
	}
}
