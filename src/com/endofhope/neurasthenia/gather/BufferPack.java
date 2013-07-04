/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.gather;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author endofhope
 *
 */
public class BufferPack {
	public static final int DEFAULT_BUFFER_SIZE = 1024*8;
	protected int bufferSize = BufferPack.DEFAULT_BUFFER_SIZE;

	protected List<ByteBuffer> appendedList;
	protected ByteBuffer currentBuffer;

	public BufferPack(){
		appendedList = new ArrayList<ByteBuffer>();
		currentBuffer = ByteBuffer.allocate(bufferSize);
	}

	public void addBytes(byte[] bytes){
		int remains = currentBuffer.remaining();
		if(remains > bytes.length){
			currentBuffer.put(bytes);
		}else{
			// 어찌되었건 넘친다.
			// 기존에 들어가 있는 것들을 먼저 보내야 되므로 currentBuffer 에 있던
			// 것들을 일단 집어넣자. 지금은 bytes 는 처리되지 않은 상태이다.
			currentBuffer.flip();
			int tmpSize = currentBuffer.remaining();
			byte[] tmpBytes = new byte[tmpSize];
			currentBuffer.get(tmpBytes);
			appendedList.add(ByteBuffer.wrap(tmpBytes));
			// 들어온 bytes 를 appendedList 에 집어넣자.
			appendedList.add(ByteBuffer.wrap(bytes));
			// 다음 읽기를 위해 currentBuffer 를 초기화 해 놓는다.
			currentBuffer.clear();
		}
	}

	public void addByte(byte oneByte){
		int remains = currentBuffer.remaining();
		if(remains > 1){
			currentBuffer.put(oneByte);
		}else if(remains == 1){
			currentBuffer.put(oneByte);
			currentBuffer.flip();
			int tmpSize = currentBuffer.remaining();
			byte[] tmpBytes = new byte[tmpSize];
			currentBuffer.get(tmpBytes);
			appendedList.add(ByteBuffer.wrap(tmpBytes));
			currentBuffer.clear();
		} else {
			new IllegalStateException("BufferPack.addByte overrun buffer").printStackTrace();
		}
	}

	protected void clear(){
		for(ByteBuffer byteBuffer : appendedList){
			byteBuffer.clear();
		}
		appendedList.clear();
		currentBuffer.clear();
	}

	public byte[] getPacked(){
		currentBuffer.flip();
		int tmpSize = currentBuffer.remaining();
		byte[] tmpBytes = new byte[tmpSize];
		currentBuffer.get(tmpBytes);
		appendedList.add(ByteBuffer.wrap(tmpBytes));
		currentBuffer.clear();

		int size = 0;
		for(ByteBuffer appended : appendedList){
			size = size + appended.remaining();
		}
		byte[] packedBytes = new byte[size];
		int index = 0;
		for(ByteBuffer appended : appendedList){
			int appendedRemaining = appended.remaining();
			appended.get(packedBytes, index, appendedRemaining);
			index = index + appendedRemaining;
		}

		clear();

		return packedBytes;
	}
}
