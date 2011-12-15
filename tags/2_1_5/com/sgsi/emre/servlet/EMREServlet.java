package com.sgsi.emre.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.project.Strings;
import com.wildtype.linx.task.ItemType;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.util.PlateLayout;
import com.sgsi.servlet.SGIServlet;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.DefaultUser;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * Parent servlet where commonly used methods can be found.
 * 
 * @author TJS/Wildtype
 * @date 6/2007
 */
public class EMREServlet extends SGIServlet
{
  public final String SELECT_PLACEHOLDER = "(Select)";
  public final String ITEM_NAME = "itemname"; // for copyFile() method
  public final String ITEM_TYPE = "itemtype"; // for copyFile() method
  
  public EMREDbHelper dbHelper = new EMREDbHelper();


  public File writeSampleSheet(File inFile, HttpServletRequest request, String requestId, 
			 String yyyyMMdd, String analysisMethod, ArrayList<String[]> alSamples, 
			 String selectedBatches, String sampleIdType, Task task, Db db)
	 {
		 FileInputStream fis = null;
		 FileOutputStream stream = null;
		 HSSFWorkbook wb = null;
		 File out = null;
		 try
		 {
			 if(!inFile.canRead())
				 throw new LinxUserException("Unable to read sample sheet template file.");
			 fis = new FileInputStream(inFile);
			 POIFSFileSystem fs = new POIFSFileSystem(fis);
		     wb = new HSSFWorkbook(fs);
		     String outfile = inFile.getParent() + "\\SampleSheet_out.xls";
		     out = new File(outfile);
		     stream = new FileOutputStream(out);
		     String login = task.getLogin(request);
		     if(WtUtils.isNullOrBlank(login))
		    	 login = "";
			 for (int k = 0; k < wb.getNumberOfSheets(); k++)
	         {
	             HSSFSheet sheet = wb.getSheetAt(k);
	             int       rows  = sheet.getPhysicalNumberOfRows();
	             System.out.println("Sheet " + k + " \""
	                                + wb.getSheetName(k) + "\" has "
	                                + rows + " row(s).");

	             boolean bFoundColumns = false;
	             for (int m = 0; m < rows; m++)
	             {
	                 HSSFRow row   = sheet.getRow(m);
	             
	                 if (row == null)
	                 {
	                     continue;
	                 }
	                 
	                 HSSFCell cell = row.getCell((short)0);
	                 if(cell == null)
	                	 continue;//ignore blank lines
	                 String cellVal = cell.getStringCellValue();
	                 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
	                
	                 if(cellVal.equalsIgnoreCase("Submission ID"))
	                 {
	                	 bFoundColumns = true;
	                	 rows = m;
	                	 break;
	                 }
	                 
	                 if(!bFoundColumns)
	                 {
	                	 if(WtUtils.isNullOrBlank(cellVal))
		                 {
		                	 //blank row
		                	 continue;
		                 }
		                 else if(cellVal.equalsIgnoreCase("Request ID"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(requestId);
		                 }
		                 else if(cellVal.equalsIgnoreCase("Request date"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(Integer.valueOf(yyyyMMdd));
		                	 //cell2.setCellValue(yyyyMMdd);
		                 }
		                 else if(cellVal.equalsIgnoreCase("Requester"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(login);
		                 }
		                 else if(cellVal.equalsIgnoreCase("Sample ID type"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(sampleIdType);
		                 }
		                 else if(cellVal.equalsIgnoreCase("Analysis method"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(analysisMethod);
		                 }
		                 else if(cellVal.equalsIgnoreCase("Sample description"))
		                 {
		                	 HSSFCell cell2 = row.getCell((short)1);
		                	 if(cell2 == null)
		                		 cell2 = row.createCell((short)1);
		                	 cell2.setCellValue(selectedBatches);
		                 }
	                 }
	               
	             }
	             //now lets write out the data
	             int nextRow = rows + 1;
	             int numSamples = alSamples.size();
	             for(int x = 0; x < numSamples; x++)
	             {
	            	
	            	String[] ayData = alSamples.get(x);
	            	String subId = ayData[0];
		            String sampId = ayData[1];
		            String dil = ayData[2];
		            	
		            HSSFRow row  = sheet.createRow(nextRow);
			            	 
		            HSSFCell cell = row.createCell((short)0);
			        HSSFCell cell1 = row.createCell((short)1);
			        HSSFCell cell2 = row.createCell((short)2);
			            	 
			        cell.setCellValue(subId);
			        cell1.setCellValue(sampId);
			        cell2.setCellValue(dil);
			        nextRow++;
	             }
	             wb.write(stream);
	             stream.close();
	             fis.close();
	         }
		 }
		 catch(Exception ex)
		 {
			 try
			 {
				 wb.write(stream);
				 fis.close();
				 fis = null;
				 stream.close();
				 stream = null;
			 }
			 catch(Exception ignoreMe)
			 {
				 
			 }
			 throw new LinxUserException("Error occurred when writting sample sheet:" + ex.getMessage());
		 }
		 return out;
	 }
  
  public RowsetView populateFreezerLocationsView(RowsetView view, ArrayList<String> alLocations,
		  int numRows, String tableName, int printCol, int locationCol, String sql,
		  String sample, HttpServletRequest request,Task task, Db db)
	{
	    // show UI table of locations for selected strain
		RowsetView.cleanupSessionViews(request);
		view = this.getSQLRowsetView(request, sql, "Print", tableName, numRows, db);
	    if(view.getRowcount() < 1)
	   	{
	   		view = this.populateLocationsView( alLocations, view, printCol, 
	   				locationCol, tableName, db);
	   	}
	    view.setWidget(printCol,LinxConfig.WIDGET.CHECKBOX);
	    view.setWidget(locationCol,LinxConfig.WIDGET.TEXTBOX);
	    view.setHideNavControls(true); 
		    
		    // default to checking 'Print'
		    //int numRows = getLocationRowCount();
	    view.setRowcount(numRows);
		for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
		{
			view.setIsSelected(rowIdx, printCol);
		}
		view.setMessage("Showing freezer locations for " + sample);
		view.setStartRow(1);
	    return view;
	 }

  public RowsetView populateLocationsView(ArrayList<String> alLocations, RowsetView view, 
		  int colPrint, int colLoc, String tableName, Db db)
	{
	    // update the RowsetView
	    //lets write a file containing the locations
	    //we can use it to populate the Rowset
	    int numRows = alLocations.size();
	    File file = writeLocationsToFile(alLocations, db);
	    String[] colHeaders = {"Print","Location"};
	    view = this.getFileRowsetView(file, "Print", 
	    		tableName, null, colHeaders, numRows );
	    view = populateLocationsView(view, colPrint, colLoc, numRows);
	    view.setMessage("Showing next available freezer locations");

	    if(file.exists())
	    {
	    	file.delete();
	    }
	    return view;
	 }
  
  protected RowsetView populateLocationsView(RowsetView view, int colPrint, 
		  int colLoc, int numRows)
	 {
	  view.setWidget(colPrint,LinxConfig.WIDGET.CHECKBOX);
	  view.setWidget(colLoc,LinxConfig.WIDGET.TEXTBOX);
	  view.setStartRow(1);
	  view.setHideNavControls(true); 
	    
	    // default to checking 'Print'
	    //int numRows = getLocationRowCount();
	    view.setRowcount(numRows);
	    for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
	    {
	    	view.setIsSelected(rowIdx, colPrint);
	    }
	    view.setMessage("Showing freezer locations for strain");
	    return view;
	}
  
  protected File writeLocationsToFile(ArrayList<String> alLocations, Db db)
  {
	  
	  StringBuffer sb = new StringBuffer();
	  String PARENTTYPE = "System Properties";
	  String APPVALUETYPE = "Temp File Dir";

		// get the local path for the bulk insert file known to SQL Server
		String sBulkPath = dbHelper.getApplicationValue(db, PARENTTYPE,
				APPVALUETYPE);
		if (sBulkPath == null)
		{
			throw new LinxSystemException(
					"Local path for temp file directory cannot be found. "
							+ "Please notify the LIMS administrator to set APPVALUE "
							+ "'" + PARENTTYPE + "'/'" + APPVALUETYPE + "'.");
		}
		sBulkPath = sBulkPath + "\\" + "locationFile.csv";
	  //write out headers
	  sb.append("Print,Location" + Strings.CHAR.NEWLINE);
	  ListIterator itor = alLocations.listIterator();
	  while(itor.hasNext())
	  {
		  String loc = (String)itor.next();
		  // write out a print placeholder and a reserved location
		  String line = " ," + loc + Strings.CHAR.NEWLINE;
		  sb.append(line);
	  }

	  // write rows to file
	  // we're not writing back to client, so no need for output stream
	  File file = new File(sBulkPath);
	  if(file.exists())
	  {
		  file.delete();
	  }
	  file = new File(sBulkPath);
	  try
	  {
		  FileWriter writer = new FileWriter(file);
		  writer.write(sb.toString());
		  writer.flush();
		  writer.close();
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
	  return file;
  }
  
  protected File writeSingleColumnToFile(ArrayList<String> alRows, String[] headers, char delim, Db db)
  {
	  
	  StringBuffer sb = new StringBuffer();
	  String PARENTTYPE = "System Properties";
	  String APPVALUETYPE = "Temp File Dir";

		// get the local path for the bulk insert file known to SQL Server
		String sBulkPath = dbHelper.getApplicationValue(db, PARENTTYPE,
				APPVALUETYPE);
		if (sBulkPath == null)
		{
			throw new LinxSystemException(
					"Local path for temp file directory cannot be found. "
							+ "Please notify the LIMS administrator to set APPVALUE "
							+ "'" + PARENTTYPE + "'/'" + APPVALUETYPE + "'.");
		}
		sBulkPath = sBulkPath + "\\" + "locationFile.csv";
	  //write out headers
		String colHeaders = "";
		int numCols = headers.length;
		for(int i = 0; i < numCols; i++)
		{
			if(i < numCols - 1)
				colHeaders += headers[i] + delim;
			else
				colHeaders += headers[i];
		}
	    sb.append(colHeaders + Strings.CHAR.NEWLINE);
	    ListIterator itor = alRows.listIterator();
	    while(itor.hasNext())
	    {
		  String loc = (String)itor.next();
		  // write out a print placeholder and a reserved location
		  String line = loc + Strings.CHAR.NEWLINE;
		  sb.append(line);
	    }

	  // write rows to file
	  // we're not writing back to client, so no need for output stream
	  File file = new File(sBulkPath);
	  if(file.exists())
	  {
		  file.delete();
	  }
	  file = new File(sBulkPath);
	  try
	  {
		  FileWriter writer = new FileWriter(file);
		  writer.write(sb.toString());
		  writer.flush();
		  writer.close();
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
	  return file;
  }
  
  protected File writeToFile(StringBuffer fileHeaders, 
		  	ArrayList<String> alLocations, Db db)
  {
	  
	  StringBuffer sb = new StringBuffer();
	  //write out headers
	  sb.append(fileHeaders + Strings.CHAR.NEWLINE);
	  String PARENTTYPE = "System Properties";
	  String APPVALUETYPE = "Temp File Dir";

		// get the local path for the bulk insert file known to SQL Server
		String sBulkPath = dbHelper.getApplicationValue(db, PARENTTYPE,
				APPVALUETYPE);
		if (sBulkPath == null)
		{
			throw new LinxSystemException(
					"Local path for temp file directory cannot be found. "
							+ "Please notify the LIMS administrator to set APPVALUE "
							+ "'" + PARENTTYPE + "'/'" + APPVALUETYPE + "'.");
		}
		sBulkPath = sBulkPath + "\\" + "locationFile.csv";
		
	  ListIterator itor = alLocations.listIterator();
	  while(itor.hasNext())
	  {
		  String loc = (String)itor.next();
		  // write out a print placeholder and a reserved location
		  String line = loc + Strings.CHAR.NEWLINE;
		  sb.append(line);
	  }

	  // write rows to file
	  // we're not writing back to client, so no need for output stream
	  File file = new File(sBulkPath);
	  if(file.exists())
	  {
		  file.delete();
	  }
	  file = new File(sBulkPath);
	  try
	  {
		  FileWriter writer = new FileWriter(file);
		  writer.write(sb.toString());
		  writer.flush();
		  writer.close();
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
	  return file;
  }
  
  /**
   * reserves freezer locations for 81 positions
   * @param spName
   * @param boxType
   * @param db
   * @return
   */
  protected ArrayList<String> reserveFreezerLocations(String spName, String boxType, Db db)
	{
	    ArrayList<String> locs = new ArrayList<String>();
		int currBoxNum = 0;
		int currPos = 0;
		int boxNum = 0;
		int pos = 0;
		String freezer = null;
		int numVials = 0;
		String padPos = null; // zero-padded position, e.g. '01'
		String padBox = null;
		String loc = null;
		
		ResultSet rs = null;
		try
		{
			// we're going for robustness via brute force here
			String sql = "exec " + spName + " '" + boxType + "'";
			rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				currBoxNum 		= rs.getInt(1);
				currPos    		= rs.getInt(2); // may be zero, which is OK
				freezer    		= rs.getString(3);
				numVials		= rs.getInt(4); // number of slots to reserve
			}// expecting only one
			rs.close();
			rs = null;
		}
		catch (SQLException e)
		{
			throw new LinxSystemException(e.getMessage());
		}
		// at exit, know last filled pos in this box type
		
		// make regular archives (ME)
		if(currBoxNum == 0) // handle boundary (start-up) condition
		{
			currBoxNum = 1;
		}
		boxNum = currBoxNum;
		pos = currPos; // may be zero if new box
		for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
		{
			if(pos < 81) // todo: eliminate magic number
			{
				// there's room in this box
				pos = pos + 1;
			}
			else
			{  
				// increment box
				boxNum = boxNum + 1;
				pos = 1;
			}
			padPos = EMRETask.zeroPad(pos, 2);
			if(boxNum < 10)
				padBox = EMRETask.zeroPad(boxNum,2);
			else
				padBox = String.valueOf(boxNum);
			loc = freezer + ":" + boxType + padBox + ":" + padPos;	
			locs.add(loc);
		}// next archive
		//*************
		// at exit, return new location
		return locs;
	}
  
  /**
   * reserves freezer locations in boxes of 64
   * @param spName
   * @param boxType
   * @param db
   * @return
   */
  protected ArrayList<String> reserveSampleFreezerLocations(String spName, String boxType, Db db)
	{
	    ArrayList<String> locs = new ArrayList<String>();
		int currBoxNum = 0;
		int currPos = 0;
		int boxNum = 0;
		int pos = 0;
		String freezer = null;
		int numVials = 0;
		String padPos = null; // zero-padded position, e.g. '01'
		String padBox = null;
		String loc = null;
		
		ResultSet rs = null;
		try
		{
			// we're going for robustness via brute force here
			String sql = "exec " + spName + " '" + boxType + "'";
			rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				currBoxNum 		= rs.getInt(1);
				currPos    		= rs.getInt(2); // may be zero, which is OK
				freezer    		= rs.getString(3);
				numVials		= rs.getInt(4); // number of slots to reserve
			}// expecting only one
			rs.close();
			rs = null;
		}
		catch (SQLException e)
		{
			throw new LinxSystemException(e.getMessage());
		}
		// at exit, know last filled pos in this box type
		
		// make regular archives (ME)
		if(currBoxNum == 0) // handle boundary (start-up) condition
		{
			currBoxNum = 1;
		}
		boxNum = currBoxNum;
		pos = currPos; // may be zero if new box
		for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
		{
			if(pos < 64) // todo: eliminate magic number
			{
				// there's room in this box
				pos = pos + 1;
			}
			else
			{  
				// increment box
				boxNum = boxNum + 1;
				pos = 1;
			}
			padPos = EMRETask.zeroPad(pos, 2);
			if(boxNum < 10)
				padBox = EMRETask.zeroPad(boxNum,2);
			else
				padBox = String.valueOf(boxNum);
			loc = freezer + ":" + boxType + padBox + ":" + padPos;	
			locs.add(loc);
		}// next archive
		//*************
		// at exit, return new location
		return locs;
	}
  
  protected ArrayList<String> reserveIsolateFreezerLocations(String spName, String isolate, 
		  String boxType, Db db)
	{
	    ArrayList<String> locs = new ArrayList<String>();
		int currBoxNum = 0;
		int currPos = 0;
		int boxNum = 0;
		int pos = 0;
		String freezer = null;
		int numVials = 0;
		String padPos = null; // zero-padded position, e.g. '01'
		String padBox = null;
		String loc = null;
		
		
		//as of 10/02/2010 SP returns the next available rack and position
		ResultSet rs = null;
		try
		{
			// we're going for robustness via brute force here
			String sql = "exec " + spName + " '" + boxType + "','" + isolate + "'";
			rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				currBoxNum 		= rs.getInt(1);
				currPos    		= rs.getInt(2); // may be zero, which is OK
				freezer    		= rs.getString(3);
				numVials		= rs.getInt(4); // number of slots to reserve
			}// expecting only one
			rs.close();
			rs = null;
		}
		catch (SQLException e)
		{
			throw new LinxSystemException(e.getMessage());
		}
		// at exit, know last filled pos in this box type
		
		// make regular archives (ME)
//		if(currBoxNum == 0) // handle boundary (start-up) condition
//		{
//			currBoxNum = 1;
//		}
//		boxNum = currBoxNum;
//		pos = currPos; // may be zero if new box
//		//do we need a new rack?
//		boolean bNewRack = false;
		//String well = isolate.substring(isolate.lastIndexOf("-") + 1);
		//if(well.equalsIgnoreCase("A01"))
		//	bNewRack = true;
		//int wellIdx = 0;
		PlateLayout.setBUseIsolateLayout(true);
//		try
//		{
//			wellIdx = PlateLayout.getNumericCoord(PlateLayout.CM_96WELL, well);
//			if(wellIdx == 96)
//				bNewRack = true;
//		}
//		catch(Exception ex)
//		{
//			//well doesn't exist
//			throw new LinxUserException(ex.getMessage());
//		}
		//rack position is automatically set in the stored proc - we just need to determine if we need 
		//to increment the box
		
//		for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
//		{
//			if(vialIdx == 0)//first row of result set is alreay incremented - just add to locations
//			{
//				padPos = EMRETask.zeroPad(pos, 2);
//				if(boxNum < 10)
//					padBox = EMRETask.zeroPad(boxNum,2);
//				else
//					padBox = String.valueOf(boxNum);
//				loc = freezer + ":" + boxType + padBox + ":" + padPos;	
//				locs.add(loc);
//			}
//			if(pos < 36 && vialIdx != 0) // todo: eliminate magic number
//			{
//				// there's room in this box
//				//if(bNewRack)
//				pos = pos + 1;
//			}
//			else if(pos == 36 && vialIdx == 0)
//			{
//				//we're at the last 
//			}
//			else
//			{  
//				// increment box
//				if(bNewRack)
//				{
//					boxNum = boxNum + 1;
//					pos = 1;
//				}
//				
//			}
			padPos = EMRETask.zeroPad(currPos, 2);
			if(currBoxNum < 10)
				padBox = EMRETask.zeroPad(currBoxNum,2);
			else
				padBox = String.valueOf(currBoxNum);
			loc = freezer + ":" + boxType + padBox + ":" + padPos;	
			locs.add(loc);
		//}// next archive
		//*************
		// at exit, return new location
		return locs;
	}
  
  public String convertDate(String date) 
	{
		String cd = null;
		if(!WtUtils.isNullOrBlank(date))
		{
			if(date.indexOf('/') > 0)
			{
				throw new LinxUserException(" Date " + date + " is not in expected format 'yymmdd'.");
			}
			try
			{
				SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
				Date d = df.parse(date);
				
				SimpleDateFormat converted = new SimpleDateFormat("yyyy-MM-dd: HH:mm:ss");		
				cd = converted.format(d);
			}
			catch(Exception ex)
			{
				throw new LinxUserException( ex.getMessage());
			}
		}
		return cd;
	}
  
  /**
   * Checks a share folder configured in APPVALUE for files or folders
   * named after an item of the given itemType. If found, performs
   * a binary copy to a destination directory configured for that
   * itemType in APPVALUE, adds an application file record as an attribute of the 
   * item, and renames the source file or folder *_copied. Eff EMRE v2.1.
   * @param itemType
   * @param db
   * @return message listing affected items of itemtype
   * @throws IOException 
   */
  public void processShareDataFiles(String itemType, EMRETask task, User user, Db db) throws IOException
  {
  	String fileType = itemType + "DataFile"; // currently StrainCultureDataFile
  	                                        // or SamplingIDDataFile  	
  	String srcPath = 
  		db.getHelper().getApplicationValue(db,"FILETYPE","Shared Data Folder");
  	srcPath = srcPath + "\\" + user.getName();
  	
  	String destPath =
  		db.getHelper().getApplicationValue(db, "FILETYPE", fileType);
      	  	
		File src = new File(srcPath);
  	if(src == null)
  	{
			throw new LinxUserException("Could not load source directory "
					+ " configured in APPVALUE table for shared data folder.");
  	}
		File dest = new File(destPath);
		if(dest == null)
		{
			throw new LinxUserException("Could not load directory at destination path"
					+ " configured in APPVALUE table for appvaluetype = " + itemType + "DataFile");
		}	
		copyFolder(src, dest, task, db);
  	
  }
  
    /**
     * Destination path is always the same; recurses through
     * any subfolders on source path, copying everything.
     * @param srcPath -- files to be copied
     * @param destPath -- copy files to here
     * @param task -- to keep track of item and itemType
     * @throws IOException 
     */
  	public static void copyFolder(File src, File dest, EMRETask task, Db db) throws IOException
  	{
  		EMREDbHelper dbHelper = (EMREDbHelper)db.getHelper();

 	  	if(src.isDirectory())
	  	{
		    File[] children = src.listFiles(); //
		    for(int idx = 0; idx < children.length; idx++)
		    {
		    	copyFolder(children[idx], dest, task, db);
		    }
	  	}
	  	else  // srcDir is a file, not a folder
	  	{
 	  		if(src.getName().endsWith("_copied"))
 	  		{
 	  			return; // skip this file; it was copied in previous session
 	  		}
	  		// make sure we don't stomp on any existing files named the same
	  		// construct uniq filename as <timestamp>_filename.suffix
	  		String destFilename = dest.getPath() + File.separator 
	  						+ System.currentTimeMillis() + "_"  + src.getName();
	  		
				// do db updates here (applicationFile and submittedDatFile tables), 
				// -- before irreversible copy
				Hashtable appliesToItem = getItemByFilePath(src, task, dbHelper, db);
				String parentPath = dest.getPath();
				String filetype = (String)task.getAttribute("filetype");
				String filenameOnly = task.getTranIdAsString() + "_"  + src.getName();
	  		dbHelper.addAppFileForSharedDataFile(parentPath, filenameOnly, filetype, 
	  					(String)appliesToItem.get("itemname"), (String)appliesToItem.get("itemtype"), task.getTranId(), db);
	      // so we don't copy it over and over in future sessions
	  		copyFile(src, new File(src.getPath() + "_copied"));
	  		try
				{
					copyFile(src, new File(destFilename));
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					throw new LinxUserException(ex);
				}
	  		
				// after successful copy and renaming to _copied
	  		src.delete();
	  	}
  	}
	
  /**
   * Does the work of binary copy from src file to dest file.
   * Called only with files, not folders.
   * @param srcFile -- file to be copied
   * @param destFile -- empty file to be copied into (created in dest directory)
   */
  public static void copyFile(File srcFile, File destFile) throws IOException 
  {
      InputStream oInStream = new FileInputStream(srcFile);
      OutputStream oOutStream = new FileOutputStream(destFile);
 
      // Transfer bytes from in to out
      byte[] oBytes = new byte[1024];
      int nLength = 0;
      BufferedInputStream oBuffInputStream = new BufferedInputStream( oInStream );
      while ((nLength = oBuffInputStream.read(oBytes)) > 0) 
      {
      	oOutStream.write(oBytes, 0, nLength);
      }
      oInStream.close();
      oOutStream.close();
   }

  /**
   *  Returns a convenience ItemType instance that provides
   *  both item name and itemtype values to caller, extracted from
   *  either the filename or its parent directory, whichever is
   *  named for an existing item. 
   * @param src -- a file, not a directory
   * @return item.item 
   * @throws IOException 
   */
  public static Hashtable getItemByFilePath(File src, EMRETask task, EMREDbHelper dbHelper, Db db) throws IOException
  {
   	Hashtable hash = new Hashtable(); // for return
  	
		// figure out which item this file applies to
		// -- is file named after an item? 
    String itemname = src.getName();
		if(itemname.indexOf(".") > 0)
		{
			itemname = itemname.substring(0,itemname.lastIndexOf("."));
		}
		// test the itemname
		if(!dbHelper.isEMREItemExisting(itemname, db))
		{
			// -- is parent folder named after an item?
			// -- note: we don't expect .suffix on folder names
			itemname = src.getParentFile().getName();		
			if(!dbHelper.isEMREItemExisting(itemname, db))
			{
		  	throw new LinxUserException("Could not determine file's association."
		  			+ " Please check the shared data folder "
						+ "to confirm that either the folder name "
						+ "or the file's name (ignoring suffix) specifies a valid " 
						+ " item such as Sampling ID, SamplingTimepoint ID or StrainCulture: "
					  + src.getPath()); 
			}
		}
		// by here, have determined the item associated with this data file
		String sql = "spEMRE_getItemTypeByItem";
		String itemtype = dbHelper.getDbValueFromStoredProc(db, sql, itemname);
		if(WtUtils.isNullOrBlank(itemtype))
		{
	  	throw new LinxUserException("Could not determine file's association."
	  			+ " Please check the shared data folder "
					+ "to confirm that either the folder name "
					+ "or the file's name (ignoring suffix) specifies a valid " 
					+ " item such as Sampling ID, SamplingTimepoint ID or StrainCulture: "
				  + src.getPath()); 
		}
		hash.put("itemname", itemname);
		hash.put("itemtype",itemtype);

		return hash;
  }
}
