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
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.PhotoHostStrainCollection;
import com.sgsi.emre.task.PhotoStrainCollection;
import com.sgsi.emre.task.StrainCollection;

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

/**
 * 
 * SvStrainCollection
 *
 * Overridden to handle custom actions to retrieve
 * past uploaded data or to print a label for a 
 * saved strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2008
 */
public class SvStrainCollection extends EMREServlet//extends SvSampleReport
{
	protected String FILE_TABLE = "StrainData";
	protected RowsetView dataFilesView = null;
	private String LOCATION_TABLE = "Locations";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	
	public static final String[] reqColumnHeaders = new String[]{"Strain ID",
		"Strain Name","Project","Page Ref [notebook-pg]","Location 1 [Freezer:Box:Position]",
		"Location 2 [Freezer:Box:Position]","Location 3 [Freezer:Box:Position]","Comment"};

	
	
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
	    	populateLocationsView(locationsView, COL_PRINT, COL_LOC, this.getLocationRowCount());
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
				((StrainCollection)task).printLabels(strain, request, db);
				// set this strain's values in UI widgets
				setDisplayItemValues(strain, task, user, request, db);				
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("NextButton") != null)
			{
				// find last strain ID and increment
				// clear UI widgets
				setDisplayItemValues(null, task, user, request, db);
				String nextStrain = ((StrainCollection)task).getNextStrainID(db);
				task.getDisplayItem(ItemType.STRAIN).setValue(nextStrain);
				
				//now lets generate freezer positions for this new strain
				ArrayList alLocations = ((StrainCollection)task).reserveLocations(nextStrain, db);
				locationsView = populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE,db);
			    RowsetView.addViewToSessionViews(request, locationsView);


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
				String prefix = null;
				if(task instanceof PhotoHostStrainCollection)
				{
					prefix = ((PhotoHostStrainCollection)task).getStrainPrefix(db);
				}
				else if(task instanceof PhotoStrainCollection)
				{
					prefix = ((PhotoStrainCollection)task).getStrainPrefix(db);
				}
				else
					prefix = ((StrainCollection)task).getStrainPrefix(db);
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
			String columnKey = "Strain ID"; //the unique identifier that lets me know i've reach the column data in the file
			fileData = new XLSParser(inFile, "Strain",
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
						String limsId = fileData.getRequiredProperty("Strain ID");
						String strainName = fileData.getRequiredProperty("Strain Name");
						String project = fileData.getRequiredProperty("Project");
						String notebook = fileData.getRequiredProperty("Page Ref [notebook-pg]");
						String loc1 = fileData.getRequiredProperty("Location 1 [Freezer:Box:Position]");
						String loc2 = fileData.getRequiredProperty("Location 2 [Freezer:Box:Position]");
						String loc3 = fileData.getRequiredProperty("Location 3 [Freezer:Box:Position]");
						String comment = fileData.getProperty("Comment");
						
						
						task.getServerItem(ItemType.STRAIN).setValue(limsId);
						task.getServerItem("Project").setValue(project);
						task.getServerItem("NotebookRef").setValue(notebook);
						task.getServerItem("StrainName").setValue(strainName);
						task.getServerItem("Comment").setValue(comment);
						
						ArrayList<String> locs = new ArrayList<String>();
						locs.add(loc1);
						locs.add(loc2);
						locs.add(loc3);
						
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
		return "exec spMet_GetStrainCollection";
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
	protected void setDisplayItemValues(String strain, Task task, User user, HttpServletRequest request,
			Db db)
	{
		// refresh display items
		task.getDisplayItem("StrainName").clearValues();
		task.getDisplayItem(ItemType.PROJECT).setSelectedValue("(Select)");
		task.getDisplayItem(DataType.NOTEBOOK_REF).clearValues();
		task.getDisplayItem(DataType.LOCATION).setValues(new ArrayList());
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
		ArrayList params = new ArrayList();
		params.add(strain);

		try
		{
			// retrieves newest comments first
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
					"spMet_GetStrainProperties", params, true);
			while (rs.next())
			{
				task.getDisplayItem("StrainName").setValue(rs.getString(1));
				task.getDisplayItem(ItemType.PROJECT).setSelectedValue(
						rs.getString(2));
				task.getDisplayItem(DataType.NOTEBOOK_REF).setValue(
						rs.getString(3));
				task.getDisplayItem(DataType.COMMENT).setValue(rs.getString(4));
				task.getDisplayItem(ItemType.STRAIN).setValue(rs.getString(5));
				break;

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
				task.getDisplayItem(FILE_TABLE).setVisible(false);
				task.setMessage("No data files have been imported for strain "
						+ strain);
			}
			else
			{
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
	    populateLocationsView(locationsView, COL_PRINT, COL_LOC, numRows);
	    locationsView.setMessage("");
		
	    if(file.exists())
	    {
	    	file.delete();
	    }
	}
  
   /**
   * Uses the reserved locations to populate the UI table
   * of Locations for user to edit, or save and print.
   * @param request
   * @param locations
   * @param db
   * @return
   */
//  protected void populateLocationsView(ArrayList<String> alLocations)
//	{
//	    // update the RowsetView
//	    //lets write a file containing the locations
//	    //we can use it to populate the Rowset
//	    int numRows = alLocations.size();
//	    File file = writeLocationsToFile(alLocations);
//	    String[] colHeaders = {"Print","Location"};
//	    locationsView = this.getFileRowsetView(file, "Print", 
//	    	     LOCATION_TABLE, null, colHeaders, numRows );
//	    populateLocationsView();
//	    locationsView.setMessage("Showing next available freezer locations");
//
//	    if(file.exists())
//	    {
//	    	file.delete();
//	    }	
//	 }

  /**
   * Basic setup of Location UI table, invoked by specific
   * custom actions: Find Strain, Next ID, and new strain definition.
   */
//     protected void populateLocationsView()
//	 {
//    	locationsView.setWidget(COL_PRINT,LinxConfig.WIDGET.CHECKBOX);
//	    locationsView.setWidget(COL_LOC,LinxConfig.WIDGET.TEXTBOX);
//	    locationsView.setStartRow(1);
//	    locationsView.setHideNavControls(true); 
//	    
//	    // default to checking 'Print'
//	    int numRows = getLocationRowCount();
//	    locationsView.setRowcount(numRows);
//	    for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
//	    {
//	    	locationsView.setIsSelected(rowIdx, COL_PRINT);
//	    }
//	    locationsView.setMessage("Showing freezer locations for strain");
//	}

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
	   	if(locationsView.getRowcount() < getLocationRowCount())
	   	{
	   		alLocations = ((StrainCollection)task).reserveLocations(strain, db);
	   		locationsView = this.populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE, db);
	   	}
	   	else
	   		locationsView = populateLocationsView(locationsView, COL_PRINT, COL_LOC, getLocationRowCount());	
	    locationsView.setMessage("Showing freezer locations for strain " + strain);
	    
  		RowsetView.addViewToSessionViews(request, locationsView);

	 }
  
  protected void findStrainLocations(String strain, HttpServletRequest request, Task task,Db db)
	{
	    // show UI table of locations for selected strain
		RowsetView.cleanupSessionViews(request);

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

  /**
   * Write a dummy file with numRows rows for populating a Rowset.
   * @param alLocations
   * @param response
   * @return
   */
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
//	  String fileName = "LimsLogs\\temp\\locationFile.csv";
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
   * Write a file containing locations reserved for a strain,
   * for showing in a Rowset table. See also method version that writes
   * blank rows.
   * @param alLocations
   * @return file of locations
   */
//  protected File writeLocationsToFile(ArrayList<String> alLocations)
//  {
//	  
//	  StringBuffer sb = new StringBuffer();
//	  //write out headers
//	  // -- 'y' signals jsp to check the box
//	  sb.append("Print,Location" + Strings.CHAR.NEWLINE);
//	  ListIterator itor = alLocations.listIterator();
//	  while(itor.hasNext())
//	  {
//		  String loc = (String)itor.next();
//		  // write out a print placeholder and a reserved location
//		  String line = " ," + loc + Strings.CHAR.NEWLINE;
//		  sb.append(line);
//	  }
//
//	  // write rows to file
//	  // we're not writing back to client, so no need for output stream
//	  String fileName = "LimsLogs\\temp\\locationFile.csv";
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
	  return 3;
  }

}
