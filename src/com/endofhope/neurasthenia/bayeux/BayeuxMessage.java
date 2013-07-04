/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.bayeux;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 
 * @author endofhope
 *
 */
public class BayeuxMessage {
	
	private static Logger logger = Logger.getLogger("bayeux");

	public static final int HANDSHAKE_REQ = 1;
	public static final int HANDSHAKE_RESP = 2;
	public static final int CONNECT_REQ = 3;
	public static final int CONNECT_RESP = 4;
	public static final int DISCONNECT_REQ = 5;
	public static final int DISCONNECT_RESP = 6;
	public static final int SUBSCRIBE_REQ = 7;
	public static final int SUBSCRIBE_RESP = 8;
	public static final int UNSUBSCRIBE_REQ = 9;
	public static final int UNSUBSCRIBE_RESP = 10;
	public static final int PUBLISH_REQ = 11;
	public static final int PUBLISH_RESP = 12;
	public static final int DELIVER_EVENT = 13;
	
	private int type;
	private String channel;
	private String version;
	private List<String> supportedConnectionTypesList;
	private String clientId;
	private String successful;
	private String error;
	private String connectionType;
	private String subscription;
	private String data;
//	private String mininumVersion;
//	private String ext;
//	private String id;
//	private Map<String, String> advice;
//	private String authSuccessful;
//	private String timestamp;
	
	public void setChannel(String channel){
		this.channel = channel;
	}
	public void setVersion(String version){
		this.version = version;
	}
	public void setSupportedConnectionTypesList(List<String> supportedConnectionTypesList){
		this.supportedConnectionTypesList = supportedConnectionTypesList;
	}
	public void setClientId(String clientId){
		this.clientId = clientId;
	}
	public void setSuccessful(String successful){
		this.successful = successful;
	}
	public void setError(String error){
		this.error = error;
	}
	public void setConnectionType(String connectionType){
		this.connectionType = connectionType;
	}
	public void setSubscription(String subscription){
		this.subscription = subscription;
	}
	public void setData(String data){
		this.data = data;
	}

	public BayeuxMessage(int type){
		this.type = type;
	}
	public BayeuxMessage(String jsonMessage) throws JSONException{
		JSONTokener jsont = new JSONTokener(jsonMessage);
		JSONArray jsona = new JSONArray(jsont);
		// FIXME 하나의 msg 만 들어온다고 가정한다.
		JSONObject jsono = jsona.getJSONObject(0);
		// channel 결정
		channel = jsono.getString("channel");
		if(channel != null && channel.startsWith("/meta/")){
			if("/meta/handshake".equals(channel)){
				// type 결정
				type = BayeuxMessage.HANDSHAKE_REQ;
				// version 결정
				version = jsono.getString("version");
				JSONArray jsonaSupportedConnectionTypes = jsono.getJSONArray("supportedConnectionTypes");
				supportedConnectionTypesList = new ArrayList<String>();
				// supportedConnectionTypes 결정
				for(int i=0; i<jsonaSupportedConnectionTypes.length(); i++){
					supportedConnectionTypesList.add(jsonaSupportedConnectionTypes.getString(i));
				}
				// Handshake req 에 대한 mandatory 부분은 모두 결정되었다.
			}else if("/meta/connect".equals(channel)){
				type = BayeuxMessage.CONNECT_REQ;
				clientId = jsono.getString("clientId");
				connectionType = jsono.getString("connectionType");
			}else if("/meta/disconnect".equals(channel)){
				type = BayeuxMessage.DISCONNECT_REQ;
				clientId = jsono.getString("clientId");
			}else if("/meta/subscribe".equals(channel)){
				type = BayeuxMessage.SUBSCRIBE_REQ;
				clientId = jsono.getString("clientId");
				subscription = jsono.getString("subscription");
			}else if("/meta/unsubscribe".equals(channel)){
				type = BayeuxMessage.UNSUBSCRIBE_REQ;
				clientId = jsono.getString("clientId");
				subscription = jsono.getString("subscription");
			}
		}else{
			type = BayeuxMessage.PUBLISH_REQ;
			data = jsono.getString("data");
		}
	}

	private JSONObject convertHandshakeReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("version", version);
		jsono.put("supportedConnectionTypes", supportedConnectionTypesList);
		return jsono;
	}
	private JSONObject convertHandshakeResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("version", version);
		jsono.put("supportedConnectionTypes", supportedConnectionTypesList);
		jsono.put("clientId", clientId);
		jsono.put("successful", successful);
		jsono.put("error", error);
		return jsono;
	}
	private JSONObject convertConnectReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("clientId", clientId);
		jsono.put("connectionType", connectionType);
		return jsono;
	}
	private JSONObject convertConnectResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("successful", successful);
		jsono.put("clientId", clientId);
		return jsono;
	}
	private JSONObject convertDisconnectReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("clientId", clientId);
		return jsono;
	}
	private JSONObject convertDisconnectResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("clientId", clientId);
		jsono.put("successful", successful);
		return jsono;
	}
	private JSONObject convertSubscribeReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("clientId", clientId);
		jsono.put("subscription", subscription);
		return jsono;
	}
	private JSONObject convertSubscribeResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("successful", successful);
		jsono.put("clientId", clientId);
		jsono.put("subscription", subscription);
		jsono.put("error", error);
		return jsono;
	}
	private JSONObject convertUnsubscribeReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("clientId", clientId);
		jsono.put("subscription", subscription);
		return jsono;
	}
	private JSONObject convertUnsubscribeResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("successful", successful);
		jsono.put("clientId", clientId);
		jsono.put("subscription", subscription);
		jsono.put("error", error);
		return jsono;
	}
	private JSONObject convertPublishReq() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("data", data);
		return jsono;
	}
	private JSONObject convertPublishResp() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("successful", successful);
		return jsono;
	}
	private JSONObject convertDeliverEvent() throws JSONException{
		JSONObject jsono = new JSONObject();
		jsono.put("channel", channel);
		jsono.put("data", data);
		return jsono;
	}
	@Override
	public String toString(){
		JSONObject jsonObject = null;
		try {
			if(type == BayeuxMessage.HANDSHAKE_REQ){
				jsonObject = convertHandshakeReq();
			}else if(type == BayeuxMessage.HANDSHAKE_RESP){
				jsonObject = convertHandshakeResp();
			}else if(type == BayeuxMessage.CONNECT_REQ){
				jsonObject = convertConnectReq();
			}else if(type == BayeuxMessage.CONNECT_RESP){
				jsonObject = convertConnectResp();
			}else if(type == BayeuxMessage.DISCONNECT_REQ){
				jsonObject = convertDisconnectReq();
			}else if(type == BayeuxMessage.DISCONNECT_RESP){
				jsonObject = convertDisconnectResp();
			}else if(type == BayeuxMessage.SUBSCRIBE_REQ){
				jsonObject = convertSubscribeReq();
			}else if(type == BayeuxMessage.SUBSCRIBE_RESP){
				jsonObject = convertSubscribeResp();
			}else if(type == BayeuxMessage.UNSUBSCRIBE_REQ){
				jsonObject = convertUnsubscribeReq();
			}else if(type == BayeuxMessage.UNSUBSCRIBE_RESP){
				jsonObject = convertUnsubscribeResp();
			}else if(type == BayeuxMessage.PUBLISH_REQ){
				jsonObject = convertPublishReq();
			}else if(type == BayeuxMessage.PUBLISH_RESP){
				jsonObject = convertPublishResp();
			}else if(type == BayeuxMessage.DELIVER_EVENT){
				jsonObject = convertDeliverEvent();
			}else{
				throw new JSONException("Invalid BayeuxMessageType "+type);
			}
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Invalid json", e);
		}
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(jsonObject);
		String result = null;
		try {
			result = jsonArray.toString(4);
		} catch (JSONException e) {
			logger.log(Level.WARNING, "Invalid json array", e);
		}
		return result;
	}
	
	
	private static void test1() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/handshake_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test2(){
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.HANDSHAKE_RESP);
		bayeuxMessage.setChannel("/meta/handshake");
		bayeuxMessage.setVersion("1.0");
		List<String> typesList = new ArrayList<String>();
		typesList.add("long-polling");
		typesList.add("callback-polling");
		bayeuxMessage.setSupportedConnectionTypesList(typesList);
		bayeuxMessage.setClientId("Un1q31d3nt1f13r");
		bayeuxMessage.setSuccessful("true");
//		bayeuxMessage.setError("eRrOr");
		System.out.println(bayeuxMessage.toString());
	}
	private static void test3() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/connect_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test4() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.CONNECT_RESP);
		bayeuxMessage.setChannel("/meta/connect");
		bayeuxMessage.setSuccessful("true");
		bayeuxMessage.setClientId("Un1q31d3nt1f13r");
		System.out.println(bayeuxMessage.toString());
	}
	private static void test5() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/disconnect_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test6() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.DISCONNECT_RESP);
		bayeuxMessage.setChannel("/meta/disconnect");
		bayeuxMessage.setClientId("Un1q31d3nt1f13r");
		bayeuxMessage.setSuccessful("true");
		System.out.println(bayeuxMessage.toString());
	}
	private static void test7() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/subscribe_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test8() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.SUBSCRIBE_RESP);
		bayeuxMessage.setChannel("/meta/subscribe");
		bayeuxMessage.setSuccessful("true");
		bayeuxMessage.setClientId("Un1q31d3nt1f13r");
		bayeuxMessage.setSubscription("/foo/**");
		bayeuxMessage.setError("403:/bar/baz:Permission Denied");
		System.out.println(bayeuxMessage.toString());
	}
	private static void test9() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/unsubscribe_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test10() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.UNSUBSCRIBE_RESP);
		bayeuxMessage.setChannel("/meta/unsubscribe");
		bayeuxMessage.setSuccessful("true");
		bayeuxMessage.setClientId("Un1q31d3nt1f13r");
		bayeuxMessage.setSubscription("/foo/**");
		bayeuxMessage.setError(null);
		System.out.println(bayeuxMessage.toString());
	}
	private static void test11() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(getString("test/publish_req.txt"));
		System.out.println(bayeuxMessage.toString());
	}
	private static void test12() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.PUBLISH_RESP);
		bayeuxMessage.setChannel("/some/channel");
		bayeuxMessage.setSuccessful("true");
		System.out.println(bayeuxMessage.toString());
	}
	private static void test13() throws JSONException, IOException{
		BayeuxMessage bayeuxMessage = new BayeuxMessage(BayeuxMessage.DELIVER_EVENT);
		bayeuxMessage.setChannel("/some/channel");
		bayeuxMessage.setData("some application string or JSON encoded object");
		System.out.println(bayeuxMessage.toString());
	}
	public static void main(String[] args) throws JSONException, IOException{
		boolean flagF = false;
		if (flagF) test1();
		if (flagF) test2();
		if (flagF) test3();
		if (flagF) test4();
		if (flagF) test5();
		if (flagF) test6();
		if (flagF) test7();
		if (flagF) test8();
		if (flagF) test9();
		if (flagF) test10();
		if (flagF) test11();
		if (flagF) test12();
		if (flagF) test13();
	}
	private static String getString(String location) throws IOException{
		StringBuilder sb = new StringBuilder();
		FileReader fr = new FileReader(location);
		int oneInt = -1;
		while(-1 != (oneInt = fr.read())){
			sb.append((char)oneInt);
		}
		fr.close();
		return sb.toString();
	}
}
