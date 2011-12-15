<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Clean out messages from tasks that may be hanging around --%>
<c:set var="clean" value="${task.cleanmessage}"/>
<html>
<form action="${pageContext.request.contextPath}/DefaultWorkflow" method="POST">
<head>
<title align="center">LIMS Login for ${workflow.workflowName}</title>
<link href="webapps/style/app.css" rel="stylesheet" type="text/css">
<link href="webapps/style/linx.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="white" onload="return setFocus()">
<script language= "javascript" type="text/javascript">
     function setFocus()
	{
		document.forms[0].elements[0].focus()
	}
    
</script>
<table align="center">
<tr><td><img  src="webapps/images/branding.jpg"></td></tr>
</table>


<h3 align="center">Welcome to ${workflow.workflowName}</h3>
<h5 align="center">Version ${workflow.limsVersion}</h5>

<table align="center">
				<jsp:include page="linxMessage.jsp" flush="true"/>
			</table>
<table align="center" border="2px" bgcolor="gray" width="410px" height="210px">
<tr>
<td>

<table bgcolor="white"  width=400px" height="200px" align="center"  border="2px">
<tr><td>
 <table align="center">
 <tr>
    <td align="center"><font size="2"><b>Please login to continue</b></font></td></tr>
   <tr>
   	  <td class="taskGroupSpacer"/>
    </tr>

   <tr><td>
   	
   	<table>
   		<tr><td>
   
     <b>User name:&nbsp;&nbsp;</b><input type="text" name="username" value="">
     </td></tr>
     
     <tr><td>
     <b>Password:&nbsp;&nbsp;&nbsp;</b><input type="password" name="password" value="">
    </td><tr>
    
	</table>
    </td></tr>

    
    <tr>
    <td>
    <table align="left">
       <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
       &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
       <input type="submit" name="Login" value="Login">
       </td></tr>
	  </table>
	</td>
	</tr>
  </table>
</td></tr>
   </td>
    </tr>
<!--<tr>
<td>
<%= new java.util.Date() %>
</tr>
</td>-->

</td>
</tr>
</table>


</table>
    <tr>
    <td>
     <table align="center">
       <tr><td><b><a href="http://www.wildtypeinfo.com" target="_blank">Powered by Wildtype Linx 3</b></a><img  src="webapps/images/WT_logo_small_1.jpg"></td></tr>
     </table>


</body>
</form>
</html>
