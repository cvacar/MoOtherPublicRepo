package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.*;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.ExtractionBulkImporter;
import com.sgsi.bean.Sample;
import com.sgsi.emre.task.EMRETask;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvRetireSample
 *
 * Overridden to support import of data
 * from a bulk file.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 7/2008
 */
public class SvRetireSample extends SvDefineProtocol
{

	/** 
	 * Overridden to display a UI table of existing
	 * protocols with drill-down links.
	 * @param request
	 * @param task
	 * @param user
	 * @param db
	 */
	@Override
	protected void preprocessTask(HttpServletRequest request, Task task,
			User user, Db db)
	{

		RowsetView.cleanupSessionViews(request);
		RowsetView.addViewToSessionViews(request, getProtocolsView(request,
				task, db));
		task.setMessage("");

	}

	/** 
	 * Handles user's selection of an existing 
	 * protocol, loading the protocol's properties 
	 * into screen widgets.
	 * @param request
	 * @param response
	 * @param task
	 * @param user
	 * @param db
	 * @return FINISH_FOR_ME
	 */
	@Override
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
		((EMRETask) task).setDb(db);
		task.setMessage("");

		RowsetView.cleanupSessionViews(request);
		RowsetView.addViewToSessionViews(request, getProtocolsView(request,
				task, db));

		// has user selected a clone from list of existing clones?
		if (request.getParameter("selCoord") != null 
				&& request.getParameter("selCoord").startsWith(PROTOCOL_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected an Protocol, so populate property fields on UI
			String selProtocol = request.getParameter("selVal");
			task.getDisplayItem(ItemType.PROTOCOL).setValue(selProtocol);
			setSelectedProtocolType(selProtocol, task, db);
			((EMRETask) task).populateDisplayItemsWithProtocolProperties(task,
					selProtocol, db);

		}

		forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		return ALL_DONE;
	}

	/**
	 * Handles custom action Import to allow user to import
	 * a bulk xls file with data for this task instead of
	 * using the screen interface.
	 * 
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return ALL_DONE
	 */
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		if (request.getAttribute("ImportButton") != null)
		{
			try
			{
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task
						.getServerItemValue(FileType.RETIREMENT_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for a bulk import file, then try again.");
				}
				importRowsFromFile(fileId, task, user, db, request,response);
			}
			catch (Exception e)
			{
				if (e.getMessage().indexOf("user-mapped") > 0)
				{
					throw new LinxUserException(
							"The selected file is in use. Please close the file, then try again.");
				}
				throw new RuntimeException(e.getMessage());
			}
			commitDb(db);
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Performs separate save for each depleted sample in import file,
	 * aborting entire tran if any of the rows
	 * generates an error.
	 * @param fileId
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		ArrayList xs = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		String colKey = "Retired Sample ID";
		ExtractionBulkImporter imp = new ExtractionBulkImporter();
		xs = imp.importXLSForBulkImport(inFile, task.getTaskName(), colKey);

		// ready for std save() processing
		task
				.setMessage("Successfully imported retired samples from bulk import file.");

		ListIterator itor = xs.listIterator();
		while (itor.hasNext())
		{
			Sample x = (Sample) itor.next();

			task.getServerItem(ItemType.SAMPLE).setValue(
					x.getProperty("Retired Sample ID"));
			task.getServerItem(DataType.COMMENT).setValue(
					x.getProperty("Comment"));
			// call std processing
			save(task, user, db, request, response);
		}// next x
		// at exit, have saved once for each x in import file

	}

}
