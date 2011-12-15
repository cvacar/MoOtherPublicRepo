package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.view.RowsetView;

public class SvReportGetLIMSSamples extends EMREServlet 
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
	     RowsetView.cleanupSessionViews(request);
		 populateView(request, db);
	     if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
	     {
	     		
	     	handleGoToRowRequest(request, response);
	     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
	     	return(ALL_DONE);
	     }
	     
	     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
	     {
	    	 RowsetView.addViewToSessionViews(request, view);
	    	 String sTable = request.getParameter(Strings.WIDGET.TABLE.TABLE);
	    	 return handleExportRequest(request, response, sTable, "LIMSSamples_"+task.getTranId(db) +".csv");
	     }
		 return FINISH_FOR_ME;
		   
	}
	
	protected void populateView(HttpServletRequest request, Db db)
	{
	    String sql = "exec spMet_GetSamples";
	    view = this.getSQLRowsetView(request, sql, "Sample", DATA_TABLE, 
	    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	    view.setStartRow(1); 
	    view.setMessage("Showing existing samples");
	    
  		RowsetView.addViewToSessionViews(request, view);

	 }
}
