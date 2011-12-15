<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Monitor Batch</title>
</head>
<%ArrayList alFermentationIds = (ArrayList)session.getAttribute("FermentationIdArray");%>
<%String batch = (String)session.getAttribute("batch");%>
<body>
<form method="post" onsubmit="return fnWorking()">
<table>
      <tr>
      	<td>
          <label>Batch ID:</label>
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
                <hr />
        </td>
        <td>
            <input type="submit" name="RetrieveData" id="RetrieveData" value="Retrieve Data" />
        </td>
      </tr>
       <%if (batch != null && !batch.equalsIgnoreCase("") )
   		{%>
      <tr>
      	<td>	
      		<label><b>Displaying EFTs for batch <%=batch %>.  To update OD values edit individual EFTs then click 'Update Data'</b></label>
      	</td>
      </tr>
      <tr>
      	<td>
      		<table border="2" cellpadding="2">
      			<tr>
      				<td nowrap="nowrap">EFT</td>
        			<%if(alFermentationIds != null)
      				{ %>
						<%int numFermentors = alFermentationIds.size(); %>
        				<%for(int j = 0; j < numFermentors; j++)
						{%>
							<%String sName =  "Fermentor" + j;%>
							<td nowrap="nowrap">
								<label id="<%= sName%>"><%= alFermentationIds.get(j)%></label>
								<input type ="hidden" name="<%= sName%>" id="<%= sName%>" value="<%= alFermentationIds.get(j)%>" ></input>
							</td>
						<%} %>
					<%} %>
				</tr>
				
					<%ArrayList alEfts = (ArrayList)session.getAttribute("EFTArray");%>
          			<%if(alEfts != null)
      				{ %>
      					
						<%int numEfts = alEfts.size(); %>
          				<%for(int k= 0; k < numEfts; k++)
	  					{%>
	  						<tr>
	  						<%ArrayList alData = (ArrayList)alEfts.get(k);%>
            				<%int numCols = alData.size();%>
            				<%for(int l = 0; l < numCols; l++)
							{%>
								
								<%String sName = "row" + k + "_col" + l; %>
								<%if(sName.contains("col0"))
								{ %>
									
									<td>
										<label><%= alData.get(l)%></label>
									</td>
								<%}
								else
								{ %>
									<td>
										<input value="<%= alData.get(l)%>" size="6" maxlength="6" type="text" name="<%= sName%>" id="<%= sName%>" />
									</td>
								<%} %>
								
            				<%}%>
            				</tr>
      					<%}%>
          			<%}%>
      		</table>
      		<hr />
      	</td>
      	 <td>
        	<input type="submit" name="Edit" id="Edit" value="Update Data" />
    	</td>
      </tr>
      <tr>
      	<td>
      		<p><b>OR</b></p>
      	</td>
      </tr>
      <tr>
      	<td>
      		<label><b>Please enter new EFT and OD values and then click 'Save Data'</b></label>
      	</td>
      </tr>
      <tr>
      	<td>
      		<table>
      			<tr>
      				<td nowrap="nowrap">
      					<label>EFT:</label>
          				<input size="8" maxlength="6" type="text" name="EFT" id="EFT" />
      				</td>
      				<%if(alFermentationIds != null)
      				{ %>
						<%int numFermentors = alFermentationIds.size(); %>
        				<%for(int j = 1; j <= numFermentors; j++)
						{
							String sName = "OD" + j;
							if(j == 1)
							{%>
							<td nowrap="nowrap">
								<label>OD:</label>
								<input size="6" maxlength="6" type="text" name="<%= sName%>" id="<%= sName%>" />
							</td>
						<%}
						else{
						%>
          					<td nowrap="nowrap">
          					<input size="6" maxlength="6" type="text" name="<%= sName%>" id="<%= sName%>" />
							</td>
        				<%}%>
        	 		<%}%>
       			<%}%>
      			</tr>
		  </table>	
      	</td> 
        <td>
        	<input type="submit" name="Save" id="Save" value="Save Data" />
        	
    	</td>
    	<td>
    		<input type="submit" name="Finish" id="Finish" value="Finish Batch" />
    	</td>
    </tr>
      
    <%}%>
</table>
</form>
</body>
</html>