/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia;
/**
 * 
 * @author endofhope
 *
 */
public class Constants {
	
	public static final byte CR = (byte)'\r';
	public static final byte LF = (byte)'\n';
	public static final byte TERMINUS = (byte)'\u0000';
	
	public static final char cCR = (char)Constants.CR;
	public static final char cLF = (char)Constants.LF;
	public static final char cTERMINUS = (char)Constants.TERMINUS;
	
	public static boolean isCr(byte oneByte){
		return (oneByte == Constants.CR);
	}
	public static boolean isLf(byte oneByte){
		return (oneByte == Constants.LF);
	}
	
	public static boolean isNotCr(byte oneByte){
		return !isCr(oneByte);
	}
	public static boolean isNotLf(byte oneByte){
		return !isLf(oneByte);
	}
	
	public static boolean isTerminus(byte oneByte){
		return (oneByte == Constants.TERMINUS);
	}
	public static boolean isNotTerminus(byte oneByte){
		return !isTerminus(oneByte);
	}
	
	public static final String STOMP_QUEUE_PREFIX = "/queue/me2day/nc/";
	
	public static final String DEFAULT_HTTP_CHARSET = "ISO-8859-1";
	public static final byte[] CRLF = new byte[]{'\r', '\n'};
	public static final String CRLFStr = "\r\n";
	public static final String SPStr = " ";
	public static final int HTTP_VERSION_11 = 11;
	
	public static final String MEMCACHED_APN_PREFIX_DEV = "me2day:dev_ME2DAY_APN_ID_";
	public static final String MEMCACHED_APN_PREFIX_INHOUSE = "me2day:inhouse_ME2DAY_APN_ID_";
	public static final String MEMCACHED_APN_PREFIX_PRODUCTION = "me2day:ME2DAY_APN_ID_";
	
	public static final String TOPIC_MANAGER_ATTRIBUTE_NAME = "__TOPIC_MANAGER";
	public static final String TOPIC_PHYSICAL_CONNECTION_KEY_ATTRIBUTE_NAME = "__TOPIC_PHYSICAL_CONNECTION_KEY";
}
