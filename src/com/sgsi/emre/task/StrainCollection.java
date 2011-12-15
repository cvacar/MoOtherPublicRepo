package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.util.S4MSmallBarcode;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Workflow;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * StrainCollection
 *
 * Handles custom action to print a strain-specific barcode label
 * and update custom table for strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2008
 */
public class StrainCollection extends EMRETask
{
  protected String ROWSET = "Locations";
  protected int COLUMN_CHECKBOX = 1;
  protected int COLUMN_LOCATION = 2;  boolean bNewStrain = false;
  protected boolean bHaveFile = false;
  protected ArrayList<String> strains = new ArrayList<String>();
  protected ArrayList<String> columns = new ArrayList<String>();


  protected String getOrganism()
	{
		// TODO Auto-generated method stub
		return getDisplayItemValue("StrainType");
	}

/**
   * Prints small (1" X 0.5 ") barcodes on the S4M printer.
   *
   *@param request
   *@param db
   */
  public void printLabels(String strain, HttpServletRequest request, Db db)
  {
	  try
	  {
		// construct the label from user's fields

		  //lets make sure they did a save first.
		  List lsStrains = db.getHelper().getListEntries("exec spMet_GetStrainLocations '" 
				  + strain + "'", db);
		  if(lsStrains.isEmpty())
		  {
			  throw new LinxUserException("Please save the strain locations before printing labels (click [Save]).");
		  }
		  String noteBook = getDisplayItemValue(DataType.NOTEBOOK_REF);
		  if(WtUtils.isNullOrBlankOrPlaceholder(noteBook))
		  {
			  throw new LinxUserException("Please enter notebook page, then try again.");
		  }
		  //add NB as a prefix to the notebook page
		  noteBook = "NB" + noteBook;
		  S4MSmallBarcode print = new S4MSmallBarcode();
		  EMREDbHelper dbHelper = new EMREDbHelper();
		  PrintService printService = dbHelper.getPrintServiceForTask("Strain Collection", db);
		  //if we made it this far we found the zebra printer
		  //now lets start printing barcodes
		  //Loop through the freezer locations and print barcodes
		  TableDataMap rowMap = new TableDataMap(request, ROWSET);
	      int numLocations = rowMap.getRowcount();
		  for(int rowIdx = 1; rowIdx <= numLocations; rowIdx++)
		  {
			  if( rowMap.isCheckboxChecked(rowIdx, COLUMN_CHECKBOX))
			  {
				  String location = (String)rowMap.getValue(rowIdx, COLUMN_LOCATION);
				  //add FZ as a prefix to the freezer location
				  location = "FZ" + location;
				  ArrayList<String> alrows = new ArrayList<String>();
				  alrows.add(noteBook);
				  alrows.add(location);
				  //we need to update the strain table with the barcode
				  String strainId = dbHelper.getDbValue("exec spEMRE_getStrainId '" + strain + "'", db);
				  //lets remove the "SGI-E" so the barcode fits on the label
				  //strain = strain.replace("-SGI-E", ""); 
				  dbHelper.executeSQL("exec spEMRE_updateBarcode 'strain'," + strainId + ",'" + strain + "'", db);
				  String label = print.getZPLforLabel(strain, alrows);
				  S4MSmallBarcode.print(printService, strain, label);
				  Thread.sleep(200);
				  alrows.clear();
				  alrows = null;
			  }
		  }// next location
		  setMessage("Successfully printed strain barcodes.");  
	  }
	  catch(Exception ex )
	  {
		  throw new LinxUserException("Error occurred during label printing: " + ex.getMessage());
	  }
  }

  
  	/**
  	 * Overridden to leave last Strain's info displayed
  	 * in UI. 
  	 * @param request
  	 * @param wf
  	 */
	public void cleanupTask(HttpServletRequest request, Workflow wf)
	{
		
		//super.cleanupTask(request, wf);
	}
	
	
	/**
	 * Generates the next freezer locations for the given strain type
	 * by looking up current positions and incrementing. Does not actually
	 * reserve any locations in the db per Gena Lee 10/17/2008. Handles
	 * no boxes and crossing boxes (boundary conditions). Handles variable
	 * number of each type of vial to make (standard & backup). Calls 
	 * stored procedure spMet_getCurrentBoxAndPosition.
	 * 
	 * @param strainType
	 * @param numStrains
	 * @param task
	 * @param db
	 */
	public ArrayList<String> reserveFreezerLocations(String nextStrain, Db db)
	{
		String strainType = nextStrain.substring(0,2);
		ArrayList<String> locs = new ArrayList<String>();
		String backupBoxPrefix = strainType + "BU"; //getStrainBackupBoxPrefix(strainType, db);
		String boxPrefix = strainType;
		int currBoxNum = 0;
		int currPos = 0;
		String boxNum = "0";
		int pos = 0;
		String freezer = null;
		int numVials = 0;
		String padPos = null; // zero-padded position, e.g. '01'
		String loc = null;
		
		String sql = null;
		ResultSet rs = null;
		try
		{
			// we're going for robustness via brute force here
			sql = "exec spMet_getCurrentBoxAndPosition '" + strainType + "', '" + boxPrefix + "'";
			rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				currBoxNum 		= rs.getInt(1);
				currPos    		= rs.getInt(2); // may be zero, which is OK
				freezer    		= rs.getString(3);
				numVials			= rs.getInt(4); // number of slots to reserve
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
		
		pos = currPos; // may be zero if new box
		int vialCount = 81; 
		if(freezer.equalsIgnoreCase("0810")) // todo: elim literal LN2 freezer name
		{
			vialCount = 84;
		}
		for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
		{
			if(pos < vialCount) // todo: eliminate magic number
			{
				// there's room in this box
				pos = pos + 1;
			}
			else
			{  
				// increment box
				currBoxNum = currBoxNum + 1;
				pos = 1;
			}
			boxNum = EMRETask.zeroPad(currBoxNum,2);
			padPos = zeroPadPosition(pos);
			loc = freezer + ":" 
			    + boxPrefix + boxNum + ":"
			    + padPos;			
			locs.add(loc);
		}// next archive
		//*************
		// at exit, locs has numVials lines in it, one per strain location
	
		// backup archive
		try
		{
			sql = "exec spMet_getCurrentBoxAndPosition '" + strainType + "', '" + backupBoxPrefix + "'";
			rs = dbHelper.getResultSet(sql, db);
			while(rs.next())
			{
				currBoxNum = rs.getInt(1);
				currPos    = rs.getInt(2);
				freezer    	= rs.getString(3);
				numVials	= rs.getInt(4); // number of archive vials to make
			}// expecting only one
			rs.close();
			rs = null;
		}
		catch (SQLException e)
		{
			throw new LinxSystemException(e.getMessage());
		}
		// at exit, know last filled pos in this box type
		
		// Backup archive(s)
		if(currBoxNum == 0) // handle boundary (start-up) condition
		{
			currBoxNum = 1;
		}
		
		pos = currPos; // may be zero if new box
		vialCount = 81; 
		if(freezer.equalsIgnoreCase("0810")) // todo: elim literal LN2 freezer name
		{
			vialCount = 84;
		}
		for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
		{
			if(pos < vialCount) // todo: eliminate magic number
			{
				// there's room in this box
				pos = pos + 1;
			}
			else
			{  
				// increment box
				currBoxNum = currBoxNum + 1;
				pos = 1;
			}
			boxNum = EMRETask.zeroPad(currBoxNum,2);
			padPos = zeroPadPosition(pos);
			loc = freezer + ":" 
			    + backupBoxPrefix + boxNum + ":"
			    + padPos;			
			locs.add(loc);
		}// next archive
		//*************
		// at exit, locs has numVials lines in it, one per strain location
		
		return locs;
	}
	
	
	/** 
	 * As of EMRE v2.1, returns first two letters
	 * of strain item.ITEM value. Corresponds to a
	 * list member from APPVALUES with parenttype = 'StrainType'.
	 * @param strainId
	 * @return first two letters of given strain ITEM.item value
	 */
	protected String getStrainType(String strainId)
	{
		return strainId.substring(0,2);
	}
	/**
	 * Returns the correct prefix for the strain based upon
	 * the given organism. If the optional param strain is not null,
	 * throws an error if the strain's prefix does not match the prefix expected
	 * given the organism.
	 * @param taskName
	 * @return prefix
	 */
	public String getStrainPrefix(String organism, String optionalStrain, Db db)
	{
		if(WtUtils.isNullOrBlankOrPlaceholder(organism))
		{
			throw new LinxUserException("Please provide strain type, then try again.");
		}
		if(organism.indexOf("-") > 0)
		{
			// can do the work right here
			// e.g., "WH - heterotroph wildtype"
			if(!WtUtils.isNullOrBlank(optionalStrain)
					&& !optionalStrain.startsWith(organism.substring(0,2)))
			{
				throw new LinxUserException("Strain IDs for strains of type [" + organism 
						+ "] must start with the prefix: " + organism.substring(0,2));
			}
		}
		ArrayList<String> params = new ArrayList<String>();
		params.add(organism);
		
		String prefix = dbHelper.getDbValueFromStoredProc(
				db, "spEMRE_getStrainPrefix", params);
		
		if(!WtUtils.isNullOrBlank(optionalStrain)
				&& !optionalStrain.startsWith(prefix))
		{
			throw new LinxUserException("Strain IDs for strains of type [" + organism 
					+ "] must start with the prefix: " + prefix);
		}
		return prefix;
	}
	
		/**
	 * Returns the correct prefix for the standard archive box.
	 * See overriddes in Photo strain tasks.
		 * @param strainType 
	 * @param taskName
	 * @return
	 */
	protected String getStrainBoxPrefix(String strainType, Db db)
	{
		String sql = "exec spMet_getStrainCollectionBoxPrefix '" + strainType + " Strain'";
		String prefix = dbHelper.getDbValue(sql, db);
		return prefix;
	}
	
	/**
	 * Returns correct prefix for boxes storing backup archives,
	 * currently 'BU' for all strains including Photo strains. 
	 * @param strainType 
	 * @return
	 */
	protected String getStrainBackupBoxPrefix(String strainType, Db db)
	{
		String sql = "exec spMet_getStrainCollectionBackupBoxPrefix '" + strainType + " Strain'";
		String prefix = db.getHelper().getDbValue(sql, db);
		return prefix;
	}
	
  /**
	 * Number of rows to show in the Location UI table
	 * on initial display, for defining new strain.
	 * @return 3 for Brown Lab
	 */
  protected int getLocationRowCount()
  {
	  return 4;
  }
  

	public String getColumnKey()
	{
		
		return "Strain ID";
	}

	public String getSheetKey()
	{
		return "Strains";
	}
  
}
