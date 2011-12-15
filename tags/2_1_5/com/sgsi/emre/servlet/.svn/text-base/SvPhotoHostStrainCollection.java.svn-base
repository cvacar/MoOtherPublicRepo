package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;


/**
 * 
 * SvPhotoHostStrainCollection
 *
 * Overridden to handle custom actions to retrieve
 * past uploaded data or to print a label for a 
 * saved strain. Super handles all methods.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 6/2008
 */
public class SvPhotoHostStrainCollection extends SvStrainCollection
{
	public static final String[] reqColumnHeaders = new String[]{"New Strain ID",
		"Strain Name","Project","Page Ref [notebook-pg]","Location 1 [Freezer:Box:Position]",
		"Location 2 [Freezer:Box:Position]","Location 3 [Freezer:Box:Position]",
		"Location 3 [Freezer:Box:Position]","Comment"};

  	/**
	 * Returns the name of stored proc to use
	 * to retrieve this type of strain. 
	 * @return name of sp to get strain report for this type of strain
	 */
    protected String getStrainReportSQL()
	{
		return "exec spMet_GetPhotoHostStrainCollection";
	}
 
        /**
     * Number of rows to show in the Location UI table
     * on initial display, for defining new strain.
     * @return 4
     */
	protected int getLocationRowCount()
	{
		return 4;
	}   
	
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
	  try
	  {
		  	XLSParser fileData = null;
			// import file
			// core has already validated file per task def
			File inFile = this.getFile(fileId, db);

			// create a list of objects while importing manifest
			//lets get the data from the file and put it into a data container object
			char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
			String columnKey = "New Strain ID"; //the unique identifier that lets me know i've reach the column data in the file
			fileData = new XLSParser(inFile, "Strain",
					delim, columnKey, reqColumnHeaders , true);

			// ready for std save() processing
			task.setMessage("Successfully imported new strains from bulk import file.");
			
			//loop though the file and insert each line
			String err = "";
			int row = 1;
			try
			{
				if(fileData.gotoFirst())
				{
					do
					{			
						String limsId = fileData.getRequiredProperty("New Strain ID");
						String strainName = fileData.getRequiredProperty("Strain Name");
						String project = fileData.getRequiredProperty("Project");
						String notebook = fileData.getRequiredProperty("Page Ref [notebook-pg]");
						String loc1 = fileData.getRequiredProperty("Location 1 [Freezer:Box:Position]");
						String loc2 = fileData.getRequiredProperty("Location 2 [Freezer:Box:Position]");
						String loc3 = fileData.getRequiredProperty("Location 3 [Freezer:Box:Position]");
						String loc4 = fileData.getRequiredProperty("Location 4 [Freezer:Box:Position]");
						String comment = fileData.getProperty("Comment");
						
						
						task.getServerItem(ItemType.STRAIN).setValue(limsId);
						task.getServerItem("Project").setValue(project);
						task.getServerItem("NotebookRef").setValue(notebook);
						task.getServerItem("StrainName").setValue(strainName);
						task.getServerItem("Comment").setValue(comment);
						
						ArrayList<String> locs = new ArrayList<String>();
						locs.add(loc1);
						locs.add(loc2);
						locs.add(loc3);
						locs.add(loc4);
						
						task.getServerItem("Location").setValues(locs);
						
						// call std processing
						save(task, user, db, request, response);
						row++;
					}
					while(fileData.gotoNext());
				}//at end we have validated all of the inputs in the file
				else
					throw new Exception("There were no rows found in the file.  Please check column headers and try again.");
			}
			catch (Exception e)
			{
				err += "Error occurred while parsing row " + row + ": " + e.getMessage();
			}
			if(!err.equals(""))
				throw new LinxUserException(err);

	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException(ex.getMessage());
	  }
		
	}
}
