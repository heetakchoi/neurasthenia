/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;

import com.endofhope.neurasthenia.config.ConfigManager.GatherInfo;
import com.endofhope.neurasthenia.config.ConfigManager.HandlerInfo;
import com.endofhope.neurasthenia.config.ConfigManager.ScatterInfo;
import com.endofhope.neurasthenia.gather.EchoGather;
import com.endofhope.neurasthenia.gather.Gather;
import com.endofhope.neurasthenia.gather.HttpGather;
import com.endofhope.neurasthenia.gather.StompGather;
import com.endofhope.neurasthenia.handler.Handler;
import com.endofhope.neurasthenia.handler.HandlerImpl;
import com.endofhope.neurasthenia.scatter.Scatter;
import com.endofhope.neurasthenia.scatter.ScatterImpl;
/**
 * 
 * @author endofhope
 *
 */
public class ServiceFactory {

	public static Gather createGather(Server server, GatherInfo gatherInfo){
		Gather gather = null;
		if("gather_stomp".equals(gatherInfo.getServiceType())){
			gather = new StompGather(server, 
					gatherInfo.getId(), gatherInfo.getServiceType(), 
					gatherInfo.getPort(), gatherInfo.getReadSelectTimeout(), gatherInfo.getReadBufferSize(), 
					server.getMessageQueue(gatherInfo.getMessageQueueId()));
		}else if("gather_http".equals(gatherInfo.getServiceType())){
			gather = new HttpGather(server,
					gatherInfo.getId(), gatherInfo.getServiceType(),
					gatherInfo.getPort(), gatherInfo.getReadSelectTimeout(), gatherInfo.getReadBufferSize(),
					server.getMessageQueue(gatherInfo.getMessageQueueId()));
		}else if("gather_echo".equals(gatherInfo.getServiceType())){
			gather = new EchoGather(server,
					gatherInfo.getId(), gatherInfo.getServiceType(),
					gatherInfo.getPort(), gatherInfo.getReadSelectTimeout(), gatherInfo.getReadBufferSize(),
					server.getMessageQueue(gatherInfo.getMessageQueueId()));
		}
		return gather;
	}
	public static Handler createHandler(Server server, HandlerInfo handlerInfo){
		Handler handler = null;
		if("handler".equals(handlerInfo.getServiceType())){
			handler = new HandlerImpl(server, 
					handlerInfo.getId(), handlerInfo.getServiceType(), 
					server.getMessageQueue(handlerInfo.getMessageQueueId()));
		}
		return handler;
	}
	public static Scatter createScatter(Server server, ScatterInfo scatterInfo){
		Scatter scatter = null;
		if("scatter".equals(scatterInfo.getServiceType())){
			scatter = new ScatterImpl(server,
					scatterInfo.getId(), scatterInfo.getServiceType(),
					server.getMessageQueue(scatterInfo.getMessageQueueId()));
		}
		return scatter;
	}
//	public static WebContainer createWebContainer(Server server, WebContainerInfo webContainerInfo){
//		return new WebContainer(
//				server,
//				webContainerInfo.getId(),
//				webContainerInfo.getServiceType(),
//				webContainerInfo.getContextRootDirectory(),
//				webContainerInfo.getCommonClassPath(),
//				webContainerInfo.getEncoding(),
//				webContainerInfo.getContextInfoList());
//	}

}
