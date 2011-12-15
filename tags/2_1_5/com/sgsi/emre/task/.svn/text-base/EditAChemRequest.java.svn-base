package com.sgsi.emre.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.AChemParser;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * 
 * @author TJS
 * @modified 2/2011 v1.15 to prevent edit of request with updated results
 */
public class EditAChemRequest extends EMRETask 
{
	XLSParser fileData = null;
	private ArrayList<String> alItemTypes = new ArrayList<String>();
	//private String itemType = null;
	//as of 09/2009 they no longer send over the itemtype so we need to find it at runtime
	//as of 11/2009 reinstate the itemtype in the file
	
	/**
	 * Overridden to parse incoming work request file
	 * for its work request ID and to load into
	 * an importer for use in building a bulk insert file 
	 * at doTaskWorkPostSave().
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		//createAnyNewAppFiles(request, response, user, db);
		
		// import file
		String fileId = getServerItemValue(FileType.ACHEM_REQUEST_FILE);
		File inFile = this.getFile(fileId, db);
		
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "Submission ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredFileHeaders);
		
		//lets get the request name (alphanumeric, e.g. GC100002)
		// -- user may generate next request ID  task screen to copy into file, 
		// -- but doesn't have to use it, so take what's in file vs value on screen
		// NOTE: DOES rely on "Request type" selection by user in screen
		String requestId = (String)fileData.getInlineProperty("Request ID");
		getServerItem(ItemType.ACHEM_REQUEST).setValue(requestId);

		if(dbHelper.isItemOnQueue(requestId, ItemType.ACHEM_REQUEST, 
				"Updated A-Chem Results", db)
				|| dbHelper.isItemOnQueue(requestId, ItemType.ACHEM_REQUEST, 
						"Updated A-Chem Request", db))
		{
			if(isUserOnAdminList(user.getName(), db) == false)
			{
				throw new LinxUserException("Because results have already been uploaded"
					+ " for Request ID " + requestId + ","
					+ " please contact A-Chem team to edit sample information.");
			}
		}
		
		//lets get the method - if FAME we have special processing
		String requestMethod = (String)fileData.getInlineProperty("Analysis method");
		if(requestMethod.equalsIgnoreCase("analysis method"))
			throw new LinxUserException("Please choose an analysis method and try again.");
		if(requestMethod.equalsIgnoreCase("GC-FAME"))
		{
			SubmitAChemRequest thisTask = new SubmitAChemRequest();
			inFile = thisTask.processFAMERequest(inFile, fileData);
		}
		//lets parse the file once more to make sure it's formatted correctly
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredFileHeaders);
		
		inFile = null;
		//as of 09/2010 the lims id type is in the data section of the file so validate on a per row basis
		//String sampIdType = (String)fileData.getInlineProperty("LIMS ID type");
		//if(sampIdType.equalsIgnoreCase("LIMS ID types"))
		//	throw new LinxUserException("Please select a LIMS ID type from the drop down in the file and try again.");
		
		//lets see if we are supposed to have an existing LIMS ID
		//if lims tracking is set to yes then the lims ids have to exist in the db.
		String limsTracking = (String)fileData.getInlineProperty("LIMS Tracking");
		//get the list of valid item types
		alItemTypes = dbHelper.getListEntries("exec spLinx_getAppValueTypeByParentType 'Achem Lims Id Type'", db);
		
		if(limsTracking.equalsIgnoreCase("yes"))
		{
			//ok, we know that all of the LIMS IDs need to exist and be of the correct type
			validateIds(fileData, alItemTypes, db);
		}
		else
		{
			//lims ids can be new but notebook page must exist
			String ntbkPg = (String)fileData.getInlineProperty("Notebook Page");
			if(WtUtils.isNullOrBlank(ntbkPg))
				throw new LinxUserException("Please fill out the Notebook Page and then submit the request again.");
			//also if any of the rows contain a LIMS ID Type of 'StrainCulture' or 'ExperimentalCulture'
			//then Lims tracking needs to be set to "yes"
			validateCultureType(fileData);
		}

		setMessage("Successfully submitted changes to Analytical Chemistry work request " + requestId + "."
				+ " Please make a note of request ID for retrieving your results later.");
		}


	/**
	 * Returns true if the user is on the admin control ist
	 * maintained in APPVALUES table. Use Manage Settings
	 * to update user permissions to edit A-Chem requests
	 * that have already had results uploaded, eff v1.15.
	 * @param name
	 * @return
	 */
	protected boolean isUserOnAdminList(String name, Db db) 
	{
		ArrayList<String> param = new ArrayList<String>();
		param.add(name);
		String b;
		b = dbHelper.getDbValueFromStoredProc(db, "spEMRE_isAChemAdminUser", name);
		return Boolean.parseBoolean(b);
	}



	/**
	 * Overridden to update WORKREQUESTDETAIL custom table
	 * with contents of work request file, i.e., submissions 
	 * for a-chem analysis.
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		updateAppFilesWithAppliesTo(request, response, user, db);
		if(fileData == null)
		{
			throw new LinxSystemException("File data is null.");
		}
		updateCustomTables(fileData, getServerItemValue(ItemType.ACHEM_REQUEST), db);
		fileData = null;
	}



	/**
	 * Overridden to update WORKREQUESTDETAIL custom table
	 * with contents of work request file, i.e., submissions 
	 * for a-chem analysis.
	 * @param fileData
	 * @param workRequest
	 * @param db
	 */
	public void updateCustomTables(XLSParser fileData, String workRequest, Db db)
	{
		// Excel being a pain again and reformatting dates as sci notation
		String requestDate = (String)fileData.getInlineProperty("Request date");
		if(requestDate.endsWith("E7"))
		{
			Double dbl = Double.parseDouble(requestDate);
			requestDate = String.valueOf(dbl.intValue());
		}
		String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		if(!requestDate.startsWith(year)
				|| requestDate.length() != "YYYYMMDD".length())
		{
			throw new LinxUserException("[Request date] "
					+ " is not in the expected format 'YYYYMMDD'."
					+ " Please correct the worksheet, then try again.");
		}
		/*String date = WtUtils.formatDateForDb(requestDate.substring(1,3)
		                            + "/" + requestDate.substring(3)
		                            + "/0" + requestDate.substring(0,1));
			*/			
		String requestMethod = (String)fileData.getInlineProperty("Analysis method");
		if(requestMethod.equalsIgnoreCase("Analysis Methods"))
			throw new LinxUserException("Please select an analysis method from the dropdown in the file and try again.");
		String requestType = requestMethod.substring(0,requestMethod.indexOf('-'));
		if(!workRequest.startsWith(requestType))
		{
			throw new LinxUserException("Request ID [" + workRequest + "]"
					+ " for " + requestMethod + " analysis should start with '" + requestType + "'."
					+ " Please correct the Request ID (or Analysis method) in the worksheet, then try again.");
		}
		String project = (String)fileData.getInlineProperty("Project");
		if(project.equalsIgnoreCase("projects"))
			throw new LinxUserException("Please select a project from the drop down in the file and try again.");
		//lets update the workRequest custom table (may be no changes)
		dbHelper.updateEditedWorkRequest(workRequest, 
				requestDate,
				(String)fileData.getInlineProperty("Requester"),
				(String)fileData.getInlineProperty("Sample description"),
				getServerItemValue(EMREStrings.FileType.ACHEM_REQUEST_FILE),
				(String)fileData.getOptionalInlineProperty("Comments"), 
				project,
				(String)fileData.getOptionalInlineProperty("Notebook Page"),
				(String)fileData.getInlineProperty("Results Email Address"),
				getTranId(), db);

		// Lets bulk update column data (may be no changes)
		String spName = "spMet_bulkUpdateEditedWorkRequestDetail";
		StringBuffer sb = buildBulkInsertData(fileData, workRequest, db);

		try
		{
			bulkInsert(sb, spName, getTranId(), db);
		}
		catch (Exception e)
		{
			throw new LinxUserException("While attempting bulk insert: " + e.getMessage());
		}
		sb.setLength(0);
		sb = null;
		
		ArrayList<String> params = new ArrayList<String>();
		//as of 10/2010 we need to store Sampling Timepoint in the samplingTimepoint table
		//loop through the file to insert the timepoint
		try
		{
			if(fileData.gotoFirst())
			{
				do
				{
					String itemType = fileData.getRequiredProperty("LIMS ID Type");
					if(!itemType.equalsIgnoreCase("StrainCulture") 
							&& !itemType.equalsIgnoreCase("ExperimentalCulture"))
					{
						continue;
					}
					//if we're here we have some sort of culture - insert the timepoint yo!
					//we need the cultureCollectionId timepoint and tranid
					String timepoint = fileData.getRequiredProperty("Sampling Timepoint");
					String culture = fileData.getRequiredProperty("LIMS ID");
					//does the timepoint exist?
					String samplingTimepointId = dbHelper.getDbValue(
							"exec spEMRE_getSamplingTimepointId '" + 
							culture + "','" + timepoint + "'", db);
					//if not, add it
					if(WtUtils.isNullOrBlank(samplingTimepointId))
					{
						int sn = getNextSerialNumberByCulture(culture, itemType, db);
						sn++;
						params.clear();
						params.add(culture + "_" + sn);
						params.add(culture);
						params.add(timepoint);
						params.add(getTranId()+"");
						dbHelper.callStoredProc(db, "spEMRE_insertSamplingTimepoint", params, false, true);
					}

					
				}
				while(fileData.gotoNext());
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Error occurred while trying to save sampling timepoints: " + ex.getMessage());
		}

	}
		

	private StringBuffer buildBulkInsertData(XLSParser data, String workRequestName,
			Db db)
	{
		StringBuffer sb = null;
		int row = 1;
			try
			{
				sb = new StringBuffer();
				String CRLF = EMREStrings.CHAR.CRLF;
				char delim = EMREStrings.CHAR.SEMI_COLON;
				if(data.gotoFirst())
				{
					do
					{				
						String line = workRequestName + delim
									+ data.getRequiredProperty("LIMS ID") + delim
									+ data.getRequiredProperty("Submission ID") + delim
									+ data.getProperty("Sampling Timepoint") + delim
									+ data.getProperty("Dilution") + delim
									+ data.getProperty("Comment") + delim
									+ data.getRequiredProperty("LIMS ID Type")
									//+ itemType
									+ EMREStrings.CHAR.CRLF;
						sb.append(line);
						    row++;
					}
					while(data.gotoNext());
				}//at end we have validated all of the inputs in the file
			}
			catch (Exception e)
			{
				throw new LinxUserException("While parsing row " + row + ": " + e.getMessage());
			}

		return sb;
	}

}
