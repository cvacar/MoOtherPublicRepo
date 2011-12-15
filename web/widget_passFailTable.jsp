<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- each table on screen has own title --%>
<h3>${item.itemType}</h3>

<%-- Table guts --%>
<table class="rowsetTable" id="${item.itemType}">


	<%-- Column header row --%>
	<tr class="columnHeadersRow">
		<TH class="corner">&nbsp;</TH>
		<c:forEach  begin="1" end="12" var="cols" varStatus="h">
		  <th>${h.count}</th>
		</c:forEach>
	</tr>
	
	<%-- Input rows --%>
    <c:forEach  items="${alpharows}" varStatus="r" var="cellValue">
		
		<tr class="dataRow">    		
   			<TD class="rowHeader"><c:out value="${cellValue}"/></TD>
   		
				<%-- (add another row with text input fields)--%>
		<c:forEach begin="1" end="12" varStatus="c" var="colHeader">
		<c:set var="coord" value="${item.itemType}.row_${r.count}.col_${c.count}" scope="page"/>
		<c:set var="currRow" value="${r.count}" scope="session"/>
		<c:set var="currCol" value="${c.count}" scope="session"/>
		
			<%-- (does not handle dropdown cells (yet))--%>
				<%-- (draws checked checkbox in each cell)--%>
					<TD class="rowsetCell">
				       <input type="checkbox" 
				       name="${coord}" 
				       value=" "
				       checked>			 
				 </TD>
		</c:forEach>
		</tr>
  
	<%-- (end of row looping)--%>
	</c:forEach>
<%-- (end of text input table) --%>
<input  type="hidden" name="${item.itemType}.rowcount" value="8" >

</table>

