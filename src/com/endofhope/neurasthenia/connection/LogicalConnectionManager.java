/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author endofhope
 *
 */
public class LogicalConnectionManager implements LogicalConnectionManagerMBean{
	
	private static final Logger logger = Logger.getLogger("logical.connection");

	private Map<String, List<LogicalConnection>> logicalConnectionMap;
	private PhysicalConnectionManager physicalConnectionManager;
	private Map<PhysicalConnectionKey, LogicalConnection> physicalKeyMap;
	
	public LogicalConnectionManager(PhysicalConnectionManager physicalConnectionManager){
		this.physicalConnectionManager = physicalConnectionManager;
		logicalConnectionMap = new ConcurrentHashMap<String, List<LogicalConnection>>();
		physicalKeyMap = new ConcurrentHashMap<PhysicalConnectionKey, LogicalConnection>();
	}
	
	public void removeLogicalConnectionByPhysicalConnectionKey(PhysicalConnectionKey physicalConnectionKey){
		LogicalConnection target = physicalKeyMap.remove(physicalConnectionKey);
		if(target != null){
			removeConnection(target.getUserId(), target.getConnectionType());
		}
	}
	/**
	 * userId, connectionType 으로 physicalConnectionKey 를 등록한다.
	 * @param userId
	 * @param connectionType
	 * @param physicalConnectionKey
	 */
	public List<PhysicalConnectionKey> addLogicalConnection(String userId, int connectionType, PhysicalConnectionKey physicalConnectionKey, ConnectionEventHandler connectionEventHandler){
		List<PhysicalConnectionKey> duplicatedPhysicalConnectionKeyList = new ArrayList<PhysicalConnectionKey>();
		// 중복 제거한다.
		List<LogicalConnection> logicalConnectionList = logicalConnectionMap.get(userId);
		if(logicalConnectionList == null){
			logicalConnectionList = new ArrayList<LogicalConnection>();
		}
		List<LogicalConnection> cleanedLogicalConnectionList = new ArrayList<LogicalConnection>();
		synchronized(logicalConnectionList){
			for(LogicalConnection logicalConnection : logicalConnectionList){
				if(LogicalConnection.isSameCategory(logicalConnection.getConnectionType(), connectionType)){
					duplicatedPhysicalConnectionKeyList.add(logicalConnection.getPhysicalConnectionKey());
				}else{
					cleanedLogicalConnectionList.add(logicalConnection);
				}
			}
			LogicalConnection logicalConnection = new LogicalConnection(userId, connectionType, physicalConnectionKey, connectionEventHandler);
			cleanedLogicalConnectionList.add(logicalConnection);
			logicalConnectionMap.put(userId, cleanedLogicalConnectionList);
			physicalKeyMap.put(physicalConnectionKey, logicalConnection);
		}
		logger.log(Level.FINER,"add logical connection {0}, {1}, {2}",
							new Object[]{userId, connectionType, physicalConnectionKey});
		return duplicatedPhysicalConnectionKeyList;
	}
	/**
	 * 등록된 LogicalConnection 중 userId 가 가진 LogicalConnection 들을 반환한다.
	 * @param userId
	 * @return
	 */
	public List<LogicalConnection> getLogicalConnectionList(String userId){
		return logicalConnectionMap.get(userId);
	}
	public LogicalConnection getLogicalConnection(String userId, int connectionType){
		LogicalConnection target = null;
		List<LogicalConnection> logicalConnectionList = logicalConnectionMap.get(userId);
		if(logicalConnectionList != null){
			synchronized(logicalConnectionList){
				for(LogicalConnection logicalConnection : logicalConnectionList){
					if(logicalConnection.getConnectionType() == connectionType){
						target = logicalConnection;
						break;
					}
				}
			}
		}
		return target;
	}
	/**
	 * LogicalConnectionManager 에서 제거한다.
	 * physicalKeyMap 에서 제거한다.
	 * PhysicalConnectionManager 에서 제거한다.
	 * Physical socket 연결을 끊는다. 
	 * @param userId
	 * @param connectionType
	 */
	public void removeConnection(String userId, int connectionType){
		List<LogicalConnection> logicalConnectionList = logicalConnectionMap.get(userId);
		if(logicalConnectionList != null){
			synchronized(logicalConnectionList){
				List<LogicalConnection> removeList = new ArrayList<LogicalConnection>();
				for(int i=0; i<logicalConnectionList.size(); i++){
					LogicalConnection logicalConnection = logicalConnectionList.get(i);
					if(logicalConnection.getConnectionType() == connectionType){
						removeList.add(logicalConnection);
					}
				}
				for(int i=0; i<removeList.size(); i++){
					LogicalConnection removeLogicalConnection = removeList.get(i);
					ConnectionEventHandler connectionEventHandler = removeLogicalConnection.getConnectionEventHandler();
					if(connectionEventHandler != null){
						connectionEventHandler.onCloseEvent();
					}
					logicalConnectionList.remove(removeLogicalConnection);
					PhysicalConnectionKey physicalConnectionKey = removeLogicalConnection.getPhysicalConnectionKey();
					physicalKeyMap.remove(physicalConnectionKey);
					physicalConnectionManager.closePhysicalConnection(physicalConnectionKey);
					logger.log(Level.FINER, "remove logical connection {0}, {1}, {2}",
							new Object[]{userId, connectionType, physicalConnectionKey});
				}
			}
			if(logicalConnectionList.size() < 1){
				logicalConnectionMap.remove(userId);
			}
		}
	}

	@Override
	public int getLogicalConnectionSize() {
		return logicalConnectionMap.size();
	}

	@Override
	public String[] getLogicalConnectionInfoArrayByUserId(String userId) {
		List<LogicalConnection> logicalConnectionList = logicalConnectionMap.get(userId);
		String[] logicalConnectionInfoArray = null;
		if(logicalConnectionList != null){
			int size = logicalConnectionList.size();
			logicalConnectionInfoArray = new String[size];
			for(int i=0; i<logicalConnectionList.size(); i++){
				LogicalConnection logicalConnection = logicalConnectionList.get(i);
				StringBuilder sb = new StringBuilder();
				sb.append(logicalConnection.getUserId()).append(":")
				.append(logicalConnection.getConnectionType()).append(":")
				.append(logicalConnection.getPhysicalConnectionKey().toString());
				logicalConnectionInfoArray[i] = sb.toString();
			}
		}else{
			logicalConnectionInfoArray = new String[]{};
		}
		return logicalConnectionInfoArray;
	}

	@Override
	public int getPhysicalConnectionSize() {
		return physicalConnectionManager.getPhysicalConnectionMap().size();
	}
	
	@Override
	public String[] getPhysicalConnectionKeyArray() {
		Set<PhysicalConnectionKey> keySet = physicalConnectionManager.getPhysicalConnectionMap().keySet();
		int size = keySet.size();
		String[] physicalConnectionKeyArray = new String[size];
		Iterator<PhysicalConnectionKey> keyIter = keySet.iterator();
		int i=0;
		while(keyIter.hasNext()){
			physicalConnectionKeyArray[i] = keyIter.next().toString();
			i++;
		}
		return physicalConnectionKeyArray;
	}

	@Override
	public String[] getLogicalConnectionInfoArray() {
		Set<String> idSet = logicalConnectionMap.keySet();
		int size = idSet.size();
		String[] logicalConnectionInfoArray = new String[size];
		Iterator<String> idIter = idSet.iterator();
		int i=0;
		while(idIter.hasNext()){
			String id = idIter.next();
			StringBuilder sb = new StringBuilder();
			sb.append(id).append(" size [").append(logicalConnectionMap.get(id).size()).append("]");
			logicalConnectionInfoArray[i] = sb.toString();
			i++;
		}
		return logicalConnectionInfoArray;
	}
}
