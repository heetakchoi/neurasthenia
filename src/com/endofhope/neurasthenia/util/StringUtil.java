/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.util;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * @author endofhope
 *
 */
public class StringUtil {
	
	private static final Logger logger = Logger.getLogger("util");
	
	public static final String makeUTF8(byte[] bytes){
		String str = null;
		try {
			str = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "UTF-8 encoding error, continue by using default instead", e);
			str = new String(bytes);
		}
		return str;
	}
	public static final byte[] getUTF8Bytes(String str){
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "UTF-8 decoding error, continue by using default instead", e);
			bytes = str.getBytes();
		}
		return bytes;
	}	public static final String getPrettyErrorHead(String msg){
		StringBuilder sb = new StringBuilder();
		sb.append("<html>\n");
		sb.append("  <head>\n");
		sb.append("    <title>500 Error</title>\n");
		sb.append("  </head>\n");
		sb.append("  <body>\n");
		sb.append("    <h2>500 ERROR</h2><br />detail : ");
		sb.append(msg);
		sb.append("    <hr />");
		sb.append("    <pre>\n");
		return sb.toString();
	}
	public static final String getPrettyErrorTail(){
		StringBuilder sb = new StringBuilder();
		sb.append("    </pre>\n");
		sb.append("  </body>\n");
		sb.append("</html>\n");
		return sb.toString();
	}
	public static final byte[] copyBytes(List<byte[]> byteArrayList){
		int size = 0;
		for(byte[] byteArray : byteArrayList){
			size = size + byteArray.length;
		}
		byte[] targetArray = new byte[size];
		int start = 0;
		for(byte[] byteArray : byteArrayList){
			System.arraycopy(byteArray, 0, targetArray, start, byteArray.length);
			start = start + byteArray.length;
		}
		return targetArray;
	}
}
