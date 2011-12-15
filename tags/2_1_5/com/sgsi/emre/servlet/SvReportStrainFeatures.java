package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

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

public class SvReportStrainFeatures extends EMREServlet
{

	protected RowsetView	view					= null;
	private final String	DATA_TABLE		= "Results";
	private final int			COLUMN_STRAIN	= 1;
	private final int			COLUMN_FILE		= 14;

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
			return handleExportRequest(request, response, sTable, "GetFeatureData_"
					+ task.getTranId(db) + ".csv");
		}
		else if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(DATA_TABLE)
				&& request.getParameter("selVal") != null)
		{

			try
			{
				// ok, we've either clicked on a hyperlink for a file
				// or we're clicking on a strain and need to forward to the
				// strain features page
				StringTokenizer strTok = new StringTokenizer(request
						.getParameter("selCoord"), ".");
				String tableName = strTok.nextToken();
				int row = Integer.parseInt(strTok.nextToken()
						.substring("row_".length()));
				int col = Integer.parseInt(strTok.nextToken()
						.substring("col_".length()));

				if (col == COLUMN_STRAIN)
				{
					String strain = request.getParameter("selVal");
					// we need to redirect to the Strain Features task
					forwardToPg("/Task_Strain_Features?taskName=Strain+Features&strain="
							+ strain, request, response, "");
				}
				else
				{
					// we need to download the results file
					String f = (String) request.getParameter("selVal");
					File file = new File(f);
					this.returnDownloadAsByteStream(response, file,
							"VectorMapFileDownload.gb", "text/plain", false);
				}
				return ALL_DONE;
			}
			catch (Exception ex)
			{
				view.setStartRow(1);
				RowsetView.addViewToSessionViews(request, view);
				throw new LinxSystemException(ex.getMessage());
			}
			// return super.doTaskWorkOnGet(request, response, task, user, db);
		}
		else
			return FINISH_FOR_ME;

	}

	@Override
	/**
	 * Handles custom action "Run Query" to display UI table
	 * of strains matching provided properties. 
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
	 * Calls SQL stored proc spEMRE_reportStrainFeatures to display
	 * strains matching the non-blank properties in the task UI table.
	 * @param request
	 * @param task
	 * @param db
	 */
	protected void runQuery(HttpServletRequest request, Task task, Db db)
	{

		RowsetView.cleanupSessionViews(request);
		ArrayList<String> params = new ArrayList<String>();

		params.add(task.getDisplayItemValue("Strain"));
		params.add(task.getDisplayItemValue("OriginStrain"));
		params.add(task.getDisplayItemValue("HostSpecies"));
		params.add(task.getDisplayItemValue("GeneDeletion"));
		params.add(task.getDisplayItemValue("Vendor"));
		params.add(task.getDisplayItemValue("SpeciesOrigin"));
		params.add(task.getDisplayItemValue("GeneAnnotation"));
		params.add(task.getDisplayItemValue("Vector"));
		params.add(task.getDisplayItemValue("Promoter"));
		params.add(task.getDisplayItemValue("AffinityTag"));
		params.add(task.getDisplayItemValue("PlasmidType"));
		params.add(task.getDisplayItemValue("Plasmid"));
		params.add(task.getDisplayItemValue("PlasmidMarker"));
		params.add(task.getDisplayItemValue("Comment"));

		String sp = "spEMRE_reportStrainFeatures";
		// show UI table with culture results
		view = getSQLRowsetView(request, sp, params, "LIMSID", DATA_TABLE, Integer
				.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		view.setName(DATA_TABLE);
		view.setWidget(1, LinxConfig.WIDGET.LINK);
		view.setWidget(14, LinxConfig.WIDGET.LINK);
		task.setMessage("Showing features returned from query.");
		task.getDisplayItem(DATA_TABLE).setVisible(true);
		RowsetView.addViewToSessionViews(request, view);

	}
}
