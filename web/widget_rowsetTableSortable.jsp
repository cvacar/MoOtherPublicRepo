<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Date		Author	Change
	8/15/2009	TJR		Added JS for TableSorter function to make it unique to each
						"table" & "pager" on the page.  Updated ids for "table" & "pager" 
						widgets to match function call.
	9/24/2009	TJR		Added "size:10" option to .tablesorterPager() jQuery function call
						and set the selected="selected" option to the "10" option
						in the NavControl at bottom of table.  By default tablesorter will
						display 10 rows.  Modify these two values in conjunction to change
						to a different number of rows displayed/paged by default.
 --%>
 
 <script type="text/javascript">
<%-- Create a custom TableSorter js function for this specific "table" id 
	along with its specific "pager" id.  "id"s here must exactly match
	the ids used in the html to create the table & pager widgets below.
	TJR - 08/15/09
--%>
$(function(){
		$("#${view.websafeName}table")
			.tablesorter({widthFixed: false, positionFixed: false, widgets: ['zebra']})
			.tablesorterPager({container: $("#${view.websafeName}pager"), positionFixed: false, size:10});
});
</script>

<%-- each table on screen has own title --%>
<h3>${view.label}</h3>

<%-- optional message for each table on screen --%>
<table>
	<tr><td class="limsmessage"><c:out value="${view.message.message}" default=""/></td></tr>
</table>

<%-- Table guts --%>
<table cellpadding="0" cellspacing="0" border="0" class="display tablesorter" id="${view.websafeName}table">
<%-- table cellpadding="0" cellspacing="0" border="0" class="display dataTable" id="tableDT" %-->

	<%-- Column header row --%>
	<thead>
		<th>Row</th>
		<c:forEach var="column" items="${view.columns}">
			<th>${column.name}</th>
		</c:forEach>
		</tr>
	</thead>

	<c:set var="rowIndex" value="${view.startRow}" scope="session" />
	<c:set var="newStartRow" value="${view.startRow}" scope="session" />

	<%-- Data rows --%>
	<tbody>
		<c:forTokens varStatus="r" var="rowData" items="${view.rowset}"
			delims="${view.rowDelimiter}">
			<tr>
				<td>
					<c:out value="${rowIndex}" />
				</td>

				<%-- helper JSP displays widgets and value for each cell --%>
				<%-- (includes own tr nodes) --%>
				<jsp:include page="widget_rowsetRow.jsp" flush="true" />
			</tr>
			<c:set var="rowIndex" value="${view.startRow + r.count}"
				scope="session" />
			<c:set var="ignoreMe" value="${view.nextRow}" scope="page" />
		</c:forTokens>
	</tbody>

	<%-- subtract 1 from the rowIndex before we submit rowcount
	because we're incrementing on the last token even though we're done with row tokens.--%>
	<input type="hidden" name="${view.name}.rowcount"
		value="${rowIndex - 1}">
	<%-- (end of column/row looping)--%>
	<%-- cleanup --%>
	<c:remove var="rowsetCellClass" scope="session" />
</table>
<%-- (end of main table) --%>

<%-- optional nav controls offer Top, Prev, Next, End etc --%>
<c:if test="${view.hideNavControls == false}">
	<div id="${view.websafeName}pager" class="pager">
	<form>
		<img src="jquery/plugins/tablesorter/addons/pager/icons/first.png" class="first"/>
		<img src="jquery/plugins/tablesorter/addons/pager/icons/prev.png" class="prev"/>
		<input type="text" class="pagedisplay"/>
		<img src="jquery/plugins/tablesorter/addons/pager/icons/next.png" class="next"/>
		<img src="jquery/plugins/tablesorter/addons/pager/icons/last.png" class="last"/>
		<select class="pagesize">
			<option selected="selected"  value="10">10</option>
			<option value="20">20</option>
			<option value="30">30</option>
			<option value="40">40</option>
		</select>
	</form>
	</div>
</c:if>

<%-- Auxiliary table organizes nav controls, buttons, etc --%>

<table>
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

