/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
public class PhysicalConnectionManager{
	
	private static final Logger logger = Logger.getLogger("physical.connection");
	
	private Map<PhysicalConnectionKey, PhysicalConnection> physicalConnectionMap;
	
	protected Map<PhysicalConnectionKey, PhysicalConnection> getPhysicalConnectionMap(){
		return physicalConnectionMap;
	}

	public PhysicalConnectionManager(){
		physicalConnectionMap = new ConcurrentHashMap<PhysicalConnectionKey, PhysicalConnection>();
	}
	/**
	 * 해당 socket channel 으로 connection 을 만들고 등록한다.
	 * @param socketChannel
	 * @return
	 */
	public PhysicalConnection register(SocketChannel socketChannel){
		PhysicalConnection candidate = new PhysicalConnection(socketChannel);
		PhysicalConnectionKey candidateKey = candidate.getPhysicalConnectionKey();
		physicalConnectionMap.put(candidateKey, candidate);
		logger.log(Level.FINER, "register physical connection {0}", candidateKey);
		return candidate;
	}
	/**
	 * 해당하는 connection 을 찾아 반환한다.
	 * socket channel 이 null 이 아니면 등록하고 반환한다.
	 * @param socketChannel
	 * @return
	 */
	public PhysicalConnection getConnection(SocketChannel socketChannel){
		PhysicalConnectionKey physicalConnectionKey = new PhysicalConnectionKey(socketChannel);
		PhysicalConnection physicalConnection = physicalConnectionMap.get(physicalConnectionKey);
		if(physicalConnection == null && socketChannel != null){
			physicalConnection = register(socketChannel);
		}
		return physicalConnection;
	}
	/**
	 * 해당하는 connection 이 없으면 null 을 반환한다.
	 * @param physicalConnectionKey
	 * @return
	 */
	public PhysicalConnection getConnection(PhysicalConnectionKey physicalConnectionKey){
		return physicalConnectionMap.get(physicalConnectionKey);
	}
	private void deregister(PhysicalConnectionKey physicalConnectionKey){
		physicalConnectionMap.remove(physicalConnectionKey);
		logger.log(Level.FINER, "deregister physical connection {0}", physicalConnectionKey);
	}
	public void closePhysicalConnection(PhysicalConnectionKey physicalConnectionKey){
		PhysicalConnection physicalConnection = getConnection(physicalConnectionKey);
		if(physicalConnection != null){
			physicalConnection.close();
			logger.log(Level.FINER,"close physical connection {0}",
					new Object[]{physicalConnectionKey});
		}else{
			logger.log(Level.FINEST,"already closed physical connection {0}",
					new Object[]{physicalConnectionKey});
		}
		deregister(physicalConnectionKey);
	}
	public static PhysicalConnectionKey createPhysicalConnectionKey(SocketChannel socketChannel){
		return new PhysicalConnectionKey(socketChannel);
	}
	public void scavenge(long during){
		long criteria = System.currentTimeMillis() - (long)(during*1000L);
		List<PhysicalConnectionKey> oldList = new ArrayList<PhysicalConnectionKey>();
		PhysicalConnectionKey[] candidatePhysicalConnectionKeyArray = new PhysicalConnectionKey[physicalConnectionMap.size()];
		Set<PhysicalConnectionKey> physicalConnectionKeySet = physicalConnectionMap.keySet();
		physicalConnectionKeySet.toArray(candidatePhysicalConnectionKeyArray);
		for(PhysicalConnectionKey onePhysicalConnectionKey : candidatePhysicalConnectionKeyArray){
			PhysicalConnection onePhysicalConnection = physicalConnectionMap.get(onePhysicalConnectionKey);
			if(onePhysicalConnection != null){
				if(onePhysicalConnection.getLastAccessTime() < criteria){
					oldList.add(onePhysicalConnectionKey);
				}
			}
		}
		for(PhysicalConnectionKey physicalConnectionKey : oldList){
			PhysicalConnection physicalConnection = physicalConnectionMap.get(physicalConnectionKey);
			if(physicalConnection.getLastAccessTime() < criteria){
				closePhysicalConnection(physicalConnectionKey);
			}
		}
	}
}
