package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.util.PlateLayout;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
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

public class SvDHCStrainCollection extends EMREServlet 
{

	protected String FILE_TABLE = "Files";
	private String RACK = "SRack";
	private String SRACKBU = "SRackBU";
	protected RowsetView dataFilesView = null;
	private String LOCATION_TABLE = "Locations";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	
	public static final String[] reqColumnHeaders = new String[]{"New Strain ID",
		"Strain Name","Project","Page Ref [notebook-pg]","Location 1 [Freezer:Box:Position]",
		"Location 2 [Freezer:Box:Position]","Library","Plasmid","Vector Map File","Comment"};

	
	/**
	 * Overridden to 
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
	    if(WtUtils.isNullOrBlank(strain))
	    {
	    	setDisplayItemValues(null, task, user, request, db);
	    }
	    else //if(!db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
	    {
	    	// in case of error, 
	    	setupLocationsView(locationsView, COL_PRINT, COL_LOC, this.getLocationRowCount());
	    }
	    //else
	    //{
	    	// after an update, show the new location(s)
	    //	findStrainLocations(strain, request, task, db);
	    //}
	  }
	
  /**
   * Handles GET request for a specific file when user
   * clicks on it in the UI table of existing data for
   * a clone.
   * @param request
   * @param response
   * @param task
   * @param user
   * @param db
   * @return
   */
  protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
	  task.populateSQLValues(user, "(Select)", db);
	  if(request.getParameter("strain") != null)
	  {
		  //we have a request from the strain query page
		  String strain = request.getParameter("strain");
		  if( db.getHelper().isItemOnQueue(strain, ItemType.STRAIN, "Discarded Strains",db))
			{
				setDisplayItemValues(null,task,user, request, db);
				throw new LinxUserException("Strain " + strain + " has been discarded and cannot be queried."
					 + " Please enter a new strain, then try again.");
			}
		  setDisplayItemValues(strain, task, user, request, db);
	  }
	  else if(WtUtils.isNullOrBlank(task.getServerItemValue(ItemType.STRAIN)))
	  {	
		  // no action unless a strain is specified
		  return FINISH_FOR_ME;
	  }
	  else if(request.getParameter(ACTION.EXPORT) != null)
	  {
		  // user wants the list of saved files (not an individual file)
		  String strain = task.getServerItemValue(ItemType.STRAIN);
		  this.exportAllTablesOnScreen(request, response, strain + " data files.txt");
	  }
	  else
	  {
	   	// user has selected an individual data file for download 
         String fileId = (String)request.getParameter("selVal");
         if(fileId != null)
         {
             File file = this.getFile(fileId, db);
             String filename = file.getName();
             this.returnDownloadAsByteStream(response, file, filename, AS_EXCEL_FILE, false);
             return ALL_DONE;
         }
         dataFilesView.setStartRow(1);
         Message msg = new Message("Could not find a file under FileID " + fileId
        		 + " [may have been moved offline.] " + Strings.MSG.ALERT_LIMS_ADMIN);
         msg.setStatusCode(Strings.LINX_STATUS.ERROR);
         task.setMessage(msg);
	  }
	  return FINISH_FOR_ME;
	}

	/**
	 * Handles custom actions Import, Print Label, and Find Strain Data
	 * by returning list of previously
	 * saved fermentation or analytical chemistry data files.
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

			if(request.getAttribute("ImportButton")!= null)
		    {
		    	// clear out old values
		    	setDisplayItemValues(null/*no strain*/,task,user, request, db);
		    	task.getDisplayItem(ItemType.STRAIN).clearValues();
		    	task.getServerItem(ItemType.STRAIN).clearValues();

		    	task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.STRAIN_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for a bulk import file, then try again.");
				}
				importRowsFromFile(fileId, task, user, db, request,response);

		    	commitDb(db);
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
				//lets print the current locations in the rowset
				  String strain = task.getServerItemValue(ItemType.STRAIN);
				  if(WtUtils.isNullOrBlankOrPlaceholder(strain))
				  {
					  throw new LinxUserException("Please enter a value for Strain ID.");
				  }
				printLabels(strain, request, task, db);
				// set this strain's values in UI widgets
				setDisplayItemValues(strain, task, user, request, db);	
				task.setMessage("Successfully printed labels.");
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("NextButton") != null)
			{
				// find last strain ID and increment
				// clear UI widgets
				setDisplayItemValues(null, task, user, request, db);
				String nextStrain = getNextStrainId(db);
				task.getDisplayItem(ItemType.STRAIN).setValue(nextStrain);
				
				//now lets generate freezer positions for this new strain
				populateLocationsView(nextStrain, request, task, db);
				//locationsView = populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE);
			    //RowsetView.addViewToSessionViews(request, locationsView);


				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("FindButton") != null)
			{
				String strain = task.getServerItemValue(ItemType.STRAIN);
				if(WtUtils.isNullOrBlank(strain))
				{
					throw new LinxUserException("Please enter a Strain ID, then try again.");
				}
				else if( !db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
				{
					setDisplayItemValues(null,task,user, request, db);
					throw new LinxUserException("Strain " + strain + " does not exist in this LIMS database."
						 + " Please check the entry, then try again.");
				}
				else if( db.getHelper().isItemOnQueue(strain, ItemType.STRAIN, "Discarded Strains",db))
				{
					setDisplayItemValues(null,task,user, request, db);
					throw new LinxUserException("Strain " + strain + " has been discarded and cannot be queried."
						 + " Please enter a new strain, then try again.");
				}
				String prefix = getStrainPrefix(db);
				if(!strain.startsWith(prefix))
				{
					setDisplayItemValues(null,task,user, request, db);
					throw new LinxUserException("Strain must start with the prefix '" + prefix + "'");
				}
				// set this strain's values in UI widgets
				setDisplayItemValues(strain, task, user, request, db);
				//look up the locations
				findStrainLocations(strain, request, task, db);
				return FINISH_FOR_ME;
			}
			else if(request.getAttribute("ContentButton") != null)
			{
				try
				{
					//ok, we need to redirect them to the strain content page
					//first lets get the strain id and name
					String strainId = task.getServerItemValue(ItemType.STRAIN);
					if(WtDOMUtils.isNullOrBlank(strainId))
						throw new Exception("The strain Id must not be blank.");
					String strainName = task.getServerItemValue("StrainName");
					if(WtDOMUtils.isNullOrBlank(strainName))
						throw new Exception("The strain name must not be blank.");
					String project = task.getServerItemValue(ItemType.PROJECT);
					if(project.equalsIgnoreCase("(select)"))
						throw new Exception("Please select a project.");
					
					String password = WtTaskUtils.hashMD5(user.getPassword());
					//now we need the URL for the redirect
					String url = dbHelper.getApplicationValue(db, "System Properties", "Strain Collection Get Content URL");
					//set the contenttype
					response.setContentType("text/html");
					//add the parameters for the get request
					url = url + "?external=True&username=" + user.getName() + "&password=" + password 
						+ "&strain_id=" + strainId + "&strain_name=" + strainName + "&project=" + project;
					//send this puppy off
					response.sendRedirect(url);
				}
				catch(Exception ex)
				{
					throw new LinxUserException(ex.getMessage());
				}		 
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			//keep the rowset as is
			locationsView.setStartRow(1);
			if(dataFilesView != null) 
			{
				dataFilesView.setStartRow(1);
			}
			if(ex instanceof LinxUserException)
			{
				throw (LinxUserException)ex;
			}
			else if(ex instanceof LinxDbException)
			{
				throw (LinxDbException)ex;
			}
			throw new LinxUserException(ex.getMessage());
		}
	}

		

	
	  /** 
   * Overridden to requests from XML client, usually Bioinformatics'
   * Strain Properties Database app.
   * @param domData
   * @param strAction
   * @param task
   * @param user
   * @param db
   * @param request
   * @param response
   */
  protected void handleCustomAction(Element domData, String strAction, Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
  {

	// xml client is asking if item (strain) is known to LIMS db
    if (strAction.equalsIgnoreCase("verify strain exists"))
    {
  		String strain = task.getServerItem(ItemType.STRAIN).getValue();
  		if(WtUtils.isNullOrBlank(strain))
  		{
  			throw new LinxUserException("Please provide a Strain name, then try again.");
  		}
  		if(db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
  		{
  			task.getMessage().setStatusCode("200");
  			task.setMessage("Verified");
  			return;
  		}
    }
    super.handleCustomAction(domData, strAction, task, user, db, request, response);
  }
  
  protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
	  try
	  {
		  XLSParser fileData = null;
			// import file
			// core has already validated file per task def
			File inFile = this.getFile(fileId, db);

			// create a list of objects while importing manifest
			//lets get the data from the file and put it into a data container object
			char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
			String columnKey = "New Strain ID"; //the unique identifier that lets me know i've reach the column data in the file
			fileData = new XLSParser(inFile, "DHC Strain Collection",
					delim, columnKey, reqColumnHeaders , true);

			// ready for std save() processing
			task.setMessage("Successfully imported new strains from bulk import file.");
			
			
			//loop though the file and insert each line
			String err = "";
			int row = 1;
			try
			{
				if(fileData.gotoFirst())
				{
					do
					{			
						String limsId = fileData.getRequiredProperty("New Strain ID");
						String project = fileData.getRequiredProperty("Project");
						String notebook = fileData.getRequiredProperty("Page Ref [notebook-pg]");
						String library = fileData.getRequiredProperty("Library");
						String strainName = fileData.getRequiredProperty("Strain Name");
						String plasmid = fileData.getRequiredProperty("Plasmid");
						String loc1 = fileData.getRequiredProperty("Location 1 [Freezer:Box:Position]");
						String loc2 = fileData.getRequiredProperty("Location 2 [Freezer:Box:Position]");
						String comment = fileData.getProperty("Comment");
						String vectorMapFile = fileData.getProperty("Vector Map File");
						
						task.getServerItem(ItemType.STRAIN).setValue(limsId);
						task.getServerItem("Project").setValue(project);
						task.getServerItem("NotebookRef").setValue(notebook);
						task.getServerItem(ItemType.LIBRARY).setValue(library);
						task.getServerItem(ItemType.PLASMID).setValue(plasmid);
						task.getServerItem("StrainName").setValue(strainName);
						task.getServerItem("VectorMapFile").setValue(vectorMapFile);
						task.getServerItem("Comment").setValue(comment);
						
						ArrayList<String> locs = new ArrayList<String>();
						locs.add(loc1);
						locs.add(loc2);
						
						task.getServerItem("Location").setValues(locs);
						
						// call std processing
						save(task, user, db, request, response);
						row++;
					}
					while(fileData.gotoNext());
				}//at end we have validated all of the inputs in the file
				else
					throw new Exception("There were no rows found in the file.  Please check column headers and try again.");
			}
			catch (Exception e)
			{
				err += "Error occurred while parsing row " + row + ": " + e.getMessage();
			}
			if(!err.equals(""))
				throw new LinxUserException(err);
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
		
	}

  
  	/**
  	 * Overridden only to re-draw Locations UI table on error.
  	 * @param task
  	 * @param user
  	 * @param db
  	 * @param request
  	 * @param response
  	 */
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			super.save(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			// redraw Locations table
			preprocessTask(request, task, user, db);
			throw new LinxUserException(ex);
		}
	}

	/**
	 * Returns the name of stored proc to use
	 * to retrieve this type of strain. 
	 * @return name of sp to get strain report for this type of strain
	 */
    protected String getStrainReportSQL()
	{
		return "exec spEMRE_GetDHCStrainCollection";
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
    String sql = "exec spMet_GetDataFilePathsForStrain '" + strain + "'";
   	RowsetView view = this.getSQLRowsetView(request, sql, "File Type", FILE_TABLE, 
   			Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
   	// set hotlink on File ID field
    view.setWidget(2, LinxConfig.WIDGET.LINK);
    view.setScroll(false);
    //view.setScrollSize("small");
    view.setStartRow(1);
     return view;
  }
  
  /**
	 * Sets properties for this strain in the UI widgets, if this is an existing
	 * strain.
	 * 
	 * @param strain
	 * @param db
	 */
	protected void setDisplayItemValues(String strain, Task task, User user, HttpServletRequest request,
			Db db)
	{
		// refresh display items
		task.getDisplayItem("StrainName").clearValues();
		task.getDisplayItem(ItemType.PROJECT).setValue("EMRE");
		task.getDisplayItem(DataType.NOTEBOOK_REF).clearValues();
		task.getDisplayItem(DataType.LOCATION).setValues(new ArrayList<String>());
		task.getDisplayItem(DataType.COMMENT).clearValues();

		// refresh server-side items, too
		task.getServerItem("StrainName").clearValues();
		task.getServerItem(ItemType.PROJECT).clearValues();
		task.getServerItem(DataType.NOTEBOOK_REF).clearValues();
		task.getServerItem(DataType.LOCATION).clearValues();
		task.getServerItem(DataType.COMMENT).clearValues();

		RowsetView.cleanupSessionViews(request);
		initLocationsView(db);
		RowsetView.addViewToSessionViews(request, locationsView);
		if (WtUtils.isNullOrBlank(strain))
		{
			// we are importing a file or defining a new single strain, 
			// so just clear values and exit
			return;
		}
		// in case new Project has been created
		task.populateSQLValues(user, "(Select)", db);
		// set values for the search item
		ArrayList<String> params = new ArrayList<String>();
		params.add(strain);

		ArrayList<String> dataFiles = new ArrayList<String>();
		try
		{
			// retrieves newest comments first
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
					"spEMRE_getDHCStrainProperties", params, true);
			while (rs.next())
			{
				task.getDisplayItem("StrainName").setValue(rs.getString(1));
				task.getDisplayItem(ItemType.PROJECT).setValue(
						rs.getString(2));
				task.getDisplayItem(DataType.NOTEBOOK_REF).setValue(
						rs.getString(3));
				task.getDisplayItem(ItemType.LIBRARY).setSelectedValue(rs.getString(4));
				task.getDisplayItem(ItemType.PLASMID).setSelectedValue(rs.getString(5));
				task.getDisplayItem(DataType.COMMENT).setValue(rs.getString(6));
				task.getDisplayItem(ItemType.STRAIN).setValue(rs.getString(7));
				dataFiles.add(rs.getString(8));
			}// expecting only one row back, but updated comments can
				// complicate things
			rs.close();
			rs = null;
			// at exit, have set UI properties for this strain, if known

			
			// populate a rowset with strain locations 
			populateLocationsView(strain, request, task, db);
			
			// populate a rowset table with any existing data file paths
			//task.getDisplayItem(FILE_TABLE).setVisible(true);
			dataFilesView = populateFilesView(strain, request, db);
			if (dataFilesView.getRowcount() < 1)
			{
				task.setMessage("No data files have been imported for strain "
						+ strain);
			}
			else
			{
				dataFilesView.setMessage("Showing data files for Strain ID " + strain);
				RowsetView.addViewToSessionViews(request, dataFilesView);
			}
		}
		catch (SQLException e)
		{
			throw new LinxSystemException("While retrieving strain properties: "
					+ e.getMessage());
		}
	}
  
  /**
	 * Show a blank UI table of freezer Locations,
	 * with each row checked for 'Print' and as many
	 * rows as required for this task's strain type 
	 * (method is inherited by all strain collection tasks.)
	 * User can enter new locations, or click Find Strain 
	 * to populate with known locations, or click Next ID
	 * to populate with next assumed available locations.
	 * 
	 */
  protected void initLocationsView(Db db)
	{
	    //lets write a file containing the locations
	    //we can use it to populate the Rowset
	    int numRows = getLocationRowCount();
	    ArrayList<String> alLocs = new ArrayList<String>();
	    for(int i = 0; i < numRows; i++)
	    {
	    	alLocs.add("");
	    }
	    File file = writeLocationsToFile(alLocs, db);
	    String[] colHeaders = {"Print","Location"};
	    locationsView = this.getFileRowsetView(file, "Print", 
	    	     LOCATION_TABLE, null, colHeaders, numRows );
	    setupLocationsView(locationsView, COL_PRINT, COL_LOC, numRows);
	    locationsView.setMessage("");
	    locationsView.setLabel("Freezer:Box:Position");
	    locationsView.setName(LOCATION_TABLE);
		
	    if(file.exists())
	    {
	    	file.delete();
	    }
	}
 

	/**
   * Uses reserved locations to populate the UI table
   * of Locations for user to edit, or save and print.
   * @param request
   * @param locations
   * @param db
   * @return
   */
  protected void populateLocationsView(String strain, HttpServletRequest request, Task task, Db db)
	{
	    // show UI table of locations for selected strain
  		RowsetView.cleanupSessionViews(request);

	    String sql = "exec spMet_GetStrainLocationsChecked '" + strain + "'";
	   	locationsView = this.getSQLRowsetView(request, sql, "Print", LOCATION_TABLE, 9, db);
	   	ArrayList<String> alLocations = null;
	   	ArrayList<String> alBULocs = null;
	   	String freezerLocSP = "spEMRE_getCurrentDHCStrainBoxAndPosition";
	   	if(locationsView.getRowcount() < getLocationRowCount())
	   	{
	   		alLocations = reserveIsolateFreezerLocations(freezerLocSP, strain,RACK, db);
	   		alBULocs = reserveIsolateFreezerLocations(freezerLocSP, strain, SRACKBU, db);
	   		for(String s: alBULocs)
	   			alLocations.add(s);
	   		locationsView = this.populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE,db);
		    locationsView.setMessage("Showing next available freezer locations");
	   	}
	   	else
	   	{
	   		locationsView = setupLocationsView(locationsView, COL_PRINT, COL_LOC, getLocationRowCount());
		    locationsView.setMessage("Showing freezer locations for strain " + strain);
	   	}
	    
  		RowsetView.addViewToSessionViews(request, locationsView);

	 }
  
  protected void findStrainLocations(String strain, HttpServletRequest request, Task task,Db db)
	{
	    // show UI table of locations for selected strain
		//RowsetView.cleanupSessionViews(request);

	    String sql = "exec spMet_GetStrainLocationsChecked '" + strain + "'";
	   	locationsView = this.getSQLRowsetView(request, sql, "Print", LOCATION_TABLE, 9, db);
	   	locationsView.setWidget(COL_PRINT,LinxConfig.WIDGET.CHECKBOX);
	   	locationsView.setWidget(COL_LOC,LinxConfig.WIDGET.TEXTBOX);
	   	locationsView.setStartRow(1);
	   	locationsView.setHideNavControls(true); 
		    
		    // default to checking 'Print'
		    int numRows = getLocationRowCount();
	   	locationsView.setRowcount(numRows);
		for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
		{
		    locationsView.setIsSelected(rowIdx, COL_PRINT);
		}
	    locationsView.setMessage("Showing freezer locations for strain " + strain);
		RowsetView.addViewToSessionViews(request, locationsView);
	 }

//  /**
//   * Write a dummy file with numRows rows for populating a Rowset.
//   * @param alLocations
//   * @param response
//   * @return
//   */
//  protected File writeLocationsToFile(int numRows)
//  {
//	  StringBuffer sb = new StringBuffer();
//	  //write out headers
//	  // -- 'y' signals jsp to check the box
//	  sb.append("Print,Location" + Strings.CHAR.NEWLINE);
//	  for(int rowIdx = 0; rowIdx < numRows; rowIdx++)
//	  {
//		  // write out a print placeholder and a reserved location
//		  String line = " , " + Strings.CHAR.NEWLINE;
//		  sb.append(line);
//	  }
//
//	  // write rows to file
//	  // we're not writing back to client, so no need for output stream
//	  String fileName = "C:\\LimsLogs\\temp\\locationFile.csv";
//	  File file = new File(fileName);
//	  if(file.exists())
//	  {
//		  file.delete();
//	  }
//	  file = new File(fileName);
//	  try
//	  {
//		  FileWriter writer = new FileWriter(file);
//		  writer.write(sb.toString());
//		  writer.flush();
//		  writer.close();
//	  }
//	  catch(Exception ex)
//	  {
//		  throw new LinxUserException(ex.getMessage());
//	  }
//	  return file;
//  }
  
  
	  /**
	 * Number of rows to show in the Location UI table
	 * on initial display, for defining new strain.
	 * @return 3 for Brown Lab
	 */
  protected int getLocationRowCount()
  {
	  return 2;
  }
  
  private String getStrainType()
	{
		return "DHC";
	}
  
  /**
	 * return the correct prefix for the strain based upon task.
	 * @param taskName
	 * @return
	 */
	public String getStrainPrefix(Db db)
	{
		String prefix = dbHelper.getDbValue("exec spMet_getStrainPrefix '" 
				+ getStrainType() + "'", db);
		String suffix = dbHelper.getDbValue("exec spMet_getIDSuffix", db);
		if(WtUtils.isNullOrBlank(suffix))
			suffix = "";
		else
			suffix = suffix + "-";
		return prefix + "-" + suffix;
	}
	
	protected String getNextStrainId(Db db)
	{
		String rtn = null;
		try
		{
			ResultSet rs = dbHelper.getResultSet("exec spEMRE_getCurrentDHCStrainPlateAndWell ", db);
			String plate = null;
			while(rs.next())
			{
				plate = rs.getString(1);
				String alphaCoord = rs.getString(2);
				//String numericCoord = rs.getString(3);
				boolean bNewPlate = false;
				//do we have a plate or is this our first one?
				if(WtUtils.isNullOrBlank(plate))
				{
					//first one - create a new plate
					rtn = "DHC-SGI-E-000001-A01";
				}
				else
				{
					//in order to get the next isolate we need to increment the numeric coord by one and see if we get a well
					try
					{
						int currentNum = PlateLayout.getNumericCoord(PlateLayout.CM_96WELL, alphaCoord);
						currentNum++;
						String nextWell = PlateLayout.getAlphaCoord(PlateLayout.CM_96WELL, currentNum);
						rtn = plate + "-" + nextWell;
					}
					catch(Exception e)
					{
						//we must be at the end of the plate 
						bNewPlate = true;
					}
					if(bNewPlate)
					{
						//increment the plate and start at well A01
						int currentPlateNum = Integer.parseInt(plate.substring(plate.lastIndexOf("-") + 1));
						int newPlateNum = currentPlateNum + 1;
						String sNewNum = EMRETask.zeroPad(newPlateNum, 6);
						String newPlate = plate.substring(0,plate.lastIndexOf("-"));
						rtn = newPlate + "-" + sNewNum + "-A01";
					}
				}
				
			}
			rs.close();
			rs = null;
			if(plate == null)
			{
				//first instance - need a new plate
				rtn = "DHC-SGI-E-000001-A01";
			}
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return rtn;
	}
	
	/**
	   * Prints small (1" X 0.5 ") barcodes on the S4M printer.
	   *
	   *@param request
	   *@param db
	   */
	  public void printLabels(String strain, HttpServletRequest request, Task task, Db db)
	  {
		  try
		  {
			// construct the label from user's fields

			  //lets make sure they did a save first.
			  List lsStrains = db.getHelper().getListEntries("exec spMet_GetStrainLocations '" 
					  + strain + "'", db);
			  if(lsStrains.isEmpty())
			  {
				  throw new LinxUserException("Please save the strain locations before printing labels (click [Save]).");
			  }
			  String dhcPlate = strain.substring(0, strain.lastIndexOf('-'));
			  String noteBook = task.getServerItemValue(DataType.NOTEBOOK_REF);
			  if(WtUtils.isNullOrBlankOrPlaceholder(noteBook))
			  {
				  throw new LinxUserException("Please enter notebook page, then try again.");
			  }
			  //add NB as a prefix to the notebook page
			  noteBook = "NB" + noteBook;
			  S4MSmallBarcode print = new S4MSmallBarcode();
			  EMREDbHelper dbHelper = new EMREDbHelper();
			  PrintService printService = dbHelper.getPrintServiceForTask("Strain Collection", db);
			  //if we made it this far we found the zebra printer
			  //now lets start printing barcodes
			  //Loop through the freezer locations and print barcodes
			  TableDataMap rowMap = new TableDataMap(request, LOCATION_TABLE);
		      int numLocations = rowMap.getRowcount();
			  for(int rowIdx = 1; rowIdx <= numLocations; rowIdx++)
			  {
				  if( rowMap.isCheckboxChecked(rowIdx, COL_PRINT))
				  {
					  String location = (String)rowMap.getValue(rowIdx, COL_LOC);
					  //add FZ as a prefix to the freezer location
					  location = "FZ" + location;
					  ArrayList<String> alrows = new ArrayList<String>();
					  alrows.add(noteBook);
					  alrows.add(location);
					  //lets remove the "SGI-E" so the barcode fits on the label
					  dhcPlate = dhcPlate.replace("-SGI-E", "");
					  String label = print.getZPLforLabel(dhcPlate, alrows);
					  S4MSmallBarcode.print(printService, dhcPlate, label);
					  Thread.sleep(200);
					  alrows.clear();
					  alrows = null;
				  }
			  }// next location
			  task.setMessage("Successfully printed strain barcodes.");
		  }
		  catch(Exception ex)
		  {
			  throw new LinxUserException("Error occurred when printing labels: " + ex.getMessage());
		  }
	  }
}
