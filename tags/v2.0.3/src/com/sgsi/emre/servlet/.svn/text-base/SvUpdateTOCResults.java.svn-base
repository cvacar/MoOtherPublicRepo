package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvUpdateTOCResults
 * 
 * Shows a UI table of current requests
 * and allows TOC user to drill or search for the most 
 * up-to-date version of a request worksheet. Used
 * by TOC users to update compound results on
 * their own experiments. 
 * 
 * @author TJS/Wildtype for SGI
 * @date 2/2011
 * @version v1.15
 */
public class SvUpdateTOCResults extends SvUpdateAChemResults 
{
	String spGet = "spEMRE_GetPendingSelfRunAChemRequestsForUpdate";

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
		String sql = "exec " + spGet + " '" + getUserObject(db, request).getUserName() + "'";
	    RowsetView.addViewToSessionViews(request, getRequestView(request, sql, db));
	  }


	/**
	 * Overridden to return the current worksheet for an AChemRequest 
	 * selected by user by clicking on request ID in the UI table.
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
		String sql = "exec " + spGet + " '" + getUserObject(db, request).getUserName() + "'";
		RowsetView.addViewToSessionViews(request, getRequestView(request, sql, db));

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
				boolean b = getFile(requestId, task, response, db);
				return b;
			}
			catch(Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}

		}
		else if(request.getParameter(ACTION.EXPORT) != null)
		  {
			  // user wants the list of requests
			  this.writeToExcel(request, response, 
					  "spEMRE_getPendingSelfRunAChemRequests '" 
					  + user.getName() + "'", db);
			  return ALL_DONE;
		  }

		task.setMessage(" ");
		forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		return ALL_DONE;
	}
	
	
}
