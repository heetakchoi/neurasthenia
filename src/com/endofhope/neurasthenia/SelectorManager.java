/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author endofhope
 *
 */
public class SelectorManager{
	
	private static final Logger logger = Logger.getLogger("selector");

	private ConcurrentLinkedQueue<Selector> selectorQueue;
	private int initQueueSize;
	
	protected SelectorManager(int initQueueSize){
		this.initQueueSize = initQueueSize;
		List<Selector> backboneList = new ArrayList<Selector>();
		for(int i=0; i<initQueueSize; i++){
			try {
				backboneList.add(Selector.open());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "selector initialize fail", e);
			}
		}
		selectorQueue = new ConcurrentLinkedQueue<Selector>(backboneList);
	}
	public Selector getSelector(){
		Selector selector = selectorQueue.poll();
		if(selector == null){
			logger.log(Level.SEVERE, "selector poll empty, current size {0}, increase size or check leak selector", initQueueSize);
		}
		return selector;
	}
	public void backSelector(Selector selector){
		if(selector == null){
			try {
				selector = Selector.open();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "selector creation fail", e);
			} finally {
				logger.log(Level.INFO, "selectorQueue.size {0} and add selector {1}", new Object[]{selectorQueue.size(), selector});
			}
		}
		if(selector != null){
			selectorQueue.offer(selector);
		}
	}
	public int getInitQueueSize(){
		return initQueueSize;
	}
	public int getIdleQueueSize(){
		return selectorQueue.size();
	}
}
