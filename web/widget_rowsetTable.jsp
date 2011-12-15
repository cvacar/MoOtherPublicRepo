<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- each table on screen has own title --%>
<h3>${view.label}</h3>

<%-- optional message for each table on screen --%>
<table>
	<tr><td class="limsmessage"><c:out value="${view.message.message}" default=""/></td></tr>
</table>

<%-- optional scrolling can replace nav controls --%>
<c:if test="${view.scroll == 'true'}">
   <div class="${view.scrollSize}scroll">
</c:if>

<%-- Table guts --%>
<table class="rowsetTable" id="${view.name}">

	<%-- Column header row --%>
	<tr class="columnHeadersRow">
		<TH class="corner">&nbsp;</TH>
		<c:forEach var="column" items="${view.columns}">
		
		  <c:choose>
			  <%-- optional sortable column --%>
			  <c:when test="${column.isSortable == true}">
		  	<th class="column">
			  	<a href="${task.taskSv}?sortCol=${view.name}.${column.name}">${column.name}</a>
		  </th>
			  </c:when>
			  <c:when test="${column.widgetType=='hidden'}">
			  </c:when>			 
			   <c:otherwise>
		  	<th class="column">
			  	${column.name}
		  </th>
			  </c:otherwise>
		  </c:choose>
		</c:forEach>
	</tr>

	<c:set var="rowIndex" value="${view.startRow}" scope="session"/>
	<c:set var="newStartRow" value="${view.startRow}" scope="session"/>
	
	<%-- Data rows --%>
	<c:forTokens varStatus="r" var="rowData" items="${view.rowset}" delims="${view.rowDelimiter}">
		
		<tr class="dataRow">    		
   			<TD class="rowHeader"><c:out value="${rowIndex}"/></TD>
   		
	   		<%-- previously selected rows are highlighted --%>
	   		<c:choose>
	   			<c:when test="${rowIndex == view.selectedRow}">
					<c:set var="rowsetCellClass" value="rowsetCellSelected" scope="session"/>
				</c:when>
				<c:otherwise>
					<c:set var="rowsetCellClass" value="rowsetCell" scope="session"/>
				</c:otherwise>
			</c:choose>
			
			<%-- helper JSP displays widgets and value for each cell --%>
			<%-- (includes own tr nodes) --%>
			<jsp:include page="widget_rowsetRow.jsp" flush="true"/>
			
		</tr>
	    <c:set var="rowIndex" value="${view.startRow + r.count}" scope="session"/>
	    <c:set var="ignoreMe" value="${view.nextRow}" scope="page"/>
	</c:forTokens>
	<%-- subtract 1 from the rowIndex before we submit rowcount
	because we're incrementing on the last token even though we're done with row tokens.--%>
	<input type="hidden" name="${view.name}.rowcount" value="${rowIndex - 1}">
	<%-- (end of column/row looping)--%>
	<%-- cleanup --%>
	<c:remove var="rowsetCellClass" scope="session"/>
</table>
<%-- (end of main table) --%>

<c:if test="${view.scroll == 'true'}">
  </div>
</c:if>
<%-- (end of optional scrolling for main table) --%>

<%-- Auxiliary table organizes nav controls, buttons, etc --%>
<table>
	<%-- optional nav controls offer Top, Prev, Next, End etc --%>
	<c:if test="${view.hideNavControls == false && view.rowcount >= view.maxRowsetSize}">
    	<jsp:include page="widget_rowsetNavControls.jsp" flush="true"/>
 	</c:if>
	<%-- optional export button, may want export visible on small rowsets, but not when user hides nav controls --%>
 	<c:if test="${view.hideNavControls == false}">
 	   <tr><td><A HREF="${task.taskSv}?table=${view.websafeName}&taskName=${task.taskName}&export" target="_blank">[Export to file]</A>
    </td></tr>
    </c:if>
 	
 
	<%-- optional Back button, to return to main table from detail table --%>
	<c:if test="${requestScope.back != null}">
		<tr><td><a href="${back}">Back</a></td><tr>
	</c:if>
	
	<%-- optional table-associated button POSTs *all* form data --%>
  <c:if test="${view.actionLabel != null}">
	<tr><td><input type="submit" name="${view.name}.${view.action}" value="${view.actionLabel}"></td></tr>
  </c:if>
  <c:if test="${view.action != null}">
	<tr><td><input type="submit" name="${view.name}.${view.action}" value="${view.action}"></td></tr>
  </c:if>

</table>

<c:remove var="rowIndex" scope="session"/>

