package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.util.ArrayList;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class PrintLabels extends EMRETask 
{

	@Override
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		String itemType = getServerItemValue("ItemType");
		if(WtUtils.isNullOrBlankOrPlaceholder(itemType))
			throw new LinxUserException("Please select a label type from the drop down list.");
		String minBarcode = getServerItemValue("MinBarcode");
		String maxBarcode = getServerItemValue("MaxBarcode");
		
		ArrayList<String> params = new ArrayList<String>();
		 params.add(minBarcode);
		 params.add(maxBarcode);
		 params.add(itemType);
		 
		 ArrayList<String[]> alData = new ArrayList<String[]>();
		 try
		 {
			 ResultSet rs  = dbHelper.getResultSetFromStoredProc(db, "spEMRE_getInfoForBulkPrint", params, false);
			 
			 while(rs.next())
			 {
					 String[] ay = new String[3];
					 ay[0] = rs.getString(1);
					 ay[1] = rs.getString(2);
					 ay[2] = rs.getString(3);
					 alData.add(ay);
					 ay = null;
			 }
			 rs.close();
			 rs = null;
			 
			//did we get anything from the DB?
			 if(alData.size() < 1)
				  throw new LinxUserException("You must save the LIMS IDs before printing labels.");
		
			//now that we know we have valid strains lets loop and print
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Strain Collection", db);
			 printer.setStartPrintXCoord(8);
			 printer.setStartPrintYCoord(10);
			 for(String[] ay : alData)
			 {
				 String s1 = ay[0];
				 String s2 = ay[1];
				 String s3 = ay[2];
				 
				 ArrayList<String> alRow = new ArrayList<String>();
				 alRow.add(s2);
				 alRow.add(s3);
				 s1 = s1.replace("-SGI-E", "");
				 String label = printer.getZPLforLabel(s1, alRow);
				 S4MSmallBarcode.print(printService, s1, label);
				 alRow = null;
				 //Lets make sure these things print in order
				 Thread.sleep(20);
			}	
		 }
		 catch(Exception ex)
		 {
			 throw new LinxUserException("Error occurred during barcode printing: " + ex.getMessage());
		 }
		 
		 
	}
	
	
}
