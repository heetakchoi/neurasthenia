package com.endofhope.neurasthenia;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CheckConnectionManager implements LifeCycle{
	
	public static final int THREAD_SIZE = 3;
	
	protected CheckConnectionManager(Server server, long initDelay, long delay, int during){
		this.server = server;
		this.initDelay = initDelay;
		this.delay = delay;
		this.during = during;
	}
	private Server server;
	private long initDelay;
	private long delay;
	private int during;

	@Override
	public void boot() {
		running = true;
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(CheckConnectionManager.THREAD_SIZE);
		Runnable r = new Runnable(){
			@Override
			public void run() {
				server.getPhysicalConnectionManager().scavenge(during);
			}
		};
		scheduledThreadPoolExecutor.scheduleAtFixedRate(r, initDelay, delay, TimeUnit.MILLISECONDS);
	}
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	@Override
	public void down() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}
	private boolean running;
}
