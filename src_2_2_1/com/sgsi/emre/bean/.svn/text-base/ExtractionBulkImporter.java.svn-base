package com.sgsi.emre.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
 * ExtractionBulkImporter
 * 
 * Imports an XLS file in DNA extraction 
 * bulk import format, with fields
 * matching the Extract DNA task screen's fields.
 * 			
 * @author TJ Stevens/Wildtype for SGI
 * @created 6/2008
 */
public class ExtractionBulkImporter extends SampleManifestImporter
{
	
	  /**
   * Parses an Excel (.xls) file in bulk import format
   * and makes available the file's task values for use by task class.
   * @param inFile 
   * @param taskName - which task to perform bulk import for
   * @return list of list of samples found
   */
  public ArrayList importXLSForBulkImport(File inFile, String taskName)
  {
	items = new ArrayList();
    try
    {
 	  // compile a list of item objects 
      // from all worksheets in bulk importer
    	// -- only the sheet for the invoked task will be imported
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      int sheetCount = wb.getNumberOfSheets();
      for(int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
      {
	      HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
	      // find the header row
	      Iterator rowItor = sheet.rowIterator();
	      while(rowItor.hasNext())
	      {
	      	// walk the file by rows, looking for header row
	    	// task name is on each sheet	
	        HSSFRow row = (HSSFRow)rowItor.next();
	        if(isWorksheetTypeRow(row, "Task:"))
	        {
	        	String sType = getWorksheetType(row, "Task:");
	        	if(!sType.equalsIgnoreCase(taskName))
	        	{
	        		// looking for specific type of worksheet
	        		// -- and this isn't it
	        		break; // go look at next worksheet
	        	}
	        }
	        else if(isHeaderRow(row))
	        {
	          ayHeaders = setHeaders(row);
	          // next row is a sample (won't handle a blank row under header row)
	          processSampleRows(rowItor, ayHeaders, taskName);
	          break;
	        }
	      }// next row, looking for headers 
	      // at break, have processed all samples - if any were found
      }// next sheet
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
   * Parses an Excel (.xls) file in bulk import format
   * and makes available the file's task values for use by task class.
   * uses the column key to determine the column header row.  Must be the first column in file.
   * @param inFile 
   * @param taskName - which task to perform bulk import for
   * @return list of list of samples found
   */
  public ArrayList importXLSForBulkImport(File inFile, String taskName, String columnKey)
  {
	items = new ArrayList();
    try
    {
 	  // compile a list of item objects 
      // from all worksheets in bulk importer
    	// -- only the sheet for the invoked task will be imported
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      int sheetCount = wb.getNumberOfSheets();
      for(int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
      {
	      HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
	      // find the header row
	      Iterator rowItor = sheet.rowIterator();
	      while(rowItor.hasNext())
	      {
	      	// walk the file by rows, looking for header row
	    	// task name is on each sheet	
	        HSSFRow row = (HSSFRow)rowItor.next();
	        if(isWorksheetTypeRow(row, "Task:"))
	        {
	        	String sType = getWorksheetType(row, "Task:");
	        	if(!sType.equalsIgnoreCase(taskName))
	        	{
	        		// looking for specific type of worksheet
	        		// -- and this isn't it
	        		break; // go look at next worksheet
	        	}
	        }
	        else if(isHeaderRow(row, columnKey))
	        {
	          ayHeaders = setHeaders(row);
	          // next row is a sample (won't handle a blank row under header row)
	          processSampleRows(rowItor, ayHeaders, taskName);
	          break;
	        }
	      }// next row, looking for headers 
	      // at break, have processed all samples - if any were found
      }// next sheet
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
  
  protected boolean isHeaderRow(HSSFRow row, String colKey)
	{
		HSSFCell cell = row.getCell((short) 0);
		if(cell != null)
		{
			int cellType = cell.getCellType();
			if (cellType == HSSFCell.CELL_TYPE_STRING)
			{
				String cellVal = getValueAsString(cell);
				//if(cellVal.equalsIgnoreCase(colKey))
				if (!WtUtils.isNullOrBlank(cellVal) && cellVal.equalsIgnoreCase(colKey))
				{
					return true;
				}
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
