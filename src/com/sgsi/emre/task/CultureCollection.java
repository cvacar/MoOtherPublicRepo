package com.sgsi.emre.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.bean.CultureCollectionParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


/**
 * Parent class to StrainCultureCollection and 
 * ExperimentalCultureCollection.
 * 
 * @author TJS/Wildtype for SGI
 * @created 4/2011 for EMRE v2.1
 *
 */
public class CultureCollection extends EMRETask 
{
	
	protected HSSFWorkbook wb = null; // for copying edited version
	
	/**
	 * Overridden to parse incoming file for
	 * data rows and to save data to custom db tables
	 * CULTURECOLLECTION and CULTURECOLLECTIONDATA,
	 * and, if editing, CULTURECOLLECTIONHISTORY.
	 * Adds cultures from file to server-side item for core tracking.
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		
		// check for an import file
		String fileId = getServerItemValue(FileType.CULTURE_COLLECTION_IMPORT_FILE);
		if (WtUtils.isNullOrBlank(fileId))
		{
			throw new LinxUserException(
					"Please browse for a culture collection import file, then try again.");
		}
		File inFile = this.getFile(fileId, db);

		// check each of sheets in latest template (currently 3)
		POIFSFileSystem fs = null;
		
		wb = null;
		
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

		// find the correct worksheet for this task
		HSSFSheet sheet = wb.getSheet(this.getTaskName());
		
		if(sheet == null)
		{
			if(this.getTaskName().contains("Master")) 
			{
					sheet = wb.getSheet(this.getTaskName().replace("Master ", ""));
					
			}
//			else if(this.getTaskName().contains("Master Experimental Culture Collection")	 ) 
//			{
//				sheet = wb.getSheet("Experimental Culture Collection");
//		}
		}
		
		
		if(sheet == null)
		{
			throw new LinxUserException(
					"Could not find a worksheet named " + this.getTaskName()
							+ " in this *.xls file. Please check that you are using the latest import template.");
		}
		// import data rows from the sheet
		CultureCollectionParser parser = new CultureCollectionParser();
		// parser will redirect to updateDataValues() method if editing
		ArrayList<String> cultures = parser.insertDataValues(sheet, this, dbHelper, db);
		
		if(cultures.size() < 1)
		{
			throw new LinxUserException(
					"Could not find any data rows in a worksheet named " 
					    + this.getTaskName()
							+ ". Please check that you are using the latest import template"
							+ " and that at least one worksheet contains values in the " 
							+ getColumnKey() + " column.");
		}
		// save edited copy of imported file that includes new culture IDs
		String infilename = null;
		
		try
		{
			// swap the file that will appear in user's download list under this appfileid
				infilename = inFile.getCanonicalPath();
				
				try
				{
					inFile.renameTo(new File(infilename + ".ed"));
				}
				catch (Exception ex)
				{
					// failed to rename -- not that important
				}
				inFile.delete();
				File finFile = new File(infilename);
			
				FileOutputStream fos = new FileOutputStream(finFile);
				wb.write(fos); // contains edited culture ID
				fos.flush();
				fos.close(); //<-- essential to make this work
				 
		}
		catch (FileNotFoundException ex)
		{
				// ignore; we just won't replace original copy
				inFile.renameTo(new File(infilename));
		}
		catch (IOException ex)
		{
				// ignore; we just won't replace original copy
				inFile.renameTo(new File(infilename));
		}
			// -- let user download from UI table (so that task message displays)
			//finFile.deleteOnExit();
			//returnDownloadAsByteStream(response, finFile, samplingId + ".xls",
			//		"application/vnd.ms-excel", false);
		// set up for core tracking
		getServerItem(getCultureType()).addValues(cultures);
		
		setMessage("Successfully imported data on " + cultures.size() + " cultures for " + this.getTaskName());		
		 
	}
	
	/**
	 * Returns the header expected in the first column
	 * of the data rows. For subclasses to override.
	 * @return header in first column above data rows
	 */
	public String getColumnKey()
	{
		return "override";
	}
	
	public String[] getRequiredHeaders()
	{
		return new String[] {getColumnKey(),"Date Started", getCultureIDHeader()};
	}

	public String getCultureIDHeader()
	{
		return "override";
	}

	public String getCultureType()
	{
		// TODO Auto-generated method stub
		return "override";
	}

	/**
	 * Throws an exception if start date is not in
	 * expected YYMMDD format. 
	 * @param startDate
	 */
	public void validateYYMMDD(String startDate)
	{
		
		
	}
}
