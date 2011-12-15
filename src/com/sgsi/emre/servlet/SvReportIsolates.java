package com.sgsi.emre.servlet;

import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvReportIsolates extends EMREServlet 
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
	     	return handleExportRequest(request, response, DATA_TABLE, "IsolateData_"+task.getTranId(db) +".csv");
	     	
	     }
		 return FINISH_FOR_ME;
	}
	
	/**
	 * Handles custom action 'Run Query' by collecting optional values 
	 * from screen UI widgets and calling stored procedure spEMRE_reportIsolates.
	 */
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if(dbHelper == null)
		{
			dbHelper = new EMREDbHelper();
		}
	
		if (request.getParameter("Run") != null)
		{
			
			String sql = "exec spEMRE_reportIsolates ";
			String params = getQueryParams(task); // may be blank (not null)
			sql = sql + params;
			view = this.getSQLRowsetView(request, response, task, sql, "LIMS ID", DATA_TABLE,
					Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
			view.setName(DATA_TABLE);
			view.setScrollSize("medium");
			// display IDs in message in case of error, bec original entries are wiped
			task.setMessage("Showing isolates filtered by query.");
			task.getDisplayItem(DATA_TABLE).setVisible(true);
			// view.setStartRow(1);
			RowsetView.addViewToSessionViews(request, view);

			return FINISH_FOR_ME;
		}
		else
		{
			return super.handleCustomAction(task, user, db, request, response);
		}
	}

	/**
	 * Collects non-blank UI widget values into a param list for use by caller to
	 * generate a report.
	 * 
	 * @param task
	 * @return params
	 */
	protected String getQueryParams(Task task)
	{
		String params = "";

		// -- keep stored proc param list sync'd with display itemType="xx"
		List ditems = task.getDisplayItems();
		ListIterator ditor = ditems.listIterator();
		while (ditor.hasNext())
		{
			DisplayItem ditem = (DisplayItem) ditor.next();
			if(ditem.getWidget().equalsIgnoreCase(Strings.WIDGET.BUTTON)
					|| ditem.getWidget().equalsIgnoreCase(Strings.WIDGET.ROWSET_VIEWS))
				continue; // skip 'Run Query'

			String itemType = ditem.getItemType();
			if(itemType.equalsIgnoreCase("Results"))
				continue;
			String value = task.getDisplayItemValue(itemType);
			if(WtUtils.isNullOrBlankOrPlaceholder(value))
			{
				continue;
			}
			if(params.length() > 0) params = params + Strings.CHAR.COMMA;
			params = params + "@" + itemType + " = '" + value + "'";

		}// next display item
		// at exit, have added non-blank widget values as params
		return params;

	}
}
