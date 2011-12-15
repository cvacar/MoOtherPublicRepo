package com.sgsi.emre.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * UpdateStrain
 *
 * Eff v2.1
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2011
 */
public class UpdateStrain extends CreateStrain
{
	

	String ID_COLUMN = "Strain ID";
	String WORKSHEET_1 = "Strains - Euk & Cyano";
	String WORKSHEET_2 = "Strains - Prok"; 		//  -- keep sync'd with worksheet tab names
	/**
	 * Overridden to extract list of existing strain IDs from import file
	 * for std core processing.
	 * Eff EMRE LIMS v2.2 7/2011.
	 */
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		if (dbHelper == null)
		{
			dbHelper = new EMREDbHelper();
			dbHelper.init();
		}
		String fileId = getServerItemValue(FileType.STRAIN_IMPORT_FILE);

		if (!WtUtils.isNullOrBlank(fileId))
		{
			doBulkImportPreSave(fileId, request, response, user, db);
			return;
		}
		// else, std core screen processing (of Strain value)
			
	}
	
	/**
	 * Handles pre-save setting of Strain itemtype values if 
	 * user has provided a bulk import file.
	 * @param fileId
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	protected void doBulkImportPreSave(String fileId, HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		// extract list of IDs from user's file and set up for core processing
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// first worksheet - may or may not have values
		ArrayList<String> ids = 
			getIdsFromImportFile(inFile, WORKSHEET_1, ID_COLUMN);

		// second worksheet - may or may not have values
		ids.addAll(getIdsFromImportFile(inFile, WORKSHEET_2, ID_COLUMN));
		if (ids.isEmpty())
		{
			// how to handle in absence of error giving cause?
			throw new LinxUserException("No rows containing existing strains were found in import file.");
		}
		getServerItem(ItemType.STRAIN).setValues(ids);
		
		setMessage("Successfully updated " + ids.size() + " strains. " 
				+ " Go to task 'Print XXX Labels' to print labels,"
				+ " or click on task Create Strain in the workflow menu to autogenerate new IDs.");
	}


	
	/**
	 * Overridden to dispatch to either bulk import processing 
	 * or screen input processing, depending on whether an 
	 * import file was provided.
	 * Eff EMRE LIMS v2.2 7/2011
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		String fileId = getServerItemValue(FileType.STRAIN_IMPORT_FILE);

		if (WtUtils.isNullOrBlank(fileId))
		{
			doScreenPostSave(request, response,user,db);
			
		}
		else
		{
			doBulkImportPostSave(fileId, request, response, user, db);
		}
	}

	/**
	 * Parses import file, creates bulk insert file,
	 * and updates custom tables with strain properties. Relies
	 * on doTaskWorkPreSave() having found strain names and
	 * std core processing having validated existing strains in ITEM.item.
	 * Eff EMRE LIMS v2.2 7/2011
	 */
	protected void doBulkImportPostSave(String fileId, HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		// extract list of strains from user's file
		File inFile = this.getFile(fileId, db);

		 // blindly parse column values into tab-delimited file 
		// -- (commas occur often in data) 
		File biFile = createBulkInsertFile(inFile, WORKSHEET_1, ID_COLUMN, db);		  
		
		String sp = "spEMRE_bulkUpdateStrains";		
		dbHelper.callStoredProc(db, sp, biFile.getPath() + "|" + WORKSHEET_1);
		
		File biFile2 = createBulkInsertFile(inFile, WORKSHEET_2, ID_COLUMN, db);		  
		 
		dbHelper.callStoredProc(db, sp, biFile2.getPath() + "|" + WORKSHEET_2);
		
		biFile.delete();
		biFile2.delete();
		
		//throw new LinxUserException("Rolling back");
	}
	
	/**
	 * Saves screen inputs to a format acceptable for bulk import,
	 * then calls bulk insert. Separately imports data files browsed
	 * to by the user, associating each with the single strain. 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	protected void doScreenPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		String DELIM = Strings.CHAR.TAB; // commas often occur in data

		// create empty bulk insert file
		String path = dbHelper.getApplicationValue(db, "System Properties", "Bulk Insert Local Dir");
		String filename = "bi_" + getTranId() + ".txt";
		File biFile = new File(path + filename);
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(biFile);
		}
		catch (IOException e1)
		{
			throw new LinxUserException(e1);
		}
		
		// walk the screen widgets, saving key/values to bulk insert file
		// ("screen scraper" strategy to adapt to new widgets quickly)
		String strainItem = getDisplayItemValue(ItemType.STRAIN);
		ArrayList<DisplayItem> ditems = (ArrayList<DisplayItem>)this.getDisplayItems();
		ListIterator ditor = ditems.listIterator();
		while(ditor.hasNext())
		{
			DisplayItem ditem = (DisplayItem)ditor.next();
			String ditemtype = ditem.getItemType();
			String label 	 = ditem.getLabel();
			/*if(label.indexOf("%") > 0)
			{
				label = label.replace("%","percent");
			}*/
			if(label.startsWith("Comment"))
			{
				label = "Comment"; // match template definition
			}
			else if(label.startsWith("Origin ID"))
			{
				label = "Origin ID"; // shorten for template match
			}
			else if(label.equalsIgnoreCase("Strain ID"))
			{
				continue; // skip
			}
			String value 	 = ditem.getValue();
			
			if(ditem.getWidget().startsWith("text")
			|| ditem.getWidget().startsWith("dropdown")) // todo: add other widget types
			{
				try
				{
					String line = strainItem + DELIM + label + DELIM + value + DELIM + getTranId();
					writer.write(line + EMREStrings.CHAR.CRLF);
					System.out.println("\n" + line);
	
				}
				catch (IOException e)
				{
					e.printStackTrace();
					throw new LinxUserException(e);
				}			
			}
		}// next display item
		// at exit, biFile contains one line per strain + property
		// todo: add freezer locations to biFile
		try
		{	
			writer.flush();
			writer.close();
			writer = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new LinxUserException(e);
		}
		
		// todo: write a separate sp for screen-based bulk import?
		ArrayList<String> params = new ArrayList<String>();
		// ** TODO: check number of rows in Locations view?
		String worksheetName = getWorksheetNameForStrain(strainItem, db);
		params.add(biFile.getPath() + "|" + worksheetName);
		String sp = "spEMRE_bulkUpdateStrains";		
		dbHelper.callStoredProc(db, sp, params, false, true);
		
		//biFile.delete();
		setMessage("Successfully updated values for Strain " + strainItem 
				+ ". Click Show Strain Data to refresh values.");

		//biFile.delete(); 
		//throw new LinxUserException("Rolling back");
	}

	/**
	 * Uses strain type to determine which worksheet name
	 * corresponds to this screen-based entry. 
	 * @param strainItem
	 * @return name of correct worksheet for strain type
	 */
	protected String getWorksheetNameForStrain(String strainItem, Db db)
	{
		String sp = "spEMRE_getStrainTypeByPrefix";
		String prefix = strainItem.substring(0,2);
		String strainType = dbHelper.getDbValueFromStoredProc(db, sp, prefix);
		if(strainType.indexOf("Euk") > 0)
			return WORKSHEET_1;
		else if(strainType.indexOf("Prok") > 0)
			return WORKSHEET_2;
		
		throw new LinxUserException("Strains of type other than WC,NC,GC,WE,NE,GE,WH,NH, and GH"
				+ " are not currently supported at Update Strain.");
	}


}
