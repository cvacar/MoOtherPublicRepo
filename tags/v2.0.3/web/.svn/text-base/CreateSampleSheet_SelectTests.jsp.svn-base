<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<script language="javascript">
function fillDown()
{
	var val;
	
	for (var i=0;i<document.forms[0].elements.length;i++)
	{
		var e=document.forms[0].elements[i];
		if (e.type=='text')
		{
			val = e.value;
			if(val != null)
				break;
		}
	}
	//now fill in the vals
	for (var i=0;i<document.forms[0].elements.length;i++)
	{
		var e=document.forms[0].elements[i];
		if (e.type=='text')
		{
			e.value = val;
		}
	}
}
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Create Sample Sheet - Select Tests to Perform</title>
</head>
<body>
<form method="post" onsubmit="return fnWorking()"  action="">
  <table width="100%" border="0">
    <tr>
      <td>	Select a Request Type:
      	<%String analysisType = (String)session.getAttribute("analysisType");%>
      	<%ArrayList alAnalysisType = (ArrayList)session.getAttribute("RequestTypes");%>
        <select name="AnalysisType" id="AnalysisType">
        	<%if (analysisType != null && !analysisType.equalsIgnoreCase("") )
    			{%>
    				<option selected="selected"><%= analysisType %></option>
    			<%}
    			else 
    			{%>
                  <option selected="selected">(Select)</option>
                <%}%>
                <%int numBatches = alAnalysisType.size();%>
                <%for(int i = 0; i < numBatches; i++)
				{
					if(alAnalysisType.get(i).toString().equalsIgnoreCase(analysisType))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alAnalysisType.get(i));%></option>
                  <%}%>
               <%}%>
      	</select>  
      	Select an Analysis Method:
      	<select name="AnalysisMethod" id="AnalysisMethod">
      		<%String analysisMethod = (String)session.getAttribute("analysisMethod");%>
      		<%ArrayList alAnalysisMethod = (ArrayList)session.getAttribute("AnalysisMethods");%>
      		<%if (analysisMethod != null && !analysisMethod.equalsIgnoreCase("") )
    			{%>
    				<option selected="selected"><%= analysisMethod %></option>
    			<%}
    			else 
    			{%>
                  <option selected="selected">(Select)</option>
                <%}%>
                <%numBatches = alAnalysisMethod.size();%>
                <%for(int i = 0; i < numBatches; i++)
				{
					if(alAnalysisMethod.get(i).toString().equalsIgnoreCase(analysisMethod))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alAnalysisMethod.get(i));%></option>
                  <%}%>
               <%}%>
      	</select>
      </td>
    </tr>
    <tr>
    	<table border = "1">
    		<tr>
    			<td>
    				<b>Sample Number</b>
	  			</td>
    			<td>
    				<b>Sample Name</b>
	  			</td>
	  			<td>
	  				<b>Dilution</b>
	  			</td>
    		</tr>
    		<%ArrayList alEfts = (ArrayList)session.getAttribute("SelectedSamples");%>
      		<%int numEfts = alEfts.size(); %>
      		<%for(int k= 0; k < numEfts; k++)
	  		{%>
	  			<%String eft = (String)alEfts.get(k); %>
	  			<%String dilutionName = "dilution_" + eft; %>
	  			<%String sampleName = eft; %>
	  			<%String rowNum =  String.valueOf(k + 1); %>
	  			<%String defaultDilution = (String)session.getAttribute("defaultDilution");%>
	  			<tr>
	  				<td>
	  					<label><%= rowNum %></label>
	  				</td>
	  				<td>
	  					<label><%= sampleName %></label>
	  				</td>
	  				<td>
	  					<%if(defaultDilution != null && !defaultDilution.equals(""))
	  					{ %>
	  						<input name="<%= dilutionName %>" type="text" size="6" maxlength="8" value="<%=defaultDilution %>" />
	  					<%}else
	  					{ %>
	  						<input name="<%= dilutionName %>" type="text" size="6" maxlength="8" />
	  					<%} %>
	  					
	  				</td>
	  				<%if(k == 0)
	  				{ %>
	  					<td>
	  						<input type="button" onclick="fillDown();" name="FillDown" id="FillDown" value="Fill Down" />
	  					</td>
	  				<%} %>
	  			</tr>
	  			
	  		<%} %>
    	</table>
    </tr>
    <tr>
      <td>
      	<input type="submit" name="Back" id="Back" value="Go Back" />
      	<input type="submit" name="CreateSampleSheet" id="CreateSampleSheet" value="Create Sample Sheet" />
      </td>
    </tr>
  </table>
</form>
</body>
</html>