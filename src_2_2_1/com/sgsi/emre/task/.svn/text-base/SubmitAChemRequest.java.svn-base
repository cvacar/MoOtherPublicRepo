package com.sgsi.emre.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.AChemParser;
import com.sgsi.emre.bean.XLSParser;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Workflow;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.util.alert.Alert;

/**
 * 
 * Modified to distinguish TOC expts and allow
 * authorized TOC users to run own expts without
 * alerting A-Chem staff.
 * @modified TJS/Wildtype for SGI
 * @date 2/2011
 * @version 1.15
 *
 */
public class SubmitAChemRequest extends EMRETask 
{
	private XLSParser fileData = null;
	private ArrayList<String> alItemTypes = new ArrayList<String>();
	//private String itemType = null;
	//as of 09/2009 they no longer send over the itemtype so we need to find it at runtime
	
	/**
	 * Overridden to parse incoming work request file
	 * for its work request ID and to load into
	 * an importer for use in building a bulk insert file 
	 * at doTaskWorkPostSave().
	 * Eff v1.15, checks if TOC requester is allowed to self-run TOC analysis
	 * and rejects request if [x] Self-Run checkbox is checked but user
	 * is not allowed on task 'Update TOC Results'.
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		//itemType = null;
		//createAnyNewAppFiles(request, response, user, db);
		
		// import file
		String fileId = getServerItemValue(FileType.ACHEM_REQUEST_FILE);
		if(WtUtils.isNullOrBlank(fileId))
		{		
			throw new LinxUserException("Please browse for a request worksheet, then try again.");
		}
		File inFile = this.getFile(fileId, db);
		
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "Submission ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredSubmittedColumnHeaders);
		
		//lets get the method - if FAME we have special processing
		String requestType = getServerItemValue("RequestType");
		String requestMethod = (String)fileData.getInlineProperty("Analysis method");
		if(requestMethod.equalsIgnoreCase("analysis method"))
		{
			inFile.delete(); // don't force user to rename failed files
			throw new LinxUserException(
					"Please choose an analysis method in the request worksheet,"
					+ " then re-submit.");
		}
		if(requestMethod.equalsIgnoreCase("GC-FAME"))
		{
			inFile = processFAMERequest(inFile, fileData);
		}
		// did a requester check the to-be-self-run checkbox?
		// -- only TOC requesters with permissions on Update TOC Results (checked later)
		// -- are allowed to check this box
		else if(!requestType.equalsIgnoreCase("TOC")
			&& request.getAttribute("TOCSelfRun") != null)
		{
			inFile.delete();
			throw new LinxUserException("The checkbox is reserved for TOC requests" +
					" and has been cleared. Please resubmit.");
		}

		//lets parse the file once more to make sure it's formatted correctly
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredSubmittedColumnHeaders);

		//if fileDate is null that means we couldn't find submission ids - complain
		if(fileData.rows.size() < 1)
		{
			inFile.delete();
			throw new LinxUserException("Could not find any data rows in file. "
				+ "Perhaps 'Submission IDs' are missing.");
		}
		//as of 09/2010 the lims id type is in the data section of the file so validate on a per row basis
//		String sampIdType = (String)fileData.getInlineProperty("LIMS ID type");
//		if(sampIdType.equalsIgnoreCase("LIMS ID types"))
//			throw new LinxUserException("Please select a LIMS ID type from the drop down in the file and try again.");
//		
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
		else if(limsTracking.equalsIgnoreCase("no"))
		{
			//lims ids can be new but notebook page must exist
			String ntbkPg = (String)fileData.getInlineProperty("Notebook Page");
			if(WtUtils.isNullOrBlank(ntbkPg))
			{
				inFile.delete();
				throw new LinxUserException(
						"Please fill out the Notebook Page in the request worksheet," +
						" then re-submit.");
			}
			//also if any of the rows contain a LIMS ID Type of 'StrainCulture' or 'ExperimentalCulture'
			//then Lims tracking needs to be set to "yes"
			validateCultureType(fileData);
		}
		else
		{
			inFile.delete();
			throw new LinxUserException("Please set LIMS Tracking to either 'yes' or 'no'" +
					" in the request worksheet, then re-submit.");
		}
		//lets get the request name (alphanumeric, e.g. GC100002)
		// -- user may generate next request ID  task screen to copy into file, 
		// -- but doesn't have to use it, so take what's in file vs value on screen
		// NOTE: DOES rely on "Request type" selection by user in screen
		String workRequest = (String)fileData.getInlineProperty("Request ID");
		getServerItem(ItemType.ACHEM_REQUEST).setValue(workRequest.trim());
		
		setMessage("Successfully submitted new Analytical Chemistry work request " + workRequest + "."
				+ " Please make a note of request ID for retrieving your results later.");
		
		}
	
	
	/**
	 * Overridden to queue request based on type of analysis
	 * (GC, HPLC, IC, etc.). Sample and Submission queueing
	 * is performed in custom stored proc spMet_bulkInsertWorkRequestDetail.
	 * @modified v1.15 by tjs/Wt to handle self-run TOC requests
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
		public void queueItems(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		super.queueItems(request, response, user, db);
		//queue request based upon analysis type
		// eff v1.15, allow TOC users to self-run analysis
		// -- (if 'Will self-run' checkbox is checked, queue to a self-run q)
		String requestType = getServerItemValue("RequestType");
		// did a TOC requester check the to-be-self-run checkbox?
		if(requestType.equalsIgnoreCase("TOC")
			&& request.getAttribute("TOCSelfRun") != null)
		{
			// confirm that requester may self-run
			if(!user.hasRight("Update TOC Results", null, (Workflow)request.getSession().getAttribute("workflow")))
			{
				throw new LinxUserException("Requester [" + user.getName()
						+ "] does not have permission"
						+ " to run later task 'Update TOC Results'."
						+ " The checkbox 'TOC to be completed by requester'"
						+ " on the task screen has been cleared." 
						+ " If you need this option, alert your LIMS administrator"
						+ " PRIOR to submitting request.");		
			}
			// at exit, user has been confirmed to have permission to self-run
			// queue to special 'self-run' queue
			dbHelper.queueItem(getServerItemValue(ItemType.ACHEM_REQUEST), ItemType.ACHEM_REQUEST, 
				"Pending Requester Run " + requestType.toUpperCase() + " Analysis", 
				getTaskName(), getTranId(), db);
				
		}
		else // not a TOC self-run request; queue normally
		{
			dbHelper.queueItem(getServerItemValue(ItemType.ACHEM_REQUEST), ItemType.ACHEM_REQUEST, 
				"Pending " + requestType.toUpperCase() + " Analysis", 
				getTaskName(), getTranId(), db);
		}

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
		String requestId = getServerItemValue(ItemType.ACHEM_REQUEST);
		updateCustomTables(fileData, requestId , db);
		
		// did a TOC requester check the to-be-self-run checkbox?
		if(request.getAttribute("TOCSelfRun") == null)
		{
			//lets send an email to the analytical chem group 
			//so they know they have a new file waiting to be processed
			sendEmail(fileData, requestId, db);
		}
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
					+ " Please correct the worksheet, then re-submit the request.");
		}
		/*String date = WtUtils.formatDateForDb(requestDate.substring(1,3)
		                            + "/" + requestDate.substring(3)
		                            + "/0" + requestDate.substring(0,1));
			*/			
		String requestMethod = (String)fileData.getInlineProperty("Analysis method");
		if(requestMethod.equalsIgnoreCase("analysis methods"))
			throw new LinxUserException(
					"Please choose an analysis method in the worksheet," +
					" then re-submit the request.");
		
		String requestType = requestMethod.substring(0,requestMethod.indexOf('-'));
		if(!workRequest.startsWith(requestType))
		{
			throw new LinxUserException("Request ID [" + workRequest + "]"
					+ " for " + requestMethod + " analysis should start with '" + requestType + "'."
					+ " Please correct the Request ID (or change Analysis method)" +
							" in the worksheet, then re-submit.");
		}
		String project = (String)fileData.getInlineProperty("Project");
		if(project.equalsIgnoreCase("projects"))
			throw new LinxUserException(
					"Please select a project from the drop down list" +
					" in the worksheet, then re-submit.");
		
		//lets insert into the workRequest custom table.
		dbHelper.insertWorkRequest(workRequest, 
				requestMethod,
				requestDate,
				(String)fileData.getInlineProperty("Requester"),
				(String)fileData.getInlineProperty("Sample description"),
				getServerItemValue(EMREStrings.FileType.ACHEM_REQUEST_FILE),
				(String)fileData.getOptionalInlineProperty("Comments"), 
				project,
				(String)fileData.getInlineProperty("LIMS Tracking"),
				(String)fileData.getOptionalInlineProperty("Notebook Page"),
				(String)fileData.getInlineProperty("Results Email Address"),
				getTranId(), db);

		// Lets bulk insert column data
		String spName = "spMet_bulkInsertWorkRequestDetail";
		StringBuffer sb = buildBulkInsertData(fileData, workRequest, db);

		try
		{
			bulkInsert(sb, spName, getTranId(), db);
		}
		catch (Exception e)
		{
			if(e.getMessage().indexOf("duplicate key") > 0)
			{
				throw new LinxUserException("Found a duplicate Submission ID."
						+ " Submission IDs must be unique and new to this LIMS database."
						+ " Please check the request worksheet, then re-submit.");
			}
			throw new LinxUserException("While attempting bulk insert: " + e.getMessage());
		}
		sb.setLength(0);
		sb = null;


	}
		

	protected StringBuffer buildBulkInsertData(XLSParser data, String workRequestName,
			Db db)
	{
	
		ArrayList<String> params = new ArrayList<String>();
		//as of 10/2010 we need to store Sampling Timepoint in the samplingTimepoint table
		// -- much easier on bulk insert sp if stps already exist
		StringBuffer sb = null;
		int row = 1;
			try
			{
				sb = new StringBuffer();
				//String CRLF = MetStrings.CHAR.CRLF;
				char delim = EMREStrings.CHAR.SEMI_COLON;
				if(data.gotoFirst())
				{
					do
					{				
						//lets make sure we have the correct barcode format for an item
						String type = (String)data.getRequiredProperty("LIMS ID Type");
						String limsId = (String)data.getRequiredProperty("LIMS ID");
						String sLimsTracking = (String)data.getInlineProperty("LIMS Tracking");
						this.getServerItem(type).setValue(limsId);
						verifyItemMasks(limsId, type, sLimsTracking, db);
					
						// create any new SamplingTimepoints
						if(type.equalsIgnoreCase("StrainCulture") 
								|| type.equalsIgnoreCase("ExperimentalCulture"))
						{
							//if we're here we have some sort of culture - insert the timepoint
							String timepoint = fileData.getRequiredProperty("Sampling Timepoint");
							//does the timepoint exist?
							String samplingTimepointId = dbHelper.getDbValue(
									"exec spEMRE_getSamplingTimepointId '" + 
									limsId + "','" + timepoint + "'", db);
							//if not, insert it
							if(WtUtils.isNullOrBlank(samplingTimepointId))
							{
								int sn = getNextSerialNumberByCulture(limsId, type, db);
								sn++;
								params.clear();
								params.add(limsId + "_" + sn);
								params.add(limsId);
								params.add(timepoint); // datetime
								params.add(getTranId()+"");
								dbHelper.callStoredProc(db, "spEMRE_insertSamplingTimepoint", params, false, true);
							}
						}// end if STP
			
						String line = workRequestName + delim
									+ limsId + delim
									+ data.getRequiredProperty("Submission ID") + delim
									+ data.getProperty("Sampling Timepoint") + delim
									+ data.getProperty("Dilution") + delim
									+ data.getProperty("Comment") + delim
									+ type
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
	
	private void verifyItemMasks(String item, String type, String limsTracking, Db db)
	{
		try
		{
			String limssuffix = db.getHelper().getApplicationValue(db, "System Properties", "LIMS ID Suffix");
			limssuffix = limssuffix + "-";
			if(type.equalsIgnoreCase(ItemType.CULTURE) && limsTracking.equalsIgnoreCase("true"))
			{
				String mask = "[A-Z]{3}[0-9]{6}_[0-9]{5}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("Culture '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.STRAIN) && limsTracking.equalsIgnoreCase("true"))
			{
				String mask = "(SB|PH|PE)-" + limssuffix + "[0-9]{4}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
				{
					mask = "(GE|NE|NC|GC|NH|GH)-" + limssuffix + "[0-9]{5}-[0-9]{6}-[0-9]{3}(-Acid|-Base)?";
					p = Pattern.compile(mask);
					m = p.matcher(item);
				}
				if(!m.matches())
					throw new Exception("Strain '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.STRAINCULTURE))
			{
				String mask = "(WT|PH)-" + limssuffix + "[0-9]{4,5}-[0-9]{6}-[0-9]{3}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
				{	// eff EMRE v2.1
					mask = "(WE|GE|NE|WC|NC|GC|WH|NH)-" + limssuffix + "[0-9]{5}-[0-9]{6}-[0-9]{3}(-Acid|-Base)?";
					p = Pattern.compile(mask);
					m = p.matcher(item);
				}
				if(!m.matches())
					throw new Exception("StrainCulture '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.EXPERIMENTALCULTURE))
			{
				String mask = "EX-" + limssuffix + "[0-9]{4,5}-[0-9]{6}-[0-9]{3}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("ExperimentalCulture '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.EFT) && limsTracking.equalsIgnoreCase("true"))
			{	// eff EMRE v2.1
				String mask = "[0-9]{6}-[A-Z]{2}[0-9]{2}_[0-9]{2}_[0-9]{3}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("Strain '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.AXENICSTRAIN) && limsTracking.equalsIgnoreCase("true"))
			{
				String mask = "SG3-" + limssuffix + "[0-9]{6}-[0-9]{2}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("AxenicStrain '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.ISOLATE) && limsTracking.equalsIgnoreCase("true"))
			{
				String mask = "SI4-" + limssuffix + "[0-9]{6}-[ABCDEFG][0-9]{2}(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("Isolate '" + item + "' is not of the correct pattern " + mask);
			}
			else if(type.equalsIgnoreCase(ItemType.SAMPLE) && limsTracking.equalsIgnoreCase("true"))
			{
				String mask = "[A-za-z][0-9]2-[0-9]3(-Acid|-Base)?";
				Pattern p = Pattern.compile(mask);
				Matcher m = p.matcher(item);
				if(!m.matches())
					throw new Exception("Sample '" + item + "' is not of the correct pattern " + mask);
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}
	
	
	/**
	 * Returns the next A-Chem Request ID to use based
	 * on the increment in NEXTID custom table.
	 * @param db
	 */
	public String getNextRequestID(String requestType, Db db)
	{
		String sql = "spMet_GetNextAChemRequestID";
		String nextId = db.getHelper().getDbValueFromStoredProc(db, sql, requestType);
		return nextId;
	}
	
	private void sendEmail(XLSParser fileData, String workRequest, Db db)
	{
		try
		{
			//lets build the subject line
			String analysisMethod = (String)fileData.getInlineProperty("Analysis method");
			int numSamples = fileData.rows.size();
			String subject = numSamples + " samples were submitted for " + analysisMethod;
			//now lets build the message
			StringBuffer emailMsg = new StringBuffer();
			emailMsg.append("Message from EMRE Analytical Chemistry: " + subject + "\r\n\r\n");
			int numDataHeaders = EMREStrings.AChem.requiredDataHeaders.length;
			for(int i = 0; i < numDataHeaders; i++)
			{
				String header = EMREStrings.AChem.requiredDataHeaders[i];
				String val = (String)fileData.getInlineProperty(header);
				if(header.equalsIgnoreCase("Request date"))
				{
					Date date = new Date();
					SimpleDateFormat converted = new SimpleDateFormat("yyyyMMdd");
					String yyyyMMdd =  converted.format(date);
					val = yyyyMMdd;
				}
				emailMsg.append(header + ":\t" + val + "\r\n");
			}
			//comments are optional = lets add them as well
			String comments = fileData.getOptionalInlineProperty("Comments");
			emailMsg.append("Comments" + ":\t" + comments + "\r\n");
			//now lets put a link to the loaded file in the email
			
			String fileId = getServerItemValue(FileType.ACHEM_REQUEST_FILE);
			String filePath = dbHelper.getDbValue("exec spLinx_getApplicationFileAndPath " + fileId, db);
			
			emailMsg.append("\r\n\r\n");
			emailMsg.append("The submitted file is located here: \r\n<" + filePath + ">\r\n\r\n");
			emailMsg.append("This is an automated message.  Please do not reply.");
			int success = Alert.sendEmail("analytical", emailMsg.toString(), subject);
			if(success != 0)//we failed
				throw new LinxUserException("Unable to send email.  Please notify LIMS support.");
			emailMsg = null;
		}
		catch(Exception ex) 
		{
			throw new LinxUserException("The following error occurred while trying to send email: \r\n" 
					+ ex.getMessage());
		}
	}
	
	
}
