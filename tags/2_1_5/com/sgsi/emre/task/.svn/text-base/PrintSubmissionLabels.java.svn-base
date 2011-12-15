package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.util.ArrayList;

import javax.print.PrintService;

import com.sgsi.emre.util.PrintBarcode;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Provides print services for the custom servlet.
 * @author TJS/Wildtype for SGI
 * @date 2/2011
 * @version 1.15
 *
 */
public class PrintSubmissionLabels extends EMRETask 
{
	
	/**
	 * Returns min or max submission ID (per "ORDER BY submissionId")
	 * for the given request. Called to populate min/max textboxes
	 * in task screen.
	 * @param requestId
	 * @param rangeEnd 
	 * @return
	 */
	public String getMinOrMaxSubmissionID(String requestId, String rangeEnd, Db db)
	{
		String sql = "spEMRE_getWorkingLabelForSubmissionRange";
		ArrayList<String> params = new ArrayList<String>();
		params.add(requestId);
		params.add(rangeEnd); // "min" or "max"
		
		String subId = dbHelper.callStoredProc(db, sql, params, true, true);
		return subId;
	}
	
	/**
	 * Retrieves user's selected range of submissions 
	 * from the database and prints the labels
	 * to the given label printer.
	 * @param requestId
	 * @param task
	 * @param db
	 */
	public void printLabels(String requestId, String minID, String maxID, String zebraPrinter, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(requestId);
			params.add(minID);
			params.add(maxID);
			String sql = "spEMRE_getSubmissionsForBulkPrint";
			 ResultSet rs  = dbHelper.getResultSetFromStoredProc(db, sql, params, false);
				 
			 ArrayList<String[]> alData = new ArrayList<String[]>();
			 while(rs.next())
			 {
				 String[] ay = new String[4];
				 ay[0] = rs.getString(1);
				 ay[1] = rs.getString(2);
				 ay[2] = rs.getString(3);
				 ay[3] = rs.getString(4);
				 alData.add(ay);
				 ay = null;
			 }
			 rs.close();
			 rs = null;
			 //did we get anything from the DB?
			 if(alData.size() < 1)
			 {
				  throw new LinxUserException(
						  "Please enter at least one label from Request "
						  + requestId 
						  + " for printing," +
						  " then try again.");
			 }
			 //now that we know we have valid submissions lets loop and print
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 //this task uses a 300DPI printer instead of the 203DPI printers that are 
			 //previously in the lab so we need to make everything a little bigger
			 printer.setBarcodeHeight(22);
			 printer.setStartPrintXCoord(30);
			 printer.setStartPrintYCoord(52);
			 printer.setFontType("B");
			 PrintService printService = this.getPrintServiceForTask(zebraPrinter, db);
			 for(String[] ay : alData)
			 {
				 String barcode 			= ay[0];
				 String limsid 				= ay[1];
				 String samplingtimepoint 	= ay[2];
				 String requester 			= ay[3];
				 
				 ArrayList<String> alRow = new ArrayList<String>();
				 if(limsid.length() > 14)//only 14 characters fit on a line - so split the id
				 {
					 int numIterations = 1;
					 char[] chars = limsid.toCharArray();
					 String line = "";
					 for(int i = 0; i < chars.length; i++)
					 {
						 do
						 {
							 do
							 {
								 line += chars[i];
								 i++;
							 }
							 while(i < chars.length && i < numIterations * 14);
							 alRow.add(line);
							 line = "";
							 numIterations++;
						 }
						 while(numIterations <= 2);//only going to print 2 lines - LIMSIDs can get huge
						 
					 }
					
				 }
				 else
				 {
					 alRow.add(limsid);
				 }
				 if(!WtUtils.isNullOrBlank(samplingtimepoint))
				 {
						 alRow.add(samplingtimepoint);
				 }
				 //users want the human readable barcode to be in a larger font so set that here
				 //the getZPLforLabel300DPI method will set the subsequent lines of the label to a smaller font.
				 printer.setFontHeight(20);
				 printer.setFontWidth(15);
				 printer.setFontType("B");
				 alRow.add(requester);
				 String label = printer.getZPLforLabel300DPI(barcode, alRow);
				 S4MSmallBarcode.print(printService, barcode, label);
				 alRow = null;
				 //Lets make sure these things print in order
				 Thread.sleep(500);
			}	
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	/**
	 * returns a PrintService for a given printer
	 * @param printer
	 * @param db
	 * @return
	 */
	public PrintService getPrintServiceForTask(String printer, Db db)
	{
		 PrintService printService = null;
		 try
		 {
			  printService = PrintBarcode.getPrintService(printer);
			  if(printService == null)
				  throw new Exception(
						  "Unable to locate the Zebra printer on the network. "
						  + "Please alert your LIMS administrator.");  
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return printService;
	}
}
