package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * CreateStrain
 *
 * Overrides getStrainPrefix() and getNextStrainID() to query db for
 * which of the nine prefixes to use depending
 * on organism and genetic background.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2011
 */
public class CreateStrain extends StrainCollection
{
	/**
	 * Returns the next strain ID to use based
	 * on the NEXTID table value (by appvalueId for ItemType/Strain).
	 * Pads ID to fill five integer places and adds prefix
	 * for the 
	 * @param db
	 */
	  public String getNextStrainID(String organism, String background, Db db)
		{
		    String prefix = getStrainPrefix(organism, background, db);
		    String nextId = 
		    	dbHelper.getDbValueFromStoredProc(db, "spEMRE_getNextStrainID",  "placeholder");
		    db.commit(); // burn the ID
		 
				// pad to create '00015' 
				String paddedId = nextId;
				while(paddedId.length() < "00000".length())
				{
					paddedId = "0" + paddedId;
				}		
				// add prefix to '00015' to make 'AA-SGI-E-00015'
				String newId = prefix + "-SGI-E-" + paddedId;
				return newId;
		}
  
		/**
		 * return the correct prefix for the strain based upon task.
		 * @param taskName
		 * @return
		 */
		public String getStrainPrefix(String organism, String background, Db db)
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(organism);
			params.add(background);
			
			String prefix = dbHelper.getDbValueFromStoredProc(db, "spEMRE_getStrainPrefix", params);
			return prefix;
		}
		
/*		 *//**
	   * Inserts new screen-based STRAIN into custom table.
	   * @param db
	   *//*
	  protected void updateCustomTables(HttpServletRequest request, Db db)
	  {
		  String strain = getServerItemValue(ItemType.STRAIN);
		  if(this.getTaskName().indexOf("Create") > -1) // new strain
		  {
				Code.info("Updating custom table record for strain " + strain);
				setMessage("Successfully imported new strain(s)");
				
			  String organism = getDisplayItemValue("Organism");

				ArrayList<String> params = new ArrayList<String>();
			    params.add(strain);
			    params.add(getServerItemValue("StrainName"));
			    params.add(getStrainPrefix(organism, db));
			    params.add(getServerItemValue(ItemType.PROJECT));
			    params.add(getServerItemValue(DataType.NOTEBOOK_REF));
			    params.add(getServerItemValue(DataType.COMMENT));
			    params.add(getTranId() + "");
			    
			    String sql = "spEMRE_InsertStrain";
			    
			    db.getHelper().callStoredProc(db, sql, params, false, true);
			    

			   	  // set multiple location values from rowset	
			     // warning: relies on specific 'location index' (position in LinxML)
			    
			      //lets get the straintype
			      String strainType = getStrainType(strain);
			      //long strainTypeId = dbHelper.getAppValueIdAsLong("StrainType", strainType, null, false, true, this, db);
			      List<String> locations = new ArrayList<String>();
			      if(bHaveFile)
			      {
			    	  //locations should be set in the dom
			    	   locations = getServerItemValues("Location");
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
					  String boxType  = box.substring(0,box.length() - 2); // assumes < 100 boxes per freezer
					  String transferPlan = strainType + " Freezer " + freezer + " Box " + boxType;
					  String transferPlanId = db.getHelper().getDbValue("exec spMet_getTransferPlanId '" 
						  + transferPlan + "','" + coord + "'", db);

					  
					  params.clear();
					  params.add(strain);
					  params.add(freezer); 
					  params.add(box);
					  params.add(coord);
					  params.add(rowIdx+""); //location index
					  params.add(strainType);
					  params.add(transferPlanId);
					  params.add(getTranId()+"");
					
			      	  sql = "spEMRE_insertStrainLocation";
					  dbHelper.callStoredProc(db, sql, params, false, true);
					  rowIdx++;
				  }// next loc index 
				  // at exit, have updated strain locations
		  }
		  else // update strain
		  {
		    	//sql = "spMet_UpdateStrain";
				//setMessage("Successfully updated strain " + strain);
		    	//as of June 2010 v1.11.0 do not allow updating of existing strain information
		    	//only allow updating of comments.
			  
			  String comment = getServerItemValue(DataType.COMMENT);
			  if(WtUtils.isNullOrBlank(comment))
			  {
				  throw new LinxUserException(
				  		"Only comments are allowed to be updated if the strain already exists."
				  		+ " Please enter comments and try again.");
			  }
			  else
			  {
				  String strainId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
				  String sql = "spEMRE_updateStrainComment " + strainId + ",'" + comment + "'";
				  dbHelper.executeSQL(sql, db);
				  setMessage("Successfully updated strain '" + strain + "' comments.");
			  }
		  }
	    
		  	    
	  }*/
	  
		/**
		 * Returns T if a history record should be saved
		 * for pre-update records.
		 * @return T
		 */
		public String createHistoryRecord()
		{
			return "T";
		}

		public String getColumnKey()
		{
			
			return "Strain ID";
		}

		public String getSheetKey()
		{
			return "Strain Bulk Import";
		}
}
