/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.endofhope.neurasthenia.LifeCycle;
import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.comet.TopicManager;
import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.config.ConfigManager.ContextInfo;

/**
 * 
 * @author endofhope
 *
 */
public class WebContainer implements LifeCycle{

	private static Logger logger = Logger.getLogger("webcontainer");
	private boolean running;
	
	private WebContextManager wcm;
	public WebContextManager getWebContextManager(){
		return wcm;
	}
	
	private Server server;
	public WebContainer(Server server){
		this.server = server;
	}
	public Server getServer(){
		return server;
	}
	
	private TopicManager topicManager;
	public TopicManager getTopicManager(){
		return topicManager;
	}

	@Override
	public boolean isRunning(){
		return running;
	}
	@Override
	public void boot(){
		if(running){
			logger.log(Level.WARNING, "already running");
			return;
		}
		
		topicManager = new TopicManager(server);
		
		try {
            // Set JSP factory
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext",
                          true,
                          this.getClass().getClassLoader());
        } catch (Throwable t) {
            // Should not occur, obviously
            logger.log(Level.SEVERE, "Couldn't initialize Jasper", t);
        }
		
		ConfigManager configManager = ConfigManager.getInstance();
		wcm = new WebContextManager(
				this,
				configManager.getWebContainerInfo().getContextRootDirectory(),
				configManager.getWebContainerInfo().getCommonClassPath(), 
				configManager.getWebContainerInfo().getEncoding());
		
		List<ContextInfo> contextInfoList = configManager.getWebContainerInfo().getContextInfoList();
		for(ContextInfo contextInfo : contextInfoList){
			wcm.addWebContext(
					contextInfo.getContextDirectory(), 
					contextInfo.getContextName(), 
					contextInfo.getContextPath(), 
					contextInfo.getContextVersion());
			logger.log(Level.FINE, "directory [{0}], name [{1}], path [{2}], version [{3}]",
					new Object[]{ 
						contextInfo.getContextDirectory(), 
						contextInfo.getContextName(), 
						contextInfo.getContextPath(), 
						contextInfo.getContextVersion() });
		}
		
		running = true;
		logger.log(Level.FINE, "WebContainer booted");
	}

	@Override
	public void down() {
		if(!running){
			logger.log(Level.WARNING, "downing or already downed");
		}
		wcm.down();
		running = false;
		logger.log(Level.FINE, "WebContainer downed");
	}
}
