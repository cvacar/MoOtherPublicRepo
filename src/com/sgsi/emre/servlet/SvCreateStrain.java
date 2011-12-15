package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

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
import com.sgsi.emre.task.CreateStrain;
import com.sgsi.emre.task.StrainCollection;
import com.sgsi.emre.task.UpdateStrain;
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
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvCreateStrain
 * 
 * Overridden to handle custom actions to retrieve past uploaded data or to
 * print a label for a saved strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2011
 * @modified 5/2011 to show
 */
public class SvCreateStrain extends EMREServlet
{

	public static final String[]	reqColumnHeaders	= new String[] { "Strain ID",
			"Strain Name", "Project", "Page Ref [notebook-pg]",
			"Location 1 [Freezer:Box:Position]", "Location 2 [Freezer:Box:Position]",
			"Location 3 [Freezer:Box:Position]", "Comment" };

	CreateStrain thisTask = new CreateStrain();
	protected String FILE_TABLE = "StrainData";
	protected RowsetView dataFilesView = null;
	protected String LOCATION_TABLE = "Locations";
	protected RowsetView locationsView = null;
	protected String IMPORTS_TABLE = "Imports";
	protected RowsetView importsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	int COL_ID = 1;

	/**
	 * Handles GET request for a specific file when user clicks on it in the UI
	 * table of data files or previously imported StrainImportFiles.
	 * 
	 * @param request
	 * @param response
	 * @param task
	 * @param user
	 * @param db
	 * @return FINISH_FOR_ME
	 */
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
		if(importsView != null)
		{
			importsView.setStartRow(1);
		}
		if (request.getParameter(ACTION.EXPORT) != null)
		{
			// user wants the list of saved files (not an individual file)
			String strain = task.getServerItemValue(ItemType.STRAIN);
			this.exportAllTablesOnScreen(request, response, strain
					+ " data files.txt");
		}
		// has user selected an import from a list of imported files?
		else if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(IMPORTS_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected an import, so get the file back
			String fileId = request.getParameter("selVal");
			return getFile(fileId, task, request, response, user, db);
		}
		else if (request.getParameter("selCoord") != null
				&& request.getParameter("selCoord").startsWith(FILE_TABLE)
				&& request.getParameter("selVal") != null)
		{
			// user has selected an individual data file for download
			String fileId = (String) request.getParameter("selVal");
			return getFile(fileId, task, request, response, user, db);
		}
		// else, showing task screen
		// populate a rowset with strain locations
		populateLocationsView(null/*no strain*/, request, task, db);

		populateImportsView(request, user, db);
		return FINISH_FOR_ME;

	}

	/**
	 * Handles custom actions Import and Next ID.
	 * 
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
		try
		{
			if (request.getAttribute("ImportButton") != null)
			{
				// clear out old values to make room for strains in import file
				setDisplayItemValues(null/* no strain */, (CreateStrain) task, user,
						request, db);
				task.createAnyNewAppFiles(request, response, user, db);

				String fileId = task.getServerItemValue(FileType.STRAIN_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for a bulk import file, then try again.");
				}
				// calls save() on every strain, for a better audit trail
				task.setMessage(" ");
				importRowsFromFile(fileId, (CreateStrain) task, user, db, request,
						response);
				commitDb(db);
				populateImportsView(request, user, db);
				task
						.setMessage("Successfully imported new strains from bulk import file.");
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("NextIDButton") != null)
			{
				setNextId(request, task, user, db);
				task.setMessage("Showing strain locations for next strain");
				importsView.setStartRow(1);
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("ExportButton") != null)
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
	 * where to put? // save clears values, so reset strain ID
	 * task.getServerItem(ItemType.STRAIN).setValue(strain);
	 * findStrainDataByStrain(request, (StrainCollection) task, user, db);
	 * task.setMessage("Successfully saved data for Strain " +
	 * task.getDisplayItemValue(ItemType.STRAIN));
	 */

	/**
	 * Overridden to use new POI-based parser instead of LIMSImporter2. Calls
	 * save() on every data row's values so that task class can perform custom
	 * table update. Eff EMRE v2.1.
	 */
	protected void importRowsFromFile(String fileId, CreateStrain task,
			User user, Db db, HttpServletRequest request, HttpServletResponse response)
	{
		boolean bIdAssigned = false;
		// open and read an xls worksheet (user's bulk import file)
		File file = this.getFile(fileId, db);
		HSSFWorkbook workbook = null;
		POIFSFileSystem fs = null;
		try
		{
			fs = new POIFSFileSystem(new FileInputStream(file));
			workbook = new HSSFWorkbook(fs);
		}
		catch (FileNotFoundException ex)
		{
			throw new LinxSystemException("While opening Excel workbook: "
					+ ex.getMessage());
		}
		catch (IOException ex)
		{
			throw new LinxSystemException("While opening Excel workbook: "
					+ ex.getMessage());
		}
		StrainPOIBulkImporter parser = new StrainPOIBulkImporter();
		int sheetCount = workbook.getNumberOfSheets();

		for(int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
		{
			HSSFSheet sheet = workbook.getSheetAt(sheetIdx);
			
			ArrayList<String> params = new ArrayList<String>();
			int locIndex = 0;
	
			String columnKey = task.getColumnKey();
	
			HSSFRow headerRow = parser.getRowWithColumnHeaders(sheet, sheet
					.getSheetName(), columnKey);
			parser.setHeaderRow(headerRow);
	
			// loop thru data rows in this worksheet
			Iterator<Row> rowItor = sheet.rowIterator();
			HSSFRow dataRow = null;
			while (rowItor.hasNext())
			{
				locIndex = 0;
				params.clear();
				dataRow = (HSSFRow) rowItor.next();
				if (dataRow.getRowNum() < parser.getHeaderRow().getRowNum() + 1)
				{
					continue; // not at data rows yet
				}
	
				try
				{
					// by here, we are in data rows
					// -- fields required to define a strain
					String strain = parser.getCellValueByColName(dataRow, columnKey);
					// check strain ID against strain type eff v2.1.7
					String strainType = parser
							.getCellValueByColName(dataRow, "Strain Type");
					if (WtUtils.isNullOrBlank(strain)
							&& WtUtils.isNullOrBlankOrPlaceholder(strainType))
					{
						break; // end of data rows
					}
					else if (WtUtils.isNullOrBlankOrPlaceholder(strainType))
					{
						throw new LinxUserException(
								"Missing required value Strain Type in row "
										+ dataRow.getRowNum());
					}
					task.getServerItem("StrainType").setValue(strainType);
					task.getStrainPrefix(strainType, strain, db);
					if (WtUtils.isNullOrBlank(strain))
					{
						// user left blank for LIMS to assign
						strain = ((CreateStrain) task).getNextStrainID(strainType, null, db);
						parser.setCellValue(parser.getHeaderRow(), "Strain ID", dataRow,
								strain);
						bIdAssigned = true;
					}
					task.getServerItem(ItemType.STRAIN).setValue(strain);
					task.getServerItem("StrainName").setValue(
							parser.getCellValueByColName(dataRow, "Strain Name"));
					task.getServerItem("Genus").setValue(
							parser.getRequiredProperty(dataRow, "Genus"));
					task.getServerItem("OriginID").setValue(
							parser.getCellValueByColName(dataRow, "Origin ID"));
					task.getServerItem("Project").setValue(
							parser.getRequiredProperty(dataRow, "Project"));
					task.getServerItem("NotebookRef").setValue(
							parser.getRequiredProperty(dataRow, "Page Ref [notebook-pg]"));
					// locations (required only for Prok) -- set first one to clear old ones
					task.getServerItem("Location").setValue(
							parser.getCellValueByColName(dataRow,
									"Location 1 [Freezer:Box:Position]"));
					task.getServerItem("Location").appendValue(
							parser.getCellValueByColName(dataRow,
									"Location 2 [Freezer:Box:Position]"));
					task.getServerItem("Location").appendValue(
							parser.getCellValueByColName(dataRow,
									"Location 3 [Freezer:Box:Position]"));
					// only certain kinds of strains have 4 locations
					task.getServerItem("Location").appendValue(
							parser.getCellValueByColName(dataRow,
									"Location 4 [Freezer:Box:Position]"));
					if(sheet.getSheetName().equalsIgnoreCase("Strains - Prok"))
					{
						if(task.getServerItemValues("Location").size() < 3)
						{
							throw new LinxUserException(
									"Locations [Freezer:Box:Position] are required "
									+ " for Prokaryotic strains (strain " + strain + ")");
						}
					}
					// optional fields
					task.getServerItem("Species").setValue(
							parser.getCellValueByColName(dataRow, "Species"));
					task.getServerItem("Comment").setValue(
							parser.getCellValueByColName(dataRow, "Comment"));
	
					// call std core processing
					// -- task class will perform db updates
					save(task, user, db, request, response);
	
					/******** INSERT/UPDATE ADD'L STRAIN PROPERTIES ******/
					// loop thru columns in header and data row,
					// -- looking for other key/value pairs
					// -- AFTER 'Comment' field
					// -- (probably none now, but lab may add cols in future)
					String cellValue = null;
					int commentIdx = parser.getCellByColName(dataRow, "Comment")
							.getColumnIndex();
					for (int idx = commentIdx; idx < parser.getHeaderRow().getLastCellNum(); idx++)
					{
						String colName = parser.getColumnNameByCellIndex(idx);
						cellValue = parser.getCellValueByColName(dataRow, colName);
	
						/******** UPDATE OTHER STRAIN PROPERTIES (ctd) ******/
						// save this key/value pair as a property of this strain
						params.clear();
						params.add(strain);
						params.add(colName);
						params.add(cellValue);
						params.add(task.getTranId() + "");
						try
						{
							// stored proc will check for required values
							// -- to keep it out of the code (per Vidya)
							dbHelper.callStoredProc(db, "spEMRE_UpdateStrain", params, false);
						}
						catch (Exception ex)
						{
							throw new LinxUserException("In worksheet [" + sheet.getSheetName()
									+ "], row " + dataRow.getRowNum() + ", column [" + colName
									+ "], strain ID " + strain + ": " + ex.getMessage());
						}
					}// next key/value cell AFTER comment field
				}
				catch (Exception ex1)
				{
					if (ex1 instanceof LinxUserException)
					{
						throw (LinxUserException) ex1;
					}
					file.delete();
					file = null;
					throw new LinxUserException("At row " + dataRow.getRowNum() + ": "
							+ ex1.getMessage());
				}
			}// next data row
			// at exit, have processed all data rows in file,
			// calling task.save() on each iteration
			if(bIdAssigned)
			{
				try
				{			
					// save any changes to file, e.g. strain ID assignments
					File finFile = new File(file.getPath());
					FileOutputStream fos = new FileOutputStream(finFile);
					workbook.write(fos);
					fos.flush();
					fos.close(); // <-- essential to make this work
				}
				catch (IOException ex)
				{
					throw new LinxUserException(ex);
				}
			}
		}// next sheet
	}// end method

	/**
	 * Sets properties for this strain in the UI widgets, if this is an existing
	 * strain.
	 * 
	 * @param strain
	 * @param db
	 */
	protected void setDisplayItemValues(String strain, DefaultTask task,
			User user, HttpServletRequest request, Db db)
	{
		if (WtUtils.isNullOrBlank(strain))
		{
			task.getDisplayItem(ItemType.STRAIN).clearValues();
			task.getServerItem(ItemType.STRAIN).clearValues();
			// todo: add other clearing up here
			return;
		}

		// else, we have a strain
		task.getDisplayItem(ItemType.STRAIN).setValue(strain);
		try
		{
			// set values for the given strain, which may be new
			ArrayList params = new ArrayList();
			params.add(strain);
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
					"spEMRE_GetStrainProperties", params, true);
			while (rs.next())
			{
				task.getDisplayItem("StrainName").setValue(rs.getString(1));
				task.getDisplayItem(ItemType.PROJECT).setSelectedValue(rs.getString(2));
				task.getDisplayItem(DataType.NOTEBOOK_REF).setValue(rs.getString(3));
				task.getDisplayItem("Comment").setValue(rs.getString(4));
				task.getDisplayItem(ItemType.STRAIN).setValue(rs.getString(5));
				task.getDisplayItem("Genus").setValue(rs.getString(6));
				task.getDisplayItem("Species").setValue(rs.getString(7));
				task.getDisplayItem("OriginID").setValue(rs.getString(8));
			}// expecting only one row, but updated comments can
			// complicate things
			rs.close();
			rs = null;
			// at exit, have set UI properties for this strain, if known

			// if not a known strain, tables may be empty (that's ok)
			populateLocationsView(strain, request, task, db);
			// populateStrainDataFilesView(strain, request, db);
		}
		catch (SQLException e)
		{
			throw new LinxSystemException("While retrieving strain properties: "
					+ e.getMessage());
		}
	}

	/**
	 * Populates screen widget with next Strain serial number by looking up in db.
	 * 
	 * @param request
	 * @param task
	 * @param user
	 * @param db
	 */
	protected void setNextId(HttpServletRequest request, Task task, User user,
			Db db)
	{
		// find last strain ID and increment
		// clear UI widgets
		String organism = task.getDisplayItemValue("StrainType");
		if (WtUtils.isNullOrBlankOrPlaceholder(organism))
		{
			throw new LinxUserException(
					"Please select 'Strain type' from the list, then try again.");
		}
		String nextStrain = ((CreateStrain) task).getNextStrainID(organism, null,
				db);
		setDisplayItemValues(nextStrain, (StrainCollection) task, user, request, db);

	}

	/**
	 * Number of rows to show in the Location UI table on initial display, for
	 * defining new strain.
	 * 
	 * @return 3 for Brown Lab
	 */
	protected int getLocationRowCount()
	{
		return 4;
	}

	/**
	 * Overridden to repopulate strain location UI on error
	 */
	protected void save(Task task, User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		try
		{
			task.setMessage("");
			super.save(task, user, db, request, response);
			task.setMessage("Successfully created new strain "
					+ task.getDisplayItemValue(ItemType.STRAIN));

			// preserve data shown in UI (if any)
			if(locationsView == null)
			{
				locationsView = this.getViewFromSession(request, LOCATION_TABLE);
			}
			if (locationsView != null)
			{
				locationsView.setStartRow(1);
				locationsView.setMessage("Showing freezer locations for strain " 
						+ task.getDisplayItemValue(ItemType.STRAIN));
			}
			//locationsView.setMessage("Click Next ID to show next available freezer locations");
			if (importsView != null)
			{
				importsView.setStartRow(1);
			}
		}
		catch (Exception ex)
		{
			if(locationsView == null)
			{
				locationsView = this.getViewFromSession(request, LOCATION_TABLE);
			}
			if (locationsView != null)
			{
				locationsView.setStartRow(1);
			}
			if (importsView != null)
			{
				importsView.setStartRow(1);
			}
			throw new LinxUserException(ex);
		}
	}



	/**
	 * Calls stored procedure spEMRE_getStrainCollectionImportsByUser to retrieve
	 * a list of the last few files imported by the logged in user. Populates
	 * screen UI table below all other screen widgets, with a hyperlink on the
	 * file id.
	 * 
	 * @param request
	 * @param user
	 * @param db
	 */
	protected void populateImportsView(HttpServletRequest request, User user,
			Db db)
	{
		// show UI table of recent imported files for this user
		String sql = "exec spEMRE_getStrainCollectionImportsByUser '"
				+ user.getName() + "'";
		importsView = getSQLRowsetView(request, sql, "File ID", IMPORTS_TABLE,
				Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		importsView.setWidget(1, LinxConfig.WIDGET.LINK);
		importsView.setStartRow(1);
		importsView.setMessage("");

		importsView.setMessage("Showing recently imported files for user "
				+ user.getName());
		addViewToSessionViews(request, importsView);

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


	/**
	 * NOT IN USE -- see Update Strain Returns a rowset view of files previously
	 * imported for the selected strain.
	 * 
	 * @param request
	 * @param db
	 * @return rowset view of files for given strain
	 *
	protected void populateStrainDataFilesView(String strain,
			HttpServletRequest request, Db db)
	{
		// show UI table of files for selected strain
		String sql = "exec spMet_GetDataFilePathsForStrain '" + strain + "'";
		dataFilesView = this.getSQLRowsetView(request, sql, "File Type",
				FILE_TABLE, Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		// set hotlink on File ID field
		dataFilesView.setWidget(2, LinxConfig.WIDGET.LINK);
		dataFilesView.setScroll(false);
		dataFilesView.setStartRow(1);
		dataFilesView.setMessage("Showing data files for Strain ID " + strain);

		addViewToSessionViews(request, dataFilesView);
	}
	*/
	

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
}
