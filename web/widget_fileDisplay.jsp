<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="file" value="${item.value}"/>
<c:choose>
	<c:when test="${item.value == ''}">
  <table class="rowsetTable">
  <tr>
   <td>No File found for: <c:out value="${item.itemType}"/>
   </td>
  </tr>
  </table>
</c:when>		
<c:otherwise>	
<div class="smallscroll">
  <table class="rowsetTable">
  <tr>
   <td>
      <jsp:include page="${file}" flush="true" />
   </td>
  </tr>
  </table>
</div>
<table>
	<tr>
	<td><a HREF="${item.value}" target="blank">view larger</a></td>
	</tr>
</table>
</c:otherwise>
</c:choose>
