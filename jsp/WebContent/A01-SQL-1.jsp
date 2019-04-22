<%@ page language="java" contentType="text/html; charset=BIG5"
	pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>SQL Injection Sample</title>
</head>
<body>
	<%
		String uid = request.getParameter("uid");
		String pwd = request.getParameter("pwd");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conn = DriverManager.getConnection((String) session
					.getAttribute("dsn"));
			sql = "select * from users where userid='" + uid
					+ "' and passwd='" + pwd + "'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			// ...
			// ...
			// ...
		} catch (Exception e) {
			System.out.println("Exception Occurs: " + sql);
		}
		try {
			rs.close();
		} catch (Exception e) {
		}
		try {
			stmt.close();
		} catch (Exception e) {
		}
		try {
			conn.close();
		} catch (Exception e) {
		}
	%>
</body>
</html>

