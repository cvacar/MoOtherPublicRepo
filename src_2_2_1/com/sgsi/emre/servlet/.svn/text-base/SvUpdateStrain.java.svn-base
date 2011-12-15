package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.StrainPOIBulkImporter;
import com.sgsi.emre.task.StrainCollection;
import com.sgsi.emre.task.UpdateStrain;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.Message;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvUpdateStrain
 * 
 * Overridden to handle custom actions 'Show Strain Data', 'Import', 'Export',
 * and 'Print Label'. Handles GET request if user clicks on a link in the table
 * of strain data files.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2008
 * @modified 4/2011 TJS/Wt -- switched parsers
 * @modified 7/2011 TJS/Wt -- added fields for strain tolerances and data files
 *           -- refactored screen & bulk import to use bulk insert as in
 *           isolation tasks
 */
public class SvUpdateStrain extends SvStrainCollection
{

	/**
	 * Overridden to handle 'Import'; parent class handles all other custom
	 * actions.
	 * 
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return ALL_DONE
	 */
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		try
		{
			if (request.getAttribute("ImportButton") != null)
			{
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.STRAIN_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException("Please browse for a bulk import file, then try again.");
				}
				task.getServerItem(ItemType.STRAIN).clearValues();
				save(task, user, db, request, response);
				commitDb(db);
				return FINISH_FOR_ME;
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch (Exception ex)
		{
			// preserve data shown in UI (if any)
			locationsView.setStartRow(1);
			if (dataFilesView != null)
			{
				dataFilesView.setStartRow(1);
			}
			if (ex instanceof LinxUserException)
			{
				throw (LinxUserException) ex;
			}
			else if (ex instanceof LinxDbException)
			{
				throw (LinxDbException) ex;
			}
			throw new LinxUserException(ex.getMessage());
		}
	}

	/**
	 * Overridden to populate locations view.
	 * 
	 * @param request
	 * @param task
	 * @param user
	 * @param db
	 */
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	{
		task.setMessage("");

		// display a clean location rowset UI table
		// -- data file rowset is hidden until a strain is selected
		String strain = task.getServerItemValue(ItemType.STRAIN);
		if (WtUtils.isNullOrBlank(strain))
		{
			setDisplayItemValues(null, (StrainCollection) task, user, request, db);
		}
		else
		// if(!db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
		{
			// in case of error,
			this.initLocationsView(db);
			setupLocationsView(locationsView, COL_PRINT, COL_LOC, this.getLocationRowCount());
		}
		// else
		// {
		// after an update, show the new location(s)
		// findStrainLocations(strain, request, task, db);
		// }
	}

	/**
	 * Handles GET request for a specific strain data file when user clicks on
	 * it in the UI rowset table.
	 * 
	 * @param request
	 * @param response
	 * @param task
	 * @param user
	 * @param db
	 * @return
	 */
	protected boolean doTaskWorkOnGet(HttpServletRequest request, HttpServletResponse response, Task task, User user,
			Db db)
	{
		task.populateSQLValues(user, "(Select)", db);
		if (request.getParameter("strain") != null)
		{
			// we have a request from the strain query page
			String strain = request.getParameter("strain");
			if (db.getHelper().isItemOnQueue(strain, ItemType.STRAIN, "Discarded Strains", db))
			{
				setDisplayItemValues(null, (StrainCollection) task, user, request, db);
				throw new LinxUserException("Strain " + strain + " has been discarded and cannot be queried."
						+ " Please enter a new strain, then try again.");
			}
			setDisplayItemValues(strain, (StrainCollection) task, user, request, db);
		}
		else if (WtUtils.isNullOrBlank(task.getServerItemValue(ItemType.STRAIN)))
		{
			// no action unless a strain is specified
			return FINISH_FOR_ME;
		}
		else if (request.getParameter(ACTION.EXPORT) != null)
		{
			// user wants the list of saved files (not an individual file)
			String strain = task.getServerItemValue(ItemType.STRAIN);
			this.exportAllTablesOnScreen(request, response, strain + " data files.txt");
		}
		else
		{
			// user has selected an individual data file for download
			String fileId = (String) request.getParameter("selVal");
			if (fileId != null)
			{
				File file = this.getFile(fileId, db);
				String filename = file.getName();
				this.returnDownloadAsByteStream(response, file, filename, AS_EXCEL_FILE, false);
				return ALL_DONE;
			}
			dataFilesView.setStartRow(1);
			Message msg = new Message("Could not find a file under File ID " + fileId
					+ " [may have been moved offline.] " + Strings.MSG.ALERT_LIMS_ADMIN);
			msg.setStatusCode(Strings.LINX_STATUS.ERROR);
			task.setMessage(msg);
		}
		return FINISH_FOR_ME;
	}

	/**
	 * Sets properties for this strain in the UI widgets, if this is an existing
	 * strain.
	 * 
	 * @param strain
	 * @param db
	 */
	protected void setDisplayItemValues(String strain, DefaultTask task, User user, HttpServletRequest request, Db db)
	{
		task.cleanupTask(request, getWorkflow());
		task.getDisplayItem(DataType.COMMENT).setValue("");

		RowsetView.cleanupSessionViews(request);
		initLocationsView(db);
		RowsetView.addViewToSessionViews(request, locationsView);
		if (WtUtils.isNullOrBlank(strain))
		{
			// we are importing a file or defining a new single strain,
			// so just clear values and exit
			task.getDisplayItem(ItemType.STRAIN).setValue("");
			return;
		}
		// in case new Project has been created
		task.populateSQLValues(user, "(Select)", db);

		// set values for the search item
		ArrayList params = new ArrayList();
		params.add(strain);

		try
		{
			// retrieves newest comments first
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db, "spEMRE_GetStrainProperties", params, true);
			if (!rs.next())
			{
				throw new LinxUserException("Could not find any strain properties for strain " + strain);
			}
			else
			{
				int colCount = rs.getMetaData().getColumnCount();
				for (int colIdx = 1; colIdx <= colCount; colIdx++)
				{
					String columnName = rs.getMetaData().getColumnName(colIdx);
					String colValue = rs.getString(colIdx);
					if (task.getDisplayItem(columnName).getWidget().startsWith("dropdown"))
					{
						if (WtUtils.isNullOrBlank(colValue))
						{
							colValue = "(Select)";
						}
						task.getDisplayItem(columnName).setSelectedValue(colValue);
					}
					else
					{
						if (WtUtils.isNullOrBlank(colValue))
						{
							colValue = " ";
						}
						task.getDisplayItem(columnName).setValue(colValue);
					}
				}
			}// expecting only one row, but updated comments can
			// complicate things
			rs.close();
			rs = null;

			// STRAIN TOLERANCES
			// to change which are collected:
			// - update task def display items
			// - update stored procedure spEMRE_getStrainTolerances
			// - match tolerance LABEL (not name) to returned column names
			ArrayList<DisplayItem> ditems = (ArrayList) task.getDisplayItems();
			rs = db.getHelper().getResultSetFromStoredProc(db, "spEMRE_GetStrainTolerances", params, true);
			while (rs.next())
			{
				String tolName = rs.getString(1);
				String tolValue = rs.getString(2);
				ListIterator ditor = ditems.listIterator();
				DisplayItem ditem = null;
				while (ditor.hasNext())
				{
					ditem = (DisplayItem) ditor.next();
					if (ditem.getLabel().equalsIgnoreCase(tolName))
						break;
				}// next display item object
				if (ditem == null)
				{
					continue; // no such displayItem -- task def is out of sync
								// w/sp
				}
				if (ditem.getWidget().startsWith("dropdown"))
				{
					if (WtUtils.isNullOrBlank(tolValue))
					{
						tolValue = "(Select)";
					}
					ditem.setSelectedValue(tolValue);
				}
				else
				{
					if (WtUtils.isNullOrBlank(tolValue))
					{
						tolValue = " ";
					}
					ditem.setValue(tolValue);
				}
			}// next strain tolerance
			rs.close();
			rs = null;
			// at exit, have set UI properties for this strain, if known
		}
		catch (SQLException e)
		{
			throw new LinxSystemException("While retrieving strain properties: " + e.getMessage());
		}

		// populate a rowset with strain locations
		populateLocationsView(strain, request, task, db);

		// populate a rowset table with any existing data file paths
		dataFilesView = populateFilesView(strain, request, db);
		if (dataFilesView.getRowcount() < 1)
		{
			task.getDisplayItem(FILE_TABLE).setVisible(false);
			task.setMessage("No data files have been imported for strain " + strain);
		}
		else
		{
			task.getDisplayItem(FILE_TABLE).setVisible(true);
			RowsetView.addViewToSessionViews(request, dataFilesView);
		}
		task.setMessage("Showing data for strain " + strain);

	}

	/**
	 * Uses reserved locations to populate the UI table of Locations for user to
	 * edit, or save and print.
	 * 
	 * @param request
	 * @param locations
	 * @param db
	 * @return
	 */
	protected void populateLocationsView(String strain, HttpServletRequest request, Task task, Db db)
	{
		// show UI table of locations for selected strain
		int MAX_ROWS = 9;
		RowsetView.cleanupSessionViews(request);

		ArrayList<String> alLocations = null;
		if (strain != null)
		{
			String sql = "exec spMet_GetStrainLocationsChecked '" + strain + "'";
			locationsView = getSQLRowsetView(request, sql, "Print", LOCATION_TABLE, MAX_ROWS, db);
			if (locationsView.getRowcount() < 2)
			{
				alLocations = ((StrainCollection) task).reserveFreezerLocations(strain, db);
				locationsView = populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE,
						db);
				locationsView.setMessage("Showing next available freezer locations");
			}
			else
			{
				locationsView = setupLocationsView(locationsView, COL_PRINT, COL_LOC, getLocationRowCount());
				locationsView.setMessage("Showing freezer locations for strain " + strain);
			}
		}
		else
		// strain is null
		{
			initLocationsView(db);
		}

		RowsetView.addViewToSessionViews(request, locationsView);

	}

	/**
	 * Number of rows to show in the Location UI table on initial display, for
	 * defining new strain.
	 * 
	 * @return 4 for Brown Lab
	 */
	protected int getLocationRowCount()
	{
		return 4;
	}

}
