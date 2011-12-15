<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="temp" value="${view.startRow - view.maxRowsetSize}"/>
<c:choose>
<c:when test="${view.startRow - view.maxRowsetSize > 0}" >
	<c:set var="prevRowset" value="${view.startRow - view.maxRowsetSize}"/>
</c:when>
<c:when test="${view.startRow - view.maxRowsetSize < 1}" >
	<c:set var="prevRowset" value="1"/>
</c:when>
</c:choose>

<c:set var="nextRowset" value="${view.startRow + view.maxRowsetSize}" scope="page"/>
<c:set var="lastRowInData" value="${view.rowcount - view.maxRowsetSize + 1}" scope="page"/>


<tr class="rowsetNavControls">
<td>
<table>
<tr>
    <td>
      <c:choose>
      <c:when test="${view.startRow > 1}">
         <a href="${task.taskSv}?gotoRow=1&taskName=${task.taskName}&table=${view.websafeName}">[Top]</a>
         <a href="${task.taskSv}?gotoRow=<c:out value="${prevRowset}&taskName=${task.taskName}"/>&table=${view.websafeName}">[Prev]</a>
      </c:when>
      <c:otherwise>
         [Top] [Prev]
       </c:otherwise>
       </c:choose>
       		<input type="text" name=<c:out value="${view.websafeName}.newStartRow"/> value="${view.startRow}" size="5px" align="center"/>
       		<input name=<c:out value="${view.websafeName}.gotoRow"/> type="submit" value="Go">
       <c:choose>
       <c:when test="${view.rowcount >= view.startRow + view.maxRowsetSize}">
       		<a href="${task.taskSv}?&taskName=${task.taskName}&gotoRow=<c:out value="${nextRowset}"/>&table=${view.websafeName}">[Next]</a>
       		<a href="${task.taskSv}?&taskName=${task.taskName}&gotoRow=<c:out value="${lastRowInData}"/>&table=${view.websafeName}">[End]</a>
       </c:when>
       <c:otherwise>
         [Next] [End]
       </c:otherwise>
       </c:choose>
    </td>
    </tr>
</table>
</td>
</tr>