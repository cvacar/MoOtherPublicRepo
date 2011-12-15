package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.SamplingDataParser;
import com.sgsi.emre.task.EditGrabData;
import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * 
 * SvEditSamplingData
 * 
 * Overridden to allow user to retrieve a file containing timepoint data to edit
 * and resubmit. Used mostly by submitter to fill in data that was missing when
 * originally submitted at Submit Grab Data.
 * 
 * @author TJS/Wildtype for SGI
 * @date 3/2011
 */
public class SvEditGrabData extends EMREServlet
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
			try
			{
				String samplingId = task.getDisplayItemValue(ItemType.SAMPLING_ID);
				if (WtUtils.isNullOrBlankOrPlaceholder(samplingId))
				{
					throw new LinxUserException(
							"Please enter a Sampling ID, then try again.");
				}
				return getFile(samplingId, request, response, (EditGrabData) task, db);
			}
			catch (Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/**
	 * Downloads to client an MS Excel file containing only the SAMPLINGTIMEPOINT
	 * data rows for given sampling ID.
	 * 
	 * @param samplingId
	 * @param task
	 * @param response
	 * @param db
	 * @return
	 */
	protected boolean getFile(String samplingId, HttpServletRequest request,
			HttpServletResponse response, SubmitGrabData task, Db db)
	{
		// get a copy of the template
		String pathToTemplate = dbHelper.getApplicationValue(db, "FILETYPE",
				"Grab Data Bulk Import Template");
		String pathToTemp = dbHelper.getApplicationValue(db, "FILETYPE",
				"CultureGrabDataImportFile");

		File templateFile = new File(pathToTemplate);
		File newFile = null;
		try
		{
			// make a copy of the template so we don't tie it up
			newFile = new File(samplingId + ".xls");
			WtUtils.copyFile(templateFile, pathToTemplate, newFile, pathToTemp);

			// create an xls worksheet (=our new file)
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(newFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			SamplingDataParser parser = new SamplingDataParser(); // a container w/util methods
			HSSFSheet sheet = parser.getWorksheet(wb, samplingId);
			HSSFRow headerRow = 
				parser.getRowWithColumnHeaders(sheet, parser.getSheetName(), "Sampling ID");
			HSSFRow dataRow = sheet.getRow(headerRow.getRowNum()+1); 
			// by here, at first data row 
			
			// populate blank row with data values by matching datatype = col name
			// -- skips obsolete cols no longer appearing in template
			String sql = "exec spEMRE_getSamplingDataBySamplingID '" + samplingId
					+ "'";
			ResultSet rs = dbHelper.getResultSet(sql, db);
			// set values for known columns

			rs.next();
			String sampId = rs.getString(1);
			String cultureId = rs.getString(2);
			String timepoint = rs.getString(3);
			String username = rs.getString(4);
			String dataType = rs.getString(5);
			String dataValue = rs.getString(6);

			// populate known columns
			parser.setCellValue(headerRow, "Sampling ID", dataRow, sampId);
			parser.setCellValue(headerRow, "Culture ID", dataRow, cultureId);
			parser.setCellValue(headerRow, "Sampling timepoint", dataRow, timepoint);
			parser.setCellValue(headerRow, "Windows Login", dataRow, username);

			// start populating unknown "grab data" columns
			parser.setCellValue(headerRow, dataType, dataRow, dataValue);

			// continue populating unknown number/type of "grab data" columns
			while (rs.next())
			{
				sampId = rs.getString(1); // ignored -- already set above
				cultureId = rs.getString(2); // ignored -- already set above
				timepoint = rs.getString(3); // ignored -- already set above
				username = rs.getString(4); // ignored -- already set above
				// new "grab data" value
				dataType = rs.getString(5);
				dataValue = rs.getString(6);

				// add to our growing row
				parser.setCellValue(headerRow, dataType, dataRow, dataValue);
			}// next datatype/datavalue pair
			// at exit, have set as many col values as we could find matches for

			// download finished file
			task.setMessage("Successfully created data file to download.");
			// backflips to download from POI library object
			File finFile = new File("out.xls");
			FileOutputStream fos = new FileOutputStream(finFile);
			wb.write(fos);
			fos.flush();
			fos.close(); //<-- essential to make this work
			returnDownloadAsByteStream(response, finFile, samplingId + ".xls",
					"application/vnd.ms-excel", false);

		}
		catch (Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return ALL_DONE;
	}


}
