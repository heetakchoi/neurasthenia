package com.endofhope.neurasthenia.webcontainer.servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPattern {

	private String urlPattern;
	private String targetName;
	private Pattern pattern;
	
	protected UrlPattern(String urlPattern, String targetName){
		this.urlPattern = urlPattern;
		this.targetName = targetName;
		init();
	}
	private void init(){
		pattern = Pattern.compile(urlPattern);
	}
	protected boolean isMatched(String url){
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}
	protected String getTargetName(){
		return targetName;
	}
}
