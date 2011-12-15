package com.sgsi.emre.bean;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

import com.wildtype.linx.log.Code;
import com.wildtype.linx.util.LimsImporter;
import com.wildtype.linx.util.LimsTokenizer;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Parses an excel xls file and puts it into the LimsImporter data container 
 * @author bobbyjo/Wildtype for SGSI
 * @date 08/2008
 */
public class XLSParser extends LimsImporter 
{
	protected String fileType = null;
	protected File dataFile = null;

	public XLSParser()
	{
		
	}
	public XLSParser(File dataFile, String fileType, char delim, String startHeading, String[] headerNames)
	{
	    //setData(dataFile);
	    dataDelim = new Character(delim);
	    refHeading = startHeading;
	    int l = headerNames.length;
	    vHeaderNames = new Vector();
	    for(int i = 0; i < l; i++)
	    {
	      vHeaderNames.addElement(headerNames[i].trim());
	    }
	    parseXLS(dataFile,fileType);
	}
	public XLSParser(char delim, String startHeading, String[] headerNames)
	{
	    //setData(dataFile);
	    dataDelim = new Character(delim);
	    refHeading = startHeading;
	    int l = headerNames.length;
	    vHeaderNames = new Vector();
	    for(int i = 0; i < l; i++)
	    {
	      vHeaderNames.addElement(headerNames[i].trim());
	    }
	}
	/**
	 * Constructor takes in bAllowMultipleSheets if true we allow multiple sheets in the workbook
	 * otherwise we don't
	 * @param dataFile
	 * @param fileType
	 * @param delim
	 * @param startHeading
	 * @param headerNames
	 * @param numWorkbookSheets
	 */
	public XLSParser(File dataFile, String fileType, char delim, String startHeading, 
			String[] headerNames, boolean bAllowMultipleSheets)
	{
	    //setData(dataFile);
	    dataDelim = new Character(delim);
	    refHeading = startHeading;
	    int l = headerNames.length;
	    vHeaderNames = new Vector();
	    for(int i = 0; i < l; i++)
	    {
	      vHeaderNames.addElement(headerNames[i].trim());
	    }
	    parseXLS(dataFile,fileType, bAllowMultipleSheets);
	}
	
	private void parseXLS(File dataFile, String fileType)
	{
		try
		{
			if(dataFile == null)
				throw new LinxUserException("Cannot import a null file.  Please set the file, then try again.");
			if(dataDelim == null)
				throw new LinxUserException("A delimiter must be set prior to importing data.");
			if(refHeading == null)
				Code.debug("No reference header provided; assuming file has no column headers.");
			if(vHeaderNames == null)
				vHeaderNames = new Vector();
			if(fileType == null || fileType.equals(""))
				throw new LinxUserException("The fileType must be set before trying to parse a file");
			
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(dataFile));
		    HSSFWorkbook wb = new HSSFWorkbook(fs);
		    int sheetCount = wb.getNumberOfSheets();
		    if(sheetCount > 1)
		    	throw new LinxUserException("There can only be one worksheet in this XLS file.  " +
		    			"Please delete extra sheets not related to this import, then try again.");
		   
		    HSSFSheet sheet = wb.getSheetAt(0); // zero-based
		    boolean bFoundSheet = false;
			// find the header row
			Iterator rowItor = sheet.rowIterator();
			while(refHeading != null && rowItor.hasNext())
			{
				// walk the file by rows, looking for header row
			    HSSFRow row = (HSSFRow)rowItor.next();
			    if(isWorksheetTypeRow(row, fileType))
			    {
			        //yay we found the correct sheet
			        bFoundSheet = true;
			        continue;
			    }
			    if(isColumnHeaderRow(row, refHeading))
			    {
			    	bFoundSheet = true;
			    	String currRow = createDelimitedString(row,dataDelim.charValue());
			    	this.setColumnNames(new LimsTokenizer(currRow,dataDelim.charValue()));
			    	break;
			    }
			    else if(bFoundSheet)
			    {
			    	// Get the first in-line header row.
			    	LimsTokenizer tk = new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue());
			        int tkCount = tk.getTokenCount();
			        // Lets make sure we have a pair of tokens.
			        if(tkCount > 1)
			        {
			            String s = tk.getTokenAt(0);
			            hshInline.put(tk.getTokenAt(0), tk.getTokenAt(1));
			        }
			        tk = null;
			    }
			}
			if(!bFoundSheet)
			{
				throw new LinxUserException("Cannot find a worksheet of type " + fileType + " in this Excel workbook.");
			}
		    while(rowItor.hasNext())//data rows
		    {
		    	HSSFRow row = (HSSFRow)rowItor.next();
		    	String s = null;
		    	try
		    	{
		    		s =  row.getCell((short)0).getStringCellValue();
		    	}
		    	catch(Exception ex)
		    	{
		    		//ignore;
		    	}
		    	if(s == null || s.equals("")) 
			    {
		    		//is the entire row blank?
		    		if(isBlankRow(row))
		    			continue;
			    }
			    if(refHeading != null 
	               && isColumnHeaderRow(row, refHeading))
		    	  {
		    		  //column headers again - lets ignore
		    		  continue;
		    	  }
		    	  else
		    	  {
		    		  this.appendRow(new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue()));
		    	  }
		    }
		}
		catch(LinxUserException e)
		{
			throw e;
		}
		catch(Exception ex)
		{
			bImported = false;
			throw new LinxUserException("Error occurred while parsing file: " + ex.getMessage());
		}
		bImported = true;
	}
	
//	public void parseXLSMediaSetup(File dataFile, String fileType)
//	{
//		try
//		{
//			if(dataFile == null)
//				throw new LinxUserException("Cannot import a null file.  Please set the file, then try again.");
//			if(dataDelim == null)
//				throw new LinxUserException("A delimiter must be set prior to importing data.");
//			if(refHeading == null)
//				Code.debug("No reference header provided; assuming file has no column headers.");
//			if(vHeaderNames == null)
//				vHeaderNames = new Vector();
//			if(fileType == null || fileType.equals(""))
//				throw new LinxUserException("The fileType must be set before trying to parse a file");
//			
//			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(dataFile));
//		    HSSFWorkbook wb = new HSSFWorkbook(fs);
//		    int sheetCount = wb.getNumberOfSheets();
//		    boolean bFoundSheet = false;
//		    Iterator rowItor = null;
//		    for(int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
//		    {
//		    	HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
//		    	// find the header row
//				rowItor = sheet.rowIterator();
//				while(refHeading != null && rowItor.hasNext())
//				{
//					// walk the file by rows, looking for header row
//				    HSSFRow row = (HSSFRow)rowItor.next();
//				    if(isWorksheetTypeRow(row, fileType))
//				    {
//				        //yay we found the correct sheet
//				        bFoundSheet = true;
//				        LimsTokenizer tk = new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue());
//				        int tkCount = tk.getTokenCount();
//				        // Lets make sure we have a pair of tokens.
//				        if(tkCount > 1)
//				        {
//				            String s = tk.getTokenAt(0);
//				            hshInline.put(tk.getTokenAt(0), tk.getTokenAt(1));
//				        }
//				        tk = null;
//				        continue;
//				    }
//				    if(isColumnHeaderRow(row, refHeading))
//				    {
//				    	bFoundSheet = true;
//				    	String currRow = createDelimitedString(row,dataDelim.charValue());
//				    	this.setColumnNames(new LimsTokenizer(currRow,dataDelim.charValue()));
//				    	break;
//				    }
//				    else if(bFoundSheet)
//				    {
//				    	// Get the first in-line header row.
//				    	LimsTokenizer tk = new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue());
//				        int tkCount = tk.getTokenCount();
//				        // Lets make sure we have a pair of tokens.
//				        if(tkCount > 1)
//				        {
//				            String s = tk.getTokenAt(0);
//				            hshInline.put(tk.getTokenAt(0), tk.getTokenAt(1));
//				        }
//				        tk = null;
//				    }
//				}
//				if(!bFoundSheet)
//				{
//					throw new LinxUserException("Cannot find a worksheet of type " + fileType + " in this Excel workbook.");
//				}
//			    while(rowItor.hasNext())//data rows
//			    {
//			    	HSSFRow row = (HSSFRow)rowItor.next();
//			    	String s = null;
//			    	try
//			    	{
//			    		s =  row.getCell((short)0).getStringCellValue();
//			    	}
//			    	catch(Exception ex)
//			    	{
//			    		//ignore;
//			    	}
//			    	if(s == null || s.equals("")) 
//				    {
//				    	//skip empty lines
//				    	continue;
//				    }
//				    else if(refHeading != null 
//		               && isColumnHeaderRow(row, refHeading))
//			    	  {
//			    		  //column headers again - lets ignore
//			    		  continue;
//			    	  }
//			    	  else
//			    	  {
//			    		  this.appendRow(new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue()));
//			    	  }
//			    }
//		    }
//			
//		}
//		catch(LinxUserException e)
//		{
//			throw e;
//		}
//		catch(Exception ex)
//		{
//			bImported = false;
//			throw new LinxUserException("Error occurred while parsing file: " + ex.getMessage());
//		}
//		bImported = true;
//	}
	
	protected void parseXLS(File dataFile, String fileType, boolean bAllowMultipleSheets)
	{
		try
		{
			if(dataFile == null)
				throw new LinxUserException("Cannot import a null file.  Please set the file, then try again.");
			if(dataDelim == null)
				throw new LinxUserException("A delimiter must be set prior to importing data.");
			if(refHeading == null)
				Code.debug("No reference header provided; assuming file has no column headers.");
			if(vHeaderNames == null)
				vHeaderNames = new Vector();
			if(fileType == null || fileType.equals(""))
				throw new LinxUserException("The fileType must be set before trying to parse a file");
			
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(dataFile));
		    HSSFWorkbook wb = new HSSFWorkbook(fs);
		    int sheetCount = wb.getNumberOfSheets();
		    if(!bAllowMultipleSheets)
		    {
		    	if(sheetCount > 1)
			    	throw new LinxUserException("There can only be one worksheet in this XLS file.  " +
			    			"Please delete extra sheets not related to this import, then try again.");
			   
		    }
		    boolean bFoundSheet = false;
		    Iterator rowItor = null;
		    for(int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++)
		    {
		    	HSSFSheet sheet = wb.getSheetAt(sheetIdx); // zero-based
				// find the header row
				rowItor = sheet.rowIterator();
				while(refHeading != null && rowItor.hasNext())
				{
					// walk the file by rows, looking for header row
				    HSSFRow row = (HSSFRow)rowItor.next();
				    if(isWorksheetTypeRow(row, "Task:") || isWorksheetTypeRow(row, "Sample Type:"))
				    {
				    	String sType = null;
				    	try
				    	{
				    		sType = getWorksheetType(row, "Task:");
				    	}
				    	catch(Exception ex)
				    	{
				    		//maybe the sheet strts with " Sample Type:"
				    		sType = getWorksheetType(row, "Sample Type:");
				    	}
			        	if(!sType.equalsIgnoreCase(fileType))
			        	{
			        		// looking for specific type of worksheet
			        		// -- and this isn't it
			        		break; // go look at next worksheet
			        	}
			        	else
			        	{
			        		//yay we found the correct sheet
			        		bFoundSheet = true;
					        LimsTokenizer tk = new LimsTokenizer(createDelimitedString(row,dataDelim), dataDelim.charValue());
					        int tkCount = tk.getTokenCount();
					        // Lets make sure we have a pair of tokens.
					        if(tkCount > 1)
					        {
					            String s = tk.getTokenAt(0);
					            hshInline.put(tk.getTokenAt(0), tk.getTokenAt(1));
					        }
					        tk = null;
					        continue;
			        	}
				        
				    }
				    else if(isColumnHeaderRow(row, refHeading))
				    {
				    	bFoundSheet = true;
				    	String currRow = createDelimitedString(row,dataDelim.charValue());
				    	this.setColumnNames(new LimsTokenizer(currRow,dataDelim.charValue()));
				    	break;
				    }
				}
				if(bFoundSheet)
					break;
			    
		    }
		    if(!bFoundSheet)
		    {
		    	throw new Exception("Unable to find the correct worksheet for " + fileType);
		    }
		    while(rowItor.hasNext())//data rows
		    {
		    	HSSFRow row = (HSSFRow)rowItor.next();
		    	String s = null;
		    	try
		    	{
		    		s =  row.getCell((short)0).getStringCellValue();
		    	}
		    	catch(Exception ex)
		    	{
		    		//ignore;
		    	}
		    	if(s == null || s.equals("")) 
			    {
		    		if(isBlankRow(row))
		    			continue;
			    }
			    if(refHeading != null 
	               && isColumnHeaderRow(row, refHeading))
		    	  {
		    		  //column headers again - lets ignore
		    		  continue;
		    	  }
		    	  else
		    	  {
		    		  String delimRow = createDelimitedString(row,dataDelim);
		    		  LimsTokenizer tk = new LimsTokenizer(delimRow, dataDelim.charValue());
		    		  int numTokens = tk.getTokenCount();
		    		  int numRequiredCols = vHeaderNames.size();
		    		  if(numTokens < numRequiredCols)
		    		  {
		    			  delimRow += dataDelim;
		    			  for(int i = numTokens; i < numRequiredCols; i++)
		    			  {
		    				  if( i < numRequiredCols - 1)
		    					  delimRow += dataDelim;
		    				  else
		    					  delimRow += "";
		    			  }
		    		  }
		    		  this.appendRow(new LimsTokenizer(delimRow, dataDelim.charValue()));
		    	  }
		    }
		}
		catch(Exception ex)
		{
			bImported = false;
			throw new LinxUserException("Error occurred while parsing file: " + ex.getMessage());
		}
		bImported = true;
	}
	
	/**
	   * Returns true if the given row contains
	   * the expected sheet header.
	   * @param row
	   * @return true if this row contains sheet's file type
	   */
	protected boolean isWorksheetTypeRow(HSSFRow row, String toMatch)
	{
		HSSFCell cell = row.getCell((short)0);
		if(cell == null)
		{
			return false;
		}
		String s = cell.getStringCellValue();
		if(!WtUtils.isNullOrBlank(s)
				  && s.startsWith(toMatch))
		{
			  return true;
		}
		return false;
	}
	
	/**
	   * Returns the value of inline header toMatch
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
	   * Iterates through a row in the file trying to find the key
	   * If the column key is found return true else false
	   * @param row The row in the file
	   * @param columnKey The unique identifier that tell me this is the row of column headers
	   * @return true if column key is found, false if not
	   * @throws Exception
	   */
	  public boolean isColumnHeaderRow(HSSFRow row, String columnKey)
	  	throws Exception
	  {
		  boolean bRtn = false;
		  try
		  {
			  Iterator cellIter = row.cellIterator();
			  while(cellIter.hasNext())
			  {
				  Object cell = cellIter.next();
				  String cellVal = cell.toString();
				  if(cellVal.equalsIgnoreCase(columnKey))
					  bRtn = true;
			  }
		  }
		  catch(Exception ex)
		  {
			  throw ex;
		  }
		  return bRtn;
	  }
	  
	  protected boolean isBlankRow(HSSFRow row)
	  	throws Exception
	  {
		  boolean bIsBlank = false;
		  String rowString = "";
		  try
		  {
			  for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext(); ) 
			  {
					HSSFCell cell = (HSSFCell)cit.next();
					String value = getValueAsString(cell);
	     			if(!WtUtils.isNullOrBlank(value))
	     				rowString += value;
			  }
			  if(WtUtils.isNullOrBlank(rowString))
				  bIsBlank = true;
		  }
		  catch(Exception ex)
		  {
			  throw ex;
		  }
		  return bIsBlank;
	  }
	  
	  protected String createDelimitedString(HSSFRow row, char delim)
	  	throws Exception
	  {
		  String rtn = "";
		  try
		  {
			  boolean isSubsequentCell = false;
			  int cellNum = 1;
			  for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext(); ) 
			  {
					HSSFCell cell = (HSSFCell)cit.next();
					int num = cell.getCellNum() + 1;
					if(cellNum != num)
					{
						do
						{
							rtn = rtn  + delim + '"' + " " + '"';
							cellNum++;
						}
						while(cellNum < num);
					}
						
					String value = getValueAsString(cell, num);
	     			  if(isSubsequentCell)
	     			  {
	     				  rtn = rtn + delim;
	     			  }
	     			  else
	     			  {
	     				  isSubsequentCell = true;
	     			  }
					  rtn = rtn + '"' + value + '"';
					  cellNum++;
				}

		  }
		  catch(Exception ex)
		  {
			  throw ex;
		  }
		  return rtn;
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
		public String getValueAsString(HSSFCell cell, int cellNum)
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
		      case HSSFCell.CELL_TYPE_FORMULA:
		        result = cell.getCellFormula();
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

		        } else {
		          result = String.valueOf (
		            cell.getNumericCellValue());
		        }
		        break;
		      }
		      case HSSFCell.CELL_TYPE_STRING:
		        result = cell.getStringCellValue();
		        break;
		      default: break;
		    }

		    return result;
		}
	  
  /**
   * Returns cell's value as string, calling
   * getNumericValueAsString if necessary.
   * @param cell
   * @return cell's value as String
   *
  protected String getValueAsString(HSSFCell cell, int cellNum)
	{
	  String value;
		try
		{
			//which cell is the Sampling timepoint in?
			int columnNum = 0;
			Vector<String> colNames  = this.getColumnNames();
			if(colNames != null)
			{
				for(int i = 0; i < colNames.size(); i++)
				{
					String col = colNames.get(i);
					if(col.equalsIgnoreCase("Sampling Timepoint") 
							|| col.contains("Timestamp"))
					{
						columnNum = i + 1;
						break;
					}
				}
				
				if(cellNum == columnNum)
				{
					//we've found the sampling timepoint
					//it's a date column so format the date correctly
					Date d = cell.getDateCellValue();
					String dv = d.toLocaleString();//Sep 7, 2010 4:43:00 PM
					SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyyy h:mm:ss a");
					Date nd = df.parse(dv);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
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
			return getNumericValueAsString(cell);
		}
	}
	*/
  
  /**
   * Returns cell's value as string, calling
   * getNumericValueAsString if necessary.
   * @param cell
   * @return cell's value as String
   */
  protected String getValueAsString(HSSFCell cell)
	{
	  String value;
		try
		{
			//which column is the Sampling timepoint in?
			int columnNum = 0;
			int cellNum = 0;
			Vector<String> colNames  = this.getColumnNames();
			if(colNames != null)
			{
				for(int i = 0; i < colNames.size(); i++)
				{
					String col = colNames.get(i);
					if(col.equalsIgnoreCase("Sampling Timepoint"))
					{
						columnNum = i + 1;
						cellNum = columnNum;
						break;
					}
				}
				
				if(cellNum == columnNum)
				{
					//we've found the sampling timepoint
					//it's a date column so format the date correctly
					Date d = cell.getDateCellValue();
					String dv = d.toLocaleString();//Sep 7, 2010 4:43:00 PM
					SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyyy h:mm:ss a");
					Date nd = df.parse(dv);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
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
			return getNumericValueAsString(cell);
		}
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

	public void appendRow(LimsTokenizer data)
	    throws Exception
	  {
	    if(data == null)
	    {
	      throw new Exception("Row Data cannot be null.");
	    }
	    try
	    {
	      Vector v = getTokens(data);
	      rows.addElement(v);
	    }
	    catch(Exception e)
	    {
	      throw new LinxUserException(e.getMessage());
	    }
	  }
	
	/**
	   * Retrieves the property of the row data for a given property name.
	   * @param propertyName The name of the Column, or the Property name.
	   * @return Object value of the property
	   */
	  public String getProperty(String propertyName)
	  {
	    Object o = null;
	    try
	    {
		    int index = getPropertyIndex(propertyName);
	      Vector v = (Vector)rows.elementAt(currentRow);
	      if(index >= v.size())
	      {
	    	  return null;
	      }
	      o = v.elementAt(index);
	      if(o == null)
	    	  return null;
	      else if(String.valueOf(o) == null)
	    	  return null;
	      else if(String.valueOf(o).equals("null"))
	    	  return null;
	      else
	    	  return String.valueOf(o);
	    }
	    catch(Throwable e)
	    {
	      throw new LinxUserException(e.getMessage());
	    }
	  }
	
	/**
	 * returns a data value for a given column
	 * throws an exception if the data is null 
	 * @param propertyName Column name
	 * @return Data value for a given column name
	 * @throws Exception
	 */
	public String getRequiredProperty(String propertyName)
  {
    String s = null;
    try
    {
        int index = getPropertyIndex(propertyName);
     Vector v = (Vector)rows.elementAt(currentRow);
      Object o = v.elementAt(index);
      if(o == null)
      {
    	  throw new Exception("Data for column '" + propertyName + "' cannot be null.");
      }
      else if(WtUtils.isNullOrBlank(String.valueOf(o).toUpperCase()))
      {
    	  throw new Exception("Data for column '" + propertyName + "' cannot be empty or blank.");
      }
      s = String.valueOf(o);
    }
    catch(Throwable e)
    {
      throw new LinxUserException(e.getMessage());
    }
    return s;
  }

	/**
	 * sets the value of a property at the current row
	 * @param propertyName
	 * @param propertyValue
	 * @throws Exception
	 */
	public void setProperty(String propertyName, String propertyValue)
    throws Exception
  {
    int index = getPropertyIndex(propertyName);
    String o = null;
    try
    {
      Vector v = (Vector)rows.elementAt(currentRow);
      v.set(index, propertyValue);
    }
    catch(Exception e)
    {
      throw new LinxUserException(e.getMessage());
    }
  }
	  
	public Object getInlineProperty(String prop)
	{
		String value = (String)super.getInlineProperty(prop);
		if(WtUtils.isNullOrBlank(value))
		{
			throw new LinxUserException("File is missing expected file header " + prop);
		}
		return value;
	}
	
	/**
	 * Returns null if there is no value for the given property
	 * in this file.
	 * @param prop
	 * @return value, or null if no value was provided
	 */
	public String getOptionalInlineProperty(String prop)
	{
		String value = (String)super.getInlineProperty(prop);
		return value;
	}
	
	

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
}
