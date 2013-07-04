/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.message.Message;

/**
 * 
 * @author endofhope
 *
 */
public class HandlerImpl extends AbstractHandler{
	
	private static final Logger logger = Logger.getLogger("handler");
	
	public HandlerImpl(Server server, String id, String serviceType,
			BlockingQueue<Message> messageQueue) {
		super(server, id, serviceType, messageQueue);
	}

	@Override
	public Runnable createWorker(Message message) {
		Runnable worker = null;
		int messageType = message.getMessageType();
		switch(messageType){
		case Message.MSG_TYPE_STOMP :
			worker = new StompWorker(message, server);
			break;
		case Message.MSG_TYPE_HTTP :
			worker = new HttpWorker(message, server);
			break;
		case Message.MSG_TYPE_BYPASS :
			worker = new BypassWorker(message, server);
			break;
		case Message.MSG_TYPE_ECHO :
			worker = new EchoWorker(message, server);
			break;
		default :
			logger.log(Level.SEVERE, "Invalid message type {0}", messageType);
		}
		return worker;
	}

}
