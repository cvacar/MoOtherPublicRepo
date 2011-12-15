package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.UpdateSamplingTimepoints;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * Overridden to handle custom actions 'Get Timepoints' and 'Import Timepoints'.
 * 
 * @author Bobby Jo Steeke/Wildtype for SGI
 * @created 10/2010
 * @modified by TJS/Wt to support EMRE v2.1 -- save Sampling Timepoints as
 *           primary items
 * 
 */
public class SvUpdateSamplingTimepoints extends EMREServlet
{
	private String				TIMEPOINT_TABLE		= "Timepoints";
	protected String			NBR_TIMEPTS				= "NumberTimepoints";
	protected RowsetView	timepointView			= null;
	private int						COLUMN_TIMEPOINT	= 1;

	/**
	 * Overridden to handle custom actions 'Get Timepoints' and 'Import
	 * Timepoints'.
	 */
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{

		if (request.getAttribute("GetTimepoints") != null)
		{
			// show a UI table with this number of blank rows
			int numTimepoints = -1;
			try
			{
				numTimepoints = Integer.parseInt(task.getDisplayItemValue(NBR_TIMEPTS));
			}
			catch (Exception ex)
			{
				throw new LinxUserException("Please enter an integer value for '"
						+ task.getDisplayItem(NBR_TIMEPTS).getLabel() + "'.");
			}
			// nested sql calls??
			timepointView = getSQLRowsetView(request, response, task, getRowsetSQL(
					task, numTimepoints, "Timepoint"), "Timepoint", TIMEPOINT_TABLE,
					numTimepoints, db);
			timepointView = configureTimepointView(timepointView, numTimepoints);
			task.getDisplayItem("Timepoints").setVisible(true);
			RowsetView.addViewToSessionViews(request, timepointView);
			return FINISH_FOR_ME;
		}
		else if (request.getAttribute("ImportButton") != null)
		{
			// user is submitting a bulk import file of timepoints
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task
					.getServerItemValue(FileType.SAMPLING_TIMEPOINT_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			importRowsFromFile(fileId, task, user, db, request, response);

			commitDb(db);
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * loops through the rows of a file and calls save per row
	 * 
	 * @param fileId
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected void importRowsFromFile(String fileId, Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		XLSParser fileData = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		// lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;// the data delimiter for the data
		// container
		String columnKey = "Culture ID"; // the unique identifier that lets me know
		// i've reach the column data in the file
		try
		{
			fileData = new XLSParser(inFile, task.getTaskName(), delim, columnKey,
					EMREStrings.GrowthRecovery.requiredUpdateTimepointsColumnHeaders,
					true);
		}
		catch (Exception ex)
		{
			throw new LinxUserException("Error occurred during file parsing: "
					+ ex.getMessage());
		}

		// loop though the file and save core items on each line
		String err = "";
		int row = 1;

		try
		{
			if (fileData.gotoFirst())
			{
				do
				{
					String culture = fileData.getRequiredProperty("Culture ID");
					String cultureType = ItemType.STRAINCULTURE;
					if (culture.startsWith("EX"))
					{
						// we have an experimental culture
						cultureType = ItemType.EXPERIMENTALCULTURE;
					}
					// get next serial number for STP of this culture
					int sn = ((UpdateSamplingTimepoints)task).getNextSerialNumberByCulture(culture, cultureType, db);
					String stp = culture + "_" + sn;
					task.getServerItem(ItemType.SAMPLING_TIMEPT).setValue(stp);

					// get the datetime of this sampling
					String timept = fileData.getRequiredProperty("Sampling Timepoint");
					task.getServerItem("Timepoint").setValue(timept);

					// do we have an ExperimentalCulture or a StrainCulture?
					// -- relies on a naming convention EX-SGI-E-x...
					if (culture.toLowerCase().startsWith("ex"))
					{
						task.getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(culture);
						task.getServerItem(ItemType.STRAINCULTURE).clearValues();
					}
					else
					{
						task.getServerItem(ItemType.STRAINCULTURE).setValue(culture);
						task.getServerItem(ItemType.EXPERIMENTALCULTURE).clearValues();
					}

					// call std processing
					save(task, user, db, request, response);
					row++;
				} while (fileData.gotoNext());
			}// at end we have saved each row of the file

		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if (!err.equals(""))
		{
			throw new LinxUserException(err);
		}
		// ready for std save() processing
		task.setMessage("Successfully imported new timepoints from bulk import file.");

	}

	/**
	 * Overridden to extract UI table data for re-display later.
	 */
	protected void save(Task task, User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		ArrayList<String> alTp = new ArrayList<String>();
		try
		{
			// do we have a file?
			String fileId = task.getServerItemValue(FileType.SAMPLING_TIMEPOINT_IMPORT_FILE);			
			if (!WtUtils.isNullOrBlank(fileId))
			{
				super.save(task, user, db, request, response);
			}
			else
			{
				/****** SCREEN-BASED SAVE ******/
				// get the data from the timepoints table for repopulating it later
				TableDataMap rowMap = new TableDataMap(request, TIMEPOINT_TABLE);
				int numRows = rowMap.getRowcount();
				for (int rowIdx = 1; rowIdx <= numRows; rowIdx++)
				{
					String timepoint = (String) rowMap.getValue(rowIdx, COLUMN_TIMEPOINT);
					if (!WtUtils.isNullOrBlank(timepoint))
					{
						timepoint.trim();
						if (!WtUtils.isNullOrBlank(timepoint))
						{
							alTp.add(timepoint);
						}
						else
							throw new LinxUserException("Timepoints cannot be null.");
					}
				}
				// set the server-side item for std processing
				task.getServerItem("Timepoint").setValues(alTp);
				super.save(task, user, db, request, response);

			}

		}
		catch (Exception ex)
		{
			// reset the timepoints table
			try
			{
				timepointView = configureTimepointView(alTp, db, request);
			}
			catch (Exception e)
			{
				// can't repopulate the timepoints - ignore
			}
			throw new LinxUserException("Error occurred during save: "
					+ ex.getMessage());
		}
	}

	private String getRowsetSQL(Task task, int numTimepoints, String colName)
	{
		return "exec spEMRE_getSQLRowsetView " + numTimepoints + ",'" + colName
				+ "'";
	}

	protected RowsetView configureTimepointView(RowsetView view, int numRows)
	{
		view.setStartRow(1);
		view.setHideNavControls(true);
		view.setRowcount(numRows);
		view.setWidget(1, LinxConfig.WIDGET.DATETIMEPICKER);
		view.getColumn(0).setDateFormat("yyyy-mm-dd h:MM:ss TT");
		return view;
	}

	protected RowsetView configureTimepointView(ArrayList<String> alRows, Db db,
			HttpServletRequest request)
	{

		String[] colHeaders = { "Timepoint" };
		char delim = ',';
		File file = writeSingleColumnToFile(alRows, colHeaders, delim, db);

		timepointView = this.getFileRowsetView(file, "Timepoint", TIMEPOINT_TABLE,
				null, colHeaders, alRows.size());
		timepointView.setStartRow(1);
		timepointView.setHideNavControls(true);
		timepointView.setRowcount(alRows.size());
		timepointView.setWidget(1, LinxConfig.WIDGET.DATETIMEPICKER);
		timepointView.getColumn(0).setDateFormat("yyyy-mm-dd h:MM:ss TT");

		RowsetView.addViewToSessionViews(request, timepointView);
		return timepointView;
	}

}
