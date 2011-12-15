<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:if test="${item.rowCount == null}">
          <textarea name="${item.itemType}"  
                wrap="virtual"
                    rows="4" 
                    cols="40">${item.value}</textarea>
</c:if>
<c:if test="${item.rowCount != null}">
      <textarea name="${item.itemType}" 
                wrap="virtual"
                rows="${item.rowCount}" 
                cols="40">${item.value}</textarea>
 </c:if>
