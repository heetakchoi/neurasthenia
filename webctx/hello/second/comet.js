function enter_room(user_name, topic_id, topic_name ){
	var enter_msg = "'SYS', 'Enter [" + user_name + "]'";
	var argument = enter_msg
	parent.sendframe.location = "send_to_topic.jsp?function_name=chat&argument="+argument+"&topic_id="+topic_id+"&topic_name='"+topic_name+"'&user_name="+user_name;
	return;
}
function chat(user_name, msg){
	var old = parent.document.all.msg_area_id.value;
	parent.document.all.msg_area_id.value = old + "\n" + user_name + " : " + msg;
	parent.document.all.msg_area_id.scrollTop = parent.document.all.msg_area_id.scrollHeight; 
	return;
}

function user_exit(user_name){
	return chat("SYS", user_name+" is exit");
}
function dummy(){
	return;
}