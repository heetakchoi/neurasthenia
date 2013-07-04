/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.config.ConfigManager.CheckConnectionInfo;
import com.endofhope.neurasthenia.config.ConfigManager.GatherInfo;
import com.endofhope.neurasthenia.config.ConfigManager.HandlerInfo;
import com.endofhope.neurasthenia.config.ConfigManager.MessageQueueInfo;
import com.endofhope.neurasthenia.config.ConfigManager.ScatterInfo;
import com.endofhope.neurasthenia.config.ConfigManager.ThreadPoolExecutorInfo;
import com.endofhope.neurasthenia.connection.LogicalConnectionManager;
import com.endofhope.neurasthenia.connection.PhysicalConnectionManager;
import com.endofhope.neurasthenia.gather.Gather;
import com.endofhope.neurasthenia.handler.Handler;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.scatter.Scatter;
import com.endofhope.neurasthenia.webcontainer.WebContainer;

/**
 * 
 * @author endofhope
 *
 */
public class ServerImpl implements Server, ServerImplMBean{
	
	private static final Logger logger = Logger.getLogger("server");
	
	private String serverId;
	private PhysicalConnectionManager physicalConnectionManager;
	private LogicalConnectionManager logicalConnectionManager;
	private ThreadPoolExecutor threadPoolExecutor;
	private Map<String, BlockingQueue<Message>> messageQueueMap;
	private SelectorManager selectorManager;
	private WebContainer webContainer;
	
	public ServerImpl(String serverId){
		this.serverId = serverId;
	}

	private volatile boolean running;
	@Override
	public boolean isRunning() {
		return running;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void boot(){
		running = true;
		atomicInt = new AtomicInteger(0);
		physicalConnectionManager = new PhysicalConnectionManager();
		logicalConnectionManager = new LogicalConnectionManager(physicalConnectionManager);
		
		ConfigManager configManager = ConfigManager.getInstance();
		
		selectorManager = new SelectorManager(configManager.getSelectorManagerInfo().getInitQueueSize());
		
		ThreadPoolExecutorInfo tpei = configManager.getThreadPoolExecutorInfo();
		BlockingQueue<Runnable> workQueue = null;
		try {
			Class clazz = Class.forName(tpei.getQueueClass());
			workQueue = (BlockingQueue<Runnable>)clazz.newInstance();
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "workqueue error", e);
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, "workqueue error", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "workqueue error", e);
		}
		threadPoolExecutor = new ThreadPoolExecutor(
				tpei.getCorePoolSize(),
				tpei.getMaximumPoolSize(), 
				tpei.getKeepAliveTime(), 
				TimeUnit.SECONDS,
				workQueue);

		messageQueueMap = new ConcurrentHashMap<String, BlockingQueue<Message>>(); 
		for(MessageQueueInfo messageQueueInfo : configManager.getMessageQueueInfoList()){
			try {
				Class clazz = Class.forName(messageQueueInfo.getQueueClass());
				BlockingQueue<Message> messageQueue = (BlockingQueue<Message>)clazz.newInstance();
				messageQueueMap.put(messageQueueInfo.getId(), messageQueue);
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "messagequeue error", e);
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, "messagequeue error", e);
			} catch (IllegalAccessException e) {
				logger.log(Level.SEVERE, "messagequeue error", e);
			}
		}
		
		scatterMap = new ConcurrentHashMap<String, Scatter>();
		for(ScatterInfo scatterInfo : configManager.getScatterInfoList()){
			Scatter scatter = ServiceFactory.createScatter(this, scatterInfo);
			scatter.boot();
			scatterMap.put(scatterInfo.getId(), scatter);
		}
		
		handlerMap = new ConcurrentHashMap<String, Handler>();
		for(HandlerInfo handlerInfo : configManager.getHandlerInfoList()){
			Handler handler = ServiceFactory.createHandler(this, handlerInfo);
			handler.boot();
		}
		
		gatherMap = new ConcurrentHashMap<String, Gather>();
		for(GatherInfo gatherInfo : configManager.getGatherInfoList()){
			Gather gather = ServiceFactory.createGather(this, gatherInfo);
			gather.boot();
			gatherMap.put(gatherInfo.getId(), gather);
		}
		if(configManager.getWebContainerInfo() != null){
			webContainer = new WebContainer(this);
			webContainer.boot();
		}
		CheckConnectionInfo checkConnectionInfo = configManager.getCheckConnectionInfo();
		if(checkConnectionInfo != null){
			checkConnectionManager = new CheckConnectionManager(
					this,
					checkConnectionInfo.getInitDelay(), 
					checkConnectionInfo.getDelay(),
					checkConnectionInfo.getDuring());
			checkConnectionManager.boot();
		}
	}
	
	private CheckConnectionManager checkConnectionManager;
	
	private Map<String, Gather> gatherMap;
	private Map<String, Handler> handlerMap;
	private Map<String, Scatter> scatterMap;
	
	@Override
	public void down() {
		running = false;
		if(checkConnectionManager != null){
			checkConnectionManager.down();
		}
		Set<String> gatherKeySet = gatherMap.keySet();
		Iterator<String> gatherKeyIter = gatherKeySet.iterator();
		while(gatherKeyIter.hasNext()){
			gatherMap.get(gatherKeyIter.next()).down();
		}
		gatherMap.clear();
		Set<String> handlerKeySet = handlerMap.keySet();
		Iterator<String> handlerKeyIter = handlerKeySet.iterator();
		while(handlerKeyIter.hasNext()){
			handlerMap.get(handlerKeyIter.next()).down();
		}
		Set<String> scatterKeySet = scatterMap.keySet();
		Iterator<String> scatterKeyIter = scatterKeySet.iterator();
		while(scatterKeyIter.hasNext()){
			scatterMap.get(scatterKeyIter.next()).down();
		}
		scatterMap.clear();
		if(webContainer != null){
			webContainer.down();
		}
	}
	@Override
	public String getServerId() {
		return serverId;
	}	
	@Override
	public PhysicalConnectionManager getPhysicalConnectionManager() {
		return physicalConnectionManager;
	}
	@Override
	public LogicalConnectionManager getLogicalConnectionManager() {
		return logicalConnectionManager;
	}
	@Override
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}
	@Override
	public Map<String, BlockingQueue<Message>> getMessageQueueMap() {
		return messageQueueMap;
	}
	@Override
	public BlockingQueue<Message> getMessageQueue(String queueId) {
		return messageQueueMap.get(queueId);
	}
	@Override
	public SelectorManager getSelectorManager(){
		return selectorManager;
	}
	@Override
	public WebContainer getWebContainer(){
		return webContainer;
	}
	private AtomicInteger atomicInt;
	@Override
	public String getUnique(){
		return serverId + atomicInt.addAndGet(1);
	}
	
	@Override
	public int getActiveThreadCount(){
		return threadPoolExecutor.getActiveCount();
	}
	@Override
	public String[] getMessageQueueInfo(){
		String[] messageQueueInfoArray = null;
		if(messageQueueMap != null){
			int size = messageQueueMap.size();
			messageQueueInfoArray = new String[size];
			Set<String> queueNameSet = messageQueueMap.keySet();
			Iterator<String> queueNameIter = queueNameSet.iterator();
			int i=0;
			while(queueNameIter.hasNext()){
				String queueName = queueNameIter.next();
				int queueSize = messageQueueMap.get(queueName).size();
				messageQueueInfoArray[i] = queueName + " : " + queueSize;
				i++;
			}
		}
		return messageQueueInfoArray;
	}
}
