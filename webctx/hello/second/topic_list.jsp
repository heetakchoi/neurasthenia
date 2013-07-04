<%@ page contentType="text/html; charset=UTF-8"
import="
com.endofhope.neurasthenia.Constants,
com.endofhope.neurasthenia.comet.TopicManager,
java.util.List,
java.util.Set,
java.util.Iterator"
%>
<%
	TopicManager topicManager = (TopicManager)getServletContext().getAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME);
	String type = request.getParameter("type");
	String topicIdString = request.getParameter("topic_id");
	if(type != null && "close".equals(type)){
		topicManager.removeTopic(Integer.parseInt(topicIdString));
	}
	Set<Integer> topicIdSet = topicManager.getTopicIdSet();
	Iterator<Integer> topicIdIter = topicIdSet.iterator();
%>
<html>
	<head>
		<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<h2>Topic List</h2>
		<ul>
<%
	while(topicIdIter.hasNext()){
		Integer topicId = topicIdIter.next();
		String topicName = topicManager.getTopicName(topicId.intValue());
		List<String> userNameList = topicManager.getUserNameList(topicId.intValue());
%>
			<li>
				Topic [<%=topicName%>], [<%=userNameList.size()%>] users  <br />
				(
<%
			for(String userName : userNameList){
				out.print("<strong>"+userName + "</strong> ");
			}
%>
				)
				<br />
				 talk to each other now.
				<form name="formname" method="post" action="index.jsp">
					<input type="hidden" name="topic_id" value="<%=topicId%>" />
					<input type="hidden" name="topic_name" value="<%=topicName%>" />
					To enter topic as nick <input type="text" name="user_name" value="" size="8"/>
					<input type="submit" name="submit" value="Click it" />
				</form>
<%
		if(userNameList.size() < 1){
%>
				<form name="formname_del" method="post" action="topic_list.jsp">
					<input type="hidden" name="topic_id" value="<%=topicId%>" />
					<input type="hidden" name="type" value="close" />
					<input type="submit" name="submit" value="close topic" />
				</form>
<%
		}
%>
			</li>
<%
	}
%>
		</ul>
		<hr />

		<form name="formname" method="post" action="add_topic.jsp">
			New topic : <input type="text" name="topic_name" value="" />
			<input type="hidden" name="back_url" value="topic_list.jsp" />
			<input type="submit" name="submit" value="create" />
		</form>

	</body>
</html>