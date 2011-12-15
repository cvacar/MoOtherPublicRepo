<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%-- Date		Author	Change
	7/15/2009	TJR		Added button_noshowwait widget
	9/25/2009	TJR		Added rowset_sort_20 widget (same as normal sortable table but default to 20 rows)
	10/22/2009	TJR		Added multiselect widget
	09/14/2010	TJR		Added datetimepicker widget
 --%>

<%-- put in the stylesheet in case we use task page outside master pg (links opened in new window --%>
<link href="webapps/style/app.css" rel="stylesheet" type="text/css">
<link href="webapps/style/linx.css" rel="stylesheet" type="text/css">

<%-- Form will submit to task.getTaskSv() value --%>
<c:choose>
	<%-- (optional multipart request caused by file input widget) --%>
	<c:when test="${task.isFileUploaded=='true'}">
		<form action="${pageContext.request.contextPath}/${task.taskSv}" method="POST" enctype="multipart/form-data" onsubmit="return fnWorking()">
	</c:when>
	<c:otherwise>
		<form action="${pageContext.request.contextPath}/${task.taskSv}" method="POST" onsubmit="return fnWorking()">
	</c:otherwise>
</c:choose>

<input type="hidden" name="taskName" value="${task.taskName}"/>

<%-- Main task, organized in a table, for neatness --%>
<table class="taskPgTable">
	<%-- (begin looping on itemTypes) --%>
	<c:forEach var="item" items="${sessionScope.task.visibleItems}">
		<c:set var="item" value="${item}" scope="session"/>
		<c:set var="widget" value="${item.widget}" scope="page"/>
		<tr>
			<c:choose>
			<c:when test="${widget != 'href'}">
			<td class="itemLabel">
				<jsp:include page="widget_itemLabel.jsp" flush="true"/>
 		    </td>
			</c:when>
			<c:otherwise>	
			<td>&nbsp</td>
			</c:otherwise>
			</c:choose>
		<td class="itemWidget">
			<c:choose>
			<%-- textarea widget --%>
			<c:when test="${widget=='textarea'}">
				<jsp:include page="widget_textarea.jsp" flush="true"/>
			</c:when>
			<%-- textDisplay widget --%>
			<c:when test="${widget=='textDisplay'}">
				<jsp:include page="widget_textDisplay.jsp" flush="true"/>
			</c:when>
			<%-- href widget --%>
			<c:when test="${widget=='href'}">
				<jsp:include page="widget_href.jsp" flush="true"/>
			</c:when>
			<%-- checkbox widget --%>
			<c:when test="${widget=='checkbox'}">
				<jsp:include page="widget_checkbox.jsp" flush="true"/>
			</c:when>
			<%-- radio button widget --%>
			<c:when test="${widget=='radio'}">
				<jsp:include page="widget_radio.jsp" flush="true"/>
			</c:when>
			<%-- file input widget --%>
			<c:when test="${widget=='filebrowse'}">
				<jsp:include page="widget_filebrowse.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='fileDisplay'}">
				<jsp:include page="widget_fileDisplay.jsp" flush="true"/>
			</c:when>
			
			<%-- password widget --%>
			<c:when test="${widget=='password'}">
				<jsp:include page="widget_password.jsp" flush="true"/>
			</c:when>
			<%-- dropdown widget --%>
			<c:when test="${widget=='dropdown'}">
				<jsp:include page="widget_dropdown.jsp" flush="true"/>
			</c:when>
			<%-- multi-select dropdown widget --%>
			<c:when test="${widget=='multiselectbox'}">
				<jsp:include page="widget_multiselectbox.jsp" flush="true"/>
			</c:when>
			<%-- add/remove selection boxes --%>
			<c:when test="${widget=='addremovelist'}">
				<jsp:include page="widget_addremovelist.jsp" flush="true"/>
			</c:when>
			<%-- button widget --%>
			<c:when test="${widget=='button'}">
				<jsp:include page="widget_button.jsp" flush="true"/>
			</c:when>
			<%-- button widget which won't show cycling set of "waiting" images while task runs its action--%>
			<c:when test="${widget=='button_noshowwait'}">
				<jsp:include page="widget_button_noshowwait.jsp" flush="true"/>
			</c:when>
			<%-- button with Save label/action --%>
			<c:when test="${widget=='savebutton'}">
				<jsp:include page="widget_savebutton.jsp" flush="true"/>
			</c:when>
			<%-- button with Verify label/action --%>
			<c:when test="${widget=='verifybutton'}">
				<jsp:include page="widget_verifybutton.jsp" flush="true"/>
			</c:when>
			<%-- label (read-only text) widget --%>
			<c:when test="${widget=='label'}">
			   <jsp:include page="widget_label.jsp" flush="true"/>
			</c:when>
			<%-- table widgets --%>
			<c:when test="${widget=='rowset'}">
			   <jsp:include page="widget_rowsetTable.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='rowsets'}">
			 	<jsp:include page="widget_rowsetsItor.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='inputtable'}">
			 	<jsp:include page="widget_inputTable.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='rowset_sort'}">
			   <jsp:include page="widget_rowsetTableSortable.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='rowset_sort_20'}">
			   <jsp:include page="widget_rowsetTableSortable_20.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='rowsets_sort'}">
			   <jsp:include page="widget_rowsetsItorSortable.jsp" flush="true"/>
			</c:when>
<c:when test="${widget=='rowsets_sort_20'}">
			   <jsp:include page="widget_rowsetsItorSortable_20.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='datepicker'}">
			 	<jsp:include page="widget_datepicker.jsp" flush="true"/>
			</c:when>
			<c:when test="${widget=='datetimepicker'}">
			 	<jsp:include page="widget_datetimepicker.jsp" flush="true"/>
			</c:when>
			<%-- read-only text widget (default) --%>
			<c:otherwise>
				<jsp:include page="widget_textbox.jsp" flush="true"/>
			 </c:otherwise>
			</c:choose>  	
		</td>
		</tr>
		<%-- cleanup --%>
		<c:remove var="item" scope="session"/>
	</c:forEach>
	<%-- (end of itemType looping) --%>
</table>
<%-- (end of widgets in default-drawn task) --%>

</form>
