package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.ExperimentalCultureCollection;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


public class SvExperimentalCultureCollection extends SvCultureCollection 
{
	protected ExperimentalCultureCollection myTask = new ExperimentalCultureCollection();
	
	protected String cultureType = ItemType.EXPERIMENTALCULTURE;
	
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			if (request.getAttribute("ExportButton") != null)
			{
				// user wants to download the entire strain collection to Excel
	        	writeToExcel(request, response, myTask.getCultureCollectionSQL(), db);
				return ALL_DONE;
			}
			else if (request.getAttribute("NextButton") != null)
			{
				//generate next culture id
				//make sure we have a strain first
				String strain = task.getDisplayItemValue(ItemType.STRAIN);
				if(WtUtils.isNullOrBlankOrPlaceholder(strain))
					throw new LinxUserException("Please enter a value for 'Origin LIMS ID'.");
				String dateStarted = task.getDisplayItemValue("DateStarted");
				if(WtUtils.isNullOrBlankOrPlaceholder(dateStarted))
					throw new LinxUserException("Please enter a value for 'Date Started'");
				String nextId = myTask.getNextCulture(strain, getCultureType(), dateStarted, db);
				task.getDisplayItem(getCultureType()).setValue(nextId);
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("ImportButton") != null)
			{
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.CULTURE_COLLECTION_IMPORT_FILE);
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
			if(ex instanceof LinxUserException)
			{
				throw new LinxUserException(ex.getMessage());
			}
			else if(ex instanceof LinxDbException)
			{
				throw new LinxDbException(ex.getMessage());
			}
			throw new LinxUserException(ex.getMessage());
		}
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
			fileData = new XLSParser(inFile, task.getTaskName(),
					delim, columnKey, EMREStrings.GrowthRecovery.requiredExpCultureCollectionColumnHeaders,
					true);
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

	public String getCultureType() {
		return cultureType;
	}

}
