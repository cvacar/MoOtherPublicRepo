package com.sgsi.emre.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.POIParser;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Handles task Environmental Aquatic Sample Logging in EMRE LIMS v2.2 by
 * parsing bulk import file and inserting new sample properties via SQL bulk insert.
 * Should improve performance and flexibility significantly.
 * 
 * @author TJS/Wildtype for SGI
 * @modified 6/2011 by TJS/Wt under SOW 004 -- now using bulk insert to copy
 *           file to db, for better performance
 * 
 */
public class LogAquaticSample extends LogEnvironmentalSample
{
	String ID_COLUMN = "LIMS ID";
	String WORKSHEET = "Aquatic";
	
	/**
	 * Overridden to extract list of new sample IDs from import file
	 * for std core processing.
	 * Eff EMRE LIMS v2.2 6/2011.
	 */
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		if (dbHelper == null)
		{
			dbHelper = new EMREDbHelper();
			dbHelper.init();
		}
		String fileId = getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);

		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException("Please browse for an import file, then try again.");
		}
		// extract list of samples from user's file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);
		ArrayList<String> samples = getIdsFromImportFile(inFile, WORKSHEET, ID_COLUMN);
		if (samples.isEmpty())
		{
			// how to handle in absence of error giving cause?
			throw new LinxUserException("No new sample rows were found in import file.");
		}

		// set up for standard core processing
		getServerItem(ItemType.ENVIRONMENTAL_SAMPLE).setValues(samples);
		setMessage("Successfully logged " + samples.size() + " new samples. " 
				+ " Go to task 'Print Labels' to print labels,"
				+ " or click on this task in the workflow menu to autogenerate new IDs.");
	}


	
	/**
	 * Overridden to parse import file, create bulk insert file,
	 * and update custom tables with sample properties. Relies
	 * on doTaskWorkPreSave() having found new sample names and
	 * std core processing having created new samples in ITEM.item.
	 * Eff EMRE LIMS v2.2 6/2011
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		String fileId = getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);

		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException("Please browse for an import file, then try again.");
		}
		// extract list of samples from user's file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		 // blindly parse column values into tab-delimited file 
		// -- (commas occur often in data) 
		File biFile = createBulkInsertFile(inFile, WORKSHEET, ID_COLUMN, db);		  
		 
		String sp = "spEMRE_bulkInsertAquaticSamples";		
		dbHelper.callStoredProc(db, sp, biFile.getPath());
		
		biFile.delete();
		
		//throw new LinxUserException("Rolling back");
	}




}
