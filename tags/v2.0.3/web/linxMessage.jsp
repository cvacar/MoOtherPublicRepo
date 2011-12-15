<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<tr>
 
 <%-- Use task message if present, default to session-stored message --%>
 <c:set var="msg" value="${task.message}"/>
 
 <c:if test="${msg.message == null || msg.message == ' ' || msg.message == ''}">
  <c:set var="msg" value="${sessionScope.message}"/>
 </c:if>
 
 <c:choose>
 	<c:when test="${msg != null && msg.statusCode == '200'}">
   		<td class="limsmessage"><c:out value="${msg.message}" default=" "/>
  		</td>
 	</c:when>
 	<c:when test="${msg != null && msg.statusCode != '200'}">
   		<td class="errorMessage"><c:out value="${msg.message}" default=" "/>
  		</td>
 	</c:when>
	<c:otherwise>
		<td class="errorMessage"><c:out value=" "/>
 		</td>
 	</c:otherwise>
 </c:choose>
 
 <c:remove var="msg"/>
 </tr>

