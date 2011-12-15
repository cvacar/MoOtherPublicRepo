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

public class SvReportEnvironmentalSample extends EMREServlet 
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
	     	return handleExportRequest(request, response, DATA_TABLE, "SampleData_"+task.getTranId(db) +".csv");
	     	
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
	    		String sql = "exec spMet_reportEnvironmentalSample '";
	    		sql += task.getDisplayItemValue(ItemType.ENVIRONMENTAL_SAMPLE) + "','";
	    		String pysForm = task.getDisplayItemValue("PhysicalForm");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(pysForm))
	    			pysForm = "";
	    		sql += pysForm + "','";
	    		sql += task.getDisplayItemValue("CollectionDate") + "','";
	    		sql += task.getDisplayItemValue("NotebookRef") + "','";
	    		sql += task.getDisplayItemValue("InternalID") + "','";
	    		sql += task.getDisplayItemValue("FieldName") + "','";
	    		sql += task.getDisplayItemValue("Description") + "','";
	    		sql += task.getDisplayItemValue("Taxonomy") + "','";
	    		sql += task.getDisplayItemValue("Tissue") + "','";
	    		sql += task.getDisplayItemValue("Volume_liters") + "','";
	    		sql += task.getDisplayItemValue("Weight_grams") + "','";
	    		sql += task.getDisplayItemValue("InSituTemp_C") + "','";
	    		sql += task.getDisplayItemValue("Temperature_C") + "','";
	    		sql += task.getDisplayItemValue("pH") + "','";
	    		sql += task.getDisplayItemValue("DissolvedOxygen_mg_per_L") + "','";
	    		sql += task.getDisplayItemValue("Conductivity_mS_per_cm") + "','";
	    		sql += task.getDisplayItemValue("SampleDepth_m") + "','";
	    		sql += task.getDisplayItemValue("Salinity_ppt") + "','";
	    		sql += task.getDisplayItemValue("Cultivar") + "','";
	    		sql += task.getDisplayItemValue("Age") + "','";
	    		sql += task.getDisplayItemValue("Latitude") + "','";
	    		sql += task.getDisplayItemValue("Longitude") + "','";
	    		sql += task.getDisplayItemValue("Altitude_m") + "','";
	    		sql += task.getDisplayItemValue("SiteDescription") + "','";
	    		String storage = task.getDisplayItemValue("StorageMethod");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(storage))
	    			storage = "";
	    		sql +=  storage + "','";
	    		sql += task.getDisplayItemValue("ClosestTown") + "','";
	    		sql += task.getDisplayItemValue("City") + "','";
	    		sql += task.getDisplayItemValue("County") + "','";
	    		sql += task.getDisplayItemValue("State") + "','";
	    		sql += task.getDisplayItemValue("Country") + "','";
	    		sql += task.getDisplayItemValue("ArchiveLocation") + "','";
	    		sql += task.getDisplayItemValue("Location") + "','";
	    		sql += task.getDisplayItemValue("Comment") + "'";
	    		
		      System.out.println(sql+Strings.CHAR.NEWLINE);
		      // show UI table with culture results
		      view = getSQLRowsetView(request, sql, "LIMS ID", DATA_TABLE, 
		    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		      view.setName(DATA_TABLE);
		      view.setScrollSize("medium");   
		      // display IDs in message in case of error, bec original entries are wiped 
		      task.setMessage("Showing samples filtered by query.");
		      task.getDisplayItem(DATA_TABLE).setVisible(true);
		      view.setStartRow(1);
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
