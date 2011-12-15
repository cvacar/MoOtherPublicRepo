<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    	<c:choose>
    	<c:when test="${message.statusCode == 'OK'}">
	    <td class="message" id="limsMessageCell" align="left" valign="top" height="15" colspan="3">
	        <label id="message" class="limsmessage">
		         <c:out value="${message.message}" default=""/>
		     </label>
		 </td>
		 </c:when>
	    <c:otherwise>
	    <td class="error" id="limsMessageCell" align="left" valign="top" height="15" colspan="3">
	        <label id="message" class="limsmessage">
		         <c:out value="${message.message}" default=""/>
		     </label>
		 </td>
		 </c:otherwise>
		 </c:choose>