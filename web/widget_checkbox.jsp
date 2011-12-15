<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

	<c:set var="selected" value ="${item.isSelected}" scope="page"/>	

<c:choose>
	<c:when test="${selected == true}">
       <input type="checkbox" name="${item.itemType}" value="${item.itemType}" checked/>
   </c:when>
   <c:otherwise>
      <input type="checkbox" name="${item.itemType}" value="${item.itemType}"/>
   </c:otherwise>
</c:choose>