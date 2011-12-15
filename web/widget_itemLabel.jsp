<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

  <c:if test="${item.widget != 'savebutton' && item.widget != 'rowsets'}">
  	<c:choose>
   	 <c:when test="${item.widget == 'button'}">
   	 	<c:if test="${item.label != item.itemType}">
           <c:out value="${item.label}"/>
        </c:if>
     </c:when>
	<c:otherwise>
       <c:out value="${item.label}" default="${item.itemType}"/>
     </c:otherwise>
   </c:choose>
 </c:if>