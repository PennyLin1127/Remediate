<%@ page language="java" contentType="text/html; charset=BIG5"
	pageEncoding="BIG5"%>
<%@ page import="java.io.*" %>
<%
	request.setCharacterEncoding("big5");   
	String file_name=request.getParameter("filename");
	response.setContentType("application/octet-stream; charset=iso-8859-1");
	response.setHeader("Content-disposition","attachment; filename="+file_name);
	out.clear();
	FileInputStream fis = new FileInputStream(request.getRealPath("/") 
		+ "download/"+file_name);
	int byteRead;
	while(-1 != (byteRead = fis.read())) 
	{	
		out.write(byteRead);				
	}
	fis.close();
	return ;
%>