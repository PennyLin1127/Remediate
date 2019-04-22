<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Admin Functions</title>
</head>
<body>
<%
	if (session.getAttribute("user") != null)
	{
		out.print("you can use admin functions now");
	}
	else
	{
		out.print("you are not a valid user");
	}
%>
</body>
</html>