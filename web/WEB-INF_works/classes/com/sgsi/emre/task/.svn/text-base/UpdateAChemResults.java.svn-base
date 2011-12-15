package com.sgsi.emre.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.project.Strings;
import com.sgsi.bean.Sample;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.AChemParser;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.util.alert.SMTPEmail;

/**
 * 
 * UpdateAChemResults.java
 * 
 * Performs post-save work to update custom tables
 * with a row for each metabolite assayed. 
 * 
 * @author TJS/Wildtype for SGI, Inc.
 * @date 9/2008
 */
public class UpdateAChemResults extends EMRETask 
{	
	AChemParser fileData = null;
	
	
	/**
	 * Overridden to parse request worksheet and set task
	 * items for std processing. See post-save custom table
	 * updates, too.
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */		
		public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
			try
			{
				//super.createAnyNewAppFiles(request, response, user, db);
				String fileId = getServerItemValue(FileType.ACHEM_REQUEST_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for the run worksheet (*.xls), then try again.");
				}
				// inits importer class member 'imp'
				importRowsFromFile(fileId, user, db);
								
			}
			catch(LinxUserException ex)
			{
				throw ex;
			}
			catch(LinxDbException ex)
			{
				throw ex;
			}
			catch (Exception e)
			{
				if (e.getMessage().indexOf("user-mapped") > 0)
				{
					throw new LinxUserException(
							"The selected file is in use. Please close the file, then try again.");
				}
				throw new LinxUserException(e.getMessage());
			}

	}

	/**
	 * Overridden to send email to requester re: updated status
	 * of a-chem request. 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		updateAppFilesWithAppliesTo(request, response, user, db);
		updateCustomTables(db);
		sendAlert(db);
	}




/**
 * Acquisition date
Samples prepared by
Calibration standards by
Data processed by
Instrument
Method
Units
Calibration curve range
Observations1
Observations2
Observations3
Observations4
*/
		private void updateCustomTables(Db db)
	{
			String aquisitionDate = (String)fileData.getInlineProperty("Acquisition date");
			if(aquisitionDate.endsWith("E7"))
			{
				Double dbl = Double.parseDouble(aquisitionDate);
				aquisitionDate = String.valueOf(dbl.intValue());
			}
			String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			if(!aquisitionDate.startsWith(year)
					|| aquisitionDate.length() != "YYYYMMDD".length())
			{
				throw new LinxUserException("[Acquisition date] "
						+ " is not in the expected format 'YYYYMMDD'."
						+ " Please correct the worksheet, then try again.");
			}
		// set a results file pointer in the WORKREQUEST table
		ArrayList<String> params = new ArrayList<String>();
		params.add(this.getServerItemValue(ItemType.ACHEM_REQUEST));
		params.add(this.getServerItemValue(FileType.ACHEM_REQUEST_FILE));
		params.add(aquisitionDate);
		params.add((String)fileData.getInlineProperty("Samples prepared by"));
		params.add((String)fileData.getInlineProperty("Calibration standards by"));
		params.add((String)fileData.getInlineProperty("Data processed by"));
		params.add((String)fileData.getInlineProperty("Instrument"));
		params.add((String)fileData.getInlineProperty("Method"));
		params.add((String)fileData.getInlineProperty("Units"));
		params.add((String)fileData.getInlineProperty("Calibration curve range"));
		params.add((String)fileData.getOptionalInlineProperty("Folder")); //added folder as of 09/2009

		dbHelper.callStoredProc(db, "spMet_UpdateWorkRequest", params, false, false);

		// add observations 1-4 to COMMENT table
		// tranId will keep the relationship
		String obs1 = (String)fileData.getOptionalInlineProperty("Observations1");
		String obs2 = (String)fileData.getOptionalInlineProperty("Observations2");
		String obs3 = (String)fileData.getOptionalInlineProperty("Observations3");
		//String obs4 = imp.getInlineHeader("Observations4"); //as of 09/2009 only have 3 observations
		
		
		dbHelper.addComment("OBSERVATIONS1",obs1, this.getTranId(), db);
		dbHelper.addComment("OBSERVATIONS2",obs2, this.getTranId(), db);
		dbHelper.addComment("OBSERVATIONS3",obs3, this.getTranId(), db);
		//dbHelper.addComment("OBSERVATIONS4",obs4, getTranId(), db);
		
		
	}




		/**
	 * Performs bulk insert save for culture starts in import file,
	 * aborting entire tran if any of the rows generates an error.
	 * @param fileId
	 * @param user
	 * @param db
	 */
	protected void importRowsFromFile(String fileId, User user, Db db)
	{
		ArrayList<Sample> xs = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		//imp = new AChemRequestBulkImporter();
		//xs = imp.importXLSForBulkImport(inFile, getTaskName());
		//if(xs.isEmpty())
		//{
		//	throw new LinxUserException("Could not find any valid rows in file " + inFile.getName()
		//			+ ". Please check against the Analytical Chemistry Run Worksheet *.xls"
		//			+ " template for valid format, then try again.");
		//}
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "Submission ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredFileHeaders);
		
		if(fileData.rows.size() < 1)
		{
			throw new LinxUserException("Could not find any valid rows in file " + inFile.getName()
					+ ". Please check against the Analytical Chemistry Run Worksheet *.xls"
					+ " template for valid format, then try again.");
		}

		// ready for std save() processing
		String requestId = (String)fileData.getInlineProperty("Request ID");
		setMessage("Successfully recorded analysis results for work request " + requestId
				+ " from results file "	+ inFile.getName() 
				+ ". Requester has been alerted by email that results are ready for download.");

		getServerItem(EMREStrings.ItemType.ACHEM_REQUEST).setValue(requestId);
		insertAChemData(requestId, fileData, user, db);
		 
	}



	/**
	 * Does the work of calling the bulk insert sp with the
	 * text buffer of a-chem run data.
	 * @param xs
	 * @param db
	 */
	protected void insertAChemData(String workRequestName, AChemParser xs, User user, Db db)
	{
		try
		{
			//ListIterator itor = xs.listIterator();
			StringBuffer errMsg = new StringBuffer();
			ArrayList<String> ids = new ArrayList<String>();
			int lineNum = 1;
			String CRLF = EMREStrings.CHAR.CRLF;
			char delim = EMREStrings.CHAR.SEMI_COLON;
			StringBuffer sb = new StringBuffer();
			
			if(xs.gotoFirst())
			{
				do
				{				
					
					String subId = xs.getRequiredProperty("Submission ID");
					String labId = xs.getProperty("Lab sample ID");
					if(ids.contains(labId))
					{
						throw new LinxUserException("Found duplicate Lab sample ID " + labId
								+ ". Please correct the results to provide a unique ID for each lab sample, then try again.");
					}
					ids.add(labId);
					if(WtUtils.isNullOrBlank(labId))
					{
						throw new LinxUserException("Analytical Chemistry Lab personnel should fill out the right"
								+ " side of the run worksheet and submit the file again."
								+ " If you are a submitter, you may run the separate LIMS task 'Edit A-Chem Request'"
								+ " to edit a request prior to analysis." );
					}
					String idPart = 
						workRequestName + delim // makes it easier for the sp
						+ subId + delim
						+ xs.getProperty("Lab sample number") + delim
						+ labId + delim
						+ xs.getProperty("Lab sample ID");
					
					// now iterate over remaining metabolite columns (number varies run by run)
					
					int colCount = xs.getColumnNames().size();
					int startIdx = 0;
					Vector<String> colNames  = xs.getColumnNames();
					for(int i = 0; i < colNames.size(); i++)
					{
						String col = colNames.get(i);
						if(col.equalsIgnoreCase("Lab sample ID"))
							startIdx = i + 1;
					}
					for(int colIdx = startIdx; colIdx < colCount; colIdx++)
					{
						String key = "UNSET";
						try
						{
							key = (String)xs.getColumnNames().get(colIdx);
						}
						catch (Exception e)
						{
							throw new LinxUserException("At line number " + lineNum + ": " + e.getMessage());
						}
						if(!WtUtils.isNullOrBlank(key))
						{
							String value = xs.getProperty(key);
							if(WtUtils.isNullOrBlank(value))
							{
								// in case of null, default to N/A
								value = "N/A";
							}
							//format the sampling timepoint if it exists
							if(key.equalsIgnoreCase("Sampling Timepoint") && !value.equalsIgnoreCase("N/A"))
							{
								//make sure the timepoint is the correct date format
								String mask = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2} (AM|PM)";
								Pattern p = Pattern.compile(mask);
								Matcher m = p.matcher(key);
								if(!m.matches())
									throw new Exception("Sampling Timepoint '" + key + "' is not of the correct pattern " + mask);
							}
							//lets make sure we have a double value
							//as of 06/11/2009 we now allow alpha numeric values per Gena Lee
//							try
//							{
//								Double.parseDouble(value);
//							}
//							catch(Exception ex)
//							{
//								throw new LinxUserException("Found a non-numeric analytical value [" + value + "] "
//										+ " for Lab ID " + labId + ", analyte " + key + "."
//										+ " Please provide a numeric analytical result for this submission and analyte, then try again.");
//							}
							String line = idPart 
									+ delim + key 
									+ delim + value
									+ CRLF;
							sb.append(line);
						}
						
					
					}
				}
				while(xs.gotoNext());
			}//at end we have validated all of the inputs in the file
			
			
			lineNum++;
			//at end we have validated all of the inputs in the file
			//before we go any further, if we have errors on file validation, tell the user now.
			if(errMsg.length() > 0)
			{
				throw new LinxUserException(errMsg.toString());
			}
			errMsg = null;
			
			//ok, since we're here the data in the file is good.  Lets bulk insert
			String spName = "spMet_bulkInsertWorkRequestData";
			try
			{
				bulkInsert(sb, spName, getTranId(), db);
			}
			catch(LinxUserException ex)
			{
				throw ex;
			}
			catch(LinxDbException de)
			{
				throw de;
			}
			catch(Exception ex)
			{
				throw new LinxUserException("While attempting to bulk insert in MSSQL 2005: " + ex.getMessage());
			}
			sb.setLength(0);
			sb = null;
			
			
//			while (itor.hasNext())
//			{
//				// extract values from item object (represents file row == a submitted sample's data)
//				Sample x = (Sample) itor.next();
//
//				//lets write one row for each metabolite assayed to the string buffer
//				String subId = x.getProperty("Submission ID");
//				String labId = x.getProperty("Lab sample ID");
//				if(ids.contains(labId))
//				{
//					throw new LinxUserException("Found duplicate Lab sample ID " + labId
//							+ ". Please correct the results to provide a unique ID for each lab sample, then try again.");
//				}
//				ids.add(labId);
//				// todo -- also check for submission ID dups?
//				if(WtUtils.isNullOrBlank(labId))
//				{
//					throw new LinxUserException("Analytical Chemistry Lab personnel should fill out the right"
//							+ " side of the run worksheet and submit the file again."
//							+ " If you are a submitter, you may run the separate LIMS task 'Edit A-Chem Request'"
//							+ " to edit a request prior to analysis." );
//				}
//				String idPart = 
//					workRequestName + delim // makes it easier for the sp
//					+ subId + delim
//					+ x.getProperty("Lab sample number") + delim
//					+ labId + delim
//					+ x.getProperty("File name");
//				
//				// now iterate over remaining metabolite columns (number varies run by run)
//				int colCount = x.getColumnHeaders().size();
//				int startIdx = x.getColumnHeaders().indexOf("File name")+1;
//				for(int colIdx = startIdx; colIdx < colCount; colIdx++)
//				{
//					String key = "UNSET";
//					try
//					{
//						key = (String)x.getColumnHeaders().get(colIdx);
//					}
//					catch (Exception e)
//					{
//						throw new LinxUserException("At line number " + lineNum + ": " + e.getMessage());
//					}
//					String value = x.getProperty(key);
//					if(WtUtils.isNullOrBlank(value))
//					{
//						// in case of null, default to N/A
//						value = "N/A";
//					}
//					//format the sampling timepoint if it exists
//					if(key.equalsIgnoreCase("Sampling Timepoint") && !value.equalsIgnoreCase("N/A"))
//					{
//						//make sure the timepoint is the correct date format
//						String mask = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2} (AM|PM)";
//						Pattern p = Pattern.compile(mask);
//						Matcher m = p.matcher(key);
//						if(!m.matches())
//							throw new Exception("Sampling Timepoint '" + key + "' is not of the correct pattern " + mask);
//					}
//					//lets make sure we have a double value
//					//as of 06/11/2009 we now allow alpha numeric values per Gena Lee
////					try
////					{
////						Double.parseDouble(value);
////					}
////					catch(Exception ex)
////					{
////						throw new LinxUserException("Found a non-numeric analytical value [" + value + "] "
////								+ " for Lab ID " + labId + ", analyte " + key + "."
////								+ " Please provide a numeric analytical result for this submission and analyte, then try again.");
////					}
//					String line = idPart 
//							+ delim + key 
//							+ delim + value
//							+ CRLF;
//					sb.append(line);
//				}// next metabolite
//				// at exit, have appended results for each metabolite	
//				
//			}// next x
//			lineNum++;
//			//at end we have validated all of the inputs in the file
//			//before we go any further, if we have errors on file validation, tell the user now.
//			if(errMsg.length() > 0)
//			{
//				throw new LinxUserException(errMsg.toString());
//			}
//			errMsg = null;
//			
//			//ok, since we're here the data in the file is good.  Lets bulk insert
//			String spName = "spMet_bulkInsertWorkRequestData";
//			try
//			{
//				bulkInsert(sb, spName, getTranId(), db);
//			}
//			catch(LinxUserException ex)
//			{
//				throw ex;
//			}
//			catch(LinxDbException de)
//			{
//				throw de;
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException("While attempting to bulk insert in MSSQL 2005: " + ex.getMessage());
//			}
//			sb.setLength(0);
//			sb = null;
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}

		
 	/**
	 * Sends an email to the requester alerting him or her to this most recent
	 * updating of the run worksheet by the a-chem lab.
	 * 
	 * @param requestId
	 * @param fileData
	 */
	protected void sendAlert(Db db)
	{
		
			// send an email to the requester
			String requestId = (String)fileData.getInlineProperty("Request ID");
			String email = (String)fileData.getInlineProperty("Results Email Address");
			//lets check to see if they put the entire address in or just the info before the @ sign
			String suffix = "@syntheticgenomics.com";
			if(WtUtils.isNullOrBlank(email))
			{
				throw new LinxUserException("The Requester Email Address cannot be blank.");
			}
			if(email.indexOf("@") >=0)
			{
				//we have a full email address lets keep it
				//do nothing here
			}
			else
			{
				//we need to get the email suffix
				suffix = dbHelper.getApplicationValue(db, "System Properties", "Email Suffix");
				if(WtUtils.isNullOrBlank(suffix))
					throw new LinxSystemException("Unable to find the email suffix in the database.  Please notify LIMS support.");
				email = email + suffix;
			}
			StringBuffer sb = new StringBuffer();
	        sb.append("Your A-Chem Request worksheet " + requestId 
	        		+ " has just been updated by the Analytical Chemistry Lab." + Strings.CHAR.NEWLINE);
	        sb.append("You may check the current status of the work request by running LIMS task 'Download A-Chem Results'." + Strings.CHAR.NEWLINE);
	        sb.append("This is an automated message.  Please do not reply." + Strings.CHAR.NEWLINE);
	        int rtn = sendEmail(email, sb.toString(), 
	        		"A-Chem Request " + requestId + " has been updated");
	      //per Judit Bartalis - send her an email - configured in LIMS as 'Update Achem Default Email Address'
			//no need to send email notification to judit anymore.  10/05/2009
	       // String defaultEmail = dbHelper.getApplicationValue(db,"System Properties","Update Achem Default Email Address");
			//if(!WtUtils.isNullOrBlank(defaultEmail))
			//	rtn = sendEmail(defaultEmail, sb.toString(), 
			//		"A-Chem Request " + requestId + " has been updated");
	}
	
	public static int sendEmail(String mailTo, String strMsg, String strSubject)
	  {
	    Element eMail   = LinxConfig.getConfigElement(LinxConfig.EMAIL.EMAIL);
	    // children of email node
	    Element eServer = (Element)eMail.getElementsByTagName(LinxConfig.EMAIL.MAIL_SERVER).item(0);
	    String strMailServer = WtDOMUtils.getElementValue(eServer);
	    String iMailServerPort = eServer.getAttribute(LinxConfig.EMAIL.MAIL_PORT);
	    String strMailFrom = WtDOMUtils.getElementValue(eMail, LinxConfig.EMAIL.FROM);

	    SMTPEmail email = new SMTPEmail(strMailServer, Integer.parseInt(iMailServerPort));
	    int iStatus = email.sendMail(strMailFrom, mailTo, strSubject, strMsg);
	    if (iStatus == 0)
	    {
	    	Code.info("Sent SMTP Email to " + mailTo + " (" + strMsg.length()
	                + " bytes)");
	    }
	    else
	    {
	    	Code.warning("Mail sent to '" + mailTo + "' on " + strMailServer
	                + ":" + iMailServerPort + " returned '" + iStatus + "'");
	    }
	    return (0);
	  }
}
