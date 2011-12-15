package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.view.RowsetView;


/**
 * SvGetProjectStatus
 *
 * Handles custom action 'Get Project Status' to run
 * a general project status report.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 7/2008
 */
public class SvGetProjectStatus extends EMREServlet
{
   protected RowsetView view = null;
   final String STATUS_TABLE = "Status";
	 /** 
   * Overridden to display a UI table of active
   * projects with drill-down links.
   * @param request
   * @param task
   * @param user
   * @param db
   */
  @Override
  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
  {
    
      super.preprocessTask(request, task, user, db);
 
      RowsetView.cleanupSessionViews(request);
      
      // show sample queueing option table
      String sql = "exec spMet_GetProjectStatusReport";
      view = getSQLRowsetView(request, sql, "Project", STATUS_TABLE, 
    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
      view.setName("Status");
      //view.setScroll(true);
      view.setScrollSize("medium");

      // todo: user can drill on a project to get a report
      //view.setWidget(1, LinxConfig.WIDGET.LINK_OPEN_IN_NEW_WINDOW); // item.item=project name
   
      view.setMessage("Showing status for active projects");
      
      RowsetView.addViewToSessionViews(request, view);

  }
  
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
		     	return handleExportRequest(request, response, "Status", "ProjectStatus_"+task.getTranId(db) +".csv");
		     	
		     }
			 return FINISH_FOR_ME;
			   
		}

}
