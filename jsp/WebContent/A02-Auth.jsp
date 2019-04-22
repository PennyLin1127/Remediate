<%@ page language="java" contentType="text/html; charset=BIG5"
	pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Authentication</title>
</head>
<body>
<%
	String usr = request.getParameter("username");
	String pwd = request.getParameter("password");
	if (usr == null || pwd == null || usr.isEmpty() || pwd.isEmpty())
	{
%>
<form method="get" >
	<label>Username</label>
	<input name="username" type="text" size="20" /><br />
	<label>Password:</label>
	<input name="password" type="password" size="20" /><br />
	<input name="submit" type="submit" value="Login"/><br />
</form>
<%
	}
	else
	{
		if (usr.equals("user1") && pwd.equals("pwd1234"))
		{
			out.print("<p>Hi, " + usr + "</p>");
			session.setAttribute("user", "good user");
		}
		else
		{
			out.print("<p>Login failed!</p>");
		}
	}
%>
</body>
</html>