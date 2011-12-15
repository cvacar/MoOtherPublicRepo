<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<tr class="breadcrumbRow">
	<td>
<a HREF="${pageContext.request.contextPath}/">Logout></a>
		<a href="${pageContext.request.contextPath}/"><c:out value="${workflow.workflowName}"/></a>
		&gt;<c:out value="${task.taskName}" default=""/>
	</td>
</tr> 
