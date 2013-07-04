/**
 * Licensed to LGPL v3.
 */
package com.endofhope.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author endofhope
 *
 */
public class HelloServlet extends HttpServlet{


	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	private void doProcess(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		PrintWriter out = resp.getWriter();
		out.write(getHead("HelloServlet"));
		Enumeration nameEnum = req.getParameterNames();
		out.write("<h2>한글이 나옵니다.</h2>\n");
		out.write("<hr />");
		while(nameEnum.hasMoreElements()){
			String key = (String)nameEnum.nextElement();
			String value = req.getParameter(key);
			out.write(key);
			out.write(" : ");
			out.write(value);
			out.write("<hr />\n");
		}
		out.write("<a href=\"javascript:history.back();\">돌아가기</a><br />");
		out.write(getTail());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doProcess(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doProcess(req, resp);
	}
	public static String getHead(String title){
		StringBuilder sb = new StringBuilder();
		sb.append("<html>\n  <head>\n    <title>")
		.append(title)
		.append("</title>\n")
		.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n")
		.append("</head>\n  <body>\n");
		return sb.toString();
	}
	public static String getTail(){
		return "  </body>\r\n</html>";
	}
}
