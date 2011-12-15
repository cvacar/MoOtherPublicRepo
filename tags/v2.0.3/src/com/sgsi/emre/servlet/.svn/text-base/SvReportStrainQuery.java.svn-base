package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvReportStrainQuery extends EMREServlet 
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
	     //RowsetView.cleanupSessionViews(request);
	     if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
	     {
	     		
	     	handleGoToRowRequest(request, response);
	     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
	     	return(ALL_DONE);
	     }
	     
	     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
	     {
	    	 //RowsetView.addViewToSessionViews(request, view);
	    	 String sTable = request.getParameter(Strings.WIDGET.TABLE.TABLE);
	    	 return handleExportRequest(request, response, sTable, "GetAssayData_"+task.getTranId(db) +".csv");
	     }
	     else if (request.getParameter("selCoord") != null
					&& request.getParameter("selCoord").startsWith(DATA_TABLE)
					&& request.getParameter("selVal") != null)
		 {
			// user has selected a strain - forward to appropriate strain page
			try
			{
				String strain = request.getParameter("selVal");
				if(strain.toLowerCase().startsWith("pe"))
				{
					//we need to redirect to the Photo Ecoli Strain Collection task
					forwardToPg("/Task_Photo_E_coli_Strain_Collection?taskName=Photo+E+coli+Strain+Collection&strain=" + strain, request, response, "");
				}
				else if (strain.toLowerCase().startsWith("ph"))
				{
					//we need to redirect to the Photo Host Strain Collection task
					forwardToPg("/Task_Photo_Host_Strain_Collection?taskName=Photo+Host+Strain+Collection&strain=" + strain, request, response, "");
				}
				else if (strain.toLowerCase().startsWith("wt") || strain.toLowerCase().startsWith("sb"))
				{
					forwardToPg("/Task_Strain_Collection?taskName=Wildtype+Strain+Collection&strain=" + strain, request, response, "");
				}
				else//we don't recognize the strain
				{
					task.setMessage("Unable to forward to the Strain Collection page.  Valid strains are 'PH','PE', and 'SB'.");
					return FINISH_FOR_ME;
				}
				
				return ALL_DONE;
			}
			catch(Exception ex)
			{
					throw new LinxSystemException(ex.getMessage());
			}
			//return super.doTaskWorkOnGet(request, response, task, user, db);
		 }
	     else
	    	 return FINISH_FOR_ME;
		   
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {

	    if(request.getParameter("Run")!= null)
	    {
	    	try
	    	{
	    		RowsetView.cleanupSessionViews(request);
	        	String sql = "exec spMet_reportStrainQuery '";
	        	String strain = task.getDisplayItemValue("Strain"); 
	            String strainName  = task.getDisplayItemValue("StrainName"); 
	            String project  = task.getDisplayItemValue("Project"); 
	            String notebook  = task.getDisplayItemValue("NotebookRef"); 
	            String location  = task.getDisplayItemValue("Location"); 
	            String comment  = task.getDisplayItemValue("Comment"); 
	            
	            if(WtUtils.isNullOrBlankOrPlaceholder(strain))
	            {
	            	strain = "";
	            }
	            if(WtUtils.isNullOrBlankOrPlaceholder(strainName))
	            {
	            	strainName = "";
	            }
	            if(WtUtils.isNullOrBlankOrPlaceholder(project))
	            {
	            	project = "";
	            }
	            if(WtUtils.isNullOrBlankOrPlaceholder(notebook))
	            {
	            	notebook = "";
	            }
	            if(WtUtils.isNullOrBlankOrPlaceholder(location))
	            {
	            	location = "";
	            }
	            if(WtUtils.isNullOrBlankOrPlaceholder(comment))
	            {
	            	comment = "";
	            }
	        	sql += strain + "','";
	        	sql += strainName + "','";
	        	sql += project + "','";
	        	sql += notebook + "','";
	        	sql += location + "','";
	        	sql += comment + "'";
	    		System.out.println(sql+Strings.CHAR.NEWLINE);

	          // show UI table with culture results
	          view = getSQLRowsetView(request, sql, "Strain", DATA_TABLE, 
	        		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	          view.setName(DATA_TABLE);
	          view.setWidget(1,LinxConfig.WIDGET.LINK);
	          task.setMessage("Showing strains returned from query.");
	          task.getDisplayItem(DATA_TABLE).setVisible(true);
	          RowsetView.addViewToSessionViews(request, view);
	         
	          return FINISH_FOR_ME;
	    	}
	    	catch(Exception ex)
	    	{
	    		throw new LinxDbException(ex.getMessage());
	    	}
	    }
	    else
	    {
	    	return super.handleCustomAction(task, user, db, request, response);
	    }
	  }
}
