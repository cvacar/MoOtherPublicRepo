<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="min" value="${tableitem.minLength}"/>
<c:set var="max" value="${tableitem.maxLength}"/>

<c:if test="${min == 0}">
   <c:set var="min" value="10"/>
</c:if>
<c:if test="${max== 0}">
  <c:set var="max" value="20/"/>
</c:if>


<%-- Table guts --%>
<table class="rowsetTable" id="${tableitem.itemType}">

	<%-- Column header row --%>
	<tr class="columnHeadersRow">
		<TH class="corner">&nbsp;</TH>
		<c:forEach varStatus="h" var="colHeader" items="${tableitem.colHeaders}">
		  <th><c:out value="${colHeader}"/></th>
		</c:forEach>
	</tr>
	
	<%-- Input rows --%>
    <c:forEach varStatus="r" var="cellValue" items="${tableitem.rowHeaders}">
		
		<tr class="dataRow">    		
   			<TD class="rowHeader"><c:out value="${cellValue}"/></TD>
   		
				<%-- (add another row with text input fields)--%>
		<c:forEach varStatus="c" var="colHeader" items="${tableitem.colHeaders}">
		<c:set var="coord" value="${tableitem.itemType}.row_${r.count}.col_${c.count}" scope="page"/>
		<c:set var="currRow" value="${r.count}" scope="session"/>
		<c:set var="currCol" value="${c.count}" scope="session"/>
		<c:set var="cellData" value="${tableitem.cellValue}" scope="page"/>
		
			<%-- (does not handle dropdown cells (yet))--%>
		  <c:if test="${tableitem.widget!='checkbox'}">
				<TD class="rowsetCell">
				       <input type="${tableitem.widget}" 
				       name="${coord}" 
				       value="${cellData}"
				       size="${min}"
				       maxLength="${max}">
			 </c:if>
			 <c:if test="${tableitem.widget=='checkbox'}">
				<%-- (handle checked and unchecked checkboxes per cell)--%>
			   <c:if test="${cellData=='true'}">
					<TD class="rowsetCell">
					       <input type="checkbox" 
					       name="${coord}" 
					       value=" "
					       checked>			 
				 </c:if>
			   <c:if test="${cellData!='true'}">
					<TD class="rowsetCell">
				       <input type="checkbox" 
				       name="${coord}" 
				       value=" ">			 
				 </c:if>
			 </c:if>
				</TD>
		</c:forEach>
		</tr>
  
	<%-- (end of row looping)--%>
	<c:set var="rowcount" value="${r.count}" scope="page"/>
	<input type="hidden" name="${tableitem.itemType}.rowcount" value="${rowcount}">
	</c:forEach>
<%-- (end of text input table) --%>
</table>

