package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.SamplingTimepointDataParser;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.EditGrabData;
import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * 
 * SvEditGrabData
 * 
 * Overridden to allow user to retrieve a file containing timepoint data to edit
 * and resubmit. Used mostly by submitter to fill in data that was missing when
 * originally submitted at Submit Grab Data.
 * 
 * @author TJS/Wildtype for SGI
 * @date 3/2011
 */
public class SvEditGrabData extends SvSubmitGrabData
{
	private String	TIMEPT_TABLE	= "Timepoints";
	
	/**
	 * Handles custom action [Get File] to return custom table data for the
	 * user-entered sampling ID in Excel worksheet format.
	 * 
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{

		if (request.getParameter("GetFile") != null
				|| request.getAttribute("GetFile") != null)
		{
			// Find by ID
			try
			{
				String samplingId = task.getDisplayItemValue(ItemType.SAMPLING_ID);
				if (WtUtils.isNullOrBlankOrPlaceholder(samplingId))
				{
					throw new LinxUserException(
							"Please enter a Sampling ID, Culture ID, or Import ID, then try again.");
				}
				return getFile(samplingId, request, response, (EditGrabData) task, db);
			}
			catch (Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}
		}
		else if(request.getAttribute("DataUploadButton") != null)
		{
	    isFirstRequest = true;

	    try
			{
				processShareDataFiles(ItemType.SAMPLING_ID, (EMRETask)task, user, db);
			}
			catch (IOException ex)
			{
				throw new LinxUserException(ex);
			}
			task.setMessage("Successfully copied all data files found in shared data folder"
					+ " ~/" + user.getName() + "/");
			commitDb(db);
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Downloads to client an MS Excel file containing only the SAMPLINGTIMEPOINT
	 * data rows for given sampling ID, culture ID, or import ID.
	 * 
	 * @param anyId
	 * @param task
	 * @param response
	 * @param db
	 * @return
	 */
	protected boolean getFile(String anyId, HttpServletRequest request,
			HttpServletResponse response, SubmitGrabData task, Db db)
	{
		// get a copy of the template
		String pathToTemplate = dbHelper.getApplicationValue(db, "FILETYPE",
				"Culture Grab Data Import Template");
		if(WtUtils.isNullOrBlank(pathToTemplate))
		{
			throw new LinxUserException("Missing required APPVALUE entry "
					+ " 'Culture Grab Data Import Template'. Please alert your LIMS administrator.");
		}
		String pathToTemp = dbHelper.getApplicationValue(db, "System Properties",
				"Temp File Dir");
		if(WtUtils.isNullOrBlank(pathToTemp))
		{
			throw new LinxUserException("Missing required APPVALUE entry "
					+ " 'CultureGrabDataImportFile'. Please alert your LIMS administrator.");
		}
		File templateFile = new File(pathToTemplate);
		File newFile = null;
		try
		{
			// make a copy of the template so we don't tie it up
			newFile = new File(anyId + ".xls");
			WtUtils.copyFile(templateFile, pathToTemplate, newFile, pathToTemp);

			// create an xls worksheet (=our new file to download to user)
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(newFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			//wb.unwriteProtectWorkbook();
			SamplingTimepointDataParser parser = new SamplingTimepointDataParser(); // util class
			// populate blank row with data values by matching datatype = col name
			// -- WARNING: skips data values whose data type no longer appears in template
			// -- informed SGI of this caveat on 4/23/2011 (TJS)
			HSSFSheet sheet = null;
			HSSFRow headerRow = null;
			HSSFRow dataRow = null; 
			HSSFCell cell = null;
			String sampId = "unset";
			String cultureId = null;
			String timepoint = null;
			String username = null;
			// key/value data
			String dataType = null;
			String dataValue = null;
			String importId = null;
			CellStyle textStyle;
			DataFormat format = wb.createDataFormat();
			textStyle = wb.createCellStyle();
		    textStyle.setDataFormat(format.getFormat(format.getFormat((short)1)));
		    
			/******** QUERY DB & POPULATE BLANK TEMPLATE *******/
			// eff 2.1.6, accepts Sampling ID, Culture ID (item.item), and Import ID
			String sql = "exec spEMRE_getSamplingDataByAnyID '"+anyId+"'";
			ResultSet rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				String newSampId = rs.getString(1);
				cultureId = rs.getString(2);
				timepoint = rs.getString(3);
				username = rs.getString(4);
				// key/value data
				dataType = rs.getString(5);
				dataValue = rs.getString(6);
				importId = rs.getString(7);
				
				if(!newSampId.equalsIgnoreCase(sampId))
				{
					sampId = newSampId;
					sheet = parser.getWorksheet(wb, sampId.substring(0,1));
					String sheetName = sheet.getSheetName(); // for debug use
					headerRow = parser.getRowWithColumnHeaders(sheet, sheetName, "Sampling ID");
					HSSFRow idRow = (HSSFRow)sheet.getRow(headerRow.getRowNum()-2);
					//parser.appendValueForInlineHeader(sheet, sheetName, "Import ID", importId);
					dataRow = parser.getBlankRow(sheet, headerRow.getRowNum()); // gtd non-null

					// at first blank data row -- populate known (required) columns
					parser.setCellValue(headerRow, "Sampling ID", dataRow, sampId);
					parser.setCellValue(headerRow, "Culture ID", dataRow, cultureId);
					parser.setCellValue(headerRow, "Sampling timepoint", dataRow, timepoint);
					parser.setCellValue(headerRow, "Windows Login", dataRow, username);
				}
				// key/value pairs			    
				parser.setCellValue(headerRow, dataType, dataRow, dataValue);
			}// next datatype/datavalue pair
			// at exit, have populated blank template with this sampling ID's data

			// download finished file from POI workbook object
			task.setMessage("Successfully created data file to download.");
			File finFile = new File("out.xls");
			FileOutputStream fos = new FileOutputStream(finFile);
			wb.write(fos);
			fos.flush();
			fos.close(); //<-- essential to make this work
			finFile.deleteOnExit();
			returnDownloadAsByteStream(response, finFile, anyId + ".xls",
					"application/vnd.ms-excel", false);
		}
		catch (Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return ALL_DONE;
	}


}
