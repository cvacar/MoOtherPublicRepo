<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<html>
<form action="${pageContext.request.contextPath}/DefaultWorkflow" method="POST">
<head>
<title>LIMS Login for ${workflow.workflowName}</title>
<link href="webapps/style/app.css" rel="stylesheet" type="text/css">
<link href="webapps/style/linx.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="white">
<img src="webapps/images/branding.jpg">
<h2>Default Error Page</h2>
<table>
	<jsp:include page="linxMessage.jsp" flush="true"/>
</table>
</body>
</form>
</html>
