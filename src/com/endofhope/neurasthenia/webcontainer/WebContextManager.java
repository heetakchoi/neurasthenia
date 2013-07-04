/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.endofhope.neurasthenia.LifeCycle;
import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.webcontainer.servlet.ServletContextImpl;

/**
 * 
 * @author endofhope
 *
 */
public class WebContextManager implements LifeCycle{
	
	private Map<String, ServletContextImpl> contextPathMap;
	private String contextRootDirectory;
	public String getContextRootDirectory(){
		return contextRootDirectory;
	}
	private String commonClassPath;
	public String getCommonClassPath(){
		return commonClassPath;
	}
	
	private boolean contextRootDirectoryValidFlag = false;
	private String encoding;
	public String getEncoding(){
		return encoding;
	}
	
	private WebContainer webContainer;
	protected WebContextManager(
			WebContainer webContainer,
			String contextRootDirectory, 
			String commonClassPath, 
			String encoding){
		this.webContainer = webContainer;
		if(contextRootDirectory == null){
			contextRootDirectory = ConfigManager.getInstance().getHomeDir().concat(File.separator).concat("webctx");
		}
		File contextRootDirectoryFile = new File(contextRootDirectory);
		if(contextRootDirectoryFile.exists()){
			this.contextRootDirectory = contextRootDirectoryFile.getAbsolutePath();
			contextRootDirectoryValidFlag = true;
		}
		this.commonClassPath = commonClassPath;
		contextPathMap = new ConcurrentHashMap<String, ServletContextImpl>();
		this.encoding = encoding;
	}

	public void addWebContext(String directory, String contextName, String contextPath, int version){
		File directoryFile = new File(directory);
		if(directoryFile.getAbsolutePath().equals(directoryFile.getPath())){
			directory = directoryFile.getAbsolutePath();
		}else{
			if(contextRootDirectoryValidFlag){
				directory = contextRootDirectory + File.separator + directory;
			}else{
				throw new IllegalArgumentException("directory is not valid");
			}
		}
		ServletContextImpl sci = new ServletContextImpl(this, directory, contextName, contextPath, version);
			sci.boot();
			contextPathMap.put(contextPath, sci);
		
	}
	
	public ServletContextImpl getServletContext(String contextPath){
		return contextPathMap.get(contextPath);
	}

	@Override
	public void boot(){
		running = true;
	}

	@Override
	public void down() {
		Set<String> keySet = contextPathMap.keySet();
		Iterator<String> keyIter = keySet.iterator();
		while(keyIter.hasNext()){
			String key = keyIter.next();
			ServletContextImpl sci = contextPathMap.get(key);
			sci.down();
		}
		running = false;
	}
	private volatile boolean running;
	@Override
	public boolean isRunning(){
		return running;
	}
	
	public WebContainer getWebContainer(){
		return webContainer;
	}
}
