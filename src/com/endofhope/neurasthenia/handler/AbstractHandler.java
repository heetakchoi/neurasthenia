/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;

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
public abstract class AbstractHandler implements Handler{

	private static final Logger logger = Logger.getLogger("handler");
	
	protected Server server;
	protected String id;
	protected String serviceType;
	protected BlockingQueue<Message> messageQueue;
	
	public AbstractHandler(Server server,
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
		handlerThread = new HandlerThread();
		handlerThread.start();
		logger.log(Level.INFO, "handler {0} booted", id);
	}
	private HandlerThread handlerThread;
	class HandlerThread extends Thread{
		@Override
		public void run(){
			ThreadPoolExecutor threadPoolExecutor = server.getThreadPoolExecutor();
			while(running){
				try {
					Message message = messageQueue.take();
					Runnable r = createWorker(message);
					if(r != null){
						threadPoolExecutor.execute(r);
					}else{
						logger.log(Level.WARNING, "createWorker fail.\nMessage\n{0}", message.toString());
					}
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "handler interrupted", e);
				} catch (Throwable t){
					logger.log(Level.SEVERE, "processing fail", t);
				}
			}
		}
	}
	@Override
	public void down() {
		running = false;
		handlerThread.interrupt();
		messageQueue.clear();
		logger.log(Level.INFO, "handler {0} downed", id);
	}

	@Override
	public abstract Runnable createWorker(Message message);
}
