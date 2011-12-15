package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class StrainFeatures extends EMRETask 
{
	private boolean bNewStrain = false;
	private String ROWSET = "GeneAdditionData";
	private int COL_SPECIES = 1;
	private int COL_GENE = 2;
	private int COL_VECTOR = 3;
	private int COL_PROMOTOR = 4;
	private int COL_AFFINITY = 5;
	
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{

		String strain = getServerItemValue(ItemType.STRAIN);
		if (WtUtils.isNullOrBlankOrPlaceholder(strain))
		{
			throw new LinxUserException(
					"Please enter a Strain ID, then try again.");
		}
		if (!dbHelper.isItemExisting(strain, ItemType.STRAIN, db))
		{
			throw new LinxUserException(
			"Strain '" + strain + "' does not exist in LIMS.  Please enter a valid Strain.");
		}
		//do features exist for this strain?
		String geneAdditionId = db.getHelper().getDbValue("exec spMet_getGeneAdditionId '" 
				+ strain + "'", db);
		if(WtUtils.isNullOrBlank(geneAdditionId))
			bNewStrain = true;
		//lets make sure we have the correct prefix for the strain
		String validPrefix = "";
		boolean bValidStrain = false;
		for(StrainPreficies s : StrainPreficies.values())
		{
			if(strain.startsWith(String.valueOf(s)))
				bValidStrain = true;
			validPrefix += s + " ";
		}
		
		if(!bValidStrain)
			throw new LinxUserException("Strain '" + strain + "' is not a valid strain.\r\n" +
					"Strains must start with " + validPrefix);
		//do we have the number of additions?
		String numAdditions = getServerItemValue("Additions");
		if(WtUtils.isNullOrBlankOrPlaceholder(numAdditions))
		{
			throw new LinxUserException("Please select the number of gene additions from the dropdown, glick get additions, fill in the gene addition information, and then click save.");
		}
		//we have the number of additions but did the user actually click the button and fill out the table?
		try
		{
			TableDataMap rowMap = new TableDataMap(request, ROWSET);
			int numRows = rowMap.getRowcount();
			if(numRows < 1)
				throw new Exception("Please enter gene addition data then try again.");
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please click the Get Additions button and fill in the table before clicking save.");
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
	  updateCustomTables(request, db);
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
		setMessage("Successfully created features for strain " + strain);
		//lets get the strain type
		//lets get the item id for the strain for update into custom strain table
		String strainItemId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
		String originId = getServerItemValue("OriginStrain");
		String originItemId = "";
		if(!WtUtils.isNullOrBlank(originId))
		{
			originItemId = dbHelper.getItemId(originId, ItemType.STRAIN, db);
			if(originItemId.equals(""))
				throw new LinxUserException("The Origin LIMS ID doesn't exist in the database.  Please enter a valid ID and try again.");
		}
		
		String vecMap = getServerItemValue("VectorMapFile");
		if(WtUtils.isNullOrBlank(vecMap))
			vecMap = "";
		String vendor = getServerItemValue("Vendor");
		if(WtUtils.isNullOrBlank(vendor))
			vendor = "";
		String numAdditions = getServerItemValue("Additions");
		String host = getServerItemValue("HostSpecies");
		String geneDelete = getServerItemValue("GeneDeletion");
		if(WtUtils.isNullOrBlank(geneDelete))
			geneDelete = "";
		String comment = getServerItemValue("Comment");
		if(WtUtils.isNullOrBlank(comment))
			comment = "";
		if(!WtUtils.isNullOrBlankOrPlaceholder(numAdditions))
		{
			//lets save the gene additions
			 TableDataMap rowMap = new TableDataMap(request, ROWSET);
		      int numRows = rowMap.getRowcount();
		      int numInserts = 0;
			  for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
			  {
					  String species = (String)rowMap.getValue(rowIdx, COL_SPECIES);
					  String gene = (String)rowMap.getValue(rowIdx, COL_GENE);
					  String vector = (String)rowMap.getValue(rowIdx, COL_VECTOR);
					  String promotor = (String)rowMap.getValue(rowIdx, COL_PROMOTOR);
					  String affinity = (String)rowMap.getValue(rowIdx, COL_AFFINITY);
					  if(WtUtils.isNullOrBlank(species) || WtUtils.isNullOrBlank(gene)
							  || WtUtils.isNullOrBlank(host) || WtUtils.isNullOrBlank(vector)
							  || WtUtils.isNullOrBlank(promotor) || WtUtils.isNullOrBlank(affinity))
					  {
						  throw new LinxUserException("Please ensure that all gene addition columns contain data.");
					  }
					  else
					  {
						  ArrayList<String> params = new ArrayList<String>();
						  params.add(strainItemId);
						  params.add(originItemId); 
						  params.add(host);
						  params.add(rowIdx + "");
						  params.add(species); 
						  params.add(gene);
						  params.add(vector);
						  params.add(promotor);
						  params.add(affinity);
						  params.add(geneDelete);
						  params.add(vendor);
						  params.add(vecMap);
						  params.add(comment);
						  params.add(getTranId()+"");
						  numInserts++;
						  
						  String sql = null;
						  //if(bNewStrain)
							  sql = "spEMRE_insertGeneAddition";
						  //else
						//	  sql = "spEMRE_updateGeneAddition";
						  
						  dbHelper.callStoredProc(db, sql, params, false, true);
					  }
			  }// next addition
				 // at exit, have updated gene additions
			  if(numInserts == 0)
					  throw new LinxUserException("At least one gene addition must be filled in.");
		}
		else
			throw new LinxUserException("Please select the number of gene additions for this strain.");
	  
		//now we need to store the plasmid info
		String plasmidType = getServerItemValue("PlasmidType");
		if(!plasmidType.equalsIgnoreCase("none"))
		{
			//we have a plasmid - store it
			String plasmid = getServerItemValue(ItemType.PLASMID);
			String numMarkers = getServerItemValue("PlasmidMarkers");
			if(WtUtils.isNullOrBlankOrPlaceholder(numMarkers))
			{
				throw new LinxUserException("Please select the number of markers for the plasmid from the drop down list.");
			}
			//does the plasmid already exist?
			if(plasmidType.equalsIgnoreCase("exists"))
			{
				String plasmidItemId = dbHelper.getItemId(plasmid, ItemType.PLASMID, db);
				if(WtUtils.isNullOrBlank(plasmidItemId))
				{
					throw new LinxUserException("Plasmid '" + plasmid + "' does not exist in this database.");
				}
				//now insert the markers
				List<String> lsMarkers = getServerItemValues("PlasmidMarkerData");
				int iNumSelectedMarkers = Integer.valueOf(numMarkers);
				int iNumAddedMarkers = lsMarkers.size();
				if(iNumSelectedMarkers != iNumAddedMarkers)
				{
					String err = "The number of selected markers from the dropdown is '" 
						+ numMarkers + "' but the number of markers added is '" + String.valueOf(iNumAddedMarkers) + "'";
					throw new LinxUserException(err);
				}
				//ok, we have the correct number of markers - insert them into the db
				String plasmidId = dbHelper.getDbValue("exec spEMRE_getPlasmidId " + plasmidItemId, db);
				dbHelper.updatePlasmidMarker(plasmidId, lsMarkers, this, db);
				//lets update the strain table with the plasmid info
				dbHelper.updateStrainWithPlasmid(strainItemId, plasmidId, db);
			}
			else
			{
				//we have a new plasmid
				String plasmidItemId = dbHelper.getItemId(plasmid, ItemType.PLASMID, db);
				String plasmidId = dbHelper.insertPlasmid(plasmidItemId, numMarkers, plasmidType, this, db);
				//now insert the markers
				List<String> lsMarkers = getServerItemValues("PlasmidMarkerData");
				int iNumSelectedMarkers = Integer.valueOf(numMarkers);
				int iNumAddedMarkers = lsMarkers.size();
				if(iNumSelectedMarkers != iNumAddedMarkers)
				{
					String err = "The number of selected markers from the dropdown is '" 
						+ numMarkers + "' but the number of markers added is '" + String.valueOf(iNumAddedMarkers) + "'";
					throw new LinxUserException(err);
				}
				//ok, we have the correct number of markers - insert them into the db
				dbHelper.insertPlasmidMarker(plasmidId, lsMarkers, this, db);
				//now we have the plasmids and their associated markers. 
				//lets update the strain table with the plasmid info
				dbHelper.updateStrainWithPlasmid(strainItemId, plasmidId, db);
			}
		}
		else
		{
			//no plasmid type selected - do we have a plasmid or markers
			String plasmid = getServerItemValue(ItemType.PLASMID);
			String numMarkers = getServerItemValue("PlasmidMarkers");
			List<String> lsMarkers = getServerItemValues("PlasmidMarkerData");
			if(!WtUtils.isNullOrBlankOrPlaceholder(plasmid) 
					|| !WtUtils.isNullOrBlankOrPlaceholder(numMarkers)
					|| lsMarkers.size() != 0)
			{
				throw new LinxUserException("In order to add plasmid and plasmid marker data you need to select a plasmid type.");
			}
		}
	}
	else
	{
		setMessage("Successfully updated comments for strain " + strain);
		String comment = getServerItemValue("Comment");
		if(WtUtils.isNullOrBlank(comment))
		  {
			  throw new LinxUserException("Only comments are allowed to be updated if the strain already exists.  Please enter comments and try again.");
		  }
		  else
		  {
			  String strainId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
			  String sql = "spEMRE_updateGeneAdditionComment " + strainId + ",'" + comment + "'";
			  dbHelper.executeSQL(sql, db);
		  }
	}
	
  }
 
  enum StrainPreficies
  {
	  PE,
	  PH,
	  WT
  }

  public String getNextPlasmidId(String type, EMREDbHelper dbHelper, Db db)
  {
	  String id = null;
	  try
	  {
		 id = dbHelper.getDbValue("exec spEMRE_getNextPlasmidId '" + type + "'", db);
	  }
	  catch(Exception ex)
	  {
		  throw new LinxDbException(ex.getMessage());
	  }
	  return id;
  }
 
}
