/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.util;
/**
 * 
 * @author endofhope
 *
 */
public class NumberUtil {
	public static final byte[] intToBytes(int i){
		byte[] bytes = new byte[4];
		bytes[0] = (byte)( i >> 24 );
		bytes[1] = (byte)( (i << 8) >> 24 );
		bytes[2] = (byte)( (i << 16) >> 24);
		bytes[3] = (byte)( (i << 24) >> 24);
		return bytes;
	}
	
	public static final int bytesToInt(byte[] bytes){
		int value = 0;
		for(int i=0; i<4; i++){
			int shift = (4 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}
