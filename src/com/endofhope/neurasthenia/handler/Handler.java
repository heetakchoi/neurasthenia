/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.handler;

import java.util.concurrent.BlockingQueue;

import com.endofhope.neurasthenia.Service;
import com.endofhope.neurasthenia.message.Message;


/**
 * message queue 에 대기하다가
 * message 가 들어오면 createWorker 가 반환한 Runnable 에 message 를 처리를 위임한다.
 * @author endofhope
 *
 */
public interface Handler extends Service{
	public BlockingQueue<Message> getMessageQueue();
	public Runnable createWorker(Message message);
}
