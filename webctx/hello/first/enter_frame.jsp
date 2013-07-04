<%@ page 
    %>
<%
   String topicIdString = request.getParameter("topic_id");
   int topicId = Integer.parseInt(topicIdString);
   String topicName = request.getParameter("topic_name");
   String userName = request.getParameter("user_name");
   %>
<html>
  <head>
    <frameset rows="100%,0%,0%" name="wrapframe">
      <frame src="main.jsp?topic_id=<%=topicId%>&topic_name=<%=topicName%>&user_name=<%=userName%>" name="mainframe" frameborder="0" />
      <frame src="send.html" name="sendframe" frameborder="0" />
      <frame src="subscribe_topic.jsp?topic_id=<%=topicId%>&topic_name=<%=topicName%>&user_name=<%=userName%>" name="receiveframe" frameborder="0" />
    </frameset>
  </head>
</html>
