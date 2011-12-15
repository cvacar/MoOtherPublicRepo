package com.sgsi.emre.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class LogAquaticSample extends LogEnvironmentalSample 
{
	private String ROWSET = "Location";
	private int COLUMN_LOCATION = 2;
	public static final String[] reqColHeaders = new String[]{
  		"New LIMS ID","Physical Form","Collection Date","Notebook Ref","Internal ID",
  		"Field Name","Description","Volume (L)","Weight (g)","Temperature (degrees C)",
  		"pH","Dissolved Oxygen (mg/L)","Conductivity (mS/m)","Depth (m)","Salinity (ppt)",
  		"Latitude","Longitude","Altitude (m)","Site Description","Storage Method",
  		"Closest Town","City","County","State","Country","Archive Location",
  		"Location 1 [Freezer:Box:Position]","Location 2 [Freezer:Box:Position]",
  		"Location 3 [Freezer:Box:Position]","Location 4 [Freezer:Box:Position]","Comment"};
	
	private String[] ayPhysicalForm = new String[]{"Liquid","Solid"};

	@Override
	  public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	  {
		  String item = getServerItemValue(itemType);
		  setMessage("Successfully logged new sample " + item + 
		  ".  Click 'Print Labels' to print or click on task name to autogenerate new IDs." );
		  XLSParser fileData = null;
		  String fileId = getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);
		  if(!WtUtils.isNullOrBlank(fileId))
		  {
			// import file
			// core has already validated file per task def
			File inFile = this.getFile(fileId, db);

			
			// create a list of objects while importing manifest
			//lets get the data from the file and put it into a data container object
			char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
			String columnKey = "New LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
			fileData = new XLSParser(inFile, getTaskName(),
					delim, columnKey, reqColHeaders, true);
			ArrayList<String> samples = new ArrayList<String>();
			try
			{
				if(fileData.gotoFirst())
				{
					do
					{
						String samp = (String)fileData.getRequiredProperty("New LIMS ID");
						if(samples.contains(samp))
						{
							throw new LinxUserException("Sample '" + samp + "' is duplicated in the file.");
						}
						else
							samples.add(samp);
						
						//validate the physical form
						String physicalForm = (String)fileData.getRequiredProperty("Physical Form");
						boolean bCorrectPhysForm = false;
						for(String s: ayPhysicalForm)
						{
							if(physicalForm.equalsIgnoreCase(s))
								bCorrectPhysForm = true;
						}
						if(!bCorrectPhysForm)
						{
							throw new LinxUserException("Invalid Physical Form.  Please see the dropdown on the web for valid entries.");
						}
					}
					while(fileData.gotoNext());
				}
				else
					throw new LinxUserException("The import file contains no data rows.");
			}
			catch(Exception ex)
			{
				inFile.delete(); 
				throw new LinxUserException(ex.getMessage());
			}
			getServerItem(ItemType.ENVIRONMENTAL_SAMPLE).setValues(samples);
			// ready for std save() processing
		  }
		  else
		  {
			//we have a screen input.
			  //make sure CollectionDate, notebook, physical form, and location are present
			  String collectionDate = getServerItemValue("CollectionDate");
			  if(WtUtils.isNullOrBlank(collectionDate))
				  throw new LinxUserException("Please provide a value for required item 'Collection Date'");
			  String notebook = getServerItemValue("NotebookRef");
			  if(WtUtils.isNullOrBlank(notebook))
				  throw new LinxUserException("Please provide a value for required item 'Page Ref [notebook-pg]'");
			  String physForm = getServerItemValue("PhysicalForm");
			  if(WtUtils.isNullOrBlank(physForm))
				  throw new LinxUserException("Please provide a value for required item 'PhysicalForm'");
		  }
		  
	  }
	
	/**
	 * Overridden to update custom tables with 
	 * properties of new samples.
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		updateAppFilesWithAppliesTo(request, response, user, db);
		String fileId = getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);
		if (!WtUtils.isNullOrBlank(fileId))
		{
			File file = getFile(fileId, db);
			updateCustomTablesFromFile(file, db);
		}
		else
			updateCustomTables(request, db);
	}

	/**
	 * 
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		
		//iterate through the display items and add to list to sent to SP
		 List ditems = this.getDisplayItems();
	     ListIterator itor = ditems.listIterator();
	      while(itor.hasNext())
	      {
	          DisplayItem ditem = (DisplayItem)itor.next();
	          if(ditem.getWidget().equals(LinxConfig.WIDGET.BUTTON)
	        	  || ditem.getWidget().equals(LinxConfig.WIDGET.FILE_SUBMIT)
	              || ditem.getWidget().equalsIgnoreCase("SAVEBUTTON")
	              || ditem.getWidget().equalsIgnoreCase("VERIFYBUTTON")
	              || ditem.getWidget().equals("rowsets")
	              || ditem.getItemType().indexOf("Placeholder") > -1)
	          {
	              // skip buttons and UI tables
	              continue;
	          }
	          if(WtUtils.isNullOrBlankOrPlaceholder(ditem.getValue())){
	        	  params.add(null);
	          }
	          else
	        	  params.add(ditem.getValue());
	      }// next displayItem
	      // at exit, have added new data to appliesToItem
	      params.add(getTranId() + "");
	      String sample = getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
	      String exists = "false";
	      if(dbHelper.isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
	      {
	    	  exists = "true";
	      }
	      params.add(exists);
	      try
			{
				String sql = "spMet_InsertEnvironmentalSample_" 
									+ getTaskName().replace(" ", "");
				db.getHelper().callStoredProc(db, 
						sql, params, false, true);
				sql = ""; 
			}
			catch (Exception e)
			{
				throw new LinxUserException(e.getMessage());
			}
			
			//now that we've inserted into sample lets insert the locations
			 TableDataMap rowMap = new TableDataMap(request, ROWSET);
		      int numRows = rowMap.getRowcount();
		      //expecting 4 locations - lets check
		      if(numRows != 4)
		    	  throw new LinxUserException("Expecting four freezer locations and found '" + numRows + "'");
		      int idx = 0;
			  for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
			  {
				String location = (String)rowMap.getValue(rowIdx, COLUMN_LOCATION);
				idx++;
				String[] aLocs = location.split(":");
				String freezer = aLocs[0];
				String box = aLocs[1];
				String position = aLocs[2];
				aLocs = null;
				params.clear();
				params.add(sample);
				params.add(freezer); 
				params.add(box);
				params.add(position);
				params.add(idx+""); //location index
				params.add(getTranId()+"");
				  
				String sql = "spMet_InsertOrUpdateSampleLocation";
				dbHelper.callStoredProc(db, sql, params, false, true);
			}
	  }
	
	private void updateCustomTablesFromFile(File file, Db db)
	{
		try
		{
			char delim = ';';
			String columnKey = "New LIMS ID";
			XLSParser fileData = new XLSParser(file, getTaskName(),
					delim, columnKey, reqColHeaders, true);
			
			if(fileData.gotoFirst())
			{
				do
				{		
					//insert the row of data
					ArrayList<String> params = new ArrayList<String>();
					String limsId = fileData.getRequiredProperty("New LIMS ID");
					params.add(limsId);		
					params.add(fileData.getRequiredProperty("Physical Form"));
					params.add(fileData.getRequiredProperty("Collection Date"));
					params.add(fileData.getRequiredProperty("Notebook Ref"));
					params.add(fileData.getProperty("Internal ID"));
					params.add(fileData.getProperty("Field Name"));
					params.add(fileData.getProperty("Description"));
					params.add(fileData.getProperty("Volume (L)"));
					params.add(fileData.getProperty("Weight (g)"));
					params.add(fileData.getProperty("Temperature (degrees C)"));
					params.add(fileData.getProperty("pH"));
					params.add(fileData.getProperty("Dissolved Oxygen (mg/L)"));
					params.add(fileData.getProperty("Conductivity (mS/m)"));
					params.add(fileData.getProperty("Depth (m)"));
					params.add(fileData.getProperty("Salinity (ppt)"));
					params.add(fileData.getProperty("Latitude"));
					params.add(fileData.getProperty("Longitude"));
					params.add(fileData.getProperty("Altitude (m)"));
					params.add(fileData.getProperty("Site Description"));
					params.add(fileData.getProperty("Storage Method"));
					params.add(fileData.getProperty("Closest Town"));
					params.add(fileData.getProperty("City"));
					params.add(fileData.getProperty("County"));
					params.add(fileData.getProperty("State"));
					params.add(fileData.getProperty("Country"));
					params.add(fileData.getProperty("Archive Location"));
					params.add(fileData.getProperty("Comment"));
					params.add(getTranId() + "");
					
					String exists = "false";
				      if(dbHelper.isItemExisting(limsId, ItemType.ENVIRONMENTAL_SAMPLE, db))
				      {
				    	  exists = "true";
				      }
					params.add(exists);
					String sql = "spMet_InsertEnvironmentalSample_" 
						+ getTaskName().replace(" ", "");
					db.getHelper().callStoredProc(db, sql, params, false, true);
					params.clear();
					params = new ArrayList<String>();
					//now add freezer locations
					//location 1
					String location = fileData.getRequiredProperty("Location 1 [Freezer:Box:Position]");
					int idx = 1;
					insertFreezerLocation(location, limsId, idx, db);
					
					//location 2
					location = fileData.getRequiredProperty("Location 2 [Freezer:Box:Position]");
					idx = 2;
					insertFreezerLocation(location, limsId, idx, db);
					//location3
					location = fileData.getRequiredProperty("Location 3 [Freezer:Box:Position]");
					idx = 3;
					insertFreezerLocation(location, limsId, idx, db);
					//location4
					location = fileData.getRequiredProperty("Location 4 [Freezer:Box:Position]");
					idx = 4;
					insertFreezerLocation(location, limsId, idx, db);
					
				}
				while(fileData.gotoNext());
			}//at end we have validated all of the inputs in the file
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	private void insertFreezerLocation(String location, String limsID, int index, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			String[] aLocs = location.split(":");
			String freezer = aLocs[0];
			String box = aLocs[1];
			String position = aLocs[2];
			aLocs = null;
			params.clear();
			params.add(limsID);
			params.add(freezer); 
			params.add(box);
			params.add(position);
			params.add(index+""); //location index
			params.add(getTranId()+"");
			  
			String sql = "spMet_InsertOrUpdateSampleLocation";
			dbHelper.callStoredProc(db, sql, params, false, true);
			params = null;
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
}
