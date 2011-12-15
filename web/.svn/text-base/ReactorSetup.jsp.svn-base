<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Reactor Setup</title>
</head>

<body>
<form method="post" onsubmit="return fnWorking()">
    <table width="100%" border="0">
      <tr>
        <td><table width="520" border="0">
          <tr>
            <td>
              <label nowrap="nowrap">Batch ID:
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
            </td>
            <td>
                <input type="submit" name="RetrieveInfo" id="RetrieveInfo" value="Retrieve Setup Info" />
            </td>
            <td>
            	<input type="submit" name="RetrieveStrain" id="RetrieveStrain" value="Retrieve Strain Info" />
            </td>
          </tr>
        </table></td>
      </tr>
      <tr>
        <td>
         <table width="100%" border="0">
         <%if(batch != null){ %>
          <tr>
                <td nowrap="nowrap">
               
                	<jsp:include page="/ReactorSetup_Column.jsp" flush="true"/>
                	
                </td>
          </tr>
          <tr>
            <td width="100%">
              <input type="submit" name="Save" id="Save" value="Save" />
            </td>
          </tr>
          <%} %>
    </table>
   </td>
  </tr>
 </table>
</form>
</body>
</html>