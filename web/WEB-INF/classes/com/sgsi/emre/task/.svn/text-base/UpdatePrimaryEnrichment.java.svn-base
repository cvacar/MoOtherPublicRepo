package com.sgsi.emre.task;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Handles task Update Primary Enrichment in EMRE LIMS v2.2 by
 * parsing bulk import file and inserting new enrichment properties via SQL bulk insert.
 * Should improve performance and flexibility significantly.
 * 
 * @author TJS/Wildtype for SGI
 * @modified 7/2011 by TJS/Wt for LIMS v2.2 
 * 	-- screen input eliminated
 * 	-- now using bulk insert to copy file to db, for better performance
 * 
 */
public class UpdatePrimaryEnrichment extends EMRETask
{
	String ID_COLUMN = "LIMS ID";
	String WORKSHEET = "Update Primary Enrichment";
	/**
	 * Overridden to extract list of new enrichment IDs from import file
	 * for std core processing.
	 * Eff EMRE LIMS v2.2 7/2011.
	 */
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		if (dbHelper == null)
		{
			dbHelper = new EMREDbHelper();
			dbHelper.init();
		}
		String fileId = getServerItemValue(FileType.PRIMARY_ENRICHMENT_IMPORT_FILE);

		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException("Please browse for an import file, then try again.");
		}
		// extract list of IDs from user's file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);
		ArrayList<String> ids = getIdsFromImportFile(inFile, WORKSHEET, ID_COLUMN);
		if (ids.isEmpty())
		{
			// how to handle in absence of error giving cause?
			throw new LinxUserException("No rows containing existing enrichments were found in import file on worksheet tab '" + WORKSHEET + "'.");
		}

		// set up for standard core processing
		getServerItem(ItemType.PRIMARY_ENRICHMENT).setValues(ids);
		setMessage("Successfully updated " + ids.size() + " enrichments. " 
				+ " Go to task 'Print Labels' to print labels,"
				+ " or click on task Primary Enrichment in the workflow menu to autogenerate new IDs.");
	}


	
	/**
	 * Overridden to parse import file, create bulk insert file,
	 * and update custom tables with enrichment properties. Relies
	 * on doTaskWorkPreSave() having found new enrichment names and
	 * std core processing having created new enrichments in ITEM.item.
	 * Eff EMRE LIMS v2.2 7/2011
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		String fileId = getServerItemValue(FileType.PRIMARY_ENRICHMENT_IMPORT_FILE);

		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException("Please browse for an import file, then try again.");
		}
		// extract list of enrichments from user's file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		 // blindly parse column values into tab-delimited file 
		// -- (commas occur often in data) 
		File biFile = createBulkInsertFile(inFile, WORKSHEET, ID_COLUMN, db);		  
		 
		String sp = "spEMRE_bulkUpdatePrimaryEnrichments";		
		dbHelper.callStoredProc(db, sp, biFile.getPath());
		
		biFile.delete();
		
		//throw new LinxUserException("Rolling back");
	}



}
