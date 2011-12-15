<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

  <c:choose>
   <c:when test="${sessionScope.showMenu == 'no'}">
   <td class="collapseCell">
	    <a href="${pageContext.request.contextPath}/DefaultWorkflow?showMenu=yes"> 
	    	<img src="webapps/images/arrow3-right-grey-16.png" align="right" border="0"></a>
   </td>
  </c:when>
  <c:otherwise>
    <td class="workflowMenuCell">
    	<jsp:include page="PgWorkflow_Accordion.jsp" flush="true" />
     </td>
 	 <td class="collapseCell">
 		<a href="${pageContext.request.contextPath}/DefaultWorkflow?showMenu=no">
			<img src="webapps/images/arrow3-left-grey-16.png" align="right" border="0"></a>
 	 </td>
</c:otherwise>
</c:choose>
