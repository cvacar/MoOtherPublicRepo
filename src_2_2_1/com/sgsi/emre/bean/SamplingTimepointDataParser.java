package com.sgsi.emre.bean;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Data container for Grab Data import file used by tasks Submit Grab Data and
 * Edit Grab Data.
 * 
 * @author TJS/Wt for SGI
 * @date 3/2011
 * 
 */

public class SamplingTimepointDataParser extends POIParser
{
	SubmitGrabData	task		= null;
	XLSParser				parser	= null;

	/**
	 * Empty constructor allows access to XLS file convenience methods that use
	 * POI API.
	 */
	public SamplingTimepointDataParser()
	{
	}

	/** NOT IN USE -- see SvSubmitGrabData.processWorksheet() instead
	 * 
	 * Returns true if data rows were found in this worksheet
	 * 
	 * @param bAllowNew
	 *          -- true = submit, false = just update
	 * @param sheet
	 * @param sheetNameIn
	 * @param importId
	 * @param columnKey
	 * @param submitGrabData
	 * @param dbHelper
	 * @param db
	 * @return
	 *
	public void parseXLS(boolean bCreateNew, HSSFSheet sheet, String sheetNameIn,
			String importId, String columnKey, SubmitGrabData task,
			EMREDbHelper dbHelper, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();

		try
		{
			// assumes that all worksheets in this template share the column key
			HSSFRow headerRow = getRowWithColumnHeaders(sheet, getSheetName(),
					columnKey);
			int startRowIdx = headerRow.getRowNum() + 1; // keep a finger on start row
			int dataRowIdx = startRowIdx;
			HSSFRow dataRow = sheet.getRow(dataRowIdx);
			HSSFCell cell = null;
			String colName = null;
			String cellValue = null;

			// assumes that all worksheets in this template share the column key
			int dataStarts = 1 + getRowWithColumnHeaders(sheet, sheet.getSheetName(),
					columnKey).getRowNum();

			Iterator<Row> rowItor = sheet.rowIterator();
			while (rowItor.hasNext())
			{
				dataRow = (HSSFRow) rowItor.next();
				if (dataRow.getRowNum() < dataStarts)
				{
					continue; // looking for first row of data values
				}
				String samplingId = getRequiredProperty(dataRow, "Sampling ID");
				String culture = getRequiredProperty(dataRow, "Culture ID");
				String timept = getRequiredProperty(dataRow, "Sampling Timepoint");

				if (bCreateNew)
				{
					params.clear();
					params.add(samplingId);
					params.add(culture);
					params.add(timept);
					params.add(task.getTranId() + "");
					try
					{
						// inserts new SAMPLINGTIMEPOINT record if needed
						// -- assumes core tables are already updated
						dbHelper.callStoredProc(db, "spEMRE_insertSamplingTimepointData", params,
								true);
					}
					catch (Exception ex)
					{
						throw new LinxUserException("In worksheet [" + getSheetName()
								+ "], row " + dataRow.getRowNum() + ", value "
								+ cell.toString() + ": " + ex.getMessage());
					}
				}
				// start inserting generic data types
				int lastCellIdx = dataRow.getLastCellNum();
				for (int cellIdx = EMREStrings.GrowthRecovery.requiredSamplingDataColumnHeaders.length; cellIdx < lastCellIdx; cellIdx++)
				{
					cell = dataRow.getCell(cellIdx);
					if (isNullOrBlank(cell))
					{
						continue;
					}
					colName = getColumnNameByCellIndex(cellIdx);
					if (colName.endsWith("*"))
					{
						cellValue = getRequiredProperty(dataRow, colName);
					}
					else
					// optional property
					{
						cellValue = getValueAsString(cell);
					}
					if (WtUtils.isNullOrBlank(cellValue))
					{
						continue; // no value for this optional column
					}
					params.clear();
					params.add(samplingId);
					params.add(colName); // already trimmed/compressed
					params.add(cellValue);
					params.add(task.getTranId() + "");
					try
					{
						// insert SAMPLINGTIMEPOINTDATA table record
						// -- assumes core and SAMPLINGTIMEPOINT records already exist
						dbHelper.callStoredProc(db, "spEMRE_updateSamplingIDData", params,
								false);
					}
					catch (Exception ex)
					{
						throw new LinxUserException("In worksheet [" + getSheetName()
								+ "], row " + dataRow.getRowNum() + ", value "
								+ cell.toString() + ": " + ex.getMessage());
					}
				}// next column
			}// next data row
		}
		catch (Exception ex)
		{
			throw new LinxUserException(
					"Error occurred while trying to save sampling timepoint data: "
							+ ex.getMessage());
		}
	}*/

	public boolean isCultureID(HSSFCell cell, HSSFRow headerRow)
	{
		int cellIdx = cell.getColumnIndex();
		String colValue = headerRow.getCell(cellIdx).toString();
		if (colValue.equalsIgnoreCase("Culture ID"))
		{
			return true;
		}
		return false;
	}

	/**
	 * returns a data value for a given column throws an exception if the data is
	 * null
	 * 
	 * @param propertyName
	 *          Column name
	 * @return Data value for a given column name
	 * @throws Exception
	 * 
	 *           public String getRequiredProperty(String propertyName) throws
	 *           Exception { int index = getPropertyIndex(propertyName); String s
	 *           = null; try { Vector v = (Vector) rows.elementAt(currentRow);
	 *           Object o = v.elementAt(index); if (o == null) { throw new
	 *           Exception("Data for column '" + propertyName +
	 *           "' cannot be null."); } else if
	 *           (WtUtils.isNullOrBlank(String.valueOf(o).toUpperCase())) { throw
	 *           new Exception("Data for column '" + propertyName +
	 *           "' cannot be empty or blank."); } s = String.valueOf(o); } catch
	 *           (Throwable e) { throw new LinxUserException(e.getMessage()); }
	 *           return s; }
	 */
	/**
	 * Returns the custom db table field name for the given column name (often
	 * just by adding underscores).
	 * 
	 * @param colName
	 * @return db table field name SAMPLINGTIMEPOINT/HISTORY NOT IN USE eff v2.0
	 *         protected String getFieldNameByColumnName(String colName) { String
	 *         fieldName = colName;
	 * 
	 *         // as much as possible, mirror the worksheet columns // -- except
	 *         for problem characters if (fieldName.indexOf("%") > 0) { fieldName
	 *         = fieldName.replace("%", " "); fieldName = fieldName.trim() +
	 *         " percent"; } // eliminate spaces fieldName =
	 *         fieldName.replace('\n', ' '); while (fieldName.indexOf("  ") > 0) {
	 *         fieldName = fieldName.replace("  ", " "); } return fieldName; }
	 */

	/**
	 * Returns cell's value as string, calling getNumericValueAsString if
	 * necessary.
	 * 
	 * @param cell
	 * @return cell's value as String
	 * 
	 *         protected String getValueAsString(HSSFCell cell) { String value;
	 *         try {
	 * 
	 * 
	 *         if(cell.getCellType() == ) { //we've found the sampling timepoint
	 *         //it's a date column so format the date correctly Date d =
	 *         cell.getDateCellValue(); String dv = d.toLocaleString();//Sep 7,
	 *         2010 4:43:00 PM SimpleDateFormat df = new
	 *         SimpleDateFormat("MMM d, yyyyy h:mm:ss a"); Date nd = df.parse(dv);
	 * 
	 *         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm");
	 *         value = sdf.format(nd); return value;
	 * 
	 *         } else { value = cell.getRichStringCellValue().getString(); return
	 *         value; } } else { value =
	 *         cell.getRichStringCellValue().getString(); return value; } } catch
	 *         (Exception e) { return parser.getNumericValueAsString(cell); } }
	 */
}
