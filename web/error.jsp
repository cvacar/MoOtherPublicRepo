<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fmt-1_0.tld" prefix="fmt" %>
<html>
	<head>
		<title>
		</title>
	</head>
	<body bgcolor="white">
		<h3>
			Server Error
		</h3>
		<p>
			${pageContext.errorData.throwable}
			<c:choose>
				<c:when test="${!empty pageContext.errorData.throwable.cause}"> : ${pageContext.errorData.throwable.cause}
				</c:when>
				<!-- 2/2010 - TJR: As of JSP 2.1 getRootCause was deprecated to getCause -->
				<!--
				<c:when test="${!empty pageContext.errorData.throwable. .rootCause}"> : ${pageContext.errorData.throwable.rootCause}
				</c:when>
				-->
			</c:choose>
	</body>
</html> 

