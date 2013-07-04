/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.scatter;

import java.util.concurrent.BlockingQueue;

import com.endofhope.neurasthenia.Service;
import com.endofhope.neurasthenia.message.Message;

/**
 * messageQueue 에 대기하다가
 * message 가 있으면 createWorker 가 반환한 Runnable 로 Message 를 처리한다.
 * 
 * @author endofhope
 *
 */
public interface Scatter extends Service{

	public BlockingQueue<Message> getMessageQueue();
	public Runnable createWorker(Message message);
}
