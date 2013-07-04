/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.comet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author endofhope
 *
 */
public class Topic {

	private int topicId;
	private String topicName;
	private List<String> userList;
	private List<String> messageList;
	
	protected Topic(int topicId, String topicName){
		this.topicId = topicId;
		this.topicName = topicName;
		userList = new ArrayList<String>();
		messageList = new LinkedList<String>();
	}
	public int getTopicId(){
		return topicId;
	}
	public String getTopicName(){
		return topicName;
	}
	protected void addUser(String userName){
		if(!userList.contains(userName)){
			userList.add(userName);
		}
	}
	protected List<String> getUserList(){
		return userList;
	}
	public List<String> getMessageList(){
		return messageList;
	}
	protected void addMessage(String message){
		messageList.add(message);
		if(messageList.size() > 10){
			messageList.remove(0);
		}
	}
	protected void removeUser(String userName){
		userList.remove(userName);
	}
}
