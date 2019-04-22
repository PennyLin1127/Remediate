<%@ page language="java" contentType="text/html; charset=BIG5" pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Cross Site Scripting Exercise - JavaScript</title>
</head>
<body>
<script language="JavaScript">
	var name = '<%=request.getParameter("username") %>';
	document.write('Dear Customer ' + name);
</script>
</body>
</html>
