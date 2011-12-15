<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:if test="${item.scroll == true}">
   <div class="${item.scrollSize}scroll">
</c:if>
 <table border="2px" width="100%">
  <tr>
   <td>
      <pre>
      ${item.value}
      </pre>
   </td>
  </tr>
  </table>
<c:if test="${item.scroll == true}">
</div>
</c:if>
<table>
  <tr>
   <td>&nbsp</td>
  </tr>
</table>