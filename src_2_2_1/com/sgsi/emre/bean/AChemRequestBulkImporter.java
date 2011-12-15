package com.sgsi.emre.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


/**
 *  
 * AChemRequestBulkImporter
 * 
 * Imports an XLS file in A-Chem Run Worksheet format.
 * Used by Analytical Chemistry Lab task Update A-Chem Request.
 * 			
 * @author TJ Stevens/Wildtype for SGI
 * @created 9/2008
 */
public class AChemRequestBulkImporter extends SampleManifestImporter
{
	HashMap inlineHeaders = new HashMap();
	  /**
   * Parses an Excel (.xls) file in bulk import format
   * and makes available the file's task values for use by task class.
   * @param inFile 
   * @param taskName - which task to perform bulk import for
   * @return list list of samples found
   */
  public ArrayList importXLSForBulkImport(File inFile, String taskName)
  {
	items = new ArrayList();
    try
    {
    	// -- only the first (zero-based sheet will be imported
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      HSSFSheet sheet = wb.getSheetAt(0); // zero-based
		// find the header row
		Iterator rowItor = sheet.rowIterator();
		while (rowItor.hasNext())
		{
			// walk the file by rows, looking for header row
			HSSFRow row = (HSSFRow) rowItor.next();
			if (isHeaderRow(row))
			{
				ayHeaders = setHeaders(row);
				// next row is a submission (won't handle a blank row under header row)
				processSampleRows(rowItor, ayHeaders, taskName);
				break;
			}
			else
			{
				// add inline header
				setInlineHeader(row,inlineHeaders);
			}
		}// next row, looking for headers
		// at break, have processed all samples - if any were found
      return items;
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      throw new LinxUserException(e);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new LinxUserException(e);
    }
  }
  

	/**
	 * Maps key/value pair from this row, which is
	 * expected to contain an inline header.
	 * 
	 * @param row
	 * @param map for storing new inline header key/value
	 */
	protected void setInlineHeader(HSSFRow row, HashMap map)
    {
    	HSSFCell cell = (HSSFCell)row.getCell((short)0);
    	if(cell == null)
    	{
    		return; // blank row
    	}
		String key   = getValueAsString(cell);
		
		cell = (HSSFCell)row.getCell((short)1);
		if(cell == null)
		{
			map.put(key, "not provided");
		}
		else
		{
			String cellVal = null;
			if(key.toUpperCase().indexOf("DATE") > -1)
			{
				// special formatting for dates
				try
				{
					cellVal =  String.valueOf(getNumericValueAsInteger(cell));
				}
				catch (RuntimeException e)
				{
					throw new LinxUserException("The value for file header [" + key + "] is not in correct format."
							+ " Please correct this value in the file, then try again.");
				}
				
			}
			else
			{
				cellVal = getValueAsString(cell);
			}
    		map.put(key,cellVal);
		}
    	
	}
	
	public HashMap getInlineHeaders()
	{
		return this.inlineHeaders;
	}
	
	public String getInlineHeader(String property)
	{
		return (String)inlineHeaders.get(property);
	}
 
 /**
	 * Returns true if row contains an expected header.
	 * 
	 * @param row
	 * @return true if header in row
	 */
	protected boolean isHeaderRow(HSSFRow row)
	{
		HSSFCell cell = row.getCell((short) 0);
		int cellType = cell.getCellType();
		if (cellType == HSSFCell.CELL_TYPE_STRING)
		{
			String cellVal = getValueAsString(cell);
			if (!WtUtils.isNullOrBlank(cellVal)
					&& cellVal.equalsIgnoreCase("Submission ID"))
			{
				return true;
			}
		}
		return false;
	}
  

	/**
   * Returns the numeric cell's value as String. Overridden
   * to eliminate ".0" added by POI Excel parsing library. 
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
