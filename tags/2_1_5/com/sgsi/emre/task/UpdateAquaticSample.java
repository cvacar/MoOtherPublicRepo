package com.sgsi.emre.task;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateAquaticSample extends UpdateEnvironmentalSample 
{
	public static final String[] reqColHeaders = new String[]{
  		"LIMS ID","Internal ID","Field Name","Description","Volume (L)",
  		"Weight (g)","Temperature (degrees C)","pH","Dissolved Oxygen (mg/L)",
  		"Conductivity (mS/m)","Depth (m)","Salinity (ppt)","Latitude","Longitude",
  		"Altitude (m)","Site Description","Storage Method","Closest Town",
  		"City","County","State","Country","Archive Location","Comment"};
	
	@Override
	  public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	  {
		  setMessage("Successfully logged new sample(s)." );
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
			String columnKey = "LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
			fileData = new XLSParser(inFile, getTaskName(),
					delim, columnKey, reqColHeaders, true);
			ArrayList<String> samples = new ArrayList<String>();
			try
			{
				if(fileData.gotoFirst())
				{
					do
					{
						String samp = (String)fileData.getRequiredProperty("LIMS ID");
						if(samples.contains(samp))
							throw new LinxUserException("Sample '" + samp + "' is duplicated in the file.");
						else
							samples.add(samp);
					}
					while(fileData.gotoNext());
				}
				else
					throw new LinxUserException("The import file contains no data rows.");
			}
			catch(Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}
			getServerItem(ItemType.ENVIRONMENTAL_SAMPLE).setValues(samples);
			// ready for std save() processing
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
	
	private void updateCustomTablesFromFile(File file, Db db)
	{
		try
		{
			char delim = ';';
			String columnKey = "LIMS ID";
			XLSParser fileData = new XLSParser(file, getTaskName(),
					delim, columnKey, reqColHeaders, true);
			
			if(fileData.gotoFirst())
			{
				do
				{		
					//insert the row of data
					ArrayList<String> params = new ArrayList<String>();
					String limsId = fileData.getRequiredProperty("LIMS ID");
					params.add(limsId);		
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
					
					String sample = getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
				    String sampleType = dbHelper.getSampleType(sample, db);
				    if(!WtUtils.isNullOrBlank(sampleType))
				    {
				    	  if(this.getTaskName().replace(" ", "").indexOf(sampleType) < 0)
					    	  throw new LinxUserException("Sample '" + sample + "' is of type '" 
					    			  + sampleType + "'.\r\nPlease select the correct Update Environmental Sample task and try again.");
					     
				    }
				    else
				    	  throw new LinxUserException("Unknown sample type.  Please enter the appropriate sample type and try again.");
				        
					String sql = "spEMRE_updateEnvironmentalSample_" 
						+ sampleType.replace(" ", "");
					db.getHelper().callStoredProc(db, sql, params, false, true);
					params.clear();
					params = new ArrayList<String>();
				}
				while(fileData.gotoNext());
			}//at end we have validated all of the inputs in the file
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	
	public void setDisplayItemValues(String sample, Task task, 
			  User user, HttpServletRequest request, Db db)
		{
			// set values for the search item
			ArrayList<String> params = new ArrayList<String>();
			params.add(sample);

			try
			{
				// retrieves newest comments first
				ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
						"spEMRE_getAquaticSampleData", params, true);
				int numRows = 0;
				while (rs.next())
				{
					numRows++;
					task.getDisplayItem("InternalID").setValue(rs.getString(2));
					task.getDisplayItem("FieldName").setValue(
							rs.getString(3));
					task.getDisplayItem("Description").setValue(
							rs.getString(4));
					task.getDisplayItem("Volume_liters").setValue(rs.getString(5));
					task.getDisplayItem("Weight_grams").setValue(rs.getString(6));
					task.getDisplayItem("Temperature_C").setValue(rs.getString(7));
					task.getDisplayItem("pH").setValue(rs.getString(8));
					task.getDisplayItem("DissolvedOxygen_mg_per_L").setValue(rs.getString(9));
					task.getDisplayItem("Conductivity_mS_per_m").setValue(rs.getString(10));
					task.getDisplayItem("Depth_m").setValue(rs.getString(11));
					task.getDisplayItem("Salinity_ppt").setValue(rs.getString(12));
					task.getDisplayItem("Latitude").setValue(rs.getString(13));
					task.getDisplayItem("Longitude").setValue(rs.getString(14));
					task.getDisplayItem("Altitude_m").setValue(rs.getString(15));
					task.getDisplayItem("SiteDescription").setValue(rs.getString(16));
					task.getDisplayItem("StorageMethod").setValue(rs.getString(17));
					task.getDisplayItem("ClosestTown").setValue(rs.getString(18));
					task.getDisplayItem("City").setValue(rs.getString(19));
					task.getDisplayItem("County").setValue(rs.getString(20));
					task.getDisplayItem("State").setValue(rs.getString(21));
					task.getDisplayItem("Country").setValue(rs.getString(22));
					task.getDisplayItem("ArchiveLocation").setValue(rs.getString(23));
					task.getDisplayItem("Comment").setValue(rs.getString(24));
					break;

				}// expecting only one row back, 
				rs.close();
				rs = null;
				// at exit, have set UI properties for this sample, if known
				if(numRows == 0)
					throw new LinxUserException("There were no rows returned from the database.\r\n" + 
							"Please ensure that you have an aquatic sample and try again.");
			}
			catch (SQLException e)
			{
				throw new LinxSystemException("While retrieving aquatic sample properties: "
						+ e.getMessage());
			}
		}
}
