package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.ItemType;

import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvDownloadAChemResults extends EMREServlet 
{
	private String REQUEST_TABLE = "Requests";
	private String requestId = null;

	@Override
	  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	  {
	    task.setMessage("");
	    requestId = null;
	    RowsetView.cleanupSessionViews(request);     
	    RowsetView.addViewToSessionViews(request, getRequestView(request, db));
	  }

	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {

	    if(request.getParameter("GetFile")!= null)
	    {
	    	String selItem = task.getDisplayItemValue("AChemRequest");
	    	requestId = selItem;
	        if(WtUtils.isNullOrBlankOrPlaceholder(selItem))
	        {
	            throw new LinxUserException("Please select (or enter) a Request ID, then try again.");
	        }
	    	boolean b = getFile(selItem, task, request, response, user, db);
	    	// -- save here, due to two ways to download (unusual)
	    	//per Judit - 02/2009 - don't save multiple downloads of the same file.  just show user the file.
			//save(task,  user, db, request, response);
			return b;
	    }
	    return super.handleCustomAction(task, user, db, request, response);
	  }
	

	@Override
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
		task.setMessage("");

		RowsetView.cleanupSessionViews(request);
		RowsetView.addViewToSessionViews(request, getRequestView(request, db));

		// has user selected a clone from list of existing clones?
		if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(REQUEST_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected a request, so download the file to browser/MS Excel

			requestId = request.getParameter("selVal");
			if(WtUtils.isNullOrBlankOrPlaceholder(requestId, "No rows returned"))
			{
				throw new LinxUserException("Not a valid Request ID");
			}
			task.getServerItem(ItemType.ACHEM_REQUEST).setValue(requestId);
			getFile(requestId, task, request, response, user, db);

			// -- save here too, due to two ways to download (unusual)
			task.setMessage("Successfully downloaded results for A-Chem Request " + requestId);
			try
			{
				db.getHelper().addTaskHistory(task, getWorkflow(), user, "pending", request, response, db);
				task.getTranId(db);
				save(task,  user, db, request, response);
				commitDb(db);
			}
			catch (LinxDbException e)
			{
				throw new LinxUserException(e.getMessage());
			}
			return ALL_DONE;
		}
		  else if(request.getParameter(ACTION.EXPORT) != null)
		  {
			  // user wants the list of requests
			  //this.exportAllTablesOnScreen(request, response, "Active A-Chem Requests.txt");
			  this.writeToExcel(request, response, "spMet_GetCompletedAChemRequests", db);
			  return ALL_DONE;
		  }


		forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		return ALL_DONE;
	}
	
	protected RowsetView getRequestView(HttpServletRequest request, Db db)
	  {
	    // show UI table of pending requests
	    String sql = "exec spMet_GetCompletedAChemRequests";
	    RowsetView view = this.getSQLRowsetView(request, sql, "Request_ID", REQUEST_TABLE, 
	    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	    view.setWidget(1,LinxConfig.WIDGET.LINK);
	    view.setStartRow(1);
	    view.setRowcount(50);
	    //view.setScroll(true);
	    //view.setScrollSize("large");
	    view.setMessage("Download latest results from Analytical Chemistry lab:");
	    return view;
	  }
	
	protected boolean getFile(String requestId, Task task,
			HttpServletRequest request, HttpServletResponse response,
			User user, Db db)
	{

		// look for path value (may not have been provided)
		if(!db.getHelper().isItemExisting(requestId, EMREStrings.ItemType.ACHEM_REQUEST, db))
		{
			// a not unlikely case
			throw new LinxUserException(
					"No record exists in this database for work request "	+ requestId 
					+ ". Please enter (or select) an existing Request ID, then try again.");
		}
		if(!db.getHelper().isItemOnQueue(requestId, EMREStrings.ItemType.ACHEM_REQUEST, "Updated A-Chem Results", db)
				&& !db.getHelper().isItemOnQueue(requestId, EMREStrings.ItemType.ACHEM_REQUEST, "Updated A-Chem Request", db))
		{
			throw new LinxUserException(
					"Work request "	+ requestId 
					+ " is still pending. To edit this request, run LIMS task 'Edit A-Chem Request'.");
			
		}
		String path = dbHelper.getDbValue("exec spMet_getWorkRequestFilePath '"
				+ requestId + "'", db);
		if (WtUtils.isNullOrBlank(path))
		{
			// a fairly common case
			throw new LinxUserException(
					"No path to the latest version of this request worksheet"
				  + " has been stored for A-Chem Request "	+ requestId);
		}
		String fileVersion = "v" + dbHelper.getDbValue("spEMRE_getUpdateAChemFileVersion '" + requestId + "'", db);
		if(fileVersion.equalsIgnoreCase("vnull"))
			fileVersion = "v1";
		// file is known
		File file = new File(path);
		this.returnDownloadAsByteStream(response, file, 
				"AChemRequest_"	+ requestId + "_" + fileVersion + ".xls", "application/vnd.ms-excel", false);
		return ALL_DONE;

	}
}
