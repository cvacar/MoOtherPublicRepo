<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

  <select name="${item.itemType}" multiple="multiple">
  	<c:forEach var="opt" items="${item.displayValues}">
  	<c:choose>
  		<c:when test="${item.selectedValue==opt}">
    		<option name="${opt}" selected="true">${opt}</option>
 		</c:when>
 		<c:otherwise>
     		<option name="${opt}">${opt}</option>
 		</c:otherwise>
 	</c:choose>
    </c:forEach>
  </select>
    <c:if test="${item.action != 'none'}">
    	<input type="submit" name="${item.action}" value="${item.action}">
    </c:if>
    </br>