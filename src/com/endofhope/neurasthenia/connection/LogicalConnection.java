/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;

/**
 * 
 * @author endofhope
 *
 */
public class LogicalConnection {
	
	public static final int CONNECTION_TYPE_STOMP_CONNECTED = 1;
	public static final int CONNECTION_TYPE_STOMP_SUBSCRIBED = 2;
	public static final int CONNECTION_TYPE_COMET_SUBSCRIBED = 3;
	
	private static final int CATEGORY_STOMP = 1;
	private static final int CATEGORY_COMET = 2;
	
	public static int getCategory(int contentType){
		int category = 0;
		switch(contentType){
		case LogicalConnection.CONNECTION_TYPE_STOMP_CONNECTED :
			category = LogicalConnection.CATEGORY_STOMP;
			break;
		case LogicalConnection.CONNECTION_TYPE_STOMP_SUBSCRIBED :
			category = LogicalConnection.CATEGORY_STOMP;
			break;
		case LogicalConnection.CONNECTION_TYPE_COMET_SUBSCRIBED :
			category = LogicalConnection.CATEGORY_COMET;
		}
		return category;
	}
	
	public static boolean isSameCategory(int connectionType1, int connectionType2){
		return (LogicalConnection.getCategory(connectionType1) == LogicalConnection.getCategory(connectionType2));
	}
	
	private String userId;
	private int connectionType;
	private PhysicalConnectionKey physicalConnectionKey;
	private ConnectionEventHandler connectionEventHandler;
	
	protected LogicalConnection(String userId, int connectionType, PhysicalConnectionKey physicalConnectionKey, ConnectionEventHandler connectionEventHandler){
		this.userId = userId;
		this.connectionType = connectionType;
		this.physicalConnectionKey = physicalConnectionKey;
		this.connectionEventHandler = connectionEventHandler;
	}
	
	public String getUserId(){
		return userId;
	}
	public int getConnectionType(){
		return connectionType;
	}
	public PhysicalConnectionKey getPhysicalConnectionKey(){
		return physicalConnectionKey;
	}
	public ConnectionEventHandler getConnectionEventHandler(){
		return connectionEventHandler;
	}
	public void setConnectionType(int connectionType){
		this.connectionType = connectionType;
	}
	
	@Override
	public boolean equals(Object otherObj){
		boolean resultFlag = false;
		if(otherObj instanceof LogicalConnection){
			LogicalConnection other = (LogicalConnection)otherObj;
			if(other.userId.equals(userId)
					&& other.connectionType == connectionType
					&& other.physicalConnectionKey.equals(physicalConnectionKey)){
				resultFlag = true;
			}
		}
		return resultFlag;
	}
	@Override
	public int hashCode(){
		return (physicalConnectionKey.toString()+userId+connectionType).hashCode();
	}
}
