package com.sgsi.emre.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sgsi.bean.Sample;

import com.wildtype.linx.util.LimsImporter;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 *  
 * SampleManifestImporter
 * 
 * Imports an XLS file in Wayne Green's 
 * sample manifest formats.
 * Called by tasks that log samples.
 * 			
 * @author TJ Stevens/Wildtype for SGI
 * @created 12/2007
 */
public class SampleManifestImporter extends LimsImporter
{
  protected ArrayList items = null;
  protected ArrayList ayHeaders = null;
  protected String comment = null;
  
   /**
   * Parses an Excel (.xls) file in sample manifest format
   * and makes available the file's samples and row properties for each
   * sample.
   * @param inFile 
   * @param sampleType - null if all worksheets should be imported
   * @return list of list of samples found
   */
  public ArrayList importXLSForSampleManifest(File inFile, String sampleType /* null if all wkshts */)
  {
	items = new ArrayList();
	String sType = null;
    try
    {
 	  // compile a list of Sample objects 
      // from all worksheets in sample manifest
    	// -- some sheets may have no samples, which is ok
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
	    	// sample type is per-sheet	
	        HSSFRow row = (HSSFRow)rowItor.next();
	        if(isWorksheetTypeRow(row, "Sample Type"))
	        {
	        	sType = getWorksheetType(row, "Sample Type");
	        	if(sampleType == null)
	        	{
	        		// do all worksheets
	        		continue;
	        	}
	        	else if(!sType.equalsIgnoreCase(sampleType))
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
	          processSampleRows(rowItor, ayHeaders, sType);
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
   * Parses an Excel (.xls) file in sample manifest format
   * and makes available the file's samples and row properties for each
   * sample. This signature is used by task Log Sample, and imports
   * all worksheets in workbook with data rows.
   * @param inFile
   * @param values 
   * @param task 
   * @param db
   * @return file in common import format
   */
  public ArrayList importXLSForSampleManifest(File inFile)
  {
	return importXLSForSampleManifest(inFile, null /*all worksheets*/);
  }
    
  /**
   * Returns true if the given row contains
   * the expected sheet header 'Sample Type:'.
   * @param row
   * @return true if this row contains sheet's sample type
   */
    protected boolean isWorksheetTypeRow(HSSFRow row, String toMatch)
    {
    	HSSFCell cell = row.getCell((short) 0);
    	if(cell == null)
    	{
    		return false;
    	}
		int cellType = cell.getCellType();
		if (cellType == HSSFCell.CELL_TYPE_STRING)
		{
			String cellVal = getValueAsString(cell);
			if (!WtUtils.isNullOrBlank(cellVal)
					&& cellVal.startsWith(toMatch))
			{
				return true;
			}
		}
		return false;
	}

	/**
     * Given a rowItor on the header row, starts
     * with next row to process sample rows and 
     * create new Sample objects, breaking
     * after encountering any blank row.
     * @param rowItor
     * @return list of samples
     */
    protected void processSampleRows(Iterator rowItor, List ayHeaders, String sampleType)
    {
	   	try
    	{
	     while(rowItor.hasNext())
	     {
	    	 HSSFRow row = (HSSFRow)rowItor.next();
	    	 if(isBlankRow(row))
	    	 {
	    		 break; // stop looking at first blank row
	    	 }
	    	 
		      // New Sample ID|Field Name|Description|Contributor|Project|...
		      Sample sample = saveRowAsSample(row);
		      String barcode = sample.getSGIBarcode();
		      if(!WtUtils.isNullOrBlank(barcode))
		      {
		    	sample.setColumnHeaders(ayHeaders);
		    	sample.setSampleType(sampleType);
		      	items.add(sample);
		      }
	        
	      }// next row
	      // at exit, samples is a list of samples 
	     // found in this sheet of manifest

	    }
	    catch (IOException e)
	    {
	      e.printStackTrace();
	      throw new LinxUserException(e);
	    }
	  }
 
    /**
     * Returns true if this row has no value 
     * in the cell (usually SampleName)
     * of a sample data row. Call only after header row
     * has been identified and sample rows started.
     * @param row
     * @return true if blank sample row, otherwise false
     */
  protected boolean isBlankRow(HSSFRow row)
	{
		HSSFCell cell = row.getCell((short)0);
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
		{
			return true;
		}
		return false;
	}

/**
 * Returns a list of column header strings collected during file parsing. Don't
 * call until after importing file. Called by task Make CSS.
 * 
 * @return
 */
	public ArrayList getHeaders()
	{
		return this.ayHeaders;
	}

/**
   * Generically stores headers from file,
   * so file format can change without badly affecting
   * code. Caveat: header row must start with "New ...",
   * and data row must be the next immediate next row.
   * @param row object
   * @return list of column headers
   */
  protected ArrayList setHeaders(HSSFRow row)
{
	  ArrayList list = new ArrayList();
	    
      // -- store the headers to use as map keys later
	  Iterator cellItor = row.cellIterator();
	  while(cellItor.hasNext())
	  {
		  HSSFCell cell = (HSSFCell)cellItor.next();
		  String colHeader   = getValueAsString(cell);
		  if(WtUtils.isNullOrBlank(colHeader))
		  {
			  break;
		  }
		  list.add(colHeader);
		  // stop at first blank column
	  }// next col header
	  // at exit, list is a list of column headers on this sheet
      	
    return list;
}
  
  /**
   * Returns the value of inline header 'Sample Type:'
   * which should be one of supported sample types,
   * e.g. Aquatic, Enrichment, et al.
   * @param row
   * @return sample type
   */
  protected String getWorksheetType(HSSFRow row, String toMatch)
{
	  String s = row.getCell((short)0).getStringCellValue();
	  if(!s.startsWith(toMatch))
	  {
		  throw new LinxUserException(
				  "Expected row containing sheet header '" + toMatch + "',"
				  + " but found " + s);
	  }
	  int iType = row.getCell((short)1).getCellType();
	  if(iType == HSSFCell.CELL_TYPE_BLANK)
	  {
		  throw new LinxUserException("Format of selected file does not match the expected format."
				  + " Cell containing '" + toMatch + "' should be followed by a cell containing"
				  + " one of the supported types."
				  + " Please review the template, correct this file, then try again.");
	  }
	  String type = row.getCell((short)1).getStringCellValue();
	  return type;
}

/**
   * Populates a new Sample object from row data.
   * Does not check for any particular columns,
   * nor complain about missing values. Uses list
   * of column headers collected earlier to set
   * properties generically on Sample object.
   * @param row
   * @return Sample object
   * @throws IOException 
   */
  protected Sample saveRowAsSample(HSSFRow row)
    throws IOException
  {
	Sample sample = new Sample();
	
	// store each column's value in the bean
    Iterator cellItor = row.cellIterator();
    while(cellItor.hasNext())
    {
      HSSFCell cell = (HSSFCell)cellItor.next();
      if(cell.getCellNum()+1 > ayHeaders.size())
      {
    	  break; // end of columns we care about
      }
      String key 	= (String)ayHeaders.get(cell.getCellNum()/*colIdx*/);
      String cellVal= getValueAsString(cell);
      sample.setProperty(key, cellVal);
      if(cell.getCellNum() == (short)0)
      {
    	  // barcode column in all cases
    	  sample.setSGIBarcode(cellVal);
      }
    }// next col
    // at exit, have set each XLS field's value on sample object
    return sample;
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
    return s;
  }
  

  /**
   * Returns the numeric cell's value as String.
   * @param cell 
   * @return number in cell as string
   */
  protected int getNumericValueAsInteger(HSSFCell cell)
  {
    // primitive parser has no 'getObjectCellValue()' option
    Double d = new Double(cell.getNumericCellValue());
    int iv = d.intValue();
    return iv;
  }
  
  /**
   * Returns cell's value as string, calling
   * getNumericValueAsString if necessary.
   * @param cell
   * @return cell's value as String
   */
  protected String getValueAsString(HSSFCell cell)
	{
		try
		{
			String value = cell.getRichStringCellValue().getString();
			// value = value.replaceAll("\\s","");
			return value;
		}
		catch (RuntimeException e)
		{
			return getNumericValueAsString(cell);
		}
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
		if(cell == null)
		{
			return false;
		}
		int cellType = cell.getCellType();
		if (cellType == HSSFCell.CELL_TYPE_STRING)
		{
			String cellVal = getValueAsString(cell);
			if (!WtUtils.isNullOrBlank(cellVal))
			{
				return true;
			}
		}
		return false;
	}
  
}
