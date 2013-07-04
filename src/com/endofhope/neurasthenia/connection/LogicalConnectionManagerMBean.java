/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.connection;
/**
 * 
 * @author endofhope
 *
 */
public interface LogicalConnectionManagerMBean {

	public int getLogicalConnectionSize();
	public String[] getLogicalConnectionInfoArrayByUserId(String userId);
	
	public int getPhysicalConnectionSize();
	
	public String[] getPhysicalConnectionKeyArray();
	public String[] getLogicalConnectionInfoArray();
}
