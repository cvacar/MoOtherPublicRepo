<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Create Sample Sheet</title>
</head>

<body>
<form method="post" onsubmit="return fnWorking()"  action="">
  <%String selectedButton = (String)session.getAttribute("selectedButton"); %>
  <%if(selectedButton == null || selectedButton.equalsIgnoreCase("back") 
  	|| selectedButton.equalsIgnoreCase("refresh") || selectedButton.equalsIgnoreCase("RetrieveBatchInfo")
  	|| selectedButton.equalsIgnoreCase("RetrieveBatch"))
  {%>
  	<jsp:include page="CreateSampleSheet_BatchInfo.jsp"></jsp:include>
  <%}
  else
  {%>
  	<%String batch = (String)session.getAttribute("BatchID"); %>
  	<jsp:include page="CreateSampleSheet_SelectTests.jsp"></jsp:include>
  	<%} %>
</form>
</body>
</html>