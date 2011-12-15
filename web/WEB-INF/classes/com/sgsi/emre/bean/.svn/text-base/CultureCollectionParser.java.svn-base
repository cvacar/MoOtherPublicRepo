package com.sgsi.emre.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.CultureCollection;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Data container for Culture Collection import file used by task
 * Culture Collection.
 * 
 * @author TJS/Wt for SGI
 * @date 4/2011
 * 
 */

public class CultureCollectionParser extends SamplingTimepointDataParser
{
	int STRAIN_INDEX = 0;
	int START_DATE_INDEX = 1;
	int CULTURE_INDEX = 2;

	ArrayList<String> ayDependentCols = null; // lists two-column properties

	/**
	 * Empty constructor allows access to XLS file 
	 * convenience methods that use POI API.
	 */
	public CultureCollectionParser()
	{
	}

	/**
	 * Called by Culture Collection to create new records
	 * in custom db table CULTURECOLLECTION and CULTURECOLLECTIONDATA
	 * and CULTURECOLLECTIONHISTORY. Culture ID field is blank in
	 * every data row; stored proc will create on insert.
	 * @param sheet
	 * @param task
	 * @param dbHelper
	 * @param db
	 * @return true if worksheet has data values
	 */
	public ArrayList<String> insertDataValues(HSSFSheet sheet, CultureCollection task, EMREDbHelper dbHelper,
			Db db)
	{
		ayDependentCols = getListOfDependentColumns(db);
		return parseXLS(true/*create new*/, sheet, task, dbHelper, db);
	}
	
/**
 * Returns true if data rows were found in this worksheet
 * @param bCreateNew -- true = submit, false = just update
 * @param sheet
 * @param task
 * @param dbHelper
 * @param db
 * @return
 */
	protected ArrayList<String> parseXLS(boolean bCreateNew, HSSFSheet sheet,
			CultureCollection task, EMREDbHelper dbHelper, Db db)
	{
		
		ArrayList<String> cultures = new ArrayList<String>();
		ArrayList<String> idParams = new ArrayList<String>();
		ArrayList<String> params = new ArrayList<String>();
		
		HSSFRow dataRow = null;
		HSSFCell cell = null;
		HSSFCell col  = null;
		String cellValue = null;
		String strain = null;
		String startDate = null;
		String culture = null;
		boolean bNew = false;

		/**** FIND HEADER ROW  ****/
		// -- iterator guarantees rows are non-null
		Iterator<Row> rowItor = sheet.rowIterator();
		while(rowItor.hasNext())
		{
			dataRow = (HSSFRow)rowItor.next();
			if(isHeaderRow(dataRow, task.getColumnKey()))
			{
				setHeaderRow(dataRow); // keep a ref to header row
				break; // rowItor is still populated
			}				
		}// next row
		if(getHeaderRow() == null)
		{
			throw new LinxUserException("Could not find header row "
					+ " starting with " + task.getColumnKey()
					+ " in worksheet " + task.getTaskName()
					+ ". Please be sure that you are using the latest template.");
		}
		// at exit, have set header row or thrown error if not found
		/**** FOUND HEADER ROW ****/
			
		/******** START OF DATA ROWS *******/
		while(rowItor.hasNext())
		{	
			// continue looping, now into the data rows
			strain = null;
			startDate = null;
			culture = null;
			bNew = false;
			
			dataRow = (HSSFRow)rowItor.next();
			if(isNullOrBlank(dataRow.getCell(0)))
			{
				return cultures; // END OF DATA ROWS
			}
			
			/***** START DATA ROW's CELLS ***/
			// must check for values in ALL required columns, so use header itor
			// -- (data row itor would not return blank/null columns)
			Iterator<Cell> colItor = getHeaderRow().cellIterator();
			while(colItor.hasNext())
			{
				params.clear();
				col = (HSSFCell)colItor.next(); 
				cell = dataRow.getCell(col.getColumnIndex());
				if(cell == null)
				{
					cell = dataRow.createCell(col.getColumnIndex());
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				}
				
				// test for required columns
				if(isNullOrBlank(cell) && isHeaderRequired(col.getColumnIndex()))
				{
						throw new LinxUserException("Missing a value in required column ["
								+ getColumnNameByCellIndex(cell.getColumnIndex())
								+ "] in worksheet [" + sheet.getSheetName() + "],"
								+ " row " + String.valueOf(dataRow.getRowNum())); 
					
				}
				else if(col.getColumnIndex() == STRAIN_INDEX)
				{
					strain = getValueAsString(cell);
					continue;
				}
				else if(col.getColumnIndex() == START_DATE_INDEX)
				{
					startDate = getValueAsString(cell);
					task.validateYYMMDD(startDate);
					continue;
				}
				else if(col.getColumnIndex() == CULTURE_INDEX)
				{
					if( cell == null )
					{
						cell = dataRow.createCell(CULTURE_INDEX);
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						cell.setCellValue("");
					}
					if(isNullOrBlank(cell))
					{
						// DEFINE A NEW CULTURECOLLECTION RECORD
						bNew = true; // set a flag
						params.add(strain);
						params.add(startDate);
						params.add(task.getCultureType());
						params.add(task.getTranId() + "");
						try
						{
							culture = 
								db.getHelper().getDbValueFromStoredProc(db, "spEMRE_insertCultureCollection", params);
							// update imported file
							cell.setCellValue(culture);
							cellValue = culture;
							// for core processing
							task.getItem(ItemType.STRAIN).appendValue(strain);
							// let task append culture to correct server-side itemtype
						}
						catch (Exception ex)
						{
							throw new LinxUserException(
									"In worksheet [" + sheet.getSheetName() + "], row " 
									+ dataRow.getRowNum()
									+ ", ID " + params.get(0) + ": " + ex.getMessage());
						}		
					}
					culture = getValueAsString(cell);
					if(!cultures.contains(culture))
					{
						cultures.add(culture);
					}
					continue;
				}
				// for cols > CULTURE_INDEX, store key/value pairs
			
				/**** GET ALL KEY/VALUE PAIRS (GENERIC COLS) IN THIS ROW *****/
				String property = getColumnName(cell);
				cellValue = getValueAsString(cell);	
				if(WtUtils.isNullOrBlankOrPlaceholder(cellValue) 
						&& !isHeaderRequired(col.getColumnIndex()))
				{
					continue; // skip this blank cell
				}
				
				// handle dependent columns (second column is usually units)
				if(isDependentColumn(col))
				{
						// DEPENDENT COLUMN
						col = (HSSFCell)colItor.next(); // burn next column
						String adjProperty = getColumnName(col);
						cell = dataRow.getCell(col.getColumnIndex());
						if(WtUtils.isNullOrBlankOrPlaceholder(adjProperty) 
								&& isHeaderRequired(col.getColumnIndex())) 
						{
							throw new LinxUserException("Missing a value in required column "
									+ task.getRequiredHeaders()[col.getColumnIndex()]
									+ " in worksheet [" + sheet.getSheetName() + "],"
									+ " row " + String.valueOf(dataRow.getRowNum())); 

						}
						// concatenate two columns
						property = property + " " + adjProperty;
						String adjValue = getValueAsString(cell);	
						cellValue = cellValue + " " + adjValue;
				}
			
				// save a key/value pair to db
				// -- (strips * from required cell headers)
				params.clear();
				params.add(culture);
				params.add(property);
				params.add(cellValue);
				params.add(String.valueOf(!bNew).substring(0,1)); // create history record for existing cultures only
				params.add(task.getTranId()+"");
				try
				{
					// stored proc provides special handling for End Date and Comments
					dbHelper.callStoredProc(
							db, "spEMRE_updateCultureCollection", params, false/*no output param*/, true);
				}
				catch (Exception ex)
				{
					throw new LinxUserException(
							"In worksheet [" + sheet.getSheetName() + "], row " 
							+ dataRow.getRowNum()
							+ ", column " + property
							+ ", value " + cellValue + ": " + ex.getMessage());
				}
			}// next cell/column
			/**** END OF KEY/VALUE PAIRS (GENERIC COLS) IN THIS ROW *****/
		}// next data row
		/****** END DATA ROWS ********/
		// at exit, have looped thru all non-null/non-blank data rows 
		return cultures;
	}



	/**
	 * Returns list of properties that depend on the column immediately
	 * preceding or following their columns.
	 * @param col
	 * @param db
	 * @return list of two-column properties
	 */
	protected ArrayList<String> getListOfDependentColumns(Db db)
	{
		String sql = "exec spEMRE_getListOfDependentCultureColumns";
		ayDependentCols = db.getHelper().getListEntries(sql, db);
		return ayDependentCols;
	}
	
	/**
	 * Looks up column name in list retrieved from db
	 * at init (spEMRE_getListOfDependentCultureColumns).
	 * @param col
	 * @return true if column has a buddy
	 */
	protected boolean isDependentColumn(HSSFCell col)
	{
		int colIdx = col.getColumnIndex();
		String colname = getColumnName(col); 
			
		int idx = ayDependentCols.indexOf(colname);
		if(idx > -1) 
		{
			return true;
		}
		return false;
	}


}
