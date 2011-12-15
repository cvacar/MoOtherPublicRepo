<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Form will submit to task.getTaskSv() value --%>
<form action="${task.taskSv}" method="POST" enctype="multipart/form-data" onsubmit="return fnWorking()">

<input type="hidden" name="taskName" value="${task.taskName}"/>


<%-- Existing collection --%>

<table class="topRowset">
    <tr>
<td width="90%">
 <c:forEach var="view1" items="${rowsetViews}">
	<c:set var="view" value="${view1}" scope="session"/>
	 <c:if test="${view.name=='Cultures'}">
	<jsp:include page="/widget_rowsetTableCompact.jsp" flush="true"/>
	</c:if>
</c:forEach>
</td>
</tr>
<tr>
<td>
<%-- button with New label/action --%>
Start from an existing culture by selecting it above, or start from a blank culture:
<input type="submit" align="left" name="New" value="Clear Values" action="New"/>

</td>
</tr>
</table>

	<c:set var="bean" value="${bean}" scope="session"/>
<hr>
<table class="promptGroups">
	<tr class="culturePromptGroup" align="left" valign="top">
		<td class="itemWidget">
		  Clone Name<br/>
			<input type="text" name="Clone" value="${bean.clone}"/>
		<input type="image" src="webapps/images/MagnifyingGlass_XSmall.png" value="${bean.clone}" name="Find" onclick="fnKillWorkfing()" />		
		</td>
		<td class="itemWidget">
		  Strain ID<br/>
			<input type="text" name="CloneId" value="${bean.cloneId}"/>
		</td>
	 	<td class="itemWidget">
	 	Project<br/>
	  <select class="dropdown" name="Project">
	  	<c:forEach var="opt" items="${task.projects}">
	  	<c:choose>
	  		<c:when test="${bean.project==opt}">
	    		<option name="${opt}" selected="true">${opt}</option>
	 		</c:when>
	 		<c:otherwise>
	     		<option name="${opt}">${opt}</option>
	 		</c:otherwise>
	 	</c:choose>
	  </c:forEach>
    </select>
    </td> 
			<td class="itemWidget">
			Notebk#-pg#<br/>
			<input type="text" name="NotebookRef" value="${bean.notebookRef}"/>
					</td>
			<td class="itemWidget">
			Freezer:Box:Position<br/>
			<input type="text" name="Location" value="${bean.location}"/>
					</td>
			<td class="itemWidget">
			Restricted<br/>
			<c:if test="${bean.restricted=='t'}">
			<input type="checkbox" name="Restricted" checked/>
			</c:if>
			<c:if test="${bean.restricted!='t'}">
			<input type="checkbox" name="Restricted"/>
			</c:if>						
		</td>
	</tr>
	<tr class="cloneTypePromptGroup">
 	<td class="itemWidget">
 	Clone Type<br/>
  <select class="dropdown" id="CloneType" name="CloneType" onchange="showCloneTypeDetail()">
  	<c:forEach var="opt" items="${task.cloneTypes}">
  	<c:choose>
  		<c:when test="${bean.cloneType==opt}">
    		<option name="${opt}" selected="true">${opt}</option>
 		</c:when>
 		<c:otherwise>
     		<option name="${opt}">${opt}</option>
 		</c:otherwise>
 	</c:choose>
  </c:forEach>
    </select>
    </td> 
 
 	<td class="itemWidget" id="Vendor">
 	Vendor<br/>
  <select class="dropdown"  name="Vendor" >
  	<c:forEach var="opt" items="${task.vendors}">
  	<c:choose>
  		<c:when test="${bean.vendor==opt}">
    		<option name="${opt}" selected="true">${opt}</option>
 		</c:when>
 		<c:otherwise>
     		<option name="${opt}">${opt}</option>
 		</c:otherwise>
 	</c:choose>
  </c:forEach>
    </select>
    </td> 
     	<td class="itemWidget"  id="CatalogNumber">
     	 Catalog Num<br/>
  		<input type="text" name="CatalogNumber" value="${bean.catalogNumber}" />
    </td> 
    <td class="itemWidget" id="TemplateId">
      Template ID<br/>
  		<input type="text"  name="TemplateId" value="${bean.templateId}"/>
    </td> 
     <td class="itemWidget" id="LibraryId">
     Library ID<br/>
  		<input type="text" name="LibraryId" value="${bean.libraryId}"/>
    </td> 
	</tr>
	</table>
	<hr>
	<table>
	<tr class="seqPromptGroup">
	 <td width="20%">
	 <input type="image" src="webapps/images/DNASeq_XSmall.png" value="${bean.clone}" name="GetDNASeq" onclick="fnKillWorking()"/>	
	
	</td>
	<td>
	  Locate new DNA sequence file:<br/>
		<input type="file" name="DNASequenceFile" value="${bean.dnaSequenceFile}" size="50"/>
		</td>
      </tr>
        <tr>
          <td>
	 <input type="image" src="webapps/images/VectorMap_XSmall.png" value="${bean.clone}" name="GetVectorMap" onclick="fnKillWorkfing()" />	
      </td>
         <td>
	  Locate new vector map:<br/>
		<input type="file" name="VectorMap" value="${bean.vectorMap}" size="50"/>
		</td>
	</tr>
	</table>
		Or, paste DNA sequence here:<br>
      <textarea name="DNASequence"
                value="${bean.dnaSequence}" 
                wrap="virtual"
                maxLength="300" 
                rows="4" 
                cols="40">${bean.dnaSequence}</textarea>
	<hr>
	<table>
	<tr class=AbPromptGroup" align="left" valign="top">
			<td class="itemWidget">
			 Selection Marker<br/>
			 <input type="text" name="AntibioticResistance" value="${bean.antibioticResistance}"/>
			 </td>
			<td class="itemWidget">
			 Medium<br/>
			 <input type="text" name="Medium" value="${bean.medium}"/>
			 </td>
			<td class="itemWidget">
			 Host Organism<br/>
			 <input type="text" name="HostOrganism" value="${bean.hostOrganism}"/>
			 </td>
			<td class="itemWidget">
			 Plasmid<br/>
			 <input type="text" name="Plasmid" value="${bean.plasmid}"/>
			 </td>
			<td class="itemWidget">
			 ORI<br/>
			 <input type="text" name="ORI" value="${bean.ORI}"/>
			 </td>
			 	<td class="itemWidget">
	  		Parental Vector<br/>
		<input type="text" name="ParentalVector" value="${bean.parentalVector}"/>
	</td>
	</tr>
	</table>

	<hr>
	
	Comments<br>
      <textarea name="Comments" 
                wrap="virtual"
                value="${bean.comments}"
                maxLength="300" 
                rows="4" 
                cols="40">${bean.comments}</textarea>
<%-- button with Verify label/action --%>
Click to save properties of this culture:
<input type="submit" align="left" name="Save" value="Save Changes" action="Save"/>
<br>
<br/>
<A class="small" HREF="Task_Add_a_Gene?menuSelected=true&taskName=Add%20a%20Gene">Add a Gene</A>
<br>
<%-- Genes for selected culture --%>
 <c:forEach var="view1" items="${rowsetViews}">
	<c:set var="view" value="${view1}" scope="session"/>
	 <c:if test="${view.name=='Genes'}">
	<jsp:include page="widget_rowsetTableCompact.jsp" flush="true"/>
	</c:if>
</c:forEach>

</form>
