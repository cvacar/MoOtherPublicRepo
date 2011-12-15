package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvReportPrimaryEnrichment extends EMREServlet 
{
	protected RowsetView view = null;
	final String DATA_TABLE = "Results";

	
	/**
	 * Handles the task GET requests ('goToRow' and 'export' currently supported)
	 * @param request The current request
	 * @param response The current response
	 * @param task The selected task
	 * @param user The logged in user
	 * @param db The db connection
	 */
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
	     request.getSession().setAttribute(Strings.TASK.TASK_PAGE, task.getTaskPg(getServletContext()));
	    	
	     if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
	     {
	     	handleGoToRowRequest(request, response);
	     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
	     	return(ALL_DONE);
	     }
	     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
	     {
	     	return handleExportRequest(request, response, DATA_TABLE, "EnrichmentData_"+task.getTranId(db) +".csv");
	     	
	     }
		 return FINISH_FOR_ME;
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
	
	    if(request.getParameter("Run")!= null)
	    {
	    	try
	    	{
	    		//build the reporting table
	    		String sql = "exec spEMRE_reportPrimaryEnrichment '";
	    		String enrichment = task.getDisplayItemValue(ItemType.PRIMARY_ENRICHMENT);
	    		if(WtUtils.isNullOrBlankOrPlaceholder(enrichment))
	    			enrichment = "";
	    		sql +=  enrichment + "','";
	    		String sample = task.getDisplayItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
	    		if(WtUtils.isNullOrBlankOrPlaceholder(sample))
	    			sample = "";
	    		sql +=  sample + "','";
	    		String notebookPage = task.getDisplayItemValue("NotebookRef");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(notebookPage))
	    			notebookPage = "";
	    		sql +=  notebookPage + "','";
	    		String date = convertDate(task.getDisplayItemValue("DateStarted"));
	    		if(WtUtils.isNullOrBlankOrPlaceholder(date))
	    			date = "";
	    		sql +=  date + "','";
	    		String vesselType = task.getDisplayItemValue("VesselType");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(vesselType))
	    			vesselType = "";
	    		sql +=  vesselType + "','";
	    		String temp = task.getDisplayItemValue("GrowthTemperature");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(temp))
	    			temp = "";
	    		sql +=  temp + "','";
	    		String medium = task.getDisplayItemValue("GrowthMedium");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(medium))
	    			medium = "";
	    		sql +=  medium + "','";
	    		String irradiance = task.getDisplayItemValue("GrowthIrradiance");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(irradiance))
	    			irradiance = "";
	    		sql +=  irradiance + "','";
	    		String internalId = task.getDisplayItemValue("InternalID");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(internalId))
	    			internalId = "";
	    		sql +=  internalId + "','";
	    		String enrichmentResult = task.getDisplayItemValue("EnrichmentResult");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(enrichmentResult))
	    			enrichmentResult = "";
	    		sql +=  enrichmentResult + "','";
	    		String comment = task.getDisplayItemValue("Comment");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(comment))
	    			comment = "";
	    		sql +=  comment + "'";
		      System.out.println(sql+Strings.CHAR.NEWLINE);
		      // show UI table with culture results
		      view = getSQLRowsetView(request, sql, "LIMS ID", DATA_TABLE, 
		    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		      view.setName(DATA_TABLE);
		      view.setScrollSize("medium");   
		      // display IDs in message in case of error, bec original entries are wiped 
		      task.setMessage("Showing primary enrichments filtered by query.");
		      task.getDisplayItem(DATA_TABLE).setVisible(true);
		      //view.setStartRow(1);
		      RowsetView.addViewToSessionViews(request, view);
	    	}
	    	catch(Exception ex)
	    	{
	    		throw new LinxDbException(ex.getMessage());
	    	}
	  		
	     
	      return FINISH_FOR_ME;
	    }
	    else
	    {
	    	return super.handleCustomAction(task, user, db, request, response);
	    }
	  }
}
