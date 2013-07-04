<%@ page 
import="
com.endofhope.neurasthenia.comet.TopicManager,
com.endofhope.neurasthenia.Constants"
%>
<%
		String topicName = request.getParameter("topic_name");
		String backUrl = request.getParameter("back_url");
		TopicManager topicManager = (TopicManager)getServletContext().getAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME);
		topicManager.createTopic(topicName);
		out.println("<script> document.location = '"+backUrl+"'; </script>");
%>