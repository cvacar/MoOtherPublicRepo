<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%-- Date		Author	Change
	7/15/2009	TJR		Modified fnKillWorking() so proper images are shown when function is called
						Modified default image shown above workflow to be named "webapps/images/workflow_menu_default_image.PNG"
 	8/15/2009	TJR		Removed TableSorter JS function from PgMaster
 	12/19/2009	TJR		Added doScroll() function to onLoad section of master body to automatically scroll page 
 							to the top of the screen (req'd when loading large sortable tables to reset to top)
 	09/18/2010	TJR		Added dateTimePicker widget support and moved datePicker JS function to widget_datepicker.jsp
 						Updated jquery to v1.4.2 along with core jquery-ui javascripts
 --%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>LIMS Task <c:out value="${task.taskName}" default=" "/></title>
<link href="webapps/style/app.css" rel="stylesheet" type="text/css">
<link href="webapps/style/linx.css" rel="stylesheet" type="text/css">

<%-- Add jQuery capabilities to all pages...--%>
<%-- Replace jquery-<version>.js with jquery-<version>.min.js for production %>
<%--  script type="text/javascript" src="../jquery/jquery-1.3.2.min.js"></script --%>
<link rel="stylesheet"  href="jquery/themes/custom-theme/jquery-ui-1.7.1.custom.css"
	type="text/css" media="print, projection, screen" />
<link rel="stylesheet" href="jquery/plugins/tablesorter/themes/blue/style.css" 
	type="text/css" media="print, projection, screen" />
<script type="text/javascript" src="jquery/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="jquery/ui/jquery-ui-1.8.4.custom.js"></script>
<script type="text/javascript" src="jquery/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="jquery/ui/jquery.ui.datepicker.js"></script>
<script type="text/javascript" src="jquery/ui/jquery.ui.resizable.js"></script>
<script type="text/javascript" src="jquery/ui/jquery.ui.accordion.js"></script>
<script type="text/javascript" src="jquery/ui/jquery.ui.tabs.js"></script>
<script type="text/javascript" src="jquery/plugins/tablesorter/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="jquery/plugins/tablesorter/addons/pager/jquery.tablesorter.pager.js"></script>
<script type="text/javascript" src="jquery/plugins/tablesorter/jquery.dimensions.js"></script>
<script type="text/javascript" src="jquery/plugins/dateTimePicker/jquery.ui.datetimepicker.js"></script>


<script language="Javascript" type="text/javascript">
   <%-- The functions below initialze the jQuery widgets used by any task in workflow.
        Base widget behavior may be overridden based on specific needs
        (e.g. see widget_datepicker.js) 
    --%>
    <%-- 
    Removed "datePicker" from PgMaster and put it directly in widget_Datepicker.jsp.
    This was done to allow the datePicker js code to also be put directly into the widget_rowsetRow.jsp
    so datePicker (and dateTimePicker) objects could be created in rowset tables.  Code in individual widget jsps
    and the code in the rowsetRow jsp will have to be modified if objects are changed.
          
	$(function() {
		$('.datepicker').datepicker({
			changeMonth: true,
			changeYear: true,
			dateFormat: 'mm-dd-yy'
		});
	});
	
    --%>
    
	<%-- Removed "TableSorter" function from PgMaster.  It is now
		created in the widget_rowsetTableSortable.jsp explicitly
		for each "table" id and "pager" id html widget 
	
	$(function() {
		$('.tablesorter')
			.tablesorter({widthFixed: false, positionFixed: false, widgets: ['zebra']})
			.tablesorterPager({container: $("#pager"), positionFixed: false});
	});
	
	--%>
	
	$(function() {
		$("#workflow").accordion({
			fillSpace: true,
			clearStyle: false,
			navigation: true,
			collapsible: false,
			autoheight: false,
			active: false,
			change: function(e, ui) {   
				// add function here to do things on "change event" for widget
				var currHeader = ui.newHeader.text();
				//alert("new currHeader: " + currHeader);   
	 			// alert(ui.newHeader.text() + " was opened, " + ui.oldHeader.text() + " was closed");     
	 			}
		});
	});
	
	$(function() {
		$("#workflowResizer").resizable({
			resize: function() {
				$("#workflow").accordion("resize");
			},
			minHeight: 140
		});
	});
	
</script>



<script language="Javascript" type="text/javascript">
<%-- scroll to top of page --%>
function doScroll()
{
  window.scrollTo(0, 0);
}
</script>


<%--Animate a message on submit, stop animation if browser is stopped --%>
<script language="Javascript" type="text/javascript">

	workingImages = new Array("webapps/images/waiting-db-grey-32.png", "webapps/images/waiting-hourglass-grey-32.png")
	currImg = 0
	imgCount = workingImages.length
	
	function fnWorking() {
	  if(document.images) 
	  {
	    currImg++
	    if(currImg == imgCount) 
	    {
	    	currImg = 0
	    }
	    document.workingImg.src=workingImages[currImg]
	  }
	  setTimeout('fnWorking()', 1*200)
	}	

</script>

<%--Prevent endless animation on a button that downloads a file --%>
<script language="Javascript" type="text/javascript">
	
	function fnKillWorking() {
		workingImages = new Array("webapps/images/workflow_menu_default_image.PNG")
		imgCount = workingImages.length
		currImg = 0
	}	
        
</script>


</head>
<body bgcolor="white" onLoad="doScroll()">
		
<table class="masterTable">
	<!-- Set message to empty, so task messages don't persist when user logs out -->
	<c:set var="msg" value=" "/>
    <jsp:include page="linxHeading.jsp" flush="true" />
  	<jsp:include page="linxBreadcrumb.jsp" flush="true"/>
  	<td><img name="workingImg" src="webapps/images/workflow_menu_default_image.PNG"></td>
	<tr id="mainContentRow">
	<td>
	<table class="mainContentTable">
	  <tr class="mainContentTableRow">
		<jsp:include page="linxWorkflowMenu.jsp" flush="true"/>
    	<td id="taskPgTableCell">
    		<table id="taskPgTable">
		    	<tr><td class="taskTitleCell">${task.taskName}</td>
		    	</tr>
				<jsp:include page="linxMessage.jsp" flush="true"/>
				 <tr class="taskPageRow">
		              <td>
				         <c:choose>
				           <c:when test="${task.taskName == null}">Please select a task to begin.</c:when>
				           <c:otherwise>
				           <table>
							<tr><td>
				           <jsp:include page="${sessionScope.taskPg}" flush="true"/>
				           </td></tr>
				           </table>
				           </c:otherwise>
				        </c:choose>
				      </td>
		         </tr>
		        </table>
        </td>
	  </tr>
    </table>
    </td>
    </tr>
    <jsp:include page="linxStatusBar.jsp" flush="true"/>
</table>
</body>
</html>
