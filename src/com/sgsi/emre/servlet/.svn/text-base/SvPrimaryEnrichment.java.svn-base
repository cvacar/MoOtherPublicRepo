package com.sgsi.emre.servlet;

import java.sql.ResultSet;
import java.util.ArrayList;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.util.S4MPCRBarcode;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Handles custom actions 'Import', 'Export', and 'Print Labels'
 * for task Primary Enrichment. Eff LIMS v2.2, accepts bulk import 
 * exclusively (no screen input).
 * @author TJS/Wildtype for SGI
 * @modified 7/2011 for LIMS v2.2
 * 	-- removed screen input widgets
 *  -- refactored to use bulk insert into db
 *
 */
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
	
	/**
	 * Overridden to handle custom actions 'Import', 'Export', and 'Print Label'.
	 */
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if (request.getAttribute("ImportButton") != null)
		{
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task.getServerItemValue(FileType.PRIMARY_ENRICHMENT_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			task.getDisplayItem(ItemType.PRIMARY_ENRICHMENT).clearValues();
			task.getServerItem(ItemType.PRIMARY_ENRICHMENT).clearValues();
			save(task, user, db, request,response);
	    	commitDb(db);
	        return FINISH_FOR_ME;    	
		}
		else if (request.getAttribute("ExportButton") != null)
		{
			// user wants to download the entire collection to Excel
        	writeToExcel(request, response, "exec spEMRE_exportPrimaryEnrichment", db);

			return ALL_DONE;
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
	 
	/**
	 * NOT IN USE - Lab uses Print Labels tasks for all printing
	 * @param barcode
	 * @param db
	 */
	 private void printLabels(String barcode, Db db)
	 {
		 try
		 {
			 //we need to update the enrichment table with the new barcode
			 String enrichmentId = dbHelper.getDbValue("exec spEMRE_getEnrichmentId '" + barcode + "'", db);
			 //lets remove the "SGI-E" so the barcode fits on the label
			 String shortbarcode = barcode;
			 //shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			 dbHelper.executeSQL("exec spEMRE_updateBarcode 'enrichment'," + enrichmentId + ",'" + shortbarcode + "'", db);
			 //now print the barcode
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Primary Enrichment", db);
			 ArrayList<String> alRow = new ArrayList<String>();
			 ResultSet rs = dbHelper.getResultSet("exec spEMRE_getPELabelValues '" + barcode + "'", db);
			 while(rs.next())
			 {
				 alRow.add(rs.getString(1));
				 alRow.add(rs.getString(2));
				 alRow.add(rs.getString(3));
			 }
			 rs.close();
			 rs = null;
			 printer.setFontType("R");
			 printer.setStartPrintYCoord(20);
			 String label = printer.getZPLforBoldBCLabel(shortbarcode, alRow);
			 S4MPCRBarcode.print(printService, shortbarcode, label); 
			 Thread.sleep(200);
		 }
		 catch(Exception ex)
		 {
			 throw new LinxSystemException(ex.getMessage());
		 }
	 }
	

	 
	/**
	 * Sets the Next LIMS ID widget value by calling db
	 * for the next available ID.
	 * @param request
	 * @param task
	 * @param db
	 */
	 protected void populateUI(HttpServletRequest request, Task task, Db db)
	 {
		 String limsId = getNextEnrichmentID(db);
		  if(limsId == null)
		  {
				  throw new LinxSystemException("LIMS did not provide a next ID for a primary enrichment.");
		  }
		  task.getDisplayItem(ItemType.PRIMARY_ENRICHMENT).setValue(limsId);

	 }
	 
	 /** 
	  * Retrieves the autogenerated next LIMS ID from the database
	  * @param db
	  * @return SI2-SGI-E-xxxxxx 
	  */
	protected String getNextEnrichmentID(Db db)
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
