<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%--
This widget will display an object (e.g. image, pdf file).  

Set item.name to the referenced path/filename of the image/object to display
Set item.target to "image" or "pdf" in order to display object properly
  "image" is the default
Set item.minLength set specify max display width
  "800" is the default
Set item.maxLength to set the max display height
  "400" is the default
--%>

<c:set var="objectSrc" value="${item.name}"/>
<c:set var="width" value="${item.minLength}"/>
<c:set var="height" value="${item.maxLength}"/>

<c:if test="${width == 0}">
  <c:set var="width" value="800"/>
</c:if>
<c:if test="${height== 0}">
  <c:set var="height" value="400"/>
</c:if>

<c:choose>
  <c:when test="${item.target == 'pdf'}">
    <p>
      <br>
      <object data="${objectSrc}" type="application/pdf" width="${width}" height="${height}"> </object>
      <br>
      <br>
      <br>
    </p>
  </c:when>
  <c:otherwise>
    <%-- We should be able to put an image into an object with the proper 'type' but IE 
       doesn't support that.  Image in an Object syntax looks like:
       <object data="webapps/images/branding.jpg" type="image/jpeg"> </object> 
    --%>
    <p>
      <br>
      <img src="${objectSrc}" width="${width}" height ="${height}">
      <br>
      <br>
      <br>
    </p>
  </c:otherwise>
</c:choose>

<c:remove var="width"/>
<c:remove var="height"/>
<c:remove var="objectSrc"/>

