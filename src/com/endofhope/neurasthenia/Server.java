/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.endofhope.neurasthenia.connection.LogicalConnectionManager;
import com.endofhope.neurasthenia.connection.PhysicalConnectionManager;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.webcontainer.WebContainer;

/**
 * 
 * @author endofhope
 *
 */
public interface Server extends LifeCycle{

	public LogicalConnectionManager getLogicalConnectionManager();
	public PhysicalConnectionManager getPhysicalConnectionManager();
	
	public ThreadPoolExecutor getThreadPoolExecutor();
	
	public Map<String, BlockingQueue<Message>> getMessageQueueMap();
	public BlockingQueue<Message> getMessageQueue(String queueId);
	
	public String getServerId();
	
	public String getUnique();
	
	public SelectorManager getSelectorManager();
	
	public WebContainer getWebContainer();
}
