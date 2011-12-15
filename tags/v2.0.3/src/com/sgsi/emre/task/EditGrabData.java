package com.sgsi.emre.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.SamplingDataParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


/**
 * Parses Excel file submitted by user and
 * updates SAMPLINGTIMEPOINT  custom tables in database 
 * with edited sampling data.
 * Can be run multiple times on the same sampling ID;
 * task will preserve a copy of each pre-update record
 * in SAMPLINGTIMEPOINTHISTORY table. Effective EMRE 2.0.
 * @author TJS/Wildtype for SGI
 * @date 3/2011
 */
public class EditGrabData extends SubmitGrabData 
{
	/**
	 * Overridden to parse incoming file for its import ID 
	 * and data rows and to save to custom db tables
	 * SAMPLINGTIMEPOINTDATA.
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		this.db = db;
		String columnKey = "Sampling ID"; // column header for data row
		boolean bSuccess = false;

		// import file
		String fileId = getServerItemValue(FileType.SAMPLING_DATA_IMPORT_FILE);
		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException(
					"Please browse for a culture grab data import file, then try again.");
		}
		File inFile = this.getFile(fileId, db);

		// check each of multiple sheets (currently 3) per workbook

		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		SamplingDataParser parser = new SamplingDataParser();
		try
		{
			FileInputStream istream = new FileInputStream(inFile);
			fs = new POIFSFileSystem(istream);
			wb = new HSSFWorkbook(fs);
			istream.close(); // prevent sharing violations on xls file
		}
		catch (FileNotFoundException ex)
		{
			throw new LinxUserException(ex.getMessage() + ": " + inFile.getPath());
		}
		catch (IOException ex)
		{
			throw new LinxUserException(ex.getMessage() + ": " + inFile.getPath());
		}

		// for each sheet,
		// look for an import ID as an indicator that sheet has data values
		int sheetCount = wb.getNumberOfSheets();
		for (int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
		{
			HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
			String sheetName = wb.getSheetName(sheetIdx);
			try
			{
				getServerItem(ItemType.IMPORT_ID).setValue(getTranIdAsString());
			}
			catch (Exception ex)
			{
				continue;
			}
			ArrayList<String> cultures = 
				parser.updateDataValues(sheet, sheetName, getTranIdAsString(), "Sampling ID",
					this, dbHelper, db);
			if(cultures.size() > 0)
			{
				bSuccess = true;
				getServerItem(ItemType.CULTURE).addValues(cultures);
			}
		}// next worksheet
		// at exit, bSuccess = true if at least one worksheet had values to import
		if (!bSuccess)
		{
			throw new LinxUserException(
					"Could not find any data rows in any of the worksheets."
							+ " Please check that you are using the latest import template"
							+ " and that at least one worksheet contains values in the 'Sampling ID' column.");
		}

		setMessage("Successfully edited culture grab data."
				+ " Please make a note of sampling IDs in case you need to edit your data later.");
	}
	
	/**
	 * Returns true if the parser should also
	 * update custom grab data tables while
	 * parsing import file, otherwise returns false.
	 * @return true to update custom tables
	 */
	protected boolean getUpdateTablesFlag()
	{
		
		return true;
	}

	/**
	 * Returns T if a samplingTimepointHistory record should be saved
	 * for pre-update records.
	 * @return T
	 */
	public String createHistoryRecord()
	{
		return "T";
	}
	

}
