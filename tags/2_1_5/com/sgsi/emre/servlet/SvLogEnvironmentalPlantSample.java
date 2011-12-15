package com.sgsi.emre.servlet;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvLogEnvironmentalPlantSample extends SvLogEnvironmentalSample 
{

	protected String LOCATION_TABLE = "Location";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	protected int rowCount = 4;
	public static final String[] reqColHeaders = new String[]{
  		"New LIMS ID","Physical Form","Collection Date","Notebook Ref","Internal ID",
  		"Field Name","Description","Volume (L)","Weight (g)","Temperature (degrees C)",
  		"pH","Dissolved Oxygen (mg/L)","Conductivity (mS/m)","Depth (m)","Salinity (ppt)",
  		"Latitude","Longitude","Altitude (m)","Site Description","Storage Method",
  		"Closest Town","City","County","State","Country","Archive Location",
  		"Freezer Location","Comment"};
	
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	{
	    
	    // display a clean location rowset UI table 
	    // -- data file rowset is hidden until a strain is selected
	    String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
	    if(WtUtils.isNullOrBlank(sample))
	    {
	    	populateUI(request, task, db);
	    }
	    else
	    {
	    	// after an update, show the new location(s)
	        populateLocationsView(sample, request, task, db);
	    }
	  }
	 @Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
	    if (request.getAttribute("PrintLabel") != null)
		{
	    	String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
			if(WtUtils.isNullOrBlankOrPlaceholder(sample))
			{
				throw new LinxUserException("Please enter a value for New LIMS ID.");
			}
			else if( !db.getHelper().isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
			{
				throw new LinxUserException("Sample " + sample + " does not exist in this LIMS database."
					 + " Please check the entry, then try again.");
			}
			String spName = "spMet_GetSampleLocations";
	    	((EMRETask)task).printSampleLabels(request, sample, spName, db);
			return FINISH_FOR_ME;
		}
	    else if (request.getAttribute("ImportButton") != null)
		{
	    	task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task.getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}

			save(task, user, db, request,response);
	    	commitDb(db);
	        return FINISH_FOR_ME;    
		}
	    else if(request.getAttribute("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
			writeToExcel(request, response, "exec spMet_reportEnvironmentalSample", db);

			return ALL_DONE;
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	 
	 protected void save(Task task, User user, Db db,
				HttpServletRequest request, HttpServletResponse response)
		{
			try
			{
				String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
				super.save(task, user, db, request, response);
				//populate the ui with the next sample
				//populateUI(request, task, db);
				populateLocationsView(sample, request, task, db);
			}
			catch(Exception ex)
			{
				// redraw Locations table
				String limsId = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
				task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(limsId);
				locationsView.setStartRow(1);
				RowsetView.addViewToSessionViews(request, locationsView);
				throw (LinxUserException)ex;
			}
		}
	 
//	 protected void importRowsFromFile(String fileId, Task task,
//				User user, Db db, HttpServletRequest request,
//				HttpServletResponse response)
//		{
//			XLSParser fileData = null;
//			// import file
//			// core has already validated file per task def
//			File inFile = this.getFile(fileId, db);
//
//			// create a list of objects while importing manifest
//			//lets get the data from the file and put it into a data container object
//			char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
//			String columnKey = "New LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
//			fileData = new XLSParser(inFile, task.getTaskName(),
//					delim, columnKey, reqColHeaders, true);
//
//			// ready for std save() processing
//			task.setMessage("Successfully imported new samples from bulk import file.");
//			
//			
//			//loop though the file and insert each line
//			String err = "";
//			int row = 1;
//			try
//			{
//				if(fileData.gotoFirst())
//				{
//					do
//					{			
//						String limsId = fileData.getRequiredProperty("New LIMS ID");
//						String collDate = fileData.getRequiredProperty("Collection Date");
//						String pageRef = fileData.getRequiredProperty("Notebook Ref");
//						String physForm = fileData.getRequiredProperty("Physical Form");
//						String location = fileData.getRequiredProperty("Freezer Location");
//						
//						task.getServerItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(limsId);
//						task.getServerItem("CollectionDate").setValue(collDate);
//						task.getServerItem("NotebookRef").setValue(pageRef);
//						task.getServerItem("PhysicalForm").setValue(physForm);
//						task.getServerItem("Location").setValue(location);
//						
//						// call std processing
//						save(task, user, db, request, response);
//						row++;
//					}
//					while(fileData.gotoNext());
//				}//at end we have validated all of the inputs in the file
//				
//			}
//			catch (Exception e)
//			{
//				err += "Error occurred while parsing row " + row + ": " + e.getMessage();
//			}
//			if(!err.equals(""))
//				throw new LinxUserException(err);
//		}
	 
	 protected void populateLocationsView(String sample, HttpServletRequest request, Task task, Db db)
		{
		    // show UI table of locations for selected sample
	  		RowsetView.cleanupSessionViews(request);
	  		task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(sample);
		    //lets also populate the locations for this new sample
			  String sql = "exec spMet_GetSampleLocationsChecked '" + sample + "'";
			  ArrayList<String> alLocations = new ArrayList<String>();
			  String freezerLocSP = "spMet_getCurrentSampleBoxAndPosition";
			  alLocations = reserveFreezerLocations(freezerLocSP, this.getBoxPrefix(db),db);
		   	  ArrayList<String> alFrz = reserveFreezerLocations(freezerLocSP, this.getBackupBoxPrefix(db),db);
		   	  for(String s : alFrz)
		   		  alLocations.add(s);
			  locationsView = super.populateFreezerLocationsView(locationsView, alLocations, rowCount, LOCATION_TABLE, 
						COL_PRINT, COL_LOC, sql, sample, request, task, db);
			  locationsView.setStartRow(1);
			locationsView.setMessage("Showing freezer locations for " + sample);
	  		locationsView.setStartRow(1);
		   	RowsetView.addViewToSessionViews(request, locationsView);

		 }

	 /**
	  * queries the database to retrieve the stored information for a given sample
	  * @param sample
	  * @param spName
	  * @param db
	  */
	 private void retrieveSampleInfo(String sample, String spName, Task task, Db db)
	 {
		 try
		 {
			 ResultSet rs = dbHelper.getResultSet(spName, db);
			 if(task.getTaskName().toLowerCase().indexOf("subsurface") >= 0)
			 {
				retrieveSubsurfaceSample(sample, rs, task, db);
			 }
			 else if (task.getTaskName().toLowerCase().indexOf("plant") >= 0)
			 {
				retrievePlantSample(sample, rs, task, db);
			 }
			 else if (task.getTaskName().toLowerCase().indexOf("composite") >= 0)
			 {
				retrieveCompositeSample(sample, rs, task, db);
			 }
			 else if (task.getTaskName().toLowerCase().indexOf("aquatic") >= 0)
			 {
				retrieveAquaticSample(sample, rs, task, db);
			 }
			 else if (task.getTaskName().toLowerCase().indexOf("animal") >= 0)
			 {
				retrieveAnimalSample(sample, rs, task, db);
			 }
			 
			 rs.close();
			 rs = null;
		 }
		 catch(Exception ex)
		 {
			 throw new LinxUserException(ex.getMessage());
		 }
	 }
	 
	 private void retrieveSubsurfaceSample(String sample, ResultSet rs, Task task, Db db)
	 {
		 try
		 {
			 while(rs.next())
			 {
				 if(rs.getString(2).toLowerCase().indexOf("subsurface") < 0)
				 {
					 throw new Exception("Sample " + sample + " is not a subsurface sample.\r\n" +  
							 "Please choose the " + rs.getString(2) + " sample logging task to retrieve the sample information.");
				 }
				 else
				 {
					 task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(rs.getString(1));
					 task.getDisplayItem("PhysicalForm").setValue(rs.getString(4));
					 task.getDisplayItem("CollectionDate").setValue(rs.getString(5));
					 task.getDisplayItem("NotebookRef").setValue(rs.getString(6));
					 task.getDisplayItem("InternalID").setValue(rs.getString(7));
					 task.getDisplayItem("FieldName").setValue(rs.getString(8));
					 task.getDisplayItem("Description").setValue(rs.getString(9));
					 task.getDisplayItem("Volume_liters").setValue(rs.getString(12));
					 task.getDisplayItem("Weight_grams").setValue(rs.getString(13));
					 task.getDisplayItem("InSituTemp_C").setValue(rs.getString(14));
					 task.getDisplayItem("Temperature_C").setValue(rs.getString(15));
					 task.getDisplayItem("DissolvedOxygen_mg_per_L").setValue(rs.getString(17));
					 task.getDisplayItem("Conductivity_mS_per_m").setValue(rs.getString(18));
					 task.getDisplayItem("Depth_m").setValue(rs.getString(19));
					 task.getDisplayItem("Salinity_ppt").setValue(rs.getString(20));
					 task.getDisplayItem("Latitude").setValue(rs.getString(22));
					 task.getDisplayItem("Longitude").setValue(rs.getString(23));
					 task.getDisplayItem("Altitude_m").setValue(rs.getString(24));
					 task.getDisplayItem("SiteDescription").setValue(rs.getString(25));
					 task.getDisplayItem("StorageMethod").setValue(rs.getString(26));
					 task.getDisplayItem("ClosestTown").setValue(rs.getString(27));
					 task.getDisplayItem("City").setValue(rs.getString(28));
					 task.getDisplayItem("County").setValue(rs.getString(29));
					 task.getDisplayItem("State").setValue(rs.getString(30));
					 task.getDisplayItem("Country").setValue(rs.getString(31));
					 task.getDisplayItem("ArchiveLocation").setValue(rs.getString(32));
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
	 
	 private void retrievePlantSample(String sample, ResultSet rs, Task task, Db db)
	 {
		 try
		 {
			 while(rs.next())
			 {
				 if(rs.getString(2).toLowerCase().indexOf("plant") < 0)
				 {
					 throw new Exception("Sample " + sample + " is not a plant sample.\r\n" +  
							 "Please choose the " + rs.getString(2) + " sample logging task to retrieve the sample information.");
				 }
				 else
				 {
					 task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(rs.getString(1));
					 task.getDisplayItem("PhysicalForm").setValue(rs.getString(4));
					 task.getDisplayItem("CollectionDate").setValue(rs.getString(5));
					 task.getDisplayItem("NotebookRef").setValue(rs.getString(6));
					 task.getDisplayItem("InternalID").setValue(rs.getString(7));
					 task.getDisplayItem("FieldName").setValue(rs.getString(8));
					 task.getDisplayItem("Description").setValue(rs.getString(9));
					 task.getDisplayItem("Cultivar").setValue(rs.getString(21));
					 task.getDisplayItem("Weight_grams").setValue(rs.getString(13));
					 task.getDisplayItem("Latitude").setValue(rs.getString(22));
					 task.getDisplayItem("Longitude").setValue(rs.getString(23));
					 task.getDisplayItem("Altitude_m").setValue(rs.getString(24));
					 task.getDisplayItem("SiteDescription").setValue(rs.getString(25));
					 task.getDisplayItem("StorageMethod").setValue(rs.getString(26));
					 task.getDisplayItem("ClosestTown").setValue(rs.getString(27));
					 task.getDisplayItem("City").setValue(rs.getString(28));
					 task.getDisplayItem("County").setValue(rs.getString(29));
					 task.getDisplayItem("State").setValue(rs.getString(30));
					 task.getDisplayItem("Country").setValue(rs.getString(31));
					 task.getDisplayItem("ArchiveLocation").setValue(rs.getString(32));
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
	 
	 private void retrieveCompositeSample(String sample, ResultSet rs, Task task, Db db)
	 {
		 try
		 {
			 while(rs.next())
			 {
				 if(rs.getString(2).toLowerCase().indexOf("composite") < 0)
				 {
					 throw new Exception("Sample " + sample + " is not a composite sample.\r\n" +  
							 "Please choose the " + rs.getString(2) + " sample logging task to retrieve the sample information.");
				 }
				 else
				 {
					 task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(rs.getString(1));
					 task.getDisplayItem("PhysicalForm").setValue(rs.getString(4));
					 task.getDisplayItem("CollectionDate").setValue(rs.getString(5));
					 task.getDisplayItem("NotebookRef").setValue(rs.getString(6));
					 task.getDisplayItem("InternalID").setValue(rs.getString(7));
					 task.getDisplayItem("FieldName").setValue(rs.getString(8));
					 task.getDisplayItem("Description").setValue(rs.getString(9));
					 task.getDisplayItem("Volume_liters").setValue(rs.getString(12));
					 task.getDisplayItem("Weight_grams").setValue(rs.getString(13));
					 task.getDisplayItem("InSituTemp_C").setValue(rs.getString(14));
					 task.getDisplayItem("Temperature_C").setValue(rs.getString(15));
					 task.getDisplayItem("pH").setValue(rs.getString(16));
					 task.getDisplayItem("Depth_m").setValue(rs.getString(19));
					 task.getDisplayItem("Latitude").setValue(rs.getString(22));
					 task.getDisplayItem("Longitude").setValue(rs.getString(23));
					 task.getDisplayItem("Altitude_m").setValue(rs.getString(24));
					 task.getDisplayItem("SiteDescription").setValue(rs.getString(25));
					 task.getDisplayItem("StorageMethod").setValue(rs.getString(26));
					 task.getDisplayItem("ClosestTown").setValue(rs.getString(27));
					 task.getDisplayItem("City").setValue(rs.getString(28));
					 task.getDisplayItem("County").setValue(rs.getString(29));
					 task.getDisplayItem("State").setValue(rs.getString(30));
					 task.getDisplayItem("Country").setValue(rs.getString(31));
					 task.getDisplayItem("ArchiveLocation").setValue(rs.getString(32));
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
	 
	 private void retrieveAquaticSample(String sample, ResultSet rs, Task task, Db db)
	 {
		 try
		 {
			 while(rs.next())
			 {
				 if(rs.getString(2).toLowerCase().indexOf("aquatic") < 0)
				 {
					 throw new Exception("Sample " + sample + " is not an aquatic sample.\r\n" +  
							 "Please choose the " + rs.getString(2) + " sample logging task to retrieve the sample information.");
				 }
				 else
				 {
					 task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(rs.getString(1));
					 task.getDisplayItem("PhysicalForm").setValue(rs.getString(4));
					 task.getDisplayItem("CollectionDate").setValue(rs.getString(5));
					 task.getDisplayItem("NotebookRef").setValue(rs.getString(6));
					 task.getDisplayItem("InternalID").setValue(rs.getString(7));
					 task.getDisplayItem("FieldName").setValue(rs.getString(8));
					 task.getDisplayItem("Description").setValue(rs.getString(9));
					 task.getDisplayItem("Volume_liters").setValue(rs.getString(12));
					 task.getDisplayItem("Weight_grams").setValue(rs.getString(13));
					 task.getDisplayItem("pH").setValue(rs.getString(16));
					 task.getDisplayItem("Temperature_C").setValue(rs.getString(15));
					 task.getDisplayItem("DissolvedOxygen_mg_per_L").setValue(rs.getString(17));
					 task.getDisplayItem("Conductivity_mS_per_m").setValue(rs.getString(18));
					 task.getDisplayItem("Depth_m").setValue(rs.getString(19));
					 task.getDisplayItem("Salinity_ppt").setValue(rs.getString(20));
					 task.getDisplayItem("Latitude").setValue(rs.getString(22));
					 task.getDisplayItem("Longitude").setValue(rs.getString(23));
					 task.getDisplayItem("Altitude_m").setValue(rs.getString(24));
					 task.getDisplayItem("SiteDescription").setValue(rs.getString(25));
					 task.getDisplayItem("StorageMethod").setValue(rs.getString(26));
					 task.getDisplayItem("ClosestTown").setValue(rs.getString(27));
					 task.getDisplayItem("City").setValue(rs.getString(28));
					 task.getDisplayItem("County").setValue(rs.getString(29));
					 task.getDisplayItem("State").setValue(rs.getString(30));
					 task.getDisplayItem("Country").setValue(rs.getString(31));
					 task.getDisplayItem("ArchiveLocation").setValue(rs.getString(32));
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
	 
	 private void retrieveAnimalSample(String sample, ResultSet rs, Task task, Db db)
	 {
		 try
		 {
			 while(rs.next())
			 {
				 if(rs.getString(2).toLowerCase().indexOf("animal") < 0)
				 {
					 throw new Exception("Sample " + sample + " is not an animal sample.\r\n" +  
							 "Please choose the " + rs.getString(2) + " sample logging task to retrieve the sample information.");
				 }
				 else
				 {
					 task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(rs.getString(1));
					 task.getDisplayItem("PhysicalForm").setValue(rs.getString(4));
					 task.getDisplayItem("CollectionDate").setValue(rs.getString(5));
					 task.getDisplayItem("NotebookRef").setValue(rs.getString(6));
					 task.getDisplayItem("InternalID").setValue(rs.getString(7));
					 task.getDisplayItem("FieldName").setValue(rs.getString(8));
					 task.getDisplayItem("Description").setValue(rs.getString(9));
					 task.getDisplayItem("Volume_liters").setValue(rs.getString(12));
					 task.getDisplayItem("Weight_grams").setValue(rs.getString(13));
					 task.getDisplayItem("Taxonomy").setValue(rs.getString(10));
					 task.getDisplayItem("Temperature_C").setValue(rs.getString(15));
					 task.getDisplayItem("Tissue").setValue(rs.getString(11));
					 task.getDisplayItem("pH").setValue(rs.getString(16));
					 task.getDisplayItem("Latitude").setValue(rs.getString(22));
					 task.getDisplayItem("Longitude").setValue(rs.getString(23));
					 task.getDisplayItem("Altitude_m").setValue(rs.getString(24));
					 task.getDisplayItem("SiteDescription").setValue(rs.getString(25));
					 task.getDisplayItem("StorageMethod").setValue(rs.getString(26));
					 task.getDisplayItem("ClosestTown").setValue(rs.getString(27));
					 task.getDisplayItem("City").setValue(rs.getString(28));
					 task.getDisplayItem("County").setValue(rs.getString(29));
					 task.getDisplayItem("State").setValue(rs.getString(30));
					 task.getDisplayItem("Country").setValue(rs.getString(31));
					 task.getDisplayItem("ArchiveLocation").setValue(rs.getString(32)); 
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
}
