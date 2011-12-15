package com.sgsi.emre.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.CreateStrain;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.UpdateStrain;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Data container for import file used by tasks Create Strain and Update Strain,
 * eff EMRE v2.1.
 * 
 * @author TJS/Wt for SGI
 * @date 4/2011
 * 
 */

public class StrainPOIBulkImporter extends POIParser
{
	
	/**
	 * Empty constructor allows access to convenience methods that use POI API to
	 * parse a Microsoft Excel *.xls file.
	 */
	public StrainPOIBulkImporter()
	{
	}
	
	/**
	 * Called by task Create Strain to create new records in custom db table
	 * STRAIN. 
	 * 
	 * @param workbook
	 * @param task
	 * @param dbHelper
	 * @param db
	 * @return list of items to add to server-side DOM
	 */
	public List<String> insertDataValues(HSSFWorkbook workbook, CreateStrain task,
			EMREDbHelper dbHelper, Db db)
	{
		
		ArrayList<String> params = new ArrayList<String>();
		int locIndex = 0;

		// expecting only one worksheet -- add a loop here if > 1
		String columnKey = task.getColumnKey();
		String sheetKey = task.getSheetKey();
		HSSFSheet sheet = getWorksheet(workbook, sheetKey);
		setSheetName(workbook.getSheetName(0)); // patch a hole in the POI API

		HSSFRow headerRow = getRowWithColumnHeaders(sheet, sheet.getSheetName(), columnKey);
		setHeaderRow(headerRow);
		
		// loop thru data rows in this worksheet
		Iterator<Row> rowItor = sheet.rowIterator();
		HSSFRow dataRow = null;
		while(rowItor.hasNext())
		{
			locIndex = 0;
			params.clear();
			dataRow = (HSSFRow)rowItor.next();
			if(dataRow.getRowNum() < getHeaderRow().getRowNum()+1)
			{
				continue; // not at data rows yet
			}
			
			/****** INSERT NEW STRAIN ******/
			// by here, we are in data rows
			String strainId 	= getCellValueByColName(dataRow, columnKey);
		
			// minimum required to define a new STRAIN record
			params.add(strainId);
			params.add(getCellValueByColName(dataRow, "Strain Name"));
			params.add(strainId.substring(0,2)); // e.g., 'WC' (prefix)
			params.add(getCellValueByColName(dataRow, "Project"));
			params.add(getCellValueByColName(dataRow, "Page Ref [notebook-pg]"));
			params.add(getCellValueByColName(dataRow, "Comment"));
			params.add(task.getTranId() + "");
			try
			{
				// ** insert into custom db table STRAIN **/
				dbHelper.callStoredProc(db, "spEMRE_insertStrain", params, false);
			}
			catch (Exception ex)
			{
				throw new LinxUserException("In worksheet [" + getSheetName()
						+ "], row " + dataRow.getRowNum() + " with strain ID "
						+ strainId + ": " + ex.getMessage());
			}
			task.getServerItem(ItemType.STRAIN).appendValue(strainId); 
			
			
			/******** INSERT/UPDATE ADD'L STRAIN PROPERTIES ******/
			// loop thru columns in header and data row, looking for key/value pairs
			String cellValue = null;
			Iterator<Cell> colItor = getHeaderRow().cellIterator();
			while(colItor.hasNext()) // every column, not just required cols
			{
				String colName = getValueAsString((HSSFCell)colItor.next());
				cellValue = getCellValueByColName(dataRow, colName);

				/****** INSERT STRAIN LOCATIONS ********/
				if(colName.startsWith("Location"))
				{
					locIndex++;
					insertStrainLocation(strainId, cellValue, locIndex, task, dbHelper, db);
					continue;
				}
				/******** INSERT OTHER STRAIN PROPERTIES (ctd) ******/
				// save this key/value pair as a property of this strain
				params.clear();
				params.add(strainId);
				params.add(colName);
				params.add(cellValue);
				params.add(task.getTranId() + "");
				try
				{
					// stored proc will check for required values
					// -- to keep it out of the code (per Vidya)
					dbHelper.callStoredProc(db, "spEMRE_UpdateStrain", params, false);
				}
				catch (Exception ex)
				{
					throw new LinxUserException("In worksheet [" + getSheetName()
							+ "], row " + dataRow.getRowNum() + ", column [" + colName 
							+ "], strain ID "	+ strainId + ": " + ex.getMessage());
				}
			}// next key/value cell
		}// next data row
		return task.getServerItemValues(ItemType.STRAIN);
	}
	
	/**
	 * Inserts new strain locations into custom location tables.
	 * @param strainId
	 * @param location
	 * @params locIndex
	 * @param task
	 * @param dbHelper
	 * @param db
	 */
	protected void insertStrainLocation(String strainId, String location, int locIndex,
			CreateStrain task, EMREDbHelper dbHelper, Db db)
	{
		if(task.getTaskName().startsWith("Update"))
		{
			// redirect
			updateStrainLocation(strainId, location, locIndex, (UpdateStrain)task, dbHelper, db);
			return;
		}
	  String[] alLocs = location.split(":");
	  String freezer = alLocs[0];
	  String box = alLocs[1];
	  String coord = alLocs[2];
	  String strainType = null;
	  if(strainId.indexOf("BU") > 0)
	  {
	  	strainType = strainId.substring(0,4);
	  }
	  else
	  {
	  	strainType = strainId.substring(0,2);
	  }
	 
	  try
	  {
		  int idxLastColon = location.lastIndexOf(':');
		  String pos = location.substring(idxLastColon + 1);
		  //now lets zero pad the position
		  if(pos.length() < 2)
		  {
			  pos = task.zeroPadPosition(pos);
			  coord = pos;
			  location = location.substring(0,idxLastColon) + ":" + pos;
		  }
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException("Unable to parse location [" + location + "]: " + ex.getMessage());
	  }
	  String boxType  = box.substring(0,box.length() - 2); // assumes < 100 boxes per freezer
	  String transferPlan = strainType + " Freezer " + freezer + " Box " + boxType;
	  String transferPlanId = 
	  	db.getHelper().getDbValue("exec spEMRE_getTransferPlanId '" 
		  + transferPlan + "','" + coord + "'", db);

	  
	  ArrayList<String> params = new ArrayList<String>();
	  params.add(strainId);
	  params.add(freezer); 
	  params.add(box);
	  params.add(coord);
	  params.add(locIndex+""); //location index
	  params.add(strainType);
	  params.add(transferPlanId);
	  params.add(task.getTranId()+"");
	
    String	  sql = "spEMRE_InsertStrainLocation";
	  dbHelper.callStoredProc(db, sql, params, false, true);
	}

	/**
	 * Updates existing strain locations into custom location tables,
	 * returning true if there is no such location index record.
	 * @param strainId
	 * @param location
	 * @params locIndex
	 * @param task
	 * @param dbHelper
	 * @param db
	 */
	protected void updateStrainLocation(String strainId, String location, int locIndex,
			UpdateStrain task, EMREDbHelper dbHelper, Db db)
	{
	  String[] alLocs = location.split(":");
	  String freezer = alLocs[0];
	  String box = alLocs[1];
	  String coord = alLocs[2];
	  String strainType = null;
	  if(strainId.indexOf("BU") > 0)
	  {
	  	strainType = strainId.substring(0,4);
	  }
	  else
	  {
	  	strainType = strainId.substring(0,2);
	  }
	 
	  try
	  {
		  int idxLastColon = location.lastIndexOf(':');
		  String pos = location.substring(idxLastColon + 1);
		  //now lets zero pad the position
		  if(pos.length() < 2)
		  {
			  pos = task.zeroPadPosition(pos);
			  coord = pos;
			  location = location.substring(0,idxLastColon) + ":" + pos;
		  }
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException("Unable to parse location [" + location + "]: " + ex.getMessage());
	  }
	  String boxType  = box.substring(0,box.length() - 2); // assumes < 100 boxes per freezer
	  String transferPlan = strainType + " Freezer " + freezer + " Box " + boxType;
	  String transferPlanId = 
	  	db.getHelper().getDbValue("exec spEMRE_getTransferPlanId '" 
		  + transferPlan + "','" + coord + "'", db);

	  
	  ArrayList<String> params = new ArrayList<String>();
	  params.add(strainId);
	  params.add(location); // all of it
	  params.add(freezer); 
	  params.add(box);
	  params.add(coord);
	  params.add(locIndex+""); //location index
	  params.add(strainType);
	  params.add(transferPlanId);
	  params.add(task.getTranId()+"");
	
    String	  sql = "spEMRE_UpdateStrainLocation";
		dbHelper.callStoredProc(db, sql, params, false, true);

	}

	/**
	 * Called by task Update Strain to update strain values
	 * that may be updated.
	 * 
	 * @param workbook
	 * @param task
	 * @param dbHelper
	 * @param db
	 * @return list of items to add to server-side DOM
	 */

	public List<String> updateDataValues(HSSFWorkbook workbook, UpdateStrain task,
			EMREDbHelper dbHelper, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		int locIndex = 0;

		// expecting only one worksheet -- add a loop here if > 1
		String columnKey = task.getColumnKey();
		String sheetKey = task.getSheetKey();
		HSSFSheet sheet = getWorksheet(workbook, sheetKey);
		setSheetName(workbook.getSheetName(0)); // patch a hole in the POI API

		HSSFRow headerRow = getRowWithColumnHeaders(sheet, sheet.getSheetName(), columnKey);
		setHeaderRow(headerRow);
		
		// loop thru data rows in this worksheet
		Iterator<Row> rowItor = sheet.rowIterator();
		HSSFRow dataRow = null;
		while(rowItor.hasNext())
		{
			locIndex = 0;
			params.clear();
			dataRow = (HSSFRow)rowItor.next();
			if(dataRow.getRowNum() < getHeaderRow().getRowNum()+1)
			{
				continue; // not at data rows yet
			}
			
			// by here, we are in data rows
			String strainId 	= getCellValueByColName(dataRow, columnKey);
			task.getServerItem(ItemType.STRAIN).appendValue(strainId); 
			
			/******** INSERT/UPDATE ADD'L STRAIN PROPERTIES ******/
			// loop thru columns in header and data row, looking for key/value pairs
			String cellValue = null;
			Iterator<Cell> colItor = getHeaderRow().cellIterator();
			while(colItor.hasNext()) // every column, not just required cols
			{
				String colName = getValueAsString((HSSFCell)colItor.next());
				cellValue = getCellValueByColName(dataRow, colName);

				/****** UPDATE STRAIN LOCATIONS ********/
				if(colName.startsWith("Location"))
				{
					locIndex++;
					insertStrainLocation(strainId, cellValue, locIndex, task, dbHelper, db);
					continue;
				}
				/******** INSERT OTHER STRAIN PROPERTIES (ctd) ******/
				// save this key/value pair as a property of this strain
				params.clear();
				params.add(strainId);
				params.add(colName);
				params.add(cellValue);
				params.add(task.getTranId() + "");
				try
				{
					// stored proc will check for required values
					// -- to keep it out of the code (per Vidya)
					dbHelper.callStoredProc(db, "spEMRE_UpdateStrain", params, false);
				}
				catch (Exception ex)
				{
					throw new LinxUserException("In worksheet [" + getSheetName()
							+ "], row " + dataRow.getRowNum() + ", column [" + colName 
							+ "], strain ID "	+ strainId + ": " + ex.getMessage());
				}
			}// next key/value cell
		}// next data row
		return task.getServerItemValues(ItemType.STRAIN);
	}
}
