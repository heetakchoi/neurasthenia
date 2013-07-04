/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.message.Message;

/**
 * 
 * @author endofhope
 *
 */
public class EchoWorker implements Runnable{
	private static final Logger logger = Logger.getLogger("worker");

	private Message message;
	private Server server;
	protected EchoWorker(Message message, Server server){
		this.message = message;
		this.server = server;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			server.getMessageQueue("scatter_queue").put(message);
			logger.log(Level.INFO, "echo msg {0}", new String(message.getData()));
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "scatter queue interrupted", e);
		}
	}
}
