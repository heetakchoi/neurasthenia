/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.webcontainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.endofhope.neurasthenia.config.ConfigManager;

/**
 * 
 * @author endofhope
 *
 */
public class MimeManager {
	private static MimeManager mm;
	private Properties mimeProps;
	
	private MimeManager(){
		ConfigManager cm = ConfigManager.getInstance();
		StringBuilder sb = new StringBuilder(cm.getHomeDir())
		.append(File.separator).append("config").append(File.separator).append("mime.properties");
		String mimePath = sb.toString();
		File mimeFile = new File(mimePath);
		mimeProps = new Properties();
		try {
			mimeProps.load(new FileReader(mimeFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public String getMimeType(String extension){
		String mimeType = mimeProps.getProperty(extension);
		if(mimeType == null){
			mimeType = mimeProps.getProperty(extension.toLowerCase());
		}
		if(mimeType == null){
			mimeType = mimeProps.getProperty("bin");
		}
		return mimeType;
	}
	
	public static synchronized MimeManager getInstance(){
		if(mm == null){
			mm = new MimeManager();
		}
		return mm;
	}
}
