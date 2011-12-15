package com.sgsi.emre.servlet;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.util.S4MPCRBarcode;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvPrimaryEnrichment extends EMREServlet 
{
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	{
	    task.setMessage("");
	    
	    String sample = task.getServerItemValue(ItemType.PRIMARY_ENRICHMENT);
	    if(WtUtils.isNullOrBlank(sample))
	    {
	    	populateUI(request, task, db);
	    }
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if (request.getAttribute("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
        	writeToExcel(request, response, "exec spEMRE_reportPrimaryEnrichment", db);

			return ALL_DONE;
		}
		else if (request.getAttribute("ImportButton") != null)
		{
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task.getServerItemValue(FileType.PRIMARY_ENRICHMENT_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			importRowsFromFile(fileId, task, user, db, request,response);

	    	commitDb(db);
	        return FINISH_FOR_ME;    	
		}
		else if (request.getAttribute("PrintLabel") != null)
		{
			//lets print 
			  String pe = task.getServerItemValue(ItemType.PRIMARY_ENRICHMENT);
			  if(WtUtils.isNullOrBlankOrPlaceholder(pe))
			  {
				  throw new LinxUserException("Please enter a value for New LIMS ID.");
			  }
			  
			  //lets check to see if we have an enrichment.
			  if(!dbHelper.isItemExisting(pe, ItemType.PRIMARY_ENRICHMENT, db))
				  throw new LinxUserException("Primary Enrichment '" + pe + "' doesn't exist in the database.");
			  //is it retired?
			  if(dbHelper.isRetired(pe, db))
				  throw new LinxUserException("Primary Enrichment '" + pe + "' is retired and cannot be used in this task");
		      try
		      {
		    	  printLabels(pe, db);
		      }
		      catch(Exception ex)
		      {
		    	  throw new LinxSystemException(ex.getMessage());
		      }
		      task.setMessage("Successfully printed barcodes.");		
			  return FINISH_FOR_ME;
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	 
	@Override
	protected void save(Task task, User user, Db db,
				HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			super.save(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			preprocessTask(request, task, user, db);
			throw new LinxUserException(ex);
		}
	}
	
	 private void printLabels(String barcode, Db db)
	 {
		 try
		 {
			 //we need to update the enrichment table with the new barcode
			 String enrichmentId = dbHelper.getDbValue("exec spEMRE_getEnrichmentId '" + barcode + "'", db);
			 //lets remove the "SGI-E" so the barcode fits on the label
			 String shortbarcode = barcode;
			 shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			 dbHelper.executeSQL("exec spEMRE_updateBarcode 'enrichment'," + enrichmentId + ",'" + shortbarcode + "'", db);
			 //now print the barcode
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Enrichment", db);
			 ArrayList<String> alRow = new ArrayList<String>();
			 ResultSet rs = dbHelper.getResultSet("exec spEMRE_getPELabelValues '" + barcode + "'", db);
			 while(rs.next())
			 {
				 alRow.add(rs.getString(1));
				 alRow.add(rs.getString(2));
			 }
			 rs.close();
			 rs = null;
			 String label = printer.getZPLforLabel(shortbarcode, alRow);
			 S4MPCRBarcode.print(printService, shortbarcode, label); 
			 Thread.sleep(200);
		 }
		 catch(Exception ex)
		 {
			 throw new LinxSystemException(ex.getMessage());
		 }
	 }
	
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		XLSParser fileData = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "New LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
		fileData = new XLSParser(inFile, FileType.PRIMARYENRICHMENT_SUBMISSION,
				delim, columnKey, EMREStrings.Enrichments.requiredPrimaryColumnHeaders, true);

		// ready for std save() processing
		task.setMessage("Successfully imported new primary enrichments from bulk import file.");
		
		
		//loop though the file and insert each line
		String err = "";
		int row = 1;
		try
		{
			if(fileData.gotoFirst())
			{
				do
				{			
					String limsId = fileData.getRequiredProperty("New LIMS ID");
					String originLimsId = fileData.getRequiredProperty("Origin LIMS ID");
					String pageRef = fileData.getRequiredProperty("Page Ref");
					String date = fileData.getRequiredProperty("Date Started");
					String vesselType = fileData.getRequiredProperty("Vessel Type");
					String growthTemp = fileData.getRequiredProperty("Growth Temperature");
					String growthMed = fileData.getRequiredProperty("Growth Medium");
					String irradiance = fileData.getRequiredProperty("Growth Irradiance");
					String intId = fileData.getProperty("Internal ID");
					String comment = fileData.getProperty("Comments");
					
					task.getServerItem(ItemType.PRIMARY_ENRICHMENT).setValue(limsId);
					task.getServerItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(originLimsId);
					task.getServerItem("NotebookRef").setValue(pageRef);
					task.getServerItem("DateStarted").setValue(date);
					task.getServerItem("VesselType").setValue(vesselType);
					task.getServerItem("GrowthTemperature").setValue(growthTemp);
					task.getServerItem("GrowthMedium").setValue(growthMed);
					task.getServerItem("GrowthIrradiance").setValue(irradiance);
					task.getServerItem("InternalID").setValue(intId);
					task.getServerItem("Comment").setValue(comment);
					
					// call std processing
					save(task, user, db, request, response);
					row++;
				}
				while(fileData.gotoNext());
			}//at end we have validated all of the inputs in the file
			
		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if(!err.equals(""))
			throw new LinxUserException(err);
	}
	 
	 protected void populateUI(HttpServletRequest request, Task task, Db db)
	 {
		 String limsId = getNextEnrichmentSample(db);
		  if(limsId == null)
				  throw new LinxSystemException("There is no primary enrichment returned from the database.");
		  task.getDisplayItem(ItemType.PRIMARY_ENRICHMENT).setValue(limsId);

	 }
	 
	 /** 
	  * retrieves the autogenerated next LIMS ID from the database
	  * @param db
	  * @return
	  */
	protected String getNextEnrichmentSample(Db db)
	{
		String s = null;
		try
		{
			s = dbHelper.getDbValue("exec spEMRE_getNextPrimaryEnrichment ", db);
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return s;
	}
	 
}
