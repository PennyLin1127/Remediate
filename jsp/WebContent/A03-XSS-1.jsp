<%@ page language="java" contentType="text/html; charset=BIG5" pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Cross Site Scripting Exercise</title>
</head>
<body>
Dear Customer <%=request.getParameter("username") %>, <br />
Please complete the following form:
<form>
	<input type="text" name="phone" size="40" /><br />
	<input type="text" name="email" size="40" /><br />
	<input type="hidden" name="in" value="<%=request.getParameter("username") %>" />
	<input type="submit" name="submit" value="submit" /><br />
</form>
</body>
</html>