package com.endofhope.neurasthenia;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.endofhope.neurasthenia.connection.LogicalConnectionManager;

public class Neurasthenia {

	public void startup(){
		if("true".equals(System.getProperty("jmxmonitor"))){
			bootWithJMX();
		}else{
			bootSilent();
		}		
	}
	private MBeanServer mbeanServer = null;
	private void bootWithJMX(){
		Server server = new ServerImpl("svr_");
		server.boot();
		LogicalConnectionManager logicalConnectionManager = server.getLogicalConnectionManager();
		ObjectName serverImplMBeanName = null;
		ObjectName logicalConnectionManagerMBeanName = null;
		try {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
			serverImplMBeanName = new ObjectName("ServerImplAgent:type=Server");
			mbeanServer.registerMBean(server, serverImplMBeanName);
			
			logicalConnectionManagerMBeanName = new ObjectName("LogicalConnectionManagerAgent:type=Connection");
			mbeanServer.registerMBean(logicalConnectionManager, logicalConnectionManagerMBeanName);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}
	
	private void bootSilent(){
		Server server = new ServerImpl("svr_");
		server.boot();		
	}
}
