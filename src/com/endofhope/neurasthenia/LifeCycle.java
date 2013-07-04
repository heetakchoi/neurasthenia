/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;
/**
 * 
 * @author endofhope
 *
 */
public interface LifeCycle {
	public boolean isRunning();
	public void boot();
	public void down();
}
