<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

  <select class="multiselectbox" name="${item.itemType}" multiple="multiple">
  
  <%-- Call DisplayItem.java function to get list of selected items from DOM --%>
  <c:set var="selItems" value="${item.selectedValues}" />
  
    <%-- Call DisplayItem.java function to get list of displayed items --%>
  	<c:forEach var="opt" items="${item.displayValues}">
  		<c:set var="optSelected" value="false" />
  		<c:forEach var="sel" items="${selItems}" >
  			<c:choose>
  				<c:when test="${sel==opt}">
    				<option value="${opt}" selected="true">${opt}</option>
    				<c:set var="optSelected" value="true" />
 				</c:when>
 			</c:choose>
 		</c:forEach>
 		<c:choose>
 			<c:when test="${optSelected==false}">
 				<option value="${opt}">${opt}</option>
 			</c:when>
 		</c:choose>
    </c:forEach>
  </select>
    <c:if test="${item.action != 'none'}">
    	<input type="submit" name="${item.action}" value="${item.action}">
    </c:if>
    </br>