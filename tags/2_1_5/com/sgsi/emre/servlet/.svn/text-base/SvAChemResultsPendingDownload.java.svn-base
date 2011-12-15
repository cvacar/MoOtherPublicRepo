package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvAChemResultsPendingDownload extends EMREServlet 
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
	        try
	        {
	        	String selItem = task.getDisplayItemValue("AChemRequest");
	        	requestId = selItem;
		        if(WtUtils.isNullOrBlankOrPlaceholder(selItem))
		        {
		            throw new LinxUserException("Please enter a RequestID and then try again.");
		        }
	        	return getFile(selItem, task, request, response, user, db);
	        }
	        catch(Exception ex)
	        {
	        	throw new LinxUserException(ex.getMessage());
	        }
	    }
	    return super.handleCustomAction(task, user, db, request, response);
	  }
	
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			if(WtUtils.isNullOrBlank(requestId))
			{
				requestId = task.getDisplayItemValue("AChemRequest");
		        if(WtUtils.isNullOrBlankOrPlaceholder(requestId))
		        {
		        	requestId = request.getParameter("selVal");
		        	if(WtUtils.isNullOrBlankOrPlaceholder(requestId))
			        {
		        		throw new Exception("Unable to queue the request because it is null.");
			        }
		        }
			}
			
	        task.getServerItem(EMREStrings.ItemType.ACHEM_REQUEST).setValue(requestId);
			
			super.save(task, user, db, request, response);
			boolean bIsOnQueue = dbHelper.isItemOnQueue(requestId, EMREStrings.ItemType.ACHEM_REQUEST, 
					"Completed A-Chem Request", db);
			if(bIsOnQueue)
			{
				dbHelper.dequeueItem(requestId, EMREStrings.ItemType.ACHEM_REQUEST, "Completed A-Chem Request", db);
				dbHelper.queueItem(requestId, EMREStrings.ItemType.ACHEM_REQUEST, "Downloaded A-Chem Results", task.getTaskName(), task.getTranId(), db);
			}
		}
		catch(Exception ex)
		{
			task.setMessage(ex.getMessage());
			throw new LinxUserException(ex.getMessage());
		}
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
			// user has selected a request, so get the file back
			try
			{
				requestId = request.getParameter("selVal");
				getFile(requestId, task, request, response, user, db);
				return ALL_DONE;
			}
			catch(Exception ex)
			{
				task.setMessage(ex.getMessage());
				throw new LinxUserException(ex.getMessage());
			}

		}

		task.setMessage("Successfully downloaded results.");
		forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		return ALL_DONE;
	}
	
	protected RowsetView getRequestView(HttpServletRequest request, Db db)
	  {
	    // show UI table of pending requests
	    String sql = "exec spMet_getAllRequests";
	    RowsetView view = this.getSQLRowsetView(request, sql, "RequestID", REQUEST_TABLE, 
	    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	    view.setWidget(1,LinxConfig.WIDGET.LINK);
	    //view.setScroll(true);
	    //view.setScrollSize("small");
	    view.setStartRow(1);
	    view.setMessage("");
	    return view;
	  }
	
	private boolean getFile(String requestId, Task task, HttpServletRequest request, 
			HttpServletResponse response, User user, Db db)
		throws Exception
	{
		try
		{
			
	        // look for path value (may not have been provided)
	        String path = dbHelper.getDbValue("exec spMet_getSubmissionRequestFilePath " + requestId, db);
	        if(WtUtils.isNullOrBlank(path))
	        {
	            // a fairly common case
	            throw new LinxUserException("No path to the latest version has been stored for A-Chem Request " + requestId);
	        }
	        // file is known
	        File file = new File(path);
	        this.returnDownloadAsByteStream(response, file, "AChemRequest_" + requestId + ".xls", "application/vnd.ms-excel", false);
	        if(task.getTranId() == 0)
	        	task.getTranId(db);
	        save(task,  user, db, request, response);
	        return ALL_DONE;
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
}
