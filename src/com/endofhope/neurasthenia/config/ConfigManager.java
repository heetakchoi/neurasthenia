/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
/**
 * 
 * @author endofhope
 *
 */
public class ConfigManager {

	private static final Logger logger = Logger.getLogger("config");

	private static ConfigManager configManager;
	private ConfigManager(){
		readFromFile();
	}
	public static synchronized ConfigManager getInstance(){
		if(configManager == null){
			configManager = new ConfigManager();
		}
		return configManager;
	}

	private String homeDir;
	public String getHomeDir(){
		return homeDir;
	}
	
	public class SelectorManagerInfo{
		private int initQueueSize;
		private SelectorManagerInfo(int initQueueSize){
			this.initQueueSize = initQueueSize;
		}
		public int getInitQueueSize(){
			return initQueueSize;
		}
	}
	private SelectorManagerInfo selectorManagerInfo;
	public SelectorManagerInfo getSelectorManagerInfo(){
		return selectorManagerInfo;
	}
	
	public class CheckConnectionInfo{
		private CheckConnectionInfo(long initDelay, long delay, int during){
			this.initDelay = initDelay;
			this.delay = delay;
			this.during = during;
		}
		private long initDelay;
		private long delay;
		private int during;
		public long getInitDelay(){
			return initDelay;
		}
		public long getDelay(){
			return delay;
		}
		public int getDuring(){
			return during;
		}
	}
	private CheckConnectionInfo checkConnectionInfo;
	public CheckConnectionInfo getCheckConnectionInfo(){
		return checkConnectionInfo;
	}

	public class ThreadPoolExecutorInfo{
		private int corePoolSize;
		private int maximumPoolSize;
		private int keepAliveTime;
		private String queueClass;
		private ThreadPoolExecutorInfo(
				int corePoolSize, int maximumPoolSize, int keepAliveTime, 
				String queueClass){
			this.corePoolSize = corePoolSize;
			this.maximumPoolSize = maximumPoolSize;
			this.keepAliveTime = keepAliveTime;
			this.queueClass = queueClass;
		}
		public int getCorePoolSize() {
			return corePoolSize;
		}
		public int getMaximumPoolSize() {
			return maximumPoolSize;
		}
		public int getKeepAliveTime() {
			return keepAliveTime;
		}
		public String getQueueClass() {
			return queueClass;
		}
	}
	private ThreadPoolExecutorInfo threadPoolExecutorInfo;
	public ThreadPoolExecutorInfo getThreadPoolExecutorInfo(){
		return threadPoolExecutorInfo;
	}

	public class MessageQueueInfo{
		private String id;
		private String queueClass;
		private MessageQueueInfo(String id, String queueClass){
			this.id = id;
			this.queueClass = queueClass;
		}
		public String getId(){
			return id;
		}
		public String getQueueClass(){
			return queueClass;
		}
	}
	private List<MessageQueueInfo> messageQueueInfoList;
	public List<MessageQueueInfo> getMessageQueueInfoList(){
		return messageQueueInfoList;
	}

	public class GatherInfo{
		private String id;
		private String serviceType;
		private int port;
		private int readSelectTimeout;
		private int readBufferSize;
		private String messageQueueId;
		private GatherInfo(
				String id, String serviceType, 
				int port, int readSelectTimeout, int readBufferSize, 
				String messageQueueId){
			this.id = id;
			this.serviceType = serviceType;
			this.port = port;
			this.readSelectTimeout = readSelectTimeout;
			this.readBufferSize = readBufferSize;
			this.messageQueueId = messageQueueId;
		}
		public String getId() {
			return id;
		}
		public String getServiceType() {
			return serviceType;
		}
		public int getPort() {
			return port;
		}
		public int getReadSelectTimeout() {
			return readSelectTimeout;
		}
		public int getReadBufferSize() {
			return readBufferSize;
		}
		public String getMessageQueueId() {
			return messageQueueId;
		}
	}
	private List<GatherInfo> gatherInfoList;
	public List<GatherInfo> getGatherInfoList(){
		return gatherInfoList;
	}

	public class HandlerInfo{
		private String id;
		private String serviceType;
		private String messageQueueId;
		private HandlerInfo(String id, String serviceType, String messageQueueId){
			this.id = id;
			this.serviceType = serviceType;
			this.messageQueueId = messageQueueId;
		}
		public String getId() {
			return id;
		}
		public String getServiceType() {
			return serviceType;
		}
		public String getMessageQueueId() {
			return messageQueueId;
		}
	}
	private List<HandlerInfo> handlerInfoList;
	public List<HandlerInfo> getHandlerInfoList(){
		return handlerInfoList;
	}

	public class ScatterInfo{
		private String id;
		private String serviceType;
		private String messageQueueId;
		private ScatterInfo(String id, String serviceType, String messageQueueId){
			this.id = id;
			this.serviceType = serviceType;
			this.messageQueueId = messageQueueId;
		}
		public String getId() {
			return id;
		}
		public String getServiceType() {
			return serviceType;
		}
		public String getMessageQueueId() {
			return messageQueueId;
		}
	}
	private List<ScatterInfo> scatterInfoList;
	public List<ScatterInfo> getScatterInfoList(){
		return scatterInfoList;
	}

	public class WebContainerInfo {
		private WebContainerInfo(
				String id, String serviceType,
				String representFiles,
				String contextRootDirectory,
				String commonClassPath,
				String encoding){
			this.id = id;
			this.serviceType = serviceType;
			this.representFiles = representFiles;
			this.contextRootDirectory = contextRootDirectory;
			this.commonClassPath = commonClassPath;
			this.encoding = encoding;
			contextInfoList = new ArrayList<ContextInfo>();
			representFileList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(representFiles);
			while(st.hasMoreTokens()){
				representFileList.add(st.nextToken());
			}
		}
		private void addContextInfo(ContextInfo contextInfo){
			contextInfoList.add(contextInfo);
		}
		private List<String> representFileList;
		private String id;
		private String serviceType;
		private String representFiles;
		private String contextRootDirectory;
		private String commonClassPath;
		private String encoding;
		private List<ContextInfo> contextInfoList;
		public String getId() {
			return id;
		}
		public String getServiceType() {
			return serviceType;
		}
		public String getRepresentFiles(){
			return representFiles;
		}
		public String getContextRootDirectory() {
			return contextRootDirectory;
		}
		public String getCommonClassPath() {
			return commonClassPath;
		}
		public String getEncoding() {
			return encoding;
		}
		public List<ContextInfo> getContextInfoList() {
			return contextInfoList;
		}
		public List<String> getRepresentFileList(){
			return representFileList;
		}
	}
	public class ContextInfo {
		private ContextInfo(String contextName, String contextPath, String contextDirectory, int contextVersion){
			this.contextName = contextName;
			this.contextPath = contextPath;
			this.contextDirectory = contextDirectory;
			this.contextVersion = contextVersion;
		}
		private String contextName;
		private String contextPath;
		private String contextDirectory;
		private int contextVersion;
		public String getContextName() {
			return contextName;
		}
		public String getContextPath() {
			return contextPath;
		}
		public String getContextDirectory() {
			return contextDirectory;
		}
		public int getContextVersion() {
			return contextVersion;
		}
	}
	private WebContainerInfo webContainerInfo;
	public WebContainerInfo getWebContainerInfo(){
		return webContainerInfo;
	}
	
	@SuppressWarnings("unchecked")
	private void readFromFile(){
		homeDir = System.getProperty("home.dir", ".");
		StringBuilder configSB = new StringBuilder(homeDir).append(File.separator)
		.append("config").append(File.separator)
		.append("config.xml");
		SAXBuilder saxb = new SAXBuilder();
		Document doc = null;
		try {
			doc = saxb.build(configSB.toString());
			Element rootElement = doc.getRootElement();
			
			Element selectorManagerElement = rootElement.getChild("selector-manager");
			selectorManagerInfo = new SelectorManagerInfo(
					Integer.parseInt(selectorManagerElement.getChildTextTrim("init-queue-size")));
			
			Element checkConnectionElement = rootElement.getChild("check-connection");
			if(checkConnectionElement != null){
				checkConnectionInfo = new CheckConnectionInfo(
						Long.parseLong(checkConnectionElement.getChildTextTrim("init-delay")),
						Long.parseLong(checkConnectionElement.getChildTextTrim("delay")),
						Integer.parseInt(checkConnectionElement.getChildTextTrim("during"))
				);
			}

			Element threadPoolExecutorElement = rootElement.getChild("thread-pool-executor");
			threadPoolExecutorInfo = new ThreadPoolExecutorInfo(
					Integer.parseInt(threadPoolExecutorElement.getChildTextTrim("core-pool-size")),
					Integer.parseInt(threadPoolExecutorElement.getChildTextTrim("maximum-pool-size")),
					Integer.parseInt(threadPoolExecutorElement.getChildTextTrim("keep-alive-time")),
					threadPoolExecutorElement.getChildTextTrim("queue-class")
			);

			messageQueueInfoList = new ArrayList<MessageQueueInfo>();
			Element messageQueueListElement = rootElement.getChild("message-queue-list");
			List<Element> messageQueueElementList = messageQueueListElement.getChildren("message-queue");
			for(Element messageQueueElement : messageQueueElementList){
				messageQueueInfoList.add(
						new MessageQueueInfo(
								messageQueueElement.getChildTextTrim("id"), 
								messageQueueElement.getChildTextTrim("queue-class")
						)
				);
			}

			gatherInfoList = new ArrayList<GatherInfo>();
			Element gatherListElement = rootElement.getChild("gather-list");
			List<Element> gatherElementList = gatherListElement.getChildren("gather");
			for(Element gatherElement : gatherElementList){
				gatherInfoList.add(
						new GatherInfo(
								gatherElement.getChildTextTrim("id"),
								gatherElement.getChildTextTrim("service-type"),
								Integer.parseInt(gatherElement.getChildTextTrim("port")),
								Integer.parseInt(gatherElement.getChildTextTrim("read-select-timeout")),
								Integer.parseInt(gatherElement.getChildTextTrim("read-buffer-size")),
								gatherElement.getChildTextTrim("ref-message-queue-id")
						)
				);
			}

			handlerInfoList = new ArrayList<HandlerInfo>();
			Element handlerListElement = rootElement.getChild("handler-list");
			List<Element> handlerElementList = handlerListElement.getChildren("handler");
			for(Element handlerElement : handlerElementList){
				handlerInfoList.add(new HandlerInfo(
						handlerElement.getChildTextTrim("id"),
						handlerElement.getChildTextTrim("service-type"),
						handlerElement.getChildTextTrim("ref-message-queue-id")));
			}

			scatterInfoList = new ArrayList<ScatterInfo>();
			Element scatterListElement = rootElement.getChild("scatter-list");
			List<Element> scatterElementList = scatterListElement.getChildren("scatter");
			for(Element scatterElement : scatterElementList){
				scatterInfoList.add(
						new ScatterInfo(
								scatterElement.getChildTextTrim("id"),
								scatterElement.getChildTextTrim("service-type"),
								scatterElement.getChildTextTrim("ref-message-queue-id")
						)
				);
			}
			
			Element webContainerElement = rootElement.getChild("web-container");
			if(webContainerElement != null){
				webContainerInfo = new WebContainerInfo(
						webContainerElement.getChildTextTrim("id"),
						webContainerElement.getChildTextTrim("service-type"),
						webContainerElement.getChildTextTrim("represent-files"),
						webContainerElement.getChildTextTrim("context-root-directory"),
						webContainerElement.getChildTextTrim("common-class-path"),
						webContainerElement.getChildTextTrim("encoding")
				);
				Element contextListElement = webContainerElement.getChild("context-list");
				List<Element> contextElementList = contextListElement.getChildren("context");
				for(Element contextElement : contextElementList){
					ContextInfo contextInfo = new ContextInfo(
							contextElement.getChildTextTrim("context-name"),
							contextElement.getChildTextTrim("context-path"),
							contextElement.getChildTextTrim("context-directory"),
							Integer.parseInt(contextElement.getChildTextTrim("context-version")));
					webContainerInfo.addContextInfo(contextInfo);
				}
			}
			
		} catch (JDOMException e) {
			logger.log(Level.SEVERE, "parse error", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "config.xml file error", e);
		}
	}
}
