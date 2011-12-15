<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="min" value="${item.minLength}"/>
<c:set var="max" value="${item.maxLength}"/>
<c:set var="rowcount" value="${item.rowCount}"/>
<input type="hidden"  name="${item.itemType}.rowcount" value="${rowcount}">
<c:set var="rowIndex" value="1"/>

<c:if test="${min == 0}">
   <c:set var="min" value="20"/>
</c:if>
<c:if test="${max== 0}">
  <c:set var="max" value="50"/>
</c:if>

<%-- check rowcount --%>
<c:if test="${rowcount >= 5}">
<%-- optional scrolling can replace nav controls --%>
<c:if test="${item.scroll == 'true'}">
   <div class="${item.scrollSize}scroll">
</c:if>
</c:if>

<%-- Table guts --%>
<table class="rowsetTable" id="${item.itemType}">

	<%-- Column header row --%>
	<tr class="columnHeadersRow">
		<TH class="corner">&nbsp;</TH>
		  <th class="column"><c:out value="${item.columnHeader}"/></th>
	</tr>
	
	<%-- Input rows --%>
    <c:forEach varStatus="c" var="cellValue" items="${item.displayValues}">
	
		<c:set var="coord" value="${item.itemType}.row_${row}.col_1" scope="page"/>
		<c:set var="row" value="${item.currentRowIndex}"/>
		<tr class="dataRow">    		
   			<TD class="rowHeader"><c:out value="${row}"/></TD>
   		
				<%-- (add another row with a single text input field)--%>
				<TD class="rowsetCell"><input type="text" 
				       name="${coord}" 
				       value="${cellValue}"
				       size="${min}"
				       maxLength="${max}"></TD>
		</tr>
	    <c:set var="ignoreMe" value="${item.nextRow}" scope="page"/>
</c:forEach>

	<%-- (end of row looping)--%>
<%-- (end of text input table) --%>

</table>
<%-- check rowcount --%>
<c:if test="${rowcount >= 5}">
<c:if test="${item.scroll == 'true'}">
  </div>
</c:if>
</c:if>
<%-- (end of optional scrolling for table) --%>

