<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="min" value="${item.minLength}"/>
<c:if test="${min == 0}">
   <c:set var="min" value="20"/>
</c:if>
<c:set var="max" value="${item.maxLength}"/>
<c:if test="${max== 0}">
  <c:set var="max" value="50"/>
</c:if>
<c:set var="df" value="${item.dateFormat}"/>
<c:if test="${df == '*'}">
  <c:set var="df" value="mm/dd/yy"/>
</c:if>

<%-- This javascript will override the date format set by the initial datepicker
	 class (set in PgMaster_Default.jsp) based on definition from task xml --%>

<%-- 	 
<script type="text/javascript">
	$(function(){
		$("#${item.itemType}").datepicker('option', {dateFormat: '${df}' }); });
	// alert('df is: ' + "'${df}'" + ' for itemType: ' + '${item.itemType}');
</script>
--%>

<script language="Javascript" type="text/javascript">
	$(function() {
		$("#${item.itemType}").datepicker({
			changeMonth: true,
			changeYear: true,
			dateFormat: '${df}'
			});
		});
	<%-- alert('df is: ' + "'${df}'" + ' for itemType: ' + '${item.itemType}'); --%>
</script>

<input type="text" name="${item.itemType}" 
				   class="datepicker"
				   id="${item.itemType}" 
                   value="${item.value}" 
                   size="${min}" 
                   maxlength="${max}">
<c:remove var="min"/>
<c:remove var="max"/>
