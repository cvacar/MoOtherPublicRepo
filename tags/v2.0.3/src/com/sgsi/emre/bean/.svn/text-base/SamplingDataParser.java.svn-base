package com.sgsi.emre.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Data container for Grab Data import file used by tasks Submit Grab
 * Data and Edit Grab Data.
 * 
 * @author TJS/Wt for SGI
 * @date 3/2011
 * 
 */

public class SamplingDataParser 
{
	SubmitGrabData	task		  = null;
	XLSParser				parser	  = null;
	String 					sheetName = null; // changes w/each parser use

	/**
	 * Empty constructor allows access to XLS file 
	 * convenience methods that use POI API.
	 */
	public SamplingDataParser(){}

	/**
	 * Called by Submit Grab Data to create new records
	 * in custom db table SAMPLINGTIMEPOINTDATA.
	 * @param sheet
	 * @param sheetNameIn
	 * @param importId
	 * @param columnKey
	 * @param task
	 * @param dbHelper
	 * @param db
	 * @return true if worksheet has data values
	 */
	public ArrayList<String> insertDataValues(HSSFSheet sheet, String sheetNameIn,
			String importId, String columnKey, SubmitGrabData task, EMREDbHelper dbHelper,
			Db db)
	{
		setSheetName(sheetNameIn);
		return parseXLS(true, sheet, sheetNameIn, importId, columnKey, task, dbHelper, db);
	}
	
	/**
	 * Called by Edit Grab Data to create new history record 
	 * in custom db table SAMPLINGTIMEPOINTDATAHISTORY and to
	 * update the existing values in SAMPLINGTIMEPOINTDATA custom table.
	 * @param sheet
	 * @param sheetNameIn
	 * @param importId
	 * @param columnKey
	 * @param task
	 * @param dbHelper
	 * @param db
	 * @return true if worksheet has data values
	 */
	public ArrayList<String> updateDataValues(HSSFSheet sheet, String sheetNameIn,
			String importId, String columnKey, SubmitGrabData task, EMREDbHelper dbHelper,
			Db db)
	{
		setSheetName(sheetNameIn);
		return parseXLS(false, sheet, sheetNameIn, importId, columnKey, task, dbHelper, db);
	}
	
/**
 * Returns true if data rows were found in this worksheet
 * @param bAllowNew -- true = submit, false = just update
 * @param sheet
 * @param sheetNameIn
 * @param importId
 * @param columnKey
 * @param submitGrabData
 * @param dbHelper
 * @param db
 * @return
 */
	protected ArrayList<String> parseXLS(boolean bAllowNew, HSSFSheet sheet,
			String sheetNameIn, String importId, String columnKey,
			SubmitGrabData task, EMREDbHelper dbHelper, Db db)
	{
		boolean KEY_VALUE_NOT_FOUND = false;
		boolean KEY_VALUES_FOUND = true;
		ArrayList<String> cultures = new ArrayList<String>();
		ArrayList<String> params = new ArrayList<String>();

		try
		{
			// assumes that all worksheets in this template share the column key
			HSSFRow headerRow = getRowWithColumnHeaders(sheet, sheetName, columnKey);
			int startRowIdx = headerRow.getRowNum() + 1; // keep a finger on start row
			int dataRowIdx = startRowIdx;
			HSSFRow dataRow = sheet.getRow(dataRowIdx);
			HSSFCell col = null;
			HSSFCell cell = null;
			String cellValue = null;
			String colValue = null;

			// start importing data rows
			for (int rowIdx = startRowIdx;; rowIdx++)
			{
				params.clear();
				dataRow = sheet.getRow(rowIdx);

				// add required header values to param list
				// -- must appear in first columns, 
				// -- but could refactor to find dynamically
				for (int cellIdx = 0; cellIdx < EMREStrings.GrowthRecovery.requiredSamplingDataColumnHeaders.length; cellIdx++)
				{
					cell = dataRow.getCell((short) cellIdx);
					cellValue = getValueAsString(cell);
					if (WtUtils.isNullOrBlank(cellValue))
					{
						if(dataRow.getRowNum() > startRowIdx || cellIdx == 0)
						{
							return cultures; // end of data rows
						}
						if(!bAllowNew)
						{
							break; // nothing to edit on this worksheet
						}
						throw new LinxUserException("Missing a value in required column "
								+ EMREStrings.GrowthRecovery.requiredSamplingDataColumnHeaders[cellIdx]
								+ " in worksheet [" + sheetName + "],"
								+ " row " + String.valueOf(dataRow.getRowNum())); 
					}
					// else, add this value to params
					params.add(cellValue);
					if(isCultureID(cell, headerRow))
					{
						cultures.add(cellValue);
					}
				}// next required col header value
				// at exit, have returned 'false' to caller if missing a key col value
				if(bAllowNew)
				{
					params.add(task.getTranIdAsString());
					try
					{
						dbHelper.callStoredProc(db, "spEMRE_insertSamplingIDData", params,
								true);
					}
					catch (Exception ex)
					{
						throw new LinxUserException(
								"In worksheet [" + sheetName + "], row " 
								+ dataRow.getRowNum()
								+ ", value " + cell.toString() + ": " + ex.getMessage());
					}
				}
				// or else added all required header values to params list
				for (int cellIdx = EMREStrings.GrowthRecovery.requiredSamplingDataColumnHeaders.length + 1;; cellIdx++)
				{
					params.clear();
					col = headerRow.getCell((short) cellIdx);
					colValue = getValueAsString(col);
					if (WtUtils.isNullOrBlank(colValue))
					{
						// end of columns
						break; 
					}
					// add an unknown 'grab data' column's value, if present
					cell = dataRow.getCell((short) cellIdx); 
					cellValue = getValueAsString(cell);
					if (WtUtils.isNullOrBlank(cellValue))
					{
						continue; // no value for this column
						// -- that's ok: many cols are optional
					}
					params.add(getValueAsString(dataRow.getCell((short)0)));
					params.add(colValue);
					params.add(cellValue);
					params.add(task.createHistoryRecord()); // T if LIMS should
					params.add(task.getTranIdAsString());
					try
					{
						dbHelper.callStoredProc(db, "spEMRE_updateSamplingIDData", params,
								false);
					}
					catch (Exception ex)
					{
						throw new LinxUserException(
								"In worksheet [" + sheetName + "], row " 
								+ dataRow.getRowNum()
								+ ", value " + cell.toString() + ": " + ex.getMessage());					}
				}// next column
			}// next data row
		}
		catch (Exception ex)
		{
			throw new LinxUserException(
					"Error occurred while trying to save sampling timepoint data: "
							+ ex.getMessage());
		}
	}

	 /**
   * Returns cell's value as string, calling
   * getNumericValueAsString if necessary.
   * @param cell
   * @return cell's value as String
   *
  protected String getValueAsString(HSSFCell cell)
	{
	  String value;
		try
		{
			
				
				if(cell.getCellType() == )
				{
					//we've found the sampling timepoint
					//it's a date column so format the date correctly
					Date d = cell.getDateCellValue();
					String dv = d.toLocaleString();//Sep 7, 2010 4:43:00 PM
					SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyyy h:mm:ss a");
					Date nd = df.parse(dv);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm");
					value = sdf.format(nd);
					return value;
					
				}
				else
				{
					value = cell.getRichStringCellValue().getString();
					return value;
				}
			}
			else
			{
				value = cell.getRichStringCellValue().getString();
				return value;
			}
		}
		catch (Exception e)
		{
			return parser.getNumericValueAsString(cell);
		}
	}
  */

	protected boolean isCultureID(HSSFCell cell, HSSFRow headerRow)
	{
		short cellIdx = cell.getCellNum();
		String colValue = headerRow.getCell(cellIdx).toString();
		if(colValue.equalsIgnoreCase("Culture ID"))
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
	public String getRequiredProperty(String propertyName) throws Exception
	{
		int index = getPropertyIndex(propertyName);
		String s = null;
		try
		{
			Vector v = (Vector) rows.elementAt(currentRow);
			Object o = v.elementAt(index);
			if (o == null)
			{
				throw new Exception("Data for column '" + propertyName
						+ "' cannot be null.");
			}
			else if (WtUtils.isNullOrBlank(String.valueOf(o).toUpperCase()))
			{
				throw new Exception("Data for column '" + propertyName
						+ "' cannot be empty or blank.");
			}
			s = String.valueOf(o);
		}
		catch (Throwable e)
		{
			throw new LinxUserException(e.getMessage());
		}
		return s;
	}
*/
	/**
	 * Returns the custom db table field name for the given column name (often
	 * just by adding underscores).
	 * 
	 * @param colName
	 * @return db table field name SAMPLINGTIMEPOINT/HISTORY
	 */
	protected String getFieldNameByColumnName(String colName)
	{
		String fieldName = colName;

		// as much as possible, mirror the worksheet columns
		// -- except for problem characters
		if (fieldName.indexOf("%") > 0)
		{
			fieldName = fieldName.replace("%", " ");
			fieldName = fieldName.trim() + " percent";
		}
		// eliminate spaces
		fieldName = fieldName.replace('\n', ' ');
		while (fieldName.indexOf("  ") > 0)
		{
			fieldName = fieldName.replace("  ", " ");
		}
		return fieldName;
	}
	
	/**
	 * Finds a column header that matches dataType and sets the value of the cell
	 * in the row below to dataValue.
	 * 
	 * @param headerRow
	 * @param dataType
	 * @param dataRow
	 * @param dataValue
	 */
	public void setCellValue(HSSFRow headerRow, String dataType,
			HSSFRow dataRow, String dataValue)
	{

		Iterator colItor = headerRow.cellIterator();
		Iterator cellItor = dataRow.cellIterator();

		while (colItor.hasNext())
		{
			HSSFCell col = (HSSFCell) colItor.next(); 

			String colName = getValueAsString(col);
			if(WtUtils.isNullOrBlank(colName))
			{
				// end of col headers
				break;
			}
			else if (colName.trim().equalsIgnoreCase(dataType))
			{
				short colIdx = col.getCellNum();
				HSSFCell dataCell = (HSSFCell)dataRow.getCell(colIdx);
				dataCell.setCellValue(dataValue);
				return;
			}
		}// next col
		// if we fall thru, no such column name found
		Code.warning("Could not find a column named '" + dataType + "'");

	}

	/**
	 * Returns the worksheet whose name starts with the same first letter as the
	 *  [G,S,W].
	 * 
	 * @param wb
	 * @param toMatch
	 * @return sheet whose first letter matches first letter of toMatch
	 */
	public HSSFSheet getWorksheet(HSSFWorkbook wb, String toMatch)
	{
		// expecting multiple sheets (currently 3) per workbook
		int sheetCount = wb.getNumberOfSheets();
		for (int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
		{
			HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
			// for each sheet, compare name to samplingId
			String sheetName = wb.getSheetName(sheetIdx);
			if (sheetName.startsWith(toMatch.substring(0, 1)))
			{
				setSheetName(sheetName); // stupid POI API has no such feature
				return sheet;
			}
		}// next sheet
		// if we fall thru, no matching sheet (unusual)
		throw new LinxUserException(
				"Could not find a matching worksheet matching " + toMatch);
	}
	
	/**
	 * Returns row starting with "header".
	 * 
	 * @param sheet
	 * @param sheetName -- for informative error msg only
	 * @param header
	 * @return String value of given inline header
	 */
	public String getValueForInlineHeader(HSSFSheet sheet, String sheetName, String header)
	{
		short COL_A = (short)0;
		short COL_B = (short)1;
		
		XLSParser parser = new XLSParser();
		Iterator rowItor = sheet.rowIterator();
		while (rowItor.hasNext())
		{
			// walk the file by rows, looking for inline header in A column
			HSSFRow row = (HSSFRow) rowItor.next();
			String cellValue = getValueAsString(row.getCell(COL_A));
			if (cellValue.equalsIgnoreCase(header))
			{
				String inlineValue = getValueAsString(row.getCell(COL_B));
				return inlineValue;
			}
			if(row.getRowNum() > 50) break; // unlikely to be after this
		}// next row
		throw new LinxUserException("Could not find a row "
				+ "containing required inline header '" + header + "'"
				+ " in worksheet named " + sheetName);
	}

	/**
	 * Returns row starting with "Sampling ID" so that caller can walk columns.
	 * 
	 * @param sheet
	 * @param sheetName -- for informative error msg only
	 * @param columnKey
	 * @return row containing column headers
	 */
	public HSSFRow getRowWithColumnHeaders(HSSFSheet sheet, String sheetName, String columnKey)
			throws Exception
	{
		XLSParser parser = new XLSParser();

		Iterator rowItor = sheet.rowIterator();
		while (rowItor.hasNext())
		{
			// walk the file by rows, looking for header row
			HSSFRow row = (HSSFRow) rowItor.next();
			String cellValue = getValueAsString(row.getCell((short)0));
			if (cellValue.equalsIgnoreCase(columnKey))
			{
				return row;
			}
		}// next row
		throw new LinxUserException("Could not find a row "
				+ "containing required column header '" + columnKey + "'"
				+ " in worksheet named " + sheetName);
	}
	/**
	 * @return the sheetName
	 */
	public String getSheetName()
	{
		return sheetName;
	}
	/**
	 * @param sheetName the sheetName to set
	 */
	public void setSheetName(String sheetName)
	{
		this.sheetName = sheetName;
	}

	/**
	 * Returns cell's value as string. 
	 * Excel macro formats dates, so this method does not
	 * reformat dates. Copied from Java developer's blog
	 * http://epramono.blogspot.com/2004/12/poi-for-excel-parser.html
	 * 
	 * 
	 * @param cell
	 * @return cell's value as String
	 */
	public String getValueAsString(HSSFCell cell)
	{
	    if (cell == null) return null;
	    
	    String result = null;

	    int cellType = cell.getCellType();
	    switch (cellType) {
	      case HSSFCell.CELL_TYPE_BLANK:
	        result = "";
	        break;
	      case HSSFCell.CELL_TYPE_BOOLEAN:
	        result = cell.getBooleanCellValue() ?
	          "true" : "false";
	        break;
	      case HSSFCell.CELL_TYPE_ERROR:
	        result = "ERROR: " + cell.getErrorCellValue();
	        break;
	      case HSSFCell.CELL_TYPE_NUMERIC:
	      {
	        HSSFCellStyle cellStyle = cell.getCellStyle();
	        short dataFormat = cellStyle.getDataFormat();

	        // assumption is made that dataFormat = 15,
	        // when cellType is HSSFCell.CELL_TYPE_NUMERIC
	        // is equal to a DATE format.
	        if (dataFormat == 15 || dataFormat == 169) 
	        {
							//we've found the sampling timepoint
							//it's a date column so format the date correctly
						Date d = cell.getDateCellValue();
						String dv = d.toLocaleString();//Sep 7, 2010 4:43:00 PM
						SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyyy h:mm:ss a");
						Date nd;
						try
						{
							nd = df.parse(dv);
						}
						catch (ParseException ex)
						{
							throw new LinxUserException(ex.getMessage());
						}
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						result = sdf.format(nd);

	        } 
	        else 
	        {
	          result = getNumericValueAsString(cell);
	        }
	        break;
	      }
	      case HSSFCell.CELL_TYPE_STRING:
	        result = cell.toString();
	        break;
	      default: break;
	    }

	    return result;
	    
	}

  /**
   * Returns the numeric cell's value as String.
   * @param cell 
   * @return number in cell as string
   */
  protected String getNumericValueAsString(HSSFCell cell)
  {
    // primitive parser has no 'getObjectCellValue()' option
	  String s = cell.toString();
	    if(!WtUtils.isNullOrBlank(s) && s.endsWith(".0"))
	    {
	    	// eliminate decimal value added by third-party parsing library
	    	s = s.substring(0, s.lastIndexOf(".0"));
	    }
	    return s;
  }
}
