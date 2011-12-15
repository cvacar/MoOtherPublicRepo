package com.sgsi.emre.servlet;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;

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
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


public class SvSecondaryEnrichment extends EMREServlet 
{
	
	@Override
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	{
	    task.setMessage("");
	    
	    // -- data file rowset is hidden until a strain is selected
	    String sample = task.getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
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
			// user wants to download the entire collection to Excel
        	writeToExcel(request, response, "exec spEMRE_reportSecondaryEnrichment", db);

			return ALL_DONE;
		}
	    else if (request.getAttribute("ImportButton") != null)
		{
    		task.createAnyNewAppFiles(request, response, user, db);
    		String fileId = task.getServerItemValue(FileType.SECONDARY_ENRICHMENT_IMPORT_FILE);
    		if(WtUtils.isNullOrBlank(fileId))
    		{
    			throw new LinxUserException("Please browse for a bulk import file, then try again.");
    		}
    	
			importRowsFromFile(fileId, task, user, db, request,response);

	    	commitDb(db);
	        return FINISH_FOR_ME;    	
		}
	    else if (request.getAttribute("PrintLabel") != null)
		{
			//lets print 
			  String se = task.getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
			  if(WtUtils.isNullOrBlankOrPlaceholder(se))
			  {
				  throw new LinxUserException("Please enter a value for New LIMS ID.");
			  }
			  
			  //lets check to see if we have an enrichment.
			  if(!dbHelper.isItemExisting(se, ItemType.SECONDARY_ENRICHMENT, db))
				  throw new LinxUserException("Secondary Enrichment '" + se + "' doesn't exist in the database.");
			  //is it retired?
			  if(dbHelper.isRetired(se, db))
				  throw new LinxUserException("Secondary Enrichment '" + se + "' is retired and cannot be used in this task");
		      try
		      {
		    	  printLabels(se, db);
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
			 //now print the label
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Enrichment", db);
			 ArrayList<String> alRow = new ArrayList<String>();
			 ResultSet rs = dbHelper.getResultSet("exec spEMRE_getSELabelValues '" + barcode + "'", db);
			 while(rs.next())
			 {
				 //alRow.add(barcode);
				 alRow.add(rs.getString(1));
				 alRow.add(rs.getString(2));
			 }
			 rs.close();
			 rs = null;
			 //only one barcode to print
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
		fileData = new XLSParser(inFile, FileType.SECONDARYENRICHMENT_SUBMISSION,
				delim, columnKey, EMREStrings.Enrichments.requiredSecondaryColumnHeaders, true);

		// ready for std save() processing
		task.setMessage("Successfully imported new secondary enrichments from bulk import file.");
		
		
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
					String originItemType = fileData.getProperty("Origin ID Type");
					String pageRef = fileData.getRequiredProperty("Page Ref");
					String growthMed = fileData.getRequiredProperty("Growth Medium");
					String o2Conc = fileData.getProperty("O2 Concentration");
					String co2Conc = fileData.getProperty("CO2 Concentration");
					String pH = fileData.getProperty("pH");
					String flocculence = fileData.getProperty("Flocculence");
					String comment = fileData.getProperty("Comments");
					
					task.getServerItem(ItemType.SECONDARY_ENRICHMENT).setValue(limsId);
					task.getServerItem(ItemType.ORIGIN_LIMS_ID).setValue(originLimsId);
					task.getServerItem("OriginItemType").setValue(originItemType);
					task.getServerItem("NotebookRef").setValue(pageRef);
					task.getServerItem("GrowthMedium").setValue(growthMed);
					task.getServerItem("OxygenConcentration").setValue(o2Conc);
					task.getServerItem("CO2Concentration").setValue(co2Conc);
					task.getServerItem("pH").setValue(pH);
					task.getServerItem("Flocculence").setValue(flocculence);
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
	 
	 protected void save(Task task, User user, Db db,
				HttpServletRequest request, HttpServletResponse response)
		{
		 	DefaultTask cleanTask = new DefaultTask(((EMRETask)task));
			try
			{
				super.save(task, user, db, request, response);
			}
			catch(Exception ex)
			{
				preprocessTask(request, task, user, db);
				if(ex.getMessage().toLowerCase().indexOf("more than one item exists") == -1
						&& ex.getMessage().toLowerCase().indexOf("more than one origin item exists") == -1)
				{
					//exception thrown when multiple items exist of different types
					//don't want to reset the DOM if we need to select a type from the dropdown
					((EMRETask)task).setTaskDOM(cleanTask.getTaskDOM());
					((EMRETask)task).setOriginIdType(EMREStrings.ItemType.ORIGIN_LIMS_ID);
				}
				throw new LinxUserException("Error occurred during save: " + ex.getMessage());
			}
		}
	 
	 protected void populateUI(HttpServletRequest request, Task task, Db db)
	 {
		 String limsId = getNextSecondaryEnrichment(db);
		  if(limsId == null)
				  throw new LinxSystemException("There is no secondary enrichment returned from the database.");
		  task.getDisplayItem(ItemType.SECONDARY_ENRICHMENT).setValue(limsId);
	 }
	
	 
	/**
	 * retrieves the next autogenerated LIMS ID from the database
	 * @param db
	 * @return
	 */ 
	protected String getNextSecondaryEnrichment(Db db)
	{
		String s = null;
		try
		{
			s = dbHelper.getDbValue("exec spEMRE_getNextSecondaryEnrichment ", db);
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return s;
	}

}
