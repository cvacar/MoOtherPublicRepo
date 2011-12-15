<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="test" value="${item.value}"/>
<c:choose>
	<c:when test="${item.value == null}">
  <a HREF="${pageContext.request.contextPath}/${item.itemType}" target="${item.target}">${item.label}</a>
   <br>
   <br>
  </c:when>
  <c:otherwise>
    <a HREF="${item.value}" target="${item.target}">${item.label}</a>
   <br>
   <br>
   </c:otherwise>
  </c:choose>