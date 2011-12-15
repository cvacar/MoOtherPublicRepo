package com.sgsi.emre.task;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.bean.Sample;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.bean.AChemParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;

/**
 * 
 * UpdateTOCResults.java
 * 
 * Performs post-save work to update custom tables
 * with a row for each metabolite assayed using 
 * the TOC method. Usually run by TOC users on
 * their own expts vs analytical staff.
 * 
 * @author TJS/Wildtype for SGI, Inc.
 * @date 2/2011
 * @version 1.15s
 */
/**
 * @author TJS
 *
 */
public class UpdateTOCResults extends UpdateAChemResults 
{	


		/**
		 * Overridden to prevent upload of TOC results by anyone
		 * other than the actual TOC requester.
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

		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "Submission ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new AChemParser(inFile, FileType.ACHEM_SUBMISSION,
				delim, columnKey, EMREStrings.AChem.requiredFileHeaders);
		
		if(fileData.rows.size() < 1)
		{
			throw new LinxUserException("Could not find any valid rows in file " + inFile.getName()
					+ ". Please check against the latest Analytical Chemistry Run Worksheet *.xls"
					+ " template for valid format, then try again.");
		}
		
		String requestId = (String)fileData.getInlineProperty("Request ID");
		if(!requestId.startsWith("TOC"))
		{
			throw new LinxUserException("Only TOC requests may be updated in this task."
					+ " Please use task 'Update A-Chem Results'"
					+ " to update other request types.");
		}

		//**********************************************************
		// only the requester may submit updated TOC results
		// -- task displays only this user's requests, 
		// -- but user might try to upload anything old file
		String requester = (String)fileData.getInlineProperty("Requester");
		if(!user.getName().equalsIgnoreCase(requester))
		{
			throw new LinxUserException("Only original TOC requester [" + requester
					+ "] may update results for Request ID [" + requestId +"]");
		}
		//***********************************************************
		
		// ready for std save() processing
		setMessage("Successfully recorded analysis results for work request " + requestId
				+ " from results file "	+ inFile.getName());

		getServerItem(EMREStrings.ItemType.ACHEM_REQUEST).setValue(requestId);
		insertAChemData(requestId, fileData, user, db);
		 
	}
	
	
	
	/** 
	 * Overriding EMRETask's override of core's method to reinstate
	 * core behavior. Not sure why EMRETask's method is not using
	 * the FileItem object to copy and operate on File object. 
	 */
	public void createAnyNewAppFiles(HttpServletRequest request,
			HttpServletResponse response, User user, Db db) 
	{
		super.createAnyNewAppFiles(request, response, user, db);
		
	}



	/**
 	 * Overridden to do nothing in the case of TOC requester 
 	 * running his or her own samples.
	 * 
	 * @param requestId
	 * @param fileData
	 */
	protected void sendAlert(Db db)
	{
		// do nothing
		return;		
	}	

}
