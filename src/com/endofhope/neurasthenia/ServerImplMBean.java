/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;
/**
 * 
 * @author endofhope
 *
 */
public interface ServerImplMBean {
	public String getServerId();
	public int getActiveThreadCount();
	public String[] getMessageQueueInfo();
}
