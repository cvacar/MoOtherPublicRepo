package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.bean.Sample;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.ExtractionBulkImporter;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvReactivateSample extends EMREServlet 
{

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
						.getServerItemValue(FileType.REACTIVATE_IMPORT_FILE);
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
		String colKey = "Reactivate Sample ID";
		ExtractionBulkImporter imp = new ExtractionBulkImporter();
		xs = imp.importXLSForBulkImport(inFile, task.getTaskName(), colKey);

		// ready for std save() processing
		task
				.setMessage("Successfully imported samples to reactivate from bulk import file.");

		ListIterator itor = xs.listIterator();
		while (itor.hasNext())
		{
			Sample x = (Sample) itor.next();

			task.getServerItem(ItemType.SAMPLE).setValue(
					x.getProperty("Reactivate Sample ID"));
			task.getServerItem(DataType.COMMENT).setValue(
					x.getProperty("Comment"));
			// call std processing
			save(task, user, db, request, response);
		}// next x
		// at exit, have saved once for each x in import file

	}
}
