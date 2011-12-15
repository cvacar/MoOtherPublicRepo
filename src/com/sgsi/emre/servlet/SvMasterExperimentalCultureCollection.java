package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;

/**
 * Handles custom actions for bulk import of cultures,
 * returning next ID available for new cultures,
 * and exporting culture collection.
 * @author BJS/Wildtype for SGI
 * @modified 4/2011 for EMRE v2.1
 *
 */
public class SvMasterExperimentalCultureCollection extends SvCultureSelection 
{
	/**
	 * Overridden to call stored proc dedicated to Experimental Culture IDs.
	 * @param strain
	 * @param cultureType Currently 'ExperimentalCulture'
	 * @param dateStarted
	 * @param db
	 * @return nextId constructed from culture type, strain, and date started + serial number
	 */
	public String getNextCultureId(String strain, String cultureType, String dateStarted, Db db)
	{

		ArrayList<String> params = new ArrayList<String>();
		params.add(dateStarted);
		String nextId = null;
		try
		{
			synchronized(this)
			{
				nextId = 
					dbHelper.getDbValueFromStoredProc(db, "spEMRE_getNextExperimentalCulture",params);
			}
		}
		catch(Exception ex)
		{
			throw new LinxDbException(
					"Error occured when trying to get the next culture id: " 
					+ ex.getMessage());
		}
		return nextId;
	}
	/**
	 * loops through the rows of a file and calls save per row
	 * @param fileId
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
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
		String columnKey = "Origin LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
		try
		{
			//CV 2011, Dec 14
			if(task.getTaskName().contains("Master")) 
			{
				//use the same worksheet for creating a "Master Experimental Culture" as we do for an "Experimental Culture"
				fileData = new XLSParser(inFile, task.getTaskName().replace("Master ", ""),
						delim, columnKey, EMREStrings.GrowthRecovery.requiredExpCultureCollectionColumnHeaders,
						true);
					
			}
			else
			{
				fileData = new XLSParser(inFile, task.getTaskName(),
						delim, columnKey, EMREStrings.GrowthRecovery.requiredExpCultureCollectionColumnHeaders,
						true);
			}
			
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Error occurred during file parsing: " + ex.getMessage());
		}
		

		// ready for std save() processing
		task.setMessage("Successfully imported new cultures from bulk import file.");
		
		
		//loop though the file and insert each line
		String err = "";
		int row = 1;
		try
		{
			if(fileData.gotoFirst())
			{
				do
				{			
					String limsId = fileData.getRequiredProperty("Experimental Culture ID");
					String originLimsId = fileData.getRequiredProperty("Origin LIMS ID");
					String pageRef = fileData.getRequiredProperty("Notebook Page");
					String dateStarted = fileData.getRequiredProperty("Date Started");
					String desc = fileData.getProperty("Culture Description");
					String comment = fileData.getProperty("Comment");
					
					task.getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(limsId);
					task.getServerItem(ItemType.STRAIN).setValue(originLimsId);
					task.getServerItem("NotebookRef").setValue(pageRef);
					task.getServerItem("DateStarted").setValue(dateStarted);
					task.getServerItem("Description").setValue(desc);
					task.getServerItem("Comment").setValue(comment);
					
					// call std processing
					save(task, user, db, request, response);
					row++;
				}
				while(fileData.gotoNext());
			}//at end we have saved each row of the file
			
		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if(!err.equals(""))
			throw new LinxUserException(err);
	}

	public String getCultureItemType()
	{
		return ItemType.EXPERIMENTALCULTURE;
	}
	

	/**
	 * Returns name of stored procedure to retrieve
	 * culture collection.
	 */
	public String getCultureCollectionSQL()
	{
		return "spEMRE_reportExperimentalCultureCollection";
	}

}
