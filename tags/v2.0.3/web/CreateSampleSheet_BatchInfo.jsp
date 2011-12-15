<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Create Sample Sheet - Select Samples</title>
</head>
<script language="javascript">

function checkRow(row)
{
	var cxbx = 'SelectRow' + row;
	var bxname = 'row' + row;
	for (var i=0; i<document.forms[0].elements.length; i++)
	{
		var e = document.forms[0].elements[i];
		if ( (e.type == 'checkbox') && (e.name.indexOf(bxname + '_') >=0) )
		{
			e.checked=cxbx.checked;
		}
	}
}

function checkAll()
{
	for (var i=0; i<document.forms[0].elements.length; i++)
	{
		var e=document.forms[0].elements[i];
		if ((e.name !='SelectAll') && (e.type=='checkbox'))
		{
			e.checked=document.forms[0].SelectAll.checked;
		}
	}
}
</script>

<body>
<form method="post" onsubmit="return fnWorking()"  action="">
  <label>Batch ID:
     <%String batch = (String)session.getAttribute("batch");%>
              <%ArrayList alBatch = (ArrayList)session.getAttribute("BatchArray");%>
                <select name="Batch" id="Batch">
                <%if (batch != null && !batch.equalsIgnoreCase("") )
    			{%>
    				<option selected="selected"><%= batch %></option>
    			<%}
    			else 
    			{%>
                  <option selected="selected">(Select)</option>
                <%}%>
                <%int numBatches = alBatch.size();%>
                <%for(int i = 0; i < numBatches; i++)
				{
					if(alBatch.get(i).toString().equalsIgnoreCase(batch))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alBatch.get(i));%></option>
                  <%}%>
               <%}%>
                </select>
      </label>
     <input type="submit" name="RetrieveBatchInfo" id="RetrieveBatchInfo" value="Retrieve Batch Info" />
     <p></p>
     <b>OR</b>
     <p></p>
     <label>Enter a batch number:</label>
     <input type="text" name="BatchText" id = "BatchText"/>
     <input type="submit" name="RetrieveBatch" id="RetrieveBatch" value="Retrieve Batch" />
     <p></p>
  <%if(batch != null){ %>
  <table border="2">
  <%ArrayList alFermentationIds = (ArrayList)session.getAttribute("FermentationIdArray");%>
  <%int numFermentors = alFermentationIds.size(); %>
    <tr>
      <td nowrap="nowrap">Fermentation ID:</td>
      <%if(alFermentationIds != null)
      { %>
        <%for(int j = 0; j < numFermentors; j++)
		{%>
			<%String sName =  "Fermentor" + j;%>
			<td nowrap="nowrap">
				<label><%= alFermentationIds.get(j)%></label>
				<input type ="hidden" name="<%= sName%>" id="<%= sName%>" value="<%= alFermentationIds.get(j)%>" ></input>
			</td>
		<%} %>
		<td>
			<input type="checkbox" name="SelectAll" onclick="checkAll();" checked="checked" value="Select All"/>Select All<br />
		</td>
	<%} %>
    </tr>
    <tr>
      <td>Time Points:</td>
      <%ArrayList alEfts = (ArrayList)session.getAttribute("EFTArray");%>
      <%int numEfts = alEfts.size(); %>
      <%for(int k= 0; k < numEfts; k++)
	  {%>
	  	<%ArrayList alData = (ArrayList)alEfts.get(k);%>
        <%int numCols = alData.size();%>
        <%for(int l = 0; l < numCols; l++)
		{%>
			<%String sName = "row" + k + "_col" + l; %>
			<%if(sName.contains("col0"))
			{ 
				if(k == 0)
				{%>
					<%String sEft = (String)alData.get(0);%>
					<%for(int j = 0; j < numFermentors; j++)
					{%>
						<%String name = "row" + k + "_col" + j; %>
						<td>
							<input type="checkbox" name="<%= name%>"  value="<%= sEft%>" checked="checked"/><%= sEft %><br />
						</td>
					<%}%>
					<td nowrap="nowrap">
						<%String ckbx = "SelectRow" + k; %>
						<input type="checkbox" name="<%= ckbx%>" onclick="checkRow(<%=k %>);" checked="checked" value="Select Row"/>Deselect Row<br />
					</td>
				<%}
				else
				{ %>
					<tr>
						<td></td>
						<%String sEft = (String)alData.get(0);%>
				  	  	<% for(int x = 0; x < numFermentors; x++)
						{%>
							<%String name = "row" + k + "_col" + x; %>
							<td>
								<input type="checkbox" name="<%= name%>" value="<%= sEft%>" checked="checked"/><%= sEft %><br />
							</td>
						<%}%>
						<td nowrap="nowrap">
							<%String ckbx = "SelectRow" + k; %>
							<input type="checkbox" name="<%= ckbx%>" onclick="checkRow(<%=k %>);" checked="checked" value="Select Row"/>Deselect Row<br />
						</td>
					</tr>
				<%} %>
			<%} %>
           <%}%>
      <%}%>
    </tr>
  </table>
  <p></p>
  <input type="submit" name="Refresh" id="Refresh" value="Refresh" />
  <input type="submit" name="SelectSamples" id="SelectSamples" value="Select Samples" />
  <%} %>
</form>
</body>
</html>