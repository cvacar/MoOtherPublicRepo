package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.util.ArrayList;

import javax.print.PrintService;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.util.S4MSmallBarcode;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class PrintBulkLabels extends EMRETask 
{
	ArrayList<String> alValidPrefixes = new ArrayList<String>();
	public PrintBulkLabels()
	{
		alValidPrefixes.clear();
		alValidPrefixes.add("PH-");
		alValidPrefixes.add("PE-");
		alValidPrefixes.add("SB-");
		alValidPrefixes.add("WT-");
		alValidPrefixes.add("WC-");
		alValidPrefixes.add("NC-");
		alValidPrefixes.add("GC-");
		alValidPrefixes.add("WE-");
		alValidPrefixes.add("WH-");
		alValidPrefixes.add("GE-");
		alValidPrefixes.add("NE-");
		alValidPrefixes.add("GH-");
		alValidPrefixes.add("NH-");
	}
	/**
	 * loops through the range of strains and prints them to the S4M printer
	 * @param minStrain
	 * @param maxStrain
	 * @param task
	 * @param db
	 */
	public void printLabels(String minStrain, String maxStrain, Task task, Db db)
	{
		try
		{
			 //lets validate the strain prefixes and numbers
			 validateStrains(minStrain, maxStrain);
			 String strainPrefix = minStrain.substring(0,minStrain.lastIndexOf('-'));
			  
			//lets make sure that the strains have been saved
			 EMREDbHelper dbHelper = new EMREDbHelper();
			 ArrayList<String> params = new ArrayList<String>();
			 params.add(minStrain);
			 params.add(maxStrain);
			 params.add(strainPrefix);
			 ResultSet rs  = dbHelper.getResultSetFromStoredProc(db, "spMet_GetStrainInfoForBulkPrint", params, false);
			 ArrayList<String[]> alData = new ArrayList<String[]>();
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
				  throw new LinxUserException("You must save the strain locations to LIMS before printing labels.");
		
			 //now that we know we have valid strains lets loop and print
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Strain Collection", db);
			Code.debug("Printing labels on printer: " + printService.getName());
			dbHelper.updateTaskHistoryComment(getTranId(), "Printer: " + printService.getName(), true/*append*/, db);
			 for(String[] ay : alData)
			 {
				 String strain = ay[0];
				 String notebook = ay[1];
				 String location = ay[2];
				 ArrayList<String> alRow = new ArrayList<String>();
				 alRow.add(notebook);
				 alRow.add(location);
				 //we need to update the strain table with the barcode
				 String strainItemId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
				 //lets remove the "SGI-E" so the barcode fits on the label
				 //strain = strain.replace("-SGI-E", "");
				 dbHelper.executeSQL("exec spEMRE_updateBarcode 'strain'," + strainItemId + ",'" + strain + "'", db);
				 String label = printer.getZPLforLabel(strain, alRow);
				 S4MSmallBarcode.print(printService, strain, label);
				 alRow = null;
				 //Lets make sure these things print in order
				 Thread.sleep(200);
			}	
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	/**
	 * validates the prefix and the numeric portion of the strain IDs
	 * @param minStrain
	 * @param maxStrain
	 * @return int[] containing the minStrainNum and MaxStrainNum at index 0 and 1 respectively
	 * @throws Exception
	 */
	private int[] validateStrains(String minStrain, String maxStrain)
		throws Exception
	{
		  int[] strainNums = new int[2];
		  String minPrefix = minStrain.substring(0,3).toUpperCase();
		  if(!alValidPrefixes.contains(minPrefix))
			  throw new Exception("Invalid min strain ID.  The strain ID must start with PH-, PE-, or SB-.");
		  //lets get the integer values of the strain id
		  int minStrainNum = -1;
		  try
		  {
			  minStrainNum = Integer.parseInt(minStrain.substring(minStrain.lastIndexOf('-')+ 1));
		  }
		  catch(Exception ex)
		  {
			  throw new Exception("Unable to parse the integer portion of the min strain ID:\r\n" + ex.getMessage());
		  }
		  String maxPrefix = null;
		  int maxStrainNum = -1;
		  if(!WtUtils.isNullOrBlank(maxStrain))
		  {
			  maxPrefix = maxStrain.substring(0,3);
			  if(!alValidPrefixes.contains(maxPrefix.toUpperCase()))
					  throw new Exception("Invalid max strain ID.  The strain ID must start with PH-, PE-, or SB-.");
			  try
			  {
				  maxStrainNum = Integer.parseInt(maxStrain.substring(maxStrain.lastIndexOf('-') + 1));
			  }
			  catch(Exception ex)
			  {
				  throw new Exception("Unable to parse the integer portion of the max strain ID:\r\n" + ex.getMessage());
			  }
			  if(minStrainNum > maxStrainNum)
				  throw new Exception("The numeric portion of the min strain ID must be smaller than the max strain ID.");
			  //lets make sure that the prefixes are the same
			  if(!minPrefix.equalsIgnoreCase(maxPrefix))
				  throw new Exception("Mismatch strain IDs.  The strain IDs must start with the same prefix.");
			  
		  }
		  
		  strainNums[0] = minStrainNum;
		  strainNums[1] = maxStrainNum;
		  return strainNums;
	}
}
