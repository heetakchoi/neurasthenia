<%@ page 
import="
java.text.SimpleDateFormat,
java.util.Date,
com.endofhope.neurasthenia.comet.TopicManager,
com.endofhope.neurasthenia.Constants"
%>
<%
		String topicIdString = request.getParameter("topic_id");
		int topicId = Integer.parseInt(topicIdString);
		String topicName = request.getParameter("topic_name");
		String userName = request.getParameter("user_name");
		String functionName = request.getParameter("function_name");
		String argument = request.getParameter("argument");
		SimpleDateFormat sdf = new SimpleDateFormat("MM:dd HH:mm:ss");
		String dateString = sdf.format(new Date(System.currentTimeMillis()));
		argument = "'["+dateString+"] "+argument.substring(1);
		String msg = "<script> "+functionName+"("+argument+"); </script>";
		if(topicName != null){
			TopicManager topicManager = (TopicManager)getServletContext().getAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME);
			topicManager.sendMessageToTopic(msg, topicId);
		}
		out.println("<html><head>");
		out.println("</head><body>");
		out.println("</body></html>");
%>