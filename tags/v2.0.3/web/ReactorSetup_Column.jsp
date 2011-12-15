<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Untitled Document</title>
</head>

<body>
<%String sNumReactors = (String)session.getAttribute("numberReactors");%>
<%int iNumReactors = Integer.parseInt(sNumReactors); %>
<%ArrayList alSupps = (ArrayList)session.getAttribute("SupplementArray");%>
<%int numSupps = alSupps.size();%>
<%ArrayList alMedium = (ArrayList)session.getAttribute("MediaArray");%>
<%int numMedia = alMedium.size();%>
<%String sBatchType = (String)session.getAttribute("batchType");%>

<table width="100%" border="0">
  <tr>
    <td nowrap="nowrap"><label >Fermentation ID:</label>&nbsp;</td>
     <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_FermentationID";%>
    	<%String fermId = (String)session.getAttribute(sName);%>
    <td>
    	<input value="<%= fermId%>" type="text" name="<%= sName%>" id="<%= sName%>" />
        <hr />
    </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Strain ID:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Strain";%>
        <%String strain = (String)session.getAttribute(sName);%>
    	<td>
    		<input value="<%= strain%>" type="text" name="<%= sName%>" id="<%= sName%>"  />
		</td>
     <%}%>
  </tr>
  <tr>
    <td><label>Strain Name:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    <%String sName = "col" + i + "_StrainName";%>
    <%String strainName = (String)session.getAttribute(sName);%>
    	<td>
    		<label name="<%= sName%>" id="<%= sName%>"><%= strainName%></label>
		</td>
    <%}%>
  </tr>
  <tr>
    <td><label>Growth Requirements:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    <%String sName = "col" + i + "_GrowthRequirements";%>
    <%String growthReqs = (String)session.getAttribute(sName);%>
    	<td>
    		<label name="<%= sName%>" id="<%= sName%>"><%= growthReqs%></label>
		</td>
    <%}%>
  </tr>
  <tr>
    <td><label>Genotype:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    <%String sName = "col" + i + "_Genotype";%>
    <%String genotype = (String)session.getAttribute(sName);%>
    	<td>
    		<label name="<%= sName%>" id="<%= sName%>"><%= genotype%></label>
		</td>
    <%}%>
  </tr>
  <tr>
    <td><label>Comments:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    <%String sName = "col" + i + "_StrainComments";%>
    <%String comments = (String)session.getAttribute(sName);%>
    	<td>
    		<label name="<%= sName%>" id="<%= sName%>"><%= comments%></label>
    		<hr />
		</td>
        
    <%}%>
  </tr>
   <tr>
    <td><label>Media:</label>&nbsp;</td>
     <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Media";%>
    	<%String media = (String)session.getAttribute(sName);%>
    <td>
    	<select name="<%= sName%>" id="<%= sName%>">
    			<%if (media != null && !media.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= media %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int j = 0; j < numMedia; j++)
				  {
				  	if(alMedium.get(j).toString().equalsIgnoreCase(media))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alMedium.get(j));%></option>
                  <%}%>
                <%}%>
            </select>
    </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Supplement 1:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Supplement1";%>
    	<%String supp = (String)session.getAttribute(sName);%>
    	<td>
                <select name="<%= sName%>" id="<%= sName%>">
                <%if (supp != null && !supp.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int k = 0; k < numSupps; k++)
				  {
				  	if(alSupps.get(k).toString().equalsIgnoreCase(supp))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alSupps.get(k));%></option>
                  <%}%>
                <%}%>
           </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td nowrap="nowrap"><label>Concentration 1:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Concentration1";%>
        <%String conc = (String)session.getAttribute(sName);%>
    	<td>
            <input value="<%= conc %>" type="text" name="<%= sName%>" id="<%= sName%>" />
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Units 1:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Units1";%>
    	<%String supp1units = (String)session.getAttribute(sName);%>
     	<td>
            <select name="<%= sName%>" id="<%= sName%>">
            	<%if (supp1units != null && !supp1units.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp1units %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!supp1units.equalsIgnoreCase("g/L"))
            	{%>
              		<option>g/L</option>
              	<%} %>
              	<%if(!supp1units.equalsIgnoreCase("mg/L"))
            	{%>
              		<option>mg/L</option>
              	<%} %>
              	<%if(!supp1units.equalsIgnoreCase("ml/L"))
            	{%>
              		<option>ml/L</option>
              	<%} %>
              	<%if(!supp1units.equalsIgnoreCase("mM"))
            	{%>
              		<option>mM</option>
              	<%} %>
            </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Supplement 2:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Supplement2";%>
    	<%String supp = (String)session.getAttribute(sName);%>
    	<td>
                <select name="<%= sName%>" id="<%= sName%>">
                <%if (supp != null && !supp.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int k = 0; k < numSupps; k++)
				  {
				  	if(alSupps.get(k).toString().equalsIgnoreCase(supp))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alSupps.get(k));%></option>
                  <%}%>
                <%}%>
                </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Concentration 2:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Concentration2";%>
    	<%String conc = (String)session.getAttribute(sName);%>
    	<td>
            <input value="<%= conc %>" type="text" name="<%= sName%>" id="<%= sName%>" />
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Units 2:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Units2";%>
    	<%String supp2units = (String)session.getAttribute(sName);%>
    	<td>
            <select name="<%= sName%>" id="<%= sName%>">
             <%if (supp2units != null && !supp2units.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp2units %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!supp2units.equalsIgnoreCase("g/L"))
            	{%>
              		<option>g/L</option>
              	<%} %>
              	<%if(!supp2units.equalsIgnoreCase("mg/L"))
            	{%>
              		<option>mg/L</option>
              	<%} %>
              	<%if(!supp2units.equalsIgnoreCase("ml/L"))
            	{%>
              		<option>ml/L</option>
              	<%} %>
              	<%if(!supp2units.equalsIgnoreCase("mM"))
            	{%>
              		<option>mM</option>
              	<%} %>
            </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Supplement 3:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Supplement3";%>
    	<%String supp = (String)session.getAttribute(sName);%>
    	<td>
                <select name="<%= sName%>" id="<%= sName%>">
                <%if (supp != null && !supp.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int k = 0; k < numSupps; k++)
				  {
				  	if(alSupps.get(k).toString().equalsIgnoreCase(supp))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alSupps.get(k));%></option>
                  <%}%>
                <%}%>
                </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Concentration 3:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Concentration3";%>
    	<%String conc = (String)session.getAttribute(sName);%>
    	<td>
            <input value="<%= conc %>" type="text" name="<%= sName%>" id="<%= sName%>" />
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Units 3:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Units3";%>
    	<%String supp3units = (String)session.getAttribute(sName);%>
    	<td>
            <select name="<%= sName%>" id="<%= sName%>">
              <%if (supp3units != null && !supp3units.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= supp3units %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!supp3units.equalsIgnoreCase("g/L"))
            	{%>
              		<option>g/L</option>
              	<%} %>
              	<%if(!supp3units.equalsIgnoreCase("mg/L"))
            	{%>
              		<option>mg/L</option>
              	<%} %>
              	<%if(!supp3units.equalsIgnoreCase("ml/L"))
            	{%>
              		<option>ml/L</option>
              	<%} %>
              	<%if(!supp3units.equalsIgnoreCase("mM"))
            	{%>
              		<option>mM</option>
              	<%} %>
            </select>
          <hr />
        </td>
    <%}%>
  </tr>
   <tr>
    <td nowrap="nowrap"><label>Temperature (degrees C):</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Temperature";%>
    	<%String temp = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= temp %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>pH:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_pH";%>
    	<%String ph = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= ph %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>DO (%):</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_DissolvedO2";%>
    	<%String disO = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= disO %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Initial Agitation (RPM):</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_InitialAgitation";%>
    	<%String agit = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= agit %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Airflow Rate:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_AirflowRate";%>
    	<%String flow = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= flow %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Airflow Rate Units:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_AirflowRateUnits";%>
    	<%String airunits = (String)session.getAttribute(sName);%>
    	<td>
            <select name="<%= sName%>" id="<%= sName%>">
            	<%if (airunits != null && !airunits.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= airunits %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!airunits.equalsIgnoreCase("splh"))
            	{%>
              		<option>splh</option>
              	<%} %>
              	<%if(!airunits.equalsIgnoreCase("splm"))
            	{%>
              		<option>splm</option>
              	<%} %>
            </select>
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Initial Volume:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_InitialVolume";%>
    	<%String iniVol = (String)session.getAttribute(sName);%>
    	<td>
             <input value="<%= iniVol %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Initial Volume Units:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_InitialVolumeUnits";%>
    	<%String intitVolUnits = (String)session.getAttribute(sName);%>
    	<td>
            <select name="<%= sName%>" id="<%= sName%>">
            	<%if (intitVolUnits != null && !intitVolUnits.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= intitVolUnits %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!intitVolUnits.equalsIgnoreCase("L"))
            	{%>
              		<option>L</option>
              	<%} %>
              	<%if(!intitVolUnits.equalsIgnoreCase("mL"))
            	{%>
              		<option>mL</option>
              	<%} %>
            </select>
          <hr />
        </td>
    <%}%>
  </tr>
   <%if(sBatchType.equalsIgnoreCase("Batch"))
   {%>
  <tr>
    <td><label>Comments:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Comments";%>
    	<%String comments = (String)session.getAttribute(sName);%>
    	<td>
            <textarea name="<%= sName%>" id="<%= sName%>" cols="30" rows="4"><%= comments%></textarea>
          <hr />
        </td>
    <%}%>
  </tr>
    <%}%>
   <%if(sBatchType.equalsIgnoreCase("Continuous Batch"))
   {%>
       <tr>
    <td><label>Feed Medium:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_FeedMedium";%>
    	<%String media = (String)session.getAttribute(sName);%>
    	<td>
    	<select name="<%= sName%>" id="<%= sName%>">
    			<%if (media != null && !media.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= media %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int j = 0; j < numMedia; j++)
				  {
				  	if(alMedium.get(j).toString().equalsIgnoreCase(media))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alMedium.get(j));%></option>
                  <%}%>
                <%}%>
            </select>
        </td>
   <%}%>
  </tr>
    <tr>
        <td><label>Dilution Rate:</label>&nbsp;</td>
        <%for(int i = 1; i <= iNumReactors; i++)
        {%>
        	<%String sName = "col" + i + "_FeedRate";%>
            <%String flowRate = (String)session.getAttribute(sName);%>
    		<td>
             	<input value="<%= flowRate %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       		</td>
     	<%}%>
  </tr>
  <tr>
    <td><label>Dilution Rate Units:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_FeedRateUnits";%>
    	<%String dilutionRate = (String)session.getAttribute(sName);%>
    	<td>
    	<select name="<%= sName%>" id="<%= sName%>">
    			<%if (dilutionRate != null && !dilutionRate.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= dilutionRate %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!dilutionRate.equalsIgnoreCase("mL/h"))
            	{%>
              		<option>mL/h</option>
              	<%} %>
              	<%if(!dilutionRate.equalsIgnoreCase("mL/min"))
            	{%>
              		<option>mL/min</option>
              	<%} %>
            </select>
          <hr />
        </td>
    <%}%>
  </tr>
    
  <tr>
    <td><label>Comments:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Comments";%>
    	<%String comments = (String)session.getAttribute(sName);%>
    	<td>
            <textarea name="<%= sName%>" id="<%= sName%>" cols="30" rows="4"><%= comments%></textarea>
          <hr />
        </td>
    <%}%>
  </tr>
   <%}%>
   <%if(sBatchType.equalsIgnoreCase("Fed-Batch"))
   {%>
    	<tr>
        <td><label>Feed Medium:</label>&nbsp;</td>
        <%for(int i = 1; i <= iNumReactors; i++)
        {%>
        	<%String sName = "col" + i + "_FeedMedium";%>
    	<%String media = (String)session.getAttribute(sName);%>
    	<td>
    	<select name="<%= sName%>" id="<%= sName%>">
    			<%if (media != null && !media.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= media %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
                  <%for(int j = 0; j < numMedia; j++)
				  {
				  	if(alMedium.get(j).toString().equalsIgnoreCase(media))
				  	{
				  		continue;
				  	}
				  	else
				  	{%>
                  		<option><%out.print(alMedium.get(j));%></option>
                  <%}%>
                <%}%>
            </select>
        </td>
   <%}%>
  </tr>
  <tr>
        <td><label>Initial Feed Rate:</label>&nbsp;</td>
        <%for(int i = 1; i <= iNumReactors; i++)
        {%>
        	<%String sName = "col" + i + "_FeedRate";%>
            <%String flowRate = (String)session.getAttribute(sName);%>
    		<td>
             	<input value="<%= flowRate %>" type="text" name="<%= sName%>" id="<%= sName%>" />
       		</td>
     	<%}%>
  </tr>
  <tr>
    <td><label>Initial Feed Rate Units:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_FeedRateUnits";%>
    	<%String feedRate = (String)session.getAttribute(sName);%>
    	<td>
            <select name="<%= sName%>" id="<%= sName%>">
            	<%if (feedRate != null && !feedRate.equalsIgnoreCase("") )
    			{%><option selected="selected"><%= feedRate %></option>
    			<%}else {%>
            	<option selected="selected">(Select)</option>
            	<%} %>
            	<%if(!feedRate.equalsIgnoreCase("mL/h"))
            	{%>
              		<option>mL/h</option>
              	<%} %>
              	<%if(!feedRate.equalsIgnoreCase("mL/min"))
            	{%>
              		<option>mL/min</option>
              	<%} %>
            </select>
          <hr />
        </td>
    <%}%>
  </tr>
  <tr>
    <td><label>Comments:</label>&nbsp;</td>
    <%for(int i = 1; i <= iNumReactors; i++)
    {%>
    	<%String sName = "col" + i + "_Comments";%>
    	<%String comments = (String)session.getAttribute(sName);%>
    	<td>
            <textarea name="<%= sName%>" id="<%= sName%>" cols="30" rows="4"><%= comments%></textarea>
          <hr />
        </td>
    <%}%>
  </tr>
  <%}%>
</table>
</body>
</html>