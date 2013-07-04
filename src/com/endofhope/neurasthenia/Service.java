/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;


/**
 * 
 * @author endofhope
 *
 */
public interface Service extends LifeCycle{

	public Server getServer();
	
	public String getId();
	public String getServiceType();
}
