package com.sgsi.emre.servlet;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.util.PlateLayout;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvIsolates extends EMREServlet 
{
	private String LOCATION_TABLE = "Location";
	private String RACK = "Rack";
	protected RowsetView locationsView = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	EMRETask myTask = new EMRETask();
	
	int numLocations = 1;
	
	
	  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	  {
	    String isolate = task.getServerItemValue(ItemType.ISOLATE);
	    
	    if(WtUtils.isNullOrBlank(isolate))
	    {
	    	populateUI(request, task, db);
	    }
	    else
	    {
	    	// after an update, show the new location(s)
	        populateLocationsView(isolate, request, task, db);
	    }
	    
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

			if (request.getAttribute("ExportButton") != null)
			{
				// user wants to download the entire strain collection to Excel
				writeToExcel(request, response, "exec spEMRE_reportIsolates", db);

				return ALL_DONE;
			}
			else if (request.getAttribute("PrintLabel") != null)
			{
				//lets print 
				  String isolate = task.getServerItemValue(ItemType.ISOLATE);
				  if(WtUtils.isNullOrBlankOrPlaceholder(isolate))
				  {
					  throw new LinxUserException("Please enter a value for New LIMS ID.");
				  }
				  
				 //lets check to see if we have a plate.
				 String plate = dbHelper.getDbValue("exec spEMRE_getPlate '" + isolate 
							  + "'", db);
				  
			      ((EMRETask)task).printVialLabels(plate, isolate, db);
			      
			      task.setMessage("Successfully printed labels.");
				// set this strain's values in UI widgets
				setDisplayItemValues(isolate, task, user, request, db);				
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("ImportButton") != null)
			{
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.ISOLATES_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for a bulk import file, then try again.");
				}
				importRowsFromFile(fileId, task, user, db, request,response);

		    	commitDb(db);
		        return FINISH_FOR_ME;    	
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			//keep the rowset as is
			locationsView.setStartRow(1);
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
	
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		XLSParser fileData = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "New LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new XLSParser(inFile, FileType.ISOLATES_SUBMISSION,
				delim, columnKey, EMREStrings.Enrichments.requiredIsolateColumnHeaders, true);

		// ready for std save() processing
		task.setMessage("Successfully imported new isolates from bulk import file.");
		
		
		//loop though the file and insert each line
		String err = "";
		int row = 1;
		try
		{
			if(fileData.gotoFirst())
			{
				do
				{			
					String limsId = fileData.getRequiredProperty("New LIMS ID");
					String originLimsId = fileData.getRequiredProperty("Origin LIMS ID");
					String originItemType = fileData.getProperty("Origin ID Type");
					String pageRef = fileData.getRequiredProperty("Page Ref");
					String vesselType = fileData.getRequiredProperty("Vessel Type");
					String isolationMethod = fileData.getRequiredProperty("Isolation Method");
					String dateArchived = fileData.getRequiredProperty("Date Archived");
					String archiveMethod = fileData.getRequiredProperty("Archive Method");
					String growthMed = fileData.getProperty("Growth Medium");
					String growthTemp = fileData.getProperty("Growth Temperature");
					String irradiance = fileData.getProperty("Growth Irradiance");
					String location = fileData.getRequiredProperty("Location");
					String comment = fileData.getProperty("Comments");
					
					task.getServerItem(ItemType.ISOLATE).setValue(limsId);
					task.getServerItem("OriginLIMSID").setValue(originLimsId);
					task.getServerItem("OriginItemType").setValue(originItemType);
					task.getServerItem("NotebookRef").setValue(pageRef);
					task.getServerItem("DateArchived").setValue(dateArchived);
					task.getServerItem("VesselType").setValue(vesselType);
					task.getServerItem("GrowthTemperature").setValue(growthTemp);
					task.getServerItem("GrowthMedium").setValue(growthMed);
					task.getServerItem("GrowthIrradiance").setValue(irradiance);
					task.getServerItem("ArchiveMethod").setValue(archiveMethod);
					task.getServerItem("IsolationMethod").setValue(isolationMethod);
					task.getServerItem("Location").setValue(location);
					task.getServerItem("Comment").setValue(comment);
					
					// call std processing
					save(task, user, db, request, response);
					row++;
				}
				while(fileData.gotoNext());
			}//at end we have validated all of the inputs in the file

		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if(!err.equals(""))
			throw new LinxUserException(err);
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
		DefaultTask cleanTask = new DefaultTask(((EMRETask)task));
		try
		{
			//String limsId = task.getServerItemValue(ItemType.ISOLATE);
			super.save(task, user, db, request, response);
			//populate the ui with the current sample
			preprocessTask(request, task, user,db);
		}
		catch(Exception ex)
		{
			/// redraw Locations table
			String limsId = task.getServerItemValue(ItemType.ISOLATE);
			task.getDisplayItem(ItemType.ISOLATE).setValue(limsId);
			try
			{
				reserveLocations(limsId, request, task, db);
			}
			catch(Exception e)
			{
				throw new LinxUserException(e.getMessage());
			}
			if(ex.getMessage().toLowerCase().indexOf("more than one item exists") == -1
					&& ex.getMessage().toLowerCase().indexOf("more than one origin item exists") == -1)
			{
				//exception thrown when multiple items exist of different types
				//don't want to reset the DOM if we need to select a type from the dropdown
				((EMRETask)task).setTaskDOM(cleanTask.getTaskDOM());
				((EMRETask)task).setOriginIdType(EMREStrings.ItemType.ORIGIN_LIMS_ID);
			}
			throw new LinxUserException("Error occurred during save: " + ex.getMessage());
		}
	}

	
  /**
	 * Sets properties for this strain in the UI widgets, if this is an existing
	 * strain.
	 * 
	 * @param strain
	 * @param db
	 */
	protected void setDisplayItemValues(String isolate, Task task, User user, HttpServletRequest request,
			Db db)
	{
		// refresh display items
		task.getDisplayItem(ItemType.ISOLATE).clearValues();
		task.getDisplayItem(DataType.NOTEBOOK_REF).clearValues();
		task.getDisplayItem(DataType.LOCATION).setValues(new ArrayList());
		task.getDisplayItem(DataType.COMMENT).clearValues();

		// refresh server-side items, too
		task.getServerItem(ItemType.ISOLATE).clearValues();
		task.getServerItem(DataType.NOTEBOOK_REF).clearValues();
		task.getServerItem(DataType.LOCATION).clearValues();
		task.getServerItem(DataType.COMMENT).clearValues();

		RowsetView.cleanupSessionViews(request);
		populateLocationsView(null,request, task, db);
		RowsetView.addViewToSessionViews(request, locationsView);
		if (WtUtils.isNullOrBlank(isolate))
		{
			//lets get the next available isolate
			isolate = getNextIsolate(db);
			task.getDisplayItem(ItemType.ISOLATE).setValue(isolate);
			reserveLocations(isolate, request, task, db);
		}
		
	}
	
	/** 
	  * retrieves the autogenerated next LIMS ID from the database
	  * @param db
	  * @return
	  */
	protected String getNextIsolate(Db db)
	{
		String rtn = null;
		try
		{
			ResultSet rs = dbHelper.getResultSet("exec spEMRE_getCurrentIsolatePlateAndWell ", db);
			while(rs.next())
			{
				String plate = rs.getString(1);
				String alphaCoord = rs.getString(2);
				String numericCoord = rs.getString(3);
				boolean bNewPlate = false;
				//do we have a plate or is this our first one?
				if(WtUtils.isNullOrBlank(plate))
				{
					//first one - create a new plate
					rtn = "SI4-SGI-E-000100-A01";
				}
				else
				{
					//in order to get the next isolate we need to increment the numeric coord by one and see if we get a well
					try
					{
						int nextNum = Integer.parseInt(numericCoord) + 1;
						PlateLayout.setBUseIsolateLayout(true);
						String nextWell = PlateLayout.getAlphaCoord(PlateLayout.CM_96WELL, nextNum);
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
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return rtn;
	}
  
  
  protected void populateLocationsView(String isolate, HttpServletRequest request, Task task, Db db)
	{
	    // show UI table of locations for selected strain
	  	task.getDisplayItem(ItemType.ISOLATE).setValue(isolate);
		String sql = "exec spEMRE_getIsolateLocationsChecked '" + isolate + "'";
		locationsView = this.getSQLRowsetView(request, sql, "Print", LOCATION_TABLE, numLocations, db);
		locationsView.setStartRow(1);
		locationsView.setWidget(COL_PRINT,LinxConfig.WIDGET.CHECKBOX);
		locationsView.setWidget(COL_LOC,LinxConfig.WIDGET.TEXTBOX);
		locationsView.setHideNavControls(true); 
		for(int rowIdx = 1; rowIdx <= numLocations; rowIdx++)
	    {
			locationsView.setIsSelected(rowIdx, COL_PRINT);
	    }
		task.getDisplayItem(LOCATION_TABLE).setVisible(true);
		RowsetView.addViewToSessionViews(request, locationsView);
	 }
  
  protected void populateUI(HttpServletRequest request, Task task, Db db)
  {
	  String limsId = getNextIsolate(db);
	  if(limsId == null)
			  throw new LinxSystemException("There is no isolate returned from the database.");
	  task.getDisplayItem(ItemType.ISOLATE).setValue(limsId);
	  //lets also populate the locations for this new sample
	  reserveLocations(limsId, request, task, db);
  }
  
  
  protected void reserveLocations(String isolate,HttpServletRequest request, Task task, Db db)
  {
	  
	  //lets populate the locations for this new sample
	  ArrayList<String> alLocations = new ArrayList<String>();
	  String freezerLocSP = "spEMRE_getCurrentIsolateBoxAndPosition";
	  try
	  {
		  alLocations = reserveIsolateFreezerLocations(freezerLocSP, isolate,RACK, db);
		  locationsView = super.populateLocationsView( alLocations, locationsView,  
					COL_PRINT, COL_LOC, LOCATION_TABLE, db);
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
	  locationsView.setStartRow(1);
	  task.getDisplayItem(LOCATION_TABLE).setVisible(true);
	  RowsetView.addViewToSessionViews(request, locationsView);
  }

}
