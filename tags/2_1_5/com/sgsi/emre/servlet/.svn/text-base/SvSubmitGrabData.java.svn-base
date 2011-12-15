package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.SamplingTimepointDataParser;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvSubmitGrabData extends EMREServlet
{

	/**
	 * Overridden to handle custom action to get next work request id for user to
	 * add to his or her file before submission (doesn't have to; will be
	 * validated as new in any case.) Unusual in that this handler commits the
	 * table update for nextId, generating TASKHISTORY and COMMENT records too.
	 * 
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return FINISH_FOR_ME
	 */
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		if (request.getAttribute("GetImportID") != null)
		{
			// currently, unique import ID is just this tranID
			// -- useful because we know it is unique
			long nextId = ((SubmitGrabData) task).getTranId(db);
			task.getDisplayItem("ImportID").setValue(String.valueOf(nextId));
			db.getHelper().addComment(
					"TASK_COMMENTTYPE",
					"Generated new Import ID " + String.valueOf(nextId)
							+ "for sampling data submission.", null, task.getTranId(), db);
			// make sure this tranId is 'burned'
			// -- also preserves TASKHISTORY and COMMENT records
			commitDb(db);

			return FINISH_FOR_ME;
		}
		else if (request.getAttribute("ImportButton") != null)
		{
			// user is submitting a bulk import file of timepoints
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task
					.getServerItemValue(FileType.SAMPLING_DATA_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			importRowsFromFile(fileId, task, user, db, request, response);

			commitDb(db);
			return FINISH_FOR_ME;
		}

		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Loops through the rows of a file and calls save per row
	 * 
	 * @param fileId
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected void importRowsFromFile(String fileId, Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		SamplingTimepointDataParser parser = new SamplingTimepointDataParser();
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
		String sheetName = null;
		int stpCount = 0;
		for (int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
		{
			HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
			sheetName = wb.getSheetName(sheetIdx);
			stpCount += processWorksheet(parser, sheet, inFile, (EMRETask)getTaskObject(request), user, db, request, response);
		}// next sheet
		// did at least one worksheet have values to import?
		if (stpCount < 1)
		{
			throw new LinxUserException(
					"Could not find data rows in any of the worksheets."
							+ " Please check that you are using the latest import template"
							+ " and that at least one worksheet contains an Import ID "
							+ " and values in the 'Sampling ID' column.");
		}
		// ready for std save() processing
		task.setMessage("Successfully saved culture grab data for " + stpCount + " timepoints.");
	}

	/**
	 * File-based save, once per row. Returns number of 
	 * timepoints found in worksheet.
	 * @param parser
	 * @param sheet
	 * @param inFile
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected int processWorksheet(SamplingTimepointDataParser parser,
			HSSFSheet sheet, File inFile, EMRETask task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		String importId = null;
		String columnKey = "Sampling ID";
		ArrayList<String> params = new ArrayList<String>();
		HSSFCell cell = null;
		String colName = null;
		String cellValue = null;
		int stpCount = 0; // number of stp's
		
		try
		{
			importId = parser.getValueForInlineHeader(sheet, sheet.getSheetName(),
					"Import ID");
			task.getServerItem(ItemType.IMPORT_ID).setValue(importId);
			if(WtUtils.isNullOrBlank(importId))
			{
				return 0;
			}
			if(task.getTaskName().startsWith("Edit")) 
			{
				checkUserAlsoSubmitter(importId, task, user, db); // if not, will throw exception
			}
		}
		catch (Exception ex)
		{
			if(ex instanceof LinxUserException)
			{
				throw new LinxUserException(ex);
			}
			return stpCount; // no data in this worksheet, or user isn't submitter
		}

		// assumes that all worksheets in this template share the column key
		HSSFRow dataRow = null;
		HSSFRow headerRow = parser.getRowWithColumnHeaders(sheet, sheet.getSheetName(), columnKey);
		int dataStarts = 1 + headerRow.getRowNum();
		
		Iterator<Row> rowItor = sheet.rowIterator();
		while (rowItor.hasNext())
		{
			dataRow = (HSSFRow) rowItor.next();
			if(dataRow.getRowNum() < dataStarts)
			{
				continue; // looking for first row of data values
			}

			// submitted row identifier
			if(parser.isNullOrBlank((dataRow.getCell(0))))
			{
				break;
			}
			String samplingId = parser.getRequiredProperty(dataRow, "Sampling ID");
			task.getServerItem("SamplingID").setValue(samplingId);

			// datetime of sampling
			String timept = parser.getRequiredProperty(dataRow, "Sampling Timepoint");
			task.getServerItem("Timepoint").setValue(timept);

			// do we have an ExperimentalCulture or a StrainCulture?
			// -- relies on a naming convention EX-SGI-E-x...
			String culture = parser.getRequiredProperty(dataRow, "Culture ID");
			String cultureType = ItemType.STRAINCULTURE;
			if (culture.toLowerCase().startsWith("ex"))
			{
				task.getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(culture);
				task.getServerItem(ItemType.STRAINCULTURE).clearValues();
				cultureType = ItemType.EXPERIMENTALCULTURE;
			}
			else
			{
				task.getServerItem(ItemType.STRAINCULTURE).setValue(culture);
				task.getServerItem(ItemType.EXPERIMENTALCULTURE).clearValues();
			}
			task.getServerItem(ItemType.CULTURE).setValue(culture);
			
			// may be new or existing item.item
			String stp = task.getSamplingTimepointByCulture(culture, cultureType, timept, db);
			task.getServerItem(ItemType.SAMPLING_TIMEPT).setValue(stp);

			// call std processing per row
			// -- task overrides post-save to update custom tables 
			save(task, user, db, request, response);
			task.getServerItem(ItemType.IMPORT_ID).clearValues();
			stpCount++;
			
			if(task.createNewTimepoints())
			{
				// create custom table record if needed
				params.clear();
				params.add(samplingId);
				params.add(stp);
				params.add(culture);
				params.add(timept);
				params.add(task.getTranId() + "");
				try
				{
					// inserts new SAMPLINGTIMEPOINT record if needed
					// -- assumes core tables are already updated
					String s = dbHelper.getDbValueFromStoredProc(db, "spEMRE_insertSamplingTimepointData", params);
				}
				catch (Exception ex)
				{
					throw new LinxUserException("In worksheet [" + sheet.getSheetName()
							+ "], row " + dataRow.getRowNum() + ": " + ex.getMessage());
				}
			}
			// insert sampling data
			// start inserting generic data types
			int lastCellIdx = headerRow.getLastCellNum();
			for (int cellIdx = EMREStrings.GrowthRecovery.requiredSamplingDataColumnHeaders.length; cellIdx < lastCellIdx; cellIdx++)
			{
				cell = dataRow.getCell(cellIdx);

				colName = parser.getColumnNameByCellIndex(cellIdx);
				if (colName.endsWith("*"))
				{
					cellValue = parser.getRequiredProperty(dataRow, colName);
				}
				else // optional property
				{
					cellValue = parser.getValueAsString(cell);
					if (parser.isNullOrBlank(cell))
					{
						// no value here
						continue;
					}				
				}

				params.clear();
				params.add(samplingId);
				params.add(stp);
				params.add(colName); // already trimmed/compressed
				params.add(cellValue);
				params.add(task.getTranId() + "");
				try
				{
					// insert SAMPLINGTIMEPOINTDATA table record
					// -- assumes core and SAMPLINGTIMEPOINT records already exist
					dbHelper.callStoredProc(db, "spEMRE_updateSamplingTimepointData", params,
							false, true);
				}
				catch (Exception ex)
				{
					throw new LinxUserException("In worksheet [" + sheet.getSheetName()
							+ "], row " + dataRow.getRowNum() + ", value "
							+ cell.toString() + ": " + ex.getMessage());
				}
			}// next column
			
		} // next row
		task
				.setMessage("Successfully saved culture grab data."
						+ " Retrieve updated sampling timepoint file "
						+ "by following the file link at LIMS task Culture Selection.");
		return stpCount;
	}

	/**
	 * Throws an exception if the given user is not the original submitter
	 * of the file associated with this import ID.
	 * @param importId
	 * @param user
	 * @param db
	 * @return true or throws error if false
	 */
	public boolean checkUserAlsoSubmitter(String importId, Task task, User user, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		params.add(importId);
		params.add(user.getName());
		params.add(task.getTranId()+"");
		String submitter = 
			db.getHelper().getDbValueFromStoredProc(db, "spEMRE_getUserByImportId", params);
		if(!submitter.equalsIgnoreCase(user.getName()))
		{
			throw new LinxUserException("Only the original submitter (" + submitter + ")"
					+ " may resubmit edited culture grab data for Import ID = " + importId);
		}
		return true;
	}
		

}
