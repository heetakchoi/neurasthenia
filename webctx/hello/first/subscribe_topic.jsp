<%@ page 
import="
java.util.List,
com.endofhope.neurasthenia.comet.TopicManager,
com.endofhope.neurasthenia.Constants,
com.endofhope.neurasthenia.connection.PhysicalConnectionKey,
com.endofhope.neurasthenia.connection.ConnectionEventHandler,
com.endofhope.neurasthenia.webcontainer.servlet.HttpServletResponseImpl"
%>
<%!
	static final String dummy = "                                                                                "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        "
		+"                                                                                                                        ";
%>
<%
		String topicIdString = request.getParameter("topic_id");
		final int topicId = Integer.parseInt(topicIdString);
		String topicName = request.getParameter("topic_name");
		final String userName = request.getParameter("user_name");
		final TopicManager topicManager = (TopicManager)getServletContext().getAttribute(Constants.TOPIC_MANAGER_ATTRIBUTE_NAME);
		topicManager.subscribeTo(
			topicId, userName, 
			(PhysicalConnectionKey)request.getAttribute(Constants.TOPIC_PHYSICAL_CONNECTION_KEY_ATTRIBUTE_NAME),
			new ConnectionEventHandler(){
				public void onCloseEvent() {
					String msg = "<script> user_exit('"+userName+"'); </script>";
					topicManager.sendMessageToTopic(msg, topicId);
					topicManager.removeUser(topicId, userName);
				}
			}
		);
		HttpServletResponseImpl httpServletResponseImpl = (HttpServletResponseImpl)response;
		httpServletResponseImpl.setCometSupport(true);
		out.println("<html><head>");
		out.println("<script src=\"comet.js\" type=\"text/javascript\"></script>");
		out.println("</head><body>");
		out.println(dummy);
		out.println("\n");
		List<String> messageList = topicManager.getLastMessageList(topicId);
		for(String message : messageList){
			out.println(message);
		}
		out.println("<script> enter_room('"+userName+"', '"+topicIdString+"', '"+topicName+"'); </script>\n");
%>