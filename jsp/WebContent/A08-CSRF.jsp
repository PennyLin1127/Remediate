<%@ page language="java" contentType="text/html; charset=BIG5"
	import="java.sql.*" pageEncoding="BIG5"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Simple CSRF Sample</title>
</head>
<body>
	<%
		String p1 = request.getParameter("newpwd");
		String p2 = request.getParameter("confirmpwd");
		if (p1 == null || p2 == null || p1.isEmpty() || p2.isEmpty()) {
	%>
	<form method="post">
		<label>New Password:</label> <input name="newpwd" type="password"
			size="20" /><br /> <label>Confirm Password:</label> <input
			name="confirmpwd" type="password" size="20" /><br /> <input
			name="submit" type="submit" value="OK" /><br />
	</form>
	<%
		} else {
			if (p1.equals(p2)) {
				String sql = "";
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				Connection conn = DriverManager
						.getConnection((String) session.getAttribute("dsn"));
				sql = "update users set passwd=? where userid=?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(0, p1);
				stmt.setString(1, (String)request.getAttribute("userid"));
				stmt.executeUpdate();
				out.print("<p>Password Change!</p>");
			} else {
				out.print("<p>Password Wrong!</p>");
			}
		}
	%>
</body>
</html>