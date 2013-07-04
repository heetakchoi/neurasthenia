/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.gather;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;

import com.endofhope.neurasthenia.Service;
import com.endofhope.neurasthenia.message.Message;

/**
 * port 에 대해 대기하다가
 * 데이터가 들어오면 onReceive 로 읽는다.
 * Message 가 완결되면 messageQueue 에 넣고 다음 읽기를 기다린다.
 * 
 * @author endofhope
 *
 */
public interface Gather extends Service{

	public int getPort();
	public int getReadSelectTimeout();
	public int getReadBufferSize();
	public BlockingQueue<Message> getMessageQueue();
	public int onReceive(SelectionKey selectionKey) throws IOException;
}
