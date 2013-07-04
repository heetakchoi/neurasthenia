/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.scatter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.message.Message;

/**
 * 
 * @author endofhope
 *
 */
public abstract class AbstractScatter implements Scatter{
	
	private static final Logger logger = Logger.getLogger("scatter");

	protected Server server;
	protected String id;
	protected String serviceType;
	protected BlockingQueue<Message> messageQueue;
	
	public AbstractScatter(
			Server server, 
			String id, String serviceType, 
			BlockingQueue<Message> messageQueue){
		this.server = server;
		this.id = id;
		this.serviceType = serviceType;
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
	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}
	private volatile boolean running;
	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void boot() {
		running = true;
		scatterThread = new ScatterThread();
		scatterThread.start();
		logger.log(Level.INFO, "scatter {0} {1} booted", new Object[]{id, serviceType});
	}
	private ScatterThread scatterThread;
	class ScatterThread extends Thread{
		@Override
		public void run(){
			ThreadPoolExecutor threadPoolExecutor = server.getThreadPoolExecutor();
			while(running){
				try {
					Message message = messageQueue.take();
					threadPoolExecutor.execute(createWorker(message));
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "scatter message queue interrupted", e);
				}
			}
		}
	}
	@Override
	public void down() {
		running = false;
		scatterThread.interrupt();
		messageQueue.clear();
		logger.log(Level.INFO, "{0} {1} downed", new Object[]{serviceType, id});
	}

	@Override
	public abstract Runnable createWorker(Message message);
}
