<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="min" value="${item.minLength}"/>
<c:set var="max" value="${item.maxLength}"/>
<c:if test="${min == 0}">
   <c:set var="min" value="20"/>
</c:if>
<c:if test="${max== 0}">
  <c:set var="max" value="50"/>
</c:if>
<input type="text" name="${item.itemType}" 
                   value="${item.value}" 
                   size="${min}" 
                   maxlength="${max}">
<c:remove var="min"/>
<c:remove var="max"/>
