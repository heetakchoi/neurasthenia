<%@ page
contentType="text/html; charset=UTF-8"
%>
<html>
	<head>
		<title>hello javaserverpages</title>
		<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<h3>JSP TEST PAGE 아아.</h3>
		<ul>
<%
	for(int i=0; i<5; i++){
%>
			<li><%=i%></li>
<%
	}
%>
		</ul>
		<a href="javascript:history.back();">돌아갑니다.</a>
	</body>
</html>