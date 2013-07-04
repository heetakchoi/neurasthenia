/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.comet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.endofhope.neurasthenia.Constants;
import com.endofhope.neurasthenia.Server;
import com.endofhope.neurasthenia.config.ConfigManager;
import com.endofhope.neurasthenia.connection.ConnectionEventHandler;
import com.endofhope.neurasthenia.connection.LogicalConnection;
import com.endofhope.neurasthenia.connection.LogicalConnectionManager;
import com.endofhope.neurasthenia.connection.PhysicalConnectionKey;
import com.endofhope.neurasthenia.message.Message;
import com.endofhope.neurasthenia.message.MessageImpl;
import com.endofhope.neurasthenia.util.StringUtil;

/**
 * 
 * @author endofhope
 *
 */
public class TopicManager {

	private Server server;
	private Map<Integer, Topic> topicMap;
	private AtomicInteger topicId;
	
	public TopicManager(Server server){
		this.server = server;
		topicId = new AtomicInteger(0);
		topicMap = new ConcurrentHashMap<Integer, Topic>();
	}
	public Set<Integer> getTopicIdSet(){
		return topicMap.keySet();
	}
	public String getTopicName(int topicId){
		return topicMap.get(new Integer(topicId)).getTopicName();
	}
	public List<String> getUserNameList(int topicId){
		return topicMap.get(new Integer(topicId)).getUserList();
	}
	public List<String> getLastMessageList(int topicId){
		Topic topic = topicMap.get(topicId);
		return topic.getMessageList();
	}
	public void createTopic(String topicName){
		int nextId = topicId.addAndGet(1);
		Topic topic = new Topic(nextId, topicName);
		topicMap.put(new Integer(nextId), topic);
	}
	public void removeUser(int topicId, String userName){
		Topic topic = topicMap.get(topicId);
		topic.removeUser(userName);
	}
	public void removeTopic(int topicId){
		topicMap.remove(new Integer(topicId));
	}
	public boolean subscribeTo(
			final int topicId, String userName, 
			PhysicalConnectionKey physicalConnectionKey,
			ConnectionEventHandler connectionEventHandler){
		boolean subscribeFlag = false;
		Topic topic = topicMap.get(new Integer(topicId));
		if(topic == null){
			subscribeFlag = false;
		}else{
			LogicalConnectionManager logicalConnectionManager = server.getLogicalConnectionManager();
			List<PhysicalConnectionKey> duplicatedPhysicalConnectionKeyList 
			= logicalConnectionManager.addLogicalConnection(
					userName, LogicalConnection.CONNECTION_TYPE_COMET_SUBSCRIBED, physicalConnectionKey, connectionEventHandler
			);
			// 같은 connectionType 중 같은 category 이고 중복된 것은 끊어버린다
			for(PhysicalConnectionKey duplicatedPhysicalConnectionKey : duplicatedPhysicalConnectionKeyList){
				logicalConnectionManager.removeLogicalConnectionByPhysicalConnectionKey(duplicatedPhysicalConnectionKey);
			}
			topic.addUser(userName);
			subscribeFlag = true;
		}
		return subscribeFlag;
	}
	public void sendMessageToTopic(String msg, int topicId){
		byte[] dataBytes = StringUtil.getUTF8Bytes(msg); 
		String hexSize = String.format("%x", dataBytes.length);
		String hexSizeStr = hexSize + Constants.CRLFStr;
		byte[] hexSizeBytes = StringUtil.getUTF8Bytes(hexSizeStr);
		List<byte[]> bytesList = new ArrayList<byte[]>();
		bytesList.add(hexSizeBytes);
		bytesList.add(dataBytes);
		bytesList.add(Constants.CRLF);
		byte[] chunkedBytes = StringUtil.copyBytes(bytesList);
		
		Topic topic = topicMap.get(new Integer(topicId));
		topic.addMessage(msg);
		List<String> userList = topic.getUserList();
		Iterator<String> userIter = userList.iterator();
		ConfigManager configManager = ConfigManager.getInstance();
		BlockingQueue<Message> messageQueue = 
			server.getMessageQueue(
					configManager.getHandlerInfoList().get(0).getMessageQueueId());
		LogicalConnectionManager logicalConnectionManager = server.getLogicalConnectionManager();
		while(userIter.hasNext()){
			String userName = userIter.next();
			LogicalConnection logicalConnection = logicalConnectionManager.getLogicalConnection(userName, LogicalConnection.CONNECTION_TYPE_COMET_SUBSCRIBED);
			if(logicalConnection != null){
				PhysicalConnectionKey physicalConnectionKey = 
					logicalConnection.getPhysicalConnectionKey();
				Message message  = new MessageImpl(
						getTopicName(topicId)+"_"+userName, 
						Message.MSG_TYPE_BYPASS, 
						physicalConnectionKey, 
						null, 
						chunkedBytes);
				try {
					messageQueue.put(message);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
