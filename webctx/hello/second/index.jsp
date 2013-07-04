<%@ page 
    %>
<%
   String topicIdString = request.getParameter("topic_id");
   int topicId = Integer.parseInt(topicIdString);
   String topicName = request.getParameter("topic_name");
   String userName = request.getParameter("user_name");
   String functionName = "chat";
   String argument = "'"+userName+"', ";
%>
<html>
  <head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script>
      function send(){
      var msg_element = document.getElementById("id_msg");
      var msg = msg_element.value;
      parent.sendframe.location = "send_to_topic.jsp?function_name=<%=functionName%>&argument=<%=argument%>'"+msg+"'&topic_id=<%=topicId%>&topic_name='<%=topicName%>'&user_name='<%=userName%>'";
      document.getElementById("id_msg").value = "";
      return;
      }
      function init_focus(){
      document.getElementById("id_msg").focus();
      return;
      }
      function exit_topic(){
		parent.receiveframe.location = "send.html";
      document.location = "/hello/index.html";
      return;
      }
    </script>
  </head>
  <body onload="javascript:init_focus();">
    <div>
      topic name : <%=topicName%><br />
      <textarea id="msg_area_id" name="msg_area" rows="10" cols="60"></textarea><br />
      <%=userName%> : <input type="text" name="msg" value="" id="id_msg" onkeyPress="if (event.keyCode==13){ send(); }" /> <input type="button" name="btn" value="send" onclick="javascript:send();" /> <input type="button" name="exit_btn" value="Exit" id="id_exit" onclick="javascript:exit_topic();" />
	</div>
    <iframe src="send.html" name="sendframe" frameborder="0" ></iframe>
    <iframe src="subscribe_topic.jsp?topic_id=<%=topicId%>&topic_name=<%=topicName%>&user_name=<%=userName%>" name="receiveframe" frameborder="0" ></iframe>
  </body>
</html>
