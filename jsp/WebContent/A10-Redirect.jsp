<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Simple Open Redirect Exercise</title>
</head>
<body>
<%
	String home = "http://localhost:12345/";
	if (request.getParameter("url") != null)
	{
		response.sendRedirect(request.getParameter("url"));
	}
	else if (request.getParameter("function") != null)
	{
		response.sendRedirect(home + request.getParameter("function"));
	}
%>
</body>
</html>