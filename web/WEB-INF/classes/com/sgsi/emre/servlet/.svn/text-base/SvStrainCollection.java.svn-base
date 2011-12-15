package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.StrainBulkImporter;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.CreateStrain;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.PhotoHostStrainCollection;
import com.sgsi.emre.task.PhotoStrainCollection;
import com.sgsi.emre.task.StrainCollection;

import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.Message;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtTaskUtils;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvStrainCollection
 * 
 * Overridden to handle custom actions to retrieve past uploaded data or to
 * print a label for a saved strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2008
 */
public class SvStrainCollection extends EMREServlet// extends SvSampleReport
{
	protected String FILE_TABLE = "Rowsets";
	protected RowsetView dataFilesView = null;
	protected String LOCATION_TABLE = "Locations";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;

	public static final String[] reqColumnHeaders = new String[] { "Strain ID", "Strain Name", "Project",
			"Page Ref [notebook-pg]", "Location 1 [Freezer:Box:Position]", "Location 2 [Freezer:Box:Position]",
			"Location 3 [Freezer:Box:Position]", "Comment" };

	/**
	 * Handles custom actions Import, Print Label, Find Strain Data and Next ID
	 * for subclasses SvCreateStrain and SvUpdateStrain.
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

		if (request.getAttribute("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
			writeToExcel(request, response, getStrainReportSQL(), db);

			return ALL_DONE;
		}
		else if (request.getAttribute("PrintLabel") != null)
		{
			// lets print the current locations in the rowset
			String strain = task.getServerItemValue(ItemType.STRAIN);
			if (WtUtils.isNullOrBlankOrPlaceholder(strain))
			{
				throw new LinxUserException("Please enter a value for Strain ID.");
			}
			((StrainCollection) task).printLabels(strain, request, db);
			// set this strain's values in UI widgets
			setDisplayItemValues(strain, (StrainCollection) task, user, request, db);
			return FINISH_FOR_ME;
		}
		else if (request.getAttribute("FindButton") != null)
		{
			findStrainDataByStrain(request, (StrainCollection) task, user, db);
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Overridden to requests from XML client, usually Bioinformatics' Strain
	 * Properties Database app.
	 * 
	 * @param domData
	 * @param strAction
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected void handleCustomAction(Element domData, String strAction, Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{

		// xml client is asking if item (strain) is known to LIMS db
		if (strAction.equalsIgnoreCase("verify strain exists"))
		{
			String strain = task.getServerItem(ItemType.STRAIN).getValue();
			if (WtUtils.isNullOrBlank(strain))
			{
				throw new LinxUserException("Please provide a Strain name, then try again.");
			}
			if (db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
			{
				task.getMessage().setStatusCode("200");
				task.setMessage("Verified");
				return;
			}
		}
		super.handleCustomAction(domData, strAction, task, user, db, request, response);
	}

	/**
	 * NOT IN USE protected void importRowsFromFile(String fileId, Task task,
	 * User user, Db db, HttpServletRequest request, HttpServletResponse
	 * response) { try { XLSParser fileData = null; // import file // core has
	 * already validated file per task def File inFile = this.getFile(fileId,
	 * db);
	 * 
	 * // create a list of objects while importing manifest // lets get the data
	 * from the file and put it into a data container object char delim =
	 * EMREStrings.CHAR.COMMA;// the data delimiter for the data // container
	 * String columnKey = "Strain ID"; // the unique identifier that lets me
	 * know // i've reach the column data in the file fileData = new
	 * XLSParser(inFile, "Strain", delim, columnKey, reqColumnHeaders, true);
	 * 
	 * // ready for std save() processing task
	 * .setMessage("Successfully imported new strains from bulk import file.");
	 * 
	 * // loop though the file and insert each line String err = ""; int row =
	 * 1; try { if (fileData.gotoFirst()) { do { String limsId =
	 * fileData.getRequiredProperty("Strain ID"); String strainName =
	 * fileData.getRequiredProperty("Strain Name"); String project =
	 * fileData.getRequiredProperty("Project"); String notebook = fileData
	 * .getRequiredProperty("Page Ref [notebook-pg]"); String loc1 = fileData
	 * .getRequiredProperty("Location 1 [Freezer:Box:Position]"); String loc2 =
	 * fileData .getRequiredProperty("Location 2 [Freezer:Box:Position]");
	 * String loc3 = fileData
	 * .getRequiredProperty("Location 3 [Freezer:Box:Position]"); String comment
	 * = fileData.getProperty("Comment");
	 * 
	 * task.getServerItem(ItemType.STRAIN).setValue(limsId);
	 * task.getServerItem("Project").setValue(project);
	 * task.getServerItem("NotebookRef").setValue(notebook);
	 * task.getServerItem("StrainName").setValue(strainName);
	 * task.getServerItem("Comment").setValue(comment);
	 * 
	 * ArrayList<String> locs = new ArrayList<String>(); locs.add(loc1);
	 * locs.add(loc2); locs.add(loc3);
	 * 
	 * task.getServerItem("Location").setValues(locs);
	 * 
	 * // call std processing save(task, user, db, request, response); row++; }
	 * while (fileData.gotoNext()); }// at end we have validated all of the
	 * inputs in the file else throw new Exception("There were no rows found in the file.  Please check column headers and try again."
	 * ); } catch (Exception e) { err += "Error occurred while parsing row " +
	 * row + ": " + e.getMessage(); } if (!err.equals("")) throw new
	 * LinxUserException(err);
	 * 
	 * } catch (Exception ex) { throw new LinxUserException(ex.getMessage()); }
	 * 
	 * }
	 */

	/**
	 * Populates strain data widgets with info about the Strain ID currently
	 * entered in the Strain display item widget. Displays current freezer
	 * locations, if known. Handles several error conditions. Called on action =
	 * "Find Strain Data" and after a successful save to show the newly saved
	 * data.
	 */
	protected void findStrainDataByStrain(HttpServletRequest request, DefaultTask task, User user, Db db)
	{
		String strain = task.getServerItemValue(ItemType.STRAIN);
		if (WtUtils.isNullOrBlank(strain))
		{
			//throw new LinxUserException("Please enter a Strain ID, then try again.");
			return; // quietly, since bulk import saves too
		}
		else if (!db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
		{
			setDisplayItemValues(null, task, user, request, db);
			throw new LinxUserException("Strain " + strain + " does not exist in this LIMS database."
					+ " Please check the entry, then try again.");
		}
		else if (db.getHelper().isItemOnQueue(strain, ItemType.STRAIN, "Discarded Strains", db))
		{
			setDisplayItemValues(null, task, user, request, db);
			throw new LinxUserException("Strain " + strain + " has been discarded and cannot be queried."
					+ " Please enter a new strain, then try again.");
		}

		// set this strain's values in UI widgets
		setDisplayItemValues(strain, task, user, request, db);
		// look up the locations
		// findStrainLocations(strain, request, task, db);
	}

	/**
	 * Returns the name of stored proc to use to retrieve this type of strain.
	 * 
	 * @return name of sp to get strain report for this type of strain
	 */
	protected String getStrainReportSQL()
	{
		//return "exec spMet_GetStrainCollection";
		return "exec spEMRE_exportStrainCollection";
	}

	/**
	 * Returns a rowset view of files previously imported for the selected
	 * strain.
	 * 
	 * @param request
	 * @param db
	 * @return rowset view of files for given strain
	 */
	protected RowsetView populateFilesView(String strain, HttpServletRequest request, Db db)
	{
		// show UI table of files for selected strain
		String sql = "exec spEMRE_GetDataFilePathsForStrain '" + strain + "'";
		RowsetView view = this.getSQLRowsetView(request, sql, "File Type", FILE_TABLE, Integer
				.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		// set hotlink on File ID field
		view.setWidget(2, LinxConfig.WIDGET.LINK);
		view.setScroll(false);
		// view.setScrollSize("small");
		view.setStartRow(1);
		view.setMessage("Showing data files for Strain ID " + strain);
		return view;
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
		// subclasses must implement (because their screen UI widgets vary)
		throw new LinxSystemException("Subclass must override");
	}

	/**
	 * Show a blank UI table of freezer Locations, with each row checked for
	 * 'Print' and as many rows as required for this task's strain type (method
	 * is inherited by all strain collection tasks.) User can enter new
	 * locations, or click Find Strain to populate with known locations, or
	 * click Next ID to populate with next assumed available locations.
	 * 
	 */
	protected void initLocationsView(Db db)
	{
		// lets write a file containing the locations
		// we can use it to populate the Rowset
		int numRows = getLocationRowCount();
		ArrayList<String> alLocs = new ArrayList<String>();
		for (int i = 0; i < numRows; i++)
		{
			alLocs.add("");
		}
		File file = writeLocationsToFile(alLocs, db);
		String[] colHeaders = { "Print", "Location" };
		locationsView = this.getFileRowsetView(file, "Print", LOCATION_TABLE, null, colHeaders, numRows);
		locationsView = setupLocationsView(locationsView, COL_PRINT, COL_LOC, numRows);
		locationsView.setMessage("");
		locationsView.setStartRow(1);

		if (file.exists())
		{
			file.delete();
		}
	}

	/**
	 * Uses reserved locations to populate the UI table of Locations
	 * for user to edit, or save and print.
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
			if(locationsView.getRowcount() < getLocationRowCount())
			{
				alLocations = ((StrainCollection) task).reserveFreezerLocations(strain, db);
				locationsView = 
					populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE, db);
				locationsView.setMessage("Showing next available freezer locations");				
			}
			else
			{
				locationsView = setupLocationsView(locationsView, COL_PRINT, COL_LOC, getLocationRowCount());
				locationsView.setMessage("Showing freezer locations for strain " + strain);
			}
		}
		else //strain is null
		{
			initLocationsView(db);
		}

		RowsetView.addViewToSessionViews(request, locationsView);

	}

	/*
	 * protected void findStrainLocations(String strain, HttpServletRequest
	 * request, Task task, Db db) { // show UI table of locations for selected
	 * strain RowsetView.cleanupSessionViews(request);
	 * 
	 * String sql = "exec spMet_GetStrainLocationsChecked '" + strain + "'";
	 * locationsView = this.getSQLRowsetView(request, sql, "Print",
	 * LOCATION_TABLE, 9, db); locationsView.setWidget(COL_PRINT,
	 * LinxConfig.WIDGET.CHECKBOX); locationsView.setWidget(COL_LOC,
	 * LinxConfig.WIDGET.TEXTBOX); locationsView.setStartRow(1);
	 * locationsView.setHideNavControls(true);
	 * 
	 * // default to checking 'Print' int numRows = getLocationRowCount();
	 * locationsView.setRowcount(numRows); for (int rowIdx = 1; rowIdx <=
	 * numRows; rowIdx++) { locationsView.setIsSelected(rowIdx, COL_PRINT); }
	 * locationsView.setMessage("Showing freezer locations for strain " +
	 * strain); RowsetView.addViewToSessionViews(request, locationsView); }
	 */

	/**
	 * Number of rows to show in the Location UI table on initial display, for
	 * defining new strain.
	 * 
	 * @return 3 for Brown Lab
	 */
	protected int getLocationRowCount()
	{
		throw new LinxUserException("Subclass must override");
	}

}
