<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

 <div class="workflow_scrolled">
<table>
 <c:forEach var="groupName" items="${sessionScope.user.taskGroupNames}">
 <TR><TD class="menuTask" nowrap>
	<c:choose>
		<c:when test="${groupName == sessionScope.selectedGroup}">
       <IMG SRC="webapps/images/menu_grouped_task.gif"></IMG>
       </c:when>
       <c:otherwise>
       <IMG SRC="webapps/images/menu_group.gif"></IMG>
       </c:otherwise>
     </c:choose>
      <B><A class="small" HREF="${pageContext.request.contextPath}/DefaultWorkflow?group=${groupName}">${groupName}</A></B>
      </TD>
      </TR>
		<c:if test="${groupName == sessionScope.selectedGroup}">
		    <c:forEach var="task1" items="${sessionScope.taskObjects}">
    		<TR>
    		  <c:choose>
    		  <c:when test="${task1.isEnabled=='false'}">
      			<TD nowrap>&nbsp;<IMG SRC="webapps/images/task_button2.gif"></IMG>
      			<A class="small">${task1.taskName}</A>
      			</TD>
      		  </c:when>
    		  <c:otherwise>
      			<TD nowrap>&nbsp;<IMG SRC="webapps/images/task_button2.gif"></IMG>
      			<A class="small" HREF="${pageContext.request.contextPath}/${task1.taskSv}?menuSelected=true&taskName=${task1.taskName}">${task1.taskName}</A>
      			</TD>
      			</c:otherwise>
      		   </c:choose>
    			</TR>
   			</c:forEach>
   			<tr>
   			<td class="taskGroupSpacer"/>
   			</tr>
		</c:if>
   			<tr>
   			<td class="taskGroupSpacer"/>
   			</tr>
  </c:forEach>
  </table>
  </div>