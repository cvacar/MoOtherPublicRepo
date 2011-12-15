package com.sgsi.emre.task;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


public class PrimaryEnrichment extends EMRETask 
{

	/**
	* Updates custom table enrichment with new enrichment properties, 
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
	/**overriden to validate file inputs
	 * 
	 */
	@Override
	public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		super.verifyItems(request, response, user, db);
		
		//make sure growth temperature is a number
		String temp = getServerItemValue("GrowthTemperature");
		validateTemperature(temp, false);
		
		//now irradiance
		//as of 06/2010 do not validate irradiance - Vidya Akella/SGI
		//String irradiance = getServerItemValue("GrowthIrradiance");
		//validateIrradiance(irradiance, false);
		
		String ves = getServerItemValue("VesselType");
		validateVesselType(ves,false, db);
		
	}
	

	/**
	 * Inserts new screen-based enrichment into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		try
		{
			String enrichment = getServerItemValue(ItemType.PRIMARY_ENRICHMENT);
			String itemId = dbHelper.getItemId(enrichment, ItemType.PRIMARY_ENRICHMENT, db);
			setMessage("Successfully created new primary enrichment(s).  Click on task name to autogenerate a New LIMS ID.");
			String originItemId = dbHelper.getItemId(getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE), 
					ItemType.ENVIRONMENTAL_SAMPLE, db);
			String sql = "spEMRE_insertPrimaryEnrichment ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(itemId);
			params.add(originItemId);
			params.add(getServerItemValue(DataType.NOTEBOOK_REF));
			params.add(convertDate(getServerItemValue("DateStarted")));
			params.add(getServerItemValue("VesselType"));
			params.add(validateTemperature(getServerItemValue("GrowthTemperature"), true));
			params.add(getServerItemValue("GrowthMedium"));
			params.add(getServerItemValue("GrowthIrradiance"));
			//make sure the optional values are checked before sending to SP
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("InternalID")))
				params.add(null);
			else
				params.add(getServerItemValue("InternalID"));
				
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
				params.add(null);
			else
				params.add(getServerItemValue("Comment"));
				
			params.add(getTranId() + "");
				
			dbHelper.callStoredProc(db, sql, params, false, false);
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}
	
	
}
