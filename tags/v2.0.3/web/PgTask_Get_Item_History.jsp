<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<form action="${pageContext.request.contextPath}/${task.taskSv}" method="POST" onsubmit="return fnWorking()"> 
<input type="hidden" name="taskName" value="${task.taskName}"/>
	
<table class="queryTextTable">
<tr align="left" valign="top">
<td> Item </td>
<td> ItemType  </td>
</tr>
<tr align="left" valign="top">
<td>
	<c:set var="item2" value="${ItemID}" scope="page"/>
	<c:choose>
	<c:when test="${item2 == null}">
		<input type="text" name="ItemID" value=" "/>
	</c:when>
	<c:otherwise>
		<input type="text" name="ItemID" value="${item2}"/>
	</c:otherwise>
	</c:choose>
</td>
<td class="itemWidget">
	<c:set var="itemType2" value="${ItemType}" scope="page"/> 

	<c:choose>
		<c:when test="${itemType2 == null}">
		    <select class="dropdown" name="ItemType">
  	<c:forEach var="opt" items="${dropdown}">
  	<c:choose>
  		<c:when test="${opt == '(Select)'}">
    		<option name="${opt}" selected="true">${opt}</option>
 		</c:when>
 		<c:otherwise>
     		<option name="${opt}">${opt}</option>
 		</c:otherwise>
 	</c:choose>
  </c:forEach>
    </select>
		</c:when>
	<c:otherwise>
  <select class="dropdown" name="ItemType">
  	<c:forEach var="opt" items="${dropdown}">
  	<c:choose>
  		<c:when test="${itemType2 == opt}">
    		<option name="${opt}" selected="true">${opt}</option>
 		</c:when>
 		<c:otherwise>
     		<option name="${opt}">${opt}</option>
 		</c:otherwise>
 	</c:choose>
  </c:forEach>
    </select>
    	</c:otherwise>
    </c:choose>
</td>
<td><input type="submit" align="left" name="GetHistory" value="Get History" action="Get History"/>
</td>
</tr>
 </table>
<c:forEach var="view1" items="${rowsetViews}">
	<c:set var="view" value="${view1}" scope="session"/>
	<c:if test="${view !=null}">
	<%-- jsp:include page="widget_rowsetTableCompact.jsp" flush="true"/ --%>
	<jsp:include page="widget_rowsetTableSortable.jsp" flush="true"/>
	</c:if>
</c:forEach>

</form>

