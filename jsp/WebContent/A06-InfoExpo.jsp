<%@ page language="java" contentType="text/html; charset=BIG5"
	pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Sensitive Date Exposure</title>
</head>
<body>
	<%
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conn = DriverManager.getConnection((String) session
					.getAttribute("dsn"));
			String sql = "select fname, lname, ccn, pin from cardholder where ssn=?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(0, request.getParameter("ssn"));
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String firstName = rs.getString(0);
				String lastName = rs.getString(1);
				String cardNo = rs.getString(2);
				String pinCode = rs.getString(3);
				String html = String.format("{1}, {0}, {2}, {3}<br />",
						firstName, lastName, cardNo, pinCode);
				out.println(html);
			}
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