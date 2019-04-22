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
		String tablename = request.getParameter("tbl");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conn = DriverManager.getConnection((String) session
					.getAttribute("dsn"));
			String sql = "select * from " + tablename + " where category=?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(0, request.getParameter("category"));
			rs = stmt.executeQuery(sql);
			// ...
			// ...
			// ...
		} catch (Exception e) {
			System.out.println("Exception Occurs: " + e);
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