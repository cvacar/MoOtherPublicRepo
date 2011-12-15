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

public class SvReportStrainFACS extends EMREServlet 
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
	     	return handleExportRequest(request, response, DATA_TABLE, "FACSData_"+task.getTranId(db) +".csv");
	     	
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
	    		String sql = "exec spMet_reportStrainFACS '";
	    		sql += task.getDisplayItemValue(ItemType.SECONDARY_ENRICHMENT) + "','";
	    		sql += task.getDisplayItemValue(ItemType.PRIMARY_ENRICHMENT) + "','";
	    		sql += task.getDisplayItemValue("NotebookRef") + "','";
	    		sql += task.getDisplayItemValue("VesselType") + "','";
	    		sql += task.getDisplayItemValue("DateArchived") + "','";
	    		String arcMeth = task.getDisplayItemValue("ArchiveMethod");
	    		if(WtUtils.isNullOrBlankOrPlaceholder(arcMeth))
	    			arcMeth = "";
	    		sql += arcMeth + "','";
	    		sql += task.getDisplayItemValue("Location") + "','";
	    		sql += task.getDisplayItemValue("Comment") + "'";
	    		
		      System.out.println(sql+Strings.CHAR.NEWLINE);
		      // show UI table with culture results
		      view = getSQLRowsetView(request, sql, "LIMS ID", DATA_TABLE, 
		    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		      view.setName(DATA_TABLE);
		      view.setScrollSize("medium");   
		      // display IDs in message in case of error, bec original entries are wiped 
		      task.setMessage("Showing strains filtered by query.");
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
