package com.sgsi.emre.servlet;

import java.util.ArrayList;

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
	protected RowsetView	view				= null;
	final String					DATA_TABLE	= "Results";

	/**
	 * Handles the task GET requests ('goToRow' and 'export' currently supported)
	 * 
	 * @param request
	 *          The current request
	 * @param response
	 *          The current response
	 * @param task
	 *          The selected task
	 * @param user
	 *          The logged in user
	 * @param db
	 *          The db connection
	 */
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{

		request.getSession().setAttribute(Strings.TASK.TASK_PAGE,
				task.getTaskPg(getServletContext()));
		// RowsetView.cleanupSessionViews(request);
		if (request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
		{

			handleGoToRowRequest(request, response);
			forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
			return (ALL_DONE);
		}

		else if (request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
		{
			// RowsetView.addViewToSessionViews(request, view);
			String sTable = request.getParameter(Strings.WIDGET.TABLE.TABLE);
			return handleExportRequest(request, response, sTable, "GetAssayData_"
					+ task.getTranId(db) + ".csv");
		}
		else if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(DATA_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected a strain - forward to appropriate strain page
			try
			{
				String strain = request.getParameter("selVal");
				if (strain.toLowerCase().startsWith("pe"))
				{
					// we need to redirect to the Photo Ecoli Strain Collection task
					forwardToPg(
							"/Task_Photo_E_coli_Strain_Collection?taskName=Photo+E+coli+Strain+Collection&strain="
									+ strain, request, response, "");
				}
				else if (strain.toLowerCase().startsWith("ph"))
				{
					// we need to redirect to the Photo Host Strain Collection task
					forwardToPg(
							"/Task_Photo_Host_Strain_Collection?taskName=Photo+Host+Strain+Collection&strain="
									+ strain, request, response, "");
				}
				else if (strain.toLowerCase().startsWith("wt")
						|| strain.toLowerCase().startsWith("sb"))
				{
					forwardToPg(
							"/Task_Strain_Collection?taskName=Wildtype+Strain+Collection&strain="
									+ strain, request, response, "");
				}
				else
				// we don't recognize the strain
				{
					task
							.setMessage("Unable to forward to the Strain Collection page.  Valid strains are 'PH','PE', and 'SB'.");
					return FINISH_FOR_ME;
				}

				return ALL_DONE;
			}
			catch (Exception ex)
			{
				throw new LinxSystemException(ex.getMessage());
			}
			// return super.doTaskWorkOnGet(request, response, task, user, db);
		}
		else
			return FINISH_FOR_ME;

	}

	@Override
	/**
	 * Handles custom action "Run Query" by displaying a UI table
	 * of strains matching non-blank params.
	 */
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{

		if (request.getParameter("Run") != null)
		{
			runQuery(request, task, db);
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Displays UI table of strains matching non-blank params entered by the user.
	 * Calls stored proc spEMRE_reportStrainQuery.
	 */
	protected void runQuery(HttpServletRequest request, Task task, Db db)
	{
		try
		{
			RowsetView.cleanupSessionViews(request);
			String sql = "spEMRE_reportStrainQuery";
			ArrayList<String> params = new ArrayList<String>();
			params.add(task.getDisplayItemValue("Strain"));
			params.add(task.getDisplayItemValue("StrainName"));
			params.add(task.getDisplayItemValue("Genus"));
			params.add(task.getDisplayItemValue("Species"));
			params.add(task.getDisplayItemValue("Project"));
			params.add(task.getDisplayItemValue("NotebookRef"));
			params.add(task.getDisplayItemValue("Location"));
			params.add(task.getDisplayItemValue("Comment"));

			// show UI table with culture results
			view = getSQLRowsetView(request, sql, params, "Strain", DATA_TABLE,
					Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
			view.setName(DATA_TABLE);
			view.setWidget(1, LinxConfig.WIDGET.LINK);
			task.setMessage("Showing strains returned from query.");
			task.getDisplayItem(DATA_TABLE).setVisible(true);
			RowsetView.addViewToSessionViews(request, view);

		}
		catch (Exception ex)
		{
			throw new LinxSystemException(ex.getMessage());
		}

	}
}
