package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;

import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvEditAChemRequest.java
 * 
 * Overridden to show a UI table of current requests
 * and allow user to drill or search for the most 
 * up-to-date version of a request worksheet. Used
 * mostly by submitter to edit submission details before
 * A-Chem runs analyses. Requested by Judit Bartalis 10/7/2008.
 * 
 * @author TJS/Wildtype for SGI
 * @date 10/2008
 */
public class SvEditAChemRequest extends EMREServlet 
{
	private String REQUEST_TABLE = "Requests";
	private String requestId = null;


	
	public SvEditAChemRequest() 
	{
		requestId = null;
	}
	/**
	 * Displays a UI table of AChemRequest items so user can drill to
	 * retrieve the worksheet if desired.
	 * @param request
	 * @param task
	 * @param user
	 * @param db
	 */
	@Override
	  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	  {
	    task.setMessage("");
	    requestId = null;
	    RowsetView.cleanupSessionViews(request);     
	    RowsetView.addViewToSessionViews(request, getRequestView(request, db));
	  }

	/**
	 * Handles custom action Get File to return the A-Chem Request worksheet
	 * for a request ID typed into the on-screen textbox (vs selecting from 
	 * UI table.)
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		
	    if(request.getParameter("GetFile")!= null
	    		|| request.getAttribute("GetFile") != null)
	    {
	        try
	        {
	        	String selItem = task.getDisplayItemValue(ItemType.ACHEM_REQUEST);
	        	requestId = selItem;
		        if(WtUtils.isNullOrBlankOrPlaceholder(selItem))
		        {
		            throw new LinxUserException("Please enter a Request ID, then try again.");
		        }
	        	return getFile(selItem, task, response, db);
	        }
	        catch(Exception ex)
	        {
	        	throw new LinxUserException(ex.getMessage());
	        }
	    }
	    else 
	    {

	    	try
	        {
	        	String selItem = task.getDisplayItemValue(ItemType.ACHEM_REQUEST);
	        	requestId = selItem;
		        if(WtUtils.isNullOrBlankOrPlaceholder(selItem))
		        {
		            throw new LinxUserException("Please enter a Request ID, then try again.");
		        }
	        	return getFile(selItem, task, response, db);
	        }
	        catch(Exception ex)
	        {
	        	throw new LinxUserException(ex.getMessage());
	        }
	    }

	  }
	
	/**
	 * Overridden to return the current worksheet for an AChemRequest 
	 * selected by user in the UI table.
	 * @param request
	 * @param response
	 * @param task
	 * @param user
	 * @param db
	 * @return ALL_DONE
	 */
	@Override
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
		((EMRETask) task).setDb(db);
		task.setMessage("");

		RowsetView.cleanupSessionViews(request);
		RowsetView.addViewToSessionViews(request, getRequestView(request, db));

		// has user selected a clone from list of existing clones?
		if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(REQUEST_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected a request, so get the file back
			try
			{
				requestId = request.getParameter("selVal");
				task.getServerItem(ItemType.ACHEM_REQUEST).setValue(requestId);
				getFile(requestId, task, response, db);
				return ALL_DONE;

			}
			catch(Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}

		}
		else if(request.getParameter(ACTION.EXPORT) != null)
		  {
			  // user wants the list of requests
			  //this.exportAllTablesOnScreen(request, response, "Active A-Chem Requests.txt");
			  this.writeToExcel(request, response, "spMet_getPendingAChemRequests", db);
			  return ALL_DONE;
		  }

		task.setMessage(" ");
		forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		return ALL_DONE;
	}
		  
	
	protected RowsetView getRequestView(HttpServletRequest request, Db db)
	  {
	    // show UI table of pending requests
	    String sql = "exec spMet_getPendingAChemRequests";
	    RowsetView view = this.getSQLRowsetView(request, sql, "Request_ID", REQUEST_TABLE, 
	    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	    view.setWidget(1,LinxConfig.WIDGET.LINK);
	    //view.setScroll(true);
	    //view.setScrollSize("small");
	    view.setStartRow(1);
	    view.setRowcount(50);
	    view.setMessage("(Optional) Download a previously submitted work request:");
	    return view;
	  }
	
	private boolean getFile(String requestId, Task task, HttpServletResponse response, Db db)
		throws Exception
	{
		try
		{
			
	        // look for path value (may not have been provided)
	        String path = dbHelper.getDbValue("exec spMet_getWorkRequestFilePath '" + requestId + "'", db);
	        if(WtUtils.isNullOrBlank(path))
	        {
	            // a fairly common case
	            throw new LinxUserException("No path to the run worksheet has been stored for A-Chem Request " + requestId);
	        }
	        // file is known
	        File file = new File(path);
	        this.returnDownloadAsByteStream(response, file, "AChemRequest_" + requestId + ".xls", "application/vnd.ms-excel", false);
	        return ALL_DONE;
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}

	

	
}
