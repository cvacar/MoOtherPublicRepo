<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:forEach var="view1" items="${rowsetViews}">
	<c:set var="view" value="${view1}" scope="session"/>
	<jsp:include page="widget_rowsetTableSortable.jsp" flush="true"/>
</c:forEach>

