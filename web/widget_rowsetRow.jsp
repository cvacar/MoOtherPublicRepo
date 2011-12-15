<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- This javascript will override the date format set by the initial datetimepicker
	 class (set in PgMaster_Default.jsp) based on definition from task xml --%>
	 

<c:if test="${min == 0}">
   <c:set var="min" value="10"/>
</c:if>
<c:if test="${max== 0}">
  <c:set var="max" value="20/"/>
</c:if>

<%-- Begin column looping --%>
<%-- (view was stored in session by task sv) --%>
<c:forEach varStatus="c" var="column" items="${view.columns}">
	
	<%-- (make vars available to off-page helper JSPs --%>
	<c:set var="widget" value="${column.widgetType}" scope="page"/>
	<c:set var="cellData" value="${column.cellValue}"/>
	<c:set var="coord" value="${view.name}.row_${rowIndex}.col_${c.count}" scope="page"/>
	<c:set var="placeholder" value ="${view.placeholders}" scope="page"/>	
	<c:set var="cellname" value ="${view.cellname}" scope="page"/>	
	<c:set var="selected" value ="${view.selectedCells}" scope="page"/>	
	<c:set var="clickable" value ="${view.clickable}" scope="page"/>
	<c:set var="selectedRadio" value ="${view.selectedRadio}" scope="page"/>
	<c:set var="selRow" value ="${view.selRow}" scope="page"/>
	<c:set var="min" value="${column.minLength}"/>
	<c:set var="max" value="${column.maxLength}"/>
	<c:set var="dateFormat" value="${column.dateFormat}"/>
	
	<td class="${rowsetCellClass}" nowrap>
		<c:choose>
		    <%-- special, for checkboxes and radio button cell --%>
			<c:when test="${placeholder == true}">${cellData}
			</c:when>
		<c:otherwise>
		  <c:choose>
		  
		    <%-- link cell --%>
			<c:when test="${widget=='href'}">
				<a href="<c:out value='${task.taskSv}'/>?taskName=${task.websafeTaskName}&selCoord=${coord}&selVal=${cellData}">${cellData}</a>
			</c:when>
			
		    <%-- link cell open in new window --%>
			<c:when test="${widget=='href_new_window'}">
				<a href="<c:out value='${task.taskSv}'/>?taskName=${task.websafeTaskName}&selCoord=${coord}&selVal=${cellData}" target="blank">${cellData}</a>
			</c:when>		
				
		    <%-- textbox cell --%>
			<c:when test="${widget=='textinput'}">
				<input type="text" 
				       name="${coord}" 
				       value="${cellData}"
				       size="${min}"
				       maxLength="${max}">
			</c:when>
			
		    <%-- textarea cell --%>
			<c:when test="${widget=='textarea'}">
            	<textarea name="${coord}" 
                	value="${cellData}" 
                    maxLength="{maxLength}" 
                    rows="{widget/@rows}" 
                    cols="{widget/@columns}">
                    ${tok1}
               </textarea>
			</c:when>
			
			 <%-- datepicker cell --%>
			<c:when test="${widget=='datepicker'}">
				<%-- JS used to create datepicker object in rowset cell --%>
				<script language="Javascript" type="text/javascript">
					$(function() {
						$('.datepicker').datepicker( {
							changeMonth : true,
							changeYear : true,
							dateFormat : '${dateFormat}'
						});
					});
					<%-- alert('df is: ' + "'${dateFormat}'" + ' for coord: ' + '${coord}'); --%>
				</script>

				<input type="text" 
					name="${coord}" 
					class="datepicker"
					id="${coord}" 
					value="${cellData}" 
					size="${min}"
					maxlength="${max}" 
					dateFormat="${column.dateFormat}">
			</c:when>

			<%-- datetimepicker cell --%>
			<c:when test="${widget=='datetimepicker'}">
				<%-- JS used to create datepicker object in rowset cell --%>
				<script language="Javascript" type="text/javascript">
					$(function() {
						$('.datetimepicker').datetimepicker({
						changeMonth: true,
						changeYear: true,
						dateFormat: '${dateFormat}'
						});
					});
				<%-- alert('df is: ' + "'${dateFormat}'" + ' for coord: ' + '${coord}'); --%>
				</script>

				<input type="text" 
					name="${coord}" 
				   	class="datetimepicker"
				   	id="${coord}" 
                   	value="${cellData}" 
                   	size="${min}" 
                   	maxlength="${max}"
                   dateFormat="${column.dateFormat}">
			</c:when>
			
		    <%-- checkbox cell --%>
			<c:when test="${widget=='checkbox'}">
				<c:choose>
					<c:when test="${cellData == 'true'}">
				       <input type="checkbox" name="${coord}" value="${cellData}" checked/>
				   </c:when>
					<c:when test="${selected == true}">
				       <input type="checkbox" name="${coord}" value="${cellData}" checked/>
				   </c:when>
					<c:when test="${cellData == ''}">
				       ''
				   </c:when>				   
				   <c:otherwise>
				      <input type="checkbox" name="${coord}" value="${cellData}"/>
				   </c:otherwise>
				</c:choose>
			</c:when>
			
		    <%-- radio button cell --%>
			<c:when test="${widget=='radio'}">
				<c:choose>
					<c:when test="${cellData == 'true'}or ${selected == true}">
     				  <c:choose>
					    <c:when test="${cellname == null}">
				         <input type="radio" name="row${rowIndex}" value="${coord}" checked>${cellData}
				       </c:when>
				     <c:otherwise>
				    <input type="radio" name="${cellname}" value="${coord}" checked>${cellData}
				  </c:otherwise>
				</c:choose>
				</c:when>
				    <c:when test="${coord == selectedRadio}">
     				  <c:choose>
					    <c:when test="${cellname == null}">
				         	<input type="radio" name="row${rowIndex}" value="${coord}" checked>${cellData}
				       	</c:when>
				<c:otherwise>
				   		 	<input type="radio" name="${cellname}" value="${coord}" checked>${cellData}
				  	  	</c:otherwise>
				 	 </c:choose>
				    </c:when>
     				<c:when test="${clickable == 'true'}">
     				  <c:choose>
     				  		<c:when test="${cellname == 'null'}">
				         		<input type="radio" name="row${rowIndex}" value="${coord}" onclick="this.form.submit()">${cellData}
				        	</c:when>
				        	<c:otherwise>
				        		<input type="radio" name="${cellname}" value="${coord}" onclick="this.form.submit()">${cellData}
				  			</c:otherwise>
     				  	</c:choose>
     				</c:when>
     				<c:otherwise>
     				  	<c:choose>
     				  		<c:when test="${cellname == 'null'}">
				         <input type="radio" name="row${rowIndex}" value="${coord}">${cellData}
				       </c:when>
				     <c:otherwise>
				    <input type="radio" name="${cellname}" value="${coord}">${cellData}
				  </c:otherwise>
				</c:choose>
			</c:otherwise>
			</c:choose>
			</c:when>
			
		    <%-- dropdown cell --%>
			<c:when test="${widget=='dropdown'}">
			
			<select name="${coord}">
            	 <option name="(Select)">(Select)</option>
  	         		<c:forEach var="opt" items="${column.widgetOptions}">
        	 	<c:choose>
  		        	 <c:when test="${cellData == opt}">
    		    		 <option name="${opt}" selected="true">${opt}</option>
 		        	 </c:when>
 		    	    <c:otherwise>
     		   		 	<option name="${opt}">${opt}</option>
 		    		</c:otherwise>
 	       		  </c:choose>
 	      	   </c:forEach>
             </select>
			</c:when>
			
		    <%-- password cell --%>
			<c:when test="${widget=='password'}">
				<input type="password" name="${coord}" value="">
			</c:when>
			
		    <%-- hidden cell --%>
			<c:when test="${widget=='hidden'}">
		      <input type="hidden" name="${coord}" value="${cellData}"/>
			</c:when>
			
		    <%-- read-only text cell --%>
			<c:otherwise>
				${cellData}
			 </c:otherwise>
			 
		</c:choose>  	
		</c:otherwise>
	</c:choose>  	
		</td>
	
	<%-- cleanup --%>
	<c:remove var="coord" scope="page"/>
	<c:remove var="widget" scope="page"/>
	<c:remove var="cellData"/>
	<c:remove var="placeholder" scope="page"/>
	<c:set var="colIndex" value="${view.nextColumn}" scope="page"/>
</c:forEach>

<c:remove var="colIndex" scope="page"/>

