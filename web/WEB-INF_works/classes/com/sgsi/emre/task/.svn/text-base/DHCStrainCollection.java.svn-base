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
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class DHCStrainCollection extends EMRETask 
{
	boolean bNewStrain = false;
	boolean bHaveFile = false;
	  XLSParser strains = null;
	  ArrayList<String> columns = new ArrayList<String>();
	  private String ROWSET = "Locations";
	  private int COLUMN_CHECKBOX = 1;
	  private int COLUMN_LOCATION = 2;

	/**
	   * If a bulk import file was provided by user,
	   * parses file and created list of Strain objects
	   * for use later.
	   * @param request
	   * @param response
	   * @param user
	   * @param db
	   */
	  public void doTaskWorkPreSave(HttpServletRequest request,
				HttpServletResponse response, User user, Db db)
		{

			// did user provide a bulk import file?
			String fileId = getServerItemValue(FileType.STRAIN_IMPORT_FILE);
			if (!WtUtils.isNullOrBlank(fileId))
			{
				// yes
				bHaveFile = true;
			}

			// else, no file - import an individual strain
			// mainly housekeeping here
			String strain = getServerItemValue(ItemType.STRAIN);
			bNewStrain = false;
			//strains.clear(); // housekeeping
			if (WtUtils.isNullOrBlankOrPlaceholder(strain))
			{
				throw new LinxUserException(
						"Please enter a Strain ID, then try again.");
			}
			if (!dbHelper.isItemExisting(strain, ItemType.STRAIN, db))
			{
				bNewStrain = true;
			}
			//lets make sure we have the correct prefix for the strain
			//if the strain already exists we should allow updates, 
			//if not then we check for the correct prefix
			if(bNewStrain)
			{
				String prefix = this.getStrainPrefix(db).toLowerCase().trim();
				if(!strain.toLowerCase().startsWith(prefix))
					throw new LinxUserException("The Strain ID must start with " + prefix);
			}
			
			if(bNewStrain)
			{
				validateProject();
				validateNotebook();
				validateLibrary();
				validateStrainName();
				validatePlasmid();
			}
			
			
		}

	/**
	 * Updates custom table STRAIN with new strain properties, creating a new strain
	 * if user's strain does not exist yet. 
	 * Separately handles screen-based definition as well as
	 * bulk file imported list of strains.
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	  @Override
	  public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	  {
		  updateAppFilesWithAppliesTo(request, response, user, db);
		  //if(strains == null || strains.rows.size() == 0)
		 // {
			 // single strain
		     updateCustomTables(request, db);
		  //}
		 // else
		 // {
			 // list of strains from bulk import file
		//	 updateCustomTablesWithList(db);
		  //}
	  }
	  
	  /**
	   * Inserts new or updates screen-based STRAIN into custom table.
	   * @param db
	   */
	  protected void updateCustomTables(HttpServletRequest request, Db db)
	  {
		  String strain = getServerItemValue(ItemType.STRAIN);
		  if(bNewStrain)
		  {
				Code.info("Updating custom table record for strain " + strain);
				setMessage("Successfully created strain " + strain);
				

				ArrayList<String> params = new ArrayList<String>();
			    params.add(strain);
			    params.add(getServerItemValue("StrainName"));
			    params.add(getStrainType());
			    params.add(getServerItemValue(ItemType.PROJECT));
			    params.add(getServerItemValue(DataType.NOTEBOOK_REF));
			    params.add(getServerItemValue(ItemType.PLASMID));
			    params.add(getServerItemValue(ItemType.LIBRARY));
			    params.add(getServerItemValue(DataType.COMMENT));
			    params.add(getTranId() + "");
			    
			    String sql = "spEMRE_insertDHCStrain";
			    
			    db.getHelper().callStoredProc(db, sql, params, false, true);
			    

			   	  // set multiple location values from rowset	
			     // warning: relies on specific 'location index' (position in LinxML)
			    
			      //lets get the straintype
			      String strainType = getStrainType();
			      //long strainTypeId = dbHelper.getAppValueIdAsLong("StrainType", strainType, null, false, true, this, db);
			      ArrayList<String> locations = new ArrayList<String>();
			      if(bHaveFile)
			      {
			    	  //locations should be set in the dom
			    	   locations = (ArrayList<String>)getServerItemValues("Location");
			      }
			      else
			      {
			    	  TableDataMap rowMap = new TableDataMap(request, ROWSET);
				      int numRows = rowMap.getRowcount();
				      //do we have the correct number of locations
				      if(numRows != this.getLocationRowCount())
				    	  throw new LinxUserException("Invalid number of freezer locations.  This task expects " 
				    			  + getLocationRowCount() + " rows.");
					  for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
					  {
						  String location = (String)rowMap.getValue(rowIdx, COLUMN_LOCATION);
						  if(location.indexOf(":") < 1)
						  {
							  throw new LinxUserException("Please provide a location in the format FRZ:BOX:POS,"
									  + " then try again.");
						  }
						  locations.add(location);
					  }
			      }
				  //if we're here we have all of the locations
			      if(locations.size() != getLocationRowCount())
			    	  throw new LinxUserException("Invalid number of freezer locations.  This task expects " 
			    			  + getLocationRowCount() + " rows.");
			      int rowIdx = 1;
			      for(String location : locations)
			      {
			    	  String[] alLocs = location.split(":");
					  String freezer = alLocs[0];
					  String box = alLocs[1];
					  String coord = alLocs[2];
					 
					  try
					  {
						  int idxLastColon = location.lastIndexOf(':');
						  String pos = location.substring(idxLastColon + 1);
						  //now lets zero pad the position
						  if(pos.length() < 2)
						  {
							  pos = zeroPadPosition(pos);
							  coord = pos;
							  location = location.substring(0,idxLastColon) + ":" + pos;
						  }
					  }
					  catch(Exception ex)
					  {
						  throw new LinxUserException("Unable to parse location [" + location + "]: " + ex.getMessage());
					  }
					  params.clear();
					  params.add(strain);
					  params.add(freezer); 
					  params.add(box);
					  params.add(coord);
					  params.add(rowIdx+""); //location index
					  params.add(strainType);
					  params.add(getTranId()+"");
					
			      	  sql = "spEMRE_insertDHCStrainLocation";
					  dbHelper.callStoredProc(db, sql, params, false, true);
					  rowIdx++;
				  }// next loc index 
				  // at exit, have updated strain locations
		  }
		  else
		  {
		    	//sql = "spMet_UpdateStrain";
				//setMessage("Successfully updated strain " + strain);
		    	//as of June 2010 v1.11.0 do not allow updating of existing strain information
		    	//only allow updating of comments.
			  
			  String comment = getServerItemValue(DataType.COMMENT);
			  if(WtUtils.isNullOrBlank(comment))
			  {
				  throw new LinxUserException("Only comments are allowed to be updated if the strain already exists.  Please enter comments and try again.");
			  }
			  else
			  {
				  String strainId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
				  String sql = "spEMRE_updateStrainComment " + strainId + ",'" + comment + "'";
				  dbHelper.executeSQL(sql, db);
				  setMessage("Successfully updated strain '" + strain + "' comments.");
			  }
		  }
	    
		  	    
	  }
	  
	  	/**
		 * Overridden to generically update custom tables with 
		 * properties of new samples, without
		 * knowledge of which strains were collected from manifest.
		 * To adapt LIMS to use a new manifest format, just revise 
		 * the stored procedure spMet_InsertStrain and custom table. 
		 * No code changes should be necessary, as manifest parser is
		 * also blind to field positions and column headers.
		 * @param db
		 */
//		protected void updateCustomTablesWithList(Db db)
//		{
//			ArrayList<String> params = new ArrayList<String>();
//			try
//			{
//				// call sp once for each Strain obj containing Strain data
//				//ListIterator strainItor = strains.listIterator();
//				if(strains.gotoFirst())
//				{
//					do
//					//while(strainItor.hasNext())
//					{
//						// Strain obj is used for convenience only
//						//Sample strain = (Sample)strainItor.next();
//						String sStrain = strains.getRequiredProperty("New Strain ID");//strain.getSGIBarcode();
//						//lets make sure the strain is 4 digits
//						String prefix = getStrainPrefix(db);
//						String strainNum = sStrain.substring(prefix.length());
//						if(strainNum.length() != 6)
//						{
//							throw new LinxUserException("The numeric portion of the Strain ID must be four digits."
//									+ " Please check Strain ID [" + sStrain + "], then try again.");
//						}
//						params.clear();		
//						ListIterator colItor = strains.getColumnNames().listIterator();
//						while(colItor.hasNext())
//						{
//							String property = (String)colItor.next();
//							String value    = strains.getProperty(property);
//							if(property.startsWith("Location"))
//							{
//							  try
//							  {
//								  int idxLastColon = value.lastIndexOf(':');
//								  String pos = value.substring(idxLastColon + 1);
//								  //now lets zero pad the position
//								  if(pos.length() < 2)
//								  {
//									  pos = zeroPadPosition(pos);
//									  value = value.substring(0,idxLastColon) + ":" + pos;
//								  }
//							  }
//							  catch(Exception ex)
//							  {
//									  throw new LinxUserException("Unable to parse location [" + value + "]: " + ex.getMessage());
//							  }					
//						    }
//							params.add(value); // may be null
//								
//						}// next column
//						// at exit, have added each column value to params
//						params.add(getStrainType());
//						params.add(getTranId() + "");
//							
//						// insert new strain described in strain bulk import file
//						try
//						{
//							String sql = getFileInsertSQL();
//							db.getHelper().callStoredProc(db, sql, params, false, true);
//						}
//						catch (Exception e)
//						{
//							throw new LinxUserException("While inserting new Strain record: " + e.getMessage());
//						}
//							
//						else
//						{
//							//we have a strain that already exists - just update the comments if they are present
//							String comment = getServerItemValue(DataType.COMMENT);
//							  if(!WtUtils.isNullOrBlank(comment))
//							  {
//								  String strainId = dbHelper.getItemId(sStrain, ItemType.STRAIN, db);
//								  String sql = "spEMRE_updateStrainComment " + strainId + ",'" + comment + "'";
//								  dbHelper.executeSQL(sql, db);
//							  }
//						}
//					}
//					while(strains.gotoNext());// next strain
//					// at exit, each strain in list has been saved to database
//				}
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException(ex.getMessage());
//			}
//			
//			
//		}
		
	  
		/**
		 * Allows subclasses to return the name of the 
		 * stored proc they need to use to insert a new
		 * strain. Photo Host Strain requires nine locations
		 * vs three, for example.
		 * @return name of sp to use for file-based inserts
		 */
//	  protected String getFileInsertSQL()
//		{
//			return "spEMRE_insertDHCStrainFromFile";
//		}

	/**
	   * Prints small (1" X 0.5 ") barcodes on the S4M printer.
	   *
	   *@param request
	   *@param db
	   */
	  public void printLabels(String strain, HttpServletRequest request, Db db)
	  {
		  // construct the label from user's fields

		  //lets make sure they did a save first.
		  List lsStrains = db.getHelper().getListEntries("exec spMet_GetStrainLocations '" 
				  + strain + "'", db);
		  if(lsStrains.isEmpty())
		  {
			  throw new LinxUserException("Please save the strain locations before printing labels (click [Save]).");
		  }
		  String noteBook = getServerItemValue(DataType.NOTEBOOK_REF);
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
				  String label = print.getZPLforLabel(strain, alrows);
				  S4MSmallBarcode.print(printService, strain, label);
				  alrows.clear();
				  alrows = null;
			  }
		  }// next location
		  setMessage("Successfully printed strain barcodes.");
	  }

	 	/**
		 * Returns name of imported file after creating a list 
		 * of Strain objects for each strain in bulk import file
		 * and setting the server item values with this list.
		 * @param fileId
		 * @param db
		 * @return
		 */
//		public String importStrainsFromBulkImportFile(String fileId, Db db)
//		{
//			
//			// core has already validated file per task def
//			File inFile = this.getFile(fileId, db);
//			
//			char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
//			String columnKey = "New Strain ID"; //the unique identifier that lets me know i've reach the column data in the file
//			try
//			{
//				strains = new XLSParser(inFile, "DHC Strain Collection",
//					delim, columnKey, EMREStrings.Enrichments.requiredIsolateColumnHeaders, true);
//			}
//			catch (RuntimeException e)
//			{
//				throw new LinxUserException("Problem encountered while reading bulk import file: " + e.getMessage());
//			}
//			
////			 add strain IDs to server-side items for std processing
//			ArrayList<String> barcodes = new ArrayList<String>();
//			//ListIterator itor = strains.listIterator();
//			//lets get the locations and check for dups
//			ArrayList<String> alLocs = new ArrayList<String>();
//			
//			try
//			{
//				if(strains.gotoFirst())
//				{
//					do
//					{
//						// Sample obj is used for convenience only
//						String strain = strains.getRequiredProperty("New Strain ID");
//						//lets make sure that the strain starts with the correct prefix.
//						String strainPrefix = getStrainPrefix(db).toUpperCase();
//						if(!strain.startsWith(strainPrefix))
//						{
//							throw new LinxUserException("Each Strain ID must start with the prefix " + strainPrefix
//									+ ". Please check Strain ID [" + strain + "], then try again.");
//						}
//						barcodes.add(strain);
//						
//						//validate some inputs
//						String project = strains.getRequiredProperty("Project");
//						String noteBook = strains.getRequiredProperty("Page Ref [notebook-pg]");
//						String library = strains.getRequiredProperty("Library");
//						String strainName = strains.getRequiredProperty("Strain Name");
//						String plasmid = strains.getRequiredProperty("Plasmid");
//						
//						
//						//loop through the locations and add them to an arraylist
//						ListIterator colItor = strains.getColumnNames().listIterator();
//						while(colItor.hasNext())
//						{
//							String property = (String)colItor.next();
//							String value    = strains.getProperty(property);
//							if(property.startsWith("Location"))
//							{
//								alLocs.add(value);
//							}
//						}
//					}
//					while(strains.gotoNext());
//				}
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException(ex.getMessage());
//			}
//			//lets check the list for duplicate strains before adding to the server dom
//			try
//			{
//				checkForDuplicates(barcodes);
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException("Duplicate Strains in file.  " + ex.getMessage());
//			}
//			getServerItem(ItemType.STRAIN).setValues(barcodes);
//			
//			//lets check for duplicate locations
//			try
//			{
//				checkForDuplicates(alLocs);
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException("Duplicate Locations in file.  " + ex.getMessage());
//			}
//			return inFile.getName();
//		}

		/**
		 * Overridden to manually add import file as data to strains,
		 * and perform no other data additions if there is a file import.
		 * @param request
		 * @param response
		 * @param user
		 * @param db
		 */
		public void createAnyNewData(HttpServletRequest request,
				HttpServletResponse response, User user, Db db)
		{
			if(WtUtils.isNullOrBlank(ItemType.STRAIN))
			{
				// importing from file
				String filename = getServerItemValue(FileType.STRAIN_IMPORT_FILE);
				List strains  = getServerItemValues(ItemType.STRAIN);
				ListIterator itor = strains.listIterator();
				while(itor.hasNext())
				{
					String strain = (String)itor.next();
					db.getHelper().addData(strain, ItemType.STRAIN, "0", filename, FileType.STRAIN_IMPORT_FILE, getTranId(), db);
				}// next strain
				// at exit, have added import file to each strain as data
			}
			// let custom sp's work if creating single strain using screen
			else
			{
				super.createAnyNewData(request, response, user, db);
			}
		}

		/**
		 * Returns the next strain ID to use based
		 * on the most recently created Strain item (by tranId)
		 * + 1. If last strain ID was not a number, throws an error.
		 * As of release 1.1.0 we are naming strains PH-NNNN for PhotoHost strains
		 * PE- for PhotoE and SB- for Brown Lab strains.
		 * @param db
		 */
		  public String getNextStrainID(Db db)
			{
			    String prefix = getStrainPrefix(db);
			    int prefixLength = prefix.length();
			    int idLength = prefix.length() + "NNNN".length();
		
			  	String sql = "exec spMet_GetLastStrainID '"  + this.getTaskName() + "'";
				String lastId = db.getHelper().getDbValue(sql, db);
				// expecting 'AA-NNNN'; all errors are handled by catch below
				try
				{
					// extract 'NNNN' from 'AA-NNNN'
					// to get something like '0014'
					// convert to 14 and increment to 15 
					int ilastId = Integer.parseInt(lastId.substring(lastId.lastIndexOf('-') + 1));
					int nextId = ilastId + 1;
					
					// pad to create '0015' 
					String paddedId = String.valueOf(nextId);
					while(paddedId.length() < (idLength - prefixLength))
					{
						paddedId = "0" + paddedId;
					}		
					// add prefix back to '0015' to make 'AA-0015'
					String newId = prefix + paddedId;
					return newId;
				}
				catch(Exception ex)
				{
					throw new LinxUserException("Last Strain ID was not numeric and cannot be incremented."
							+ " Please download the list of strains to determine next Strain ID.");
				}
			}

		/**
		 * 
		 * @param request
		 * @param response
		 * @param task
		 * @param db
		 * @throws Exception
		 */
		public ArrayList<String> reserveLocations(String strainID, Db db)
		{
			//we're going to call a save here which will autogenerate freezer 
			//locations for the number of strains entered on the form
			String strainType = this.getStrainType();
			//first lets get the strain type
			
			//ok here we have some valid info, lets generate the freezer positions
			return reserveFreezerLocations(strainType, db);
		}
		
		/**
		 * Generates the next freezer locations for the given strain type
		 * by looking up current positions and incrementing. Does not actually
		 * reserve any locations in the db per Gena Lee 10/17/2008. Handles
		 * no boxes yet and crossing boxes (boundary conditions). Handles variable
		 * number of each type of vial to make (standard & backup). Calls 
		 * stored procedure spMet_getCurrentBoxAndPosition.
		 * 
		 * @param strainType
		 * @param numStrains
		 * @param task
		 * @param db
		 */
		protected ArrayList<String> reserveFreezerLocations(String strainType, Db db)
		{
			ArrayList<String> locs = new ArrayList<String>();
			String backupBoxPrefix = getStrainBackupBoxPrefix(db);
			String boxPrefix = getStrainBoxPrefix(db);
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
					numVials		= rs.getInt(4); // number of slots to reserve
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
			for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
			{
				if(pos < 36) // todo: eliminate magic number
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
					numVials	= rs.getInt(4); // number of archive boxes to make
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
			for(int vialIdx = 0; vialIdx < numVials; vialIdx++)
			{
				if(pos < 81) // todo: eliminate magic number
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
		
		
		private String getStrainType()
		{
			return "DHC";
		}
		/**
		 * return the correct prefix for the strain based upon task.
		 * @param taskName
		 * @return
		 */
		public String getStrainPrefix(Db db)
		{
			String prefix = dbHelper.getDbValue("exec spMet_getStrainPrefix '" 
					+ getStrainType() + "'", db);
			String suffix = dbHelper.getDbValue("exec spMet_getIDSuffix", db);
			if(WtUtils.isNullOrBlank(suffix))
				suffix = "";
			else
				suffix = suffix + "-";
			return prefix + "-" + suffix;
		}
		
			/**
		 * Returns the correct prefix for the standard archive box.
		 * See overriddes in Photo strain tasks.
		 * @param taskName
		 * @return
		 */
		protected String getStrainBoxPrefix(Db db)
		{
			String sql = "exec spMet_getStrainCollectionBoxPrefix '" + getStrainType() + " Strain'";
			String prefix = dbHelper.getDbValue(sql, db);
			return prefix;
		}
		
		/**
		 * Returns correct prefix for boxes storing backup archives,
		 * currently 'BU' for all strains including Photo strains. 
		 * @return
		 */
		protected String getStrainBackupBoxPrefix(Db db)
		{
			String sql = "exec spMet_getStrainCollectionBackupBoxPrefix '" + getStrainType() + " Strain'";
			String prefix = db.getHelper().getDbValue(sql, db);
			return prefix;
		}
		
		protected void checkForDuplicates(ArrayList<String> items)
			throws Exception
		{
			ArrayList<String> alSingletons = new ArrayList<String>();
			String duplicates = "";
			for(String s : items)
			{
				if(!alSingletons.contains(s))
					alSingletons.add(s);
				else
					duplicates += s + Strings.CHAR.COMMA;
			}
			if(!duplicates.equals(""))
			{
				duplicates = duplicates.substring(0, duplicates.lastIndexOf(','));
				throw new Exception("The following items are duplicated: " + duplicates);
			}
		}
		
		  /**
		 * Number of rows to show in the Location UI table
		 * on initial display, for defining new strain.
		 * @return 3 for Brown Lab
		 */
	  protected int getLocationRowCount()
	  {
		  return 2;
	  }
	  
	  private void validateProject()
	  {
		  String project = getServerItemValue(ItemType.PROJECT);
		  if (WtUtils.isNullOrBlankOrPlaceholder(project))
		  {
			  throw new LinxUserException("Please select a Project, then try again.");
		  }
	  }
	  private void validateNotebook()
	  {
		String notebookPg = getServerItemValue(DataType.NOTEBOOK_REF);
		if(WtUtils.isNullOrBlank(notebookPg))
		{
			throw new LinxUserException(
			"Please enter a notebook page, then try again.");
		}
	  }
	  private void validateLibrary()
	  {
		String library = getServerItemValue(ItemType.LIBRARY);
		if(WtUtils.isNullOrBlankOrPlaceholder(library))
		{
			throw new LinxUserException("Please enter a Library, then try again.");
		}
	  }
	  
	  private void validateStrainName()
	  {
		String strainName = getServerItemValue("StrainName");
		if(WtUtils.isNullOrBlankOrPlaceholder(strainName))
		{
			throw new LinxUserException("Please enter a Strain Name, then try again.");
		}
	  }
	  private void validatePlasmid()
	  {
		  String plasmid = getServerItemValue(ItemType.PLASMID);
			if(WtUtils.isNullOrBlankOrPlaceholder(plasmid))
			{
				throw new LinxUserException("Please enter a Plasmid, then try again.");
			} 
	  }
	  
		
}
