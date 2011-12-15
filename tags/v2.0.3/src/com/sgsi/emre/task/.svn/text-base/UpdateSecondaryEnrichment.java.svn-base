package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateSecondaryEnrichment extends EMRETask 
{
	
	/**
	* Updates custom table enrichment with new strain enrichment, 
	* creating a new enrichment
	* if user's enrichment does not exist yet. 
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
	 * Inserts new or updates screen-based enrichment into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		String enrichment = getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
		String itemId = dbHelper.getItemId(enrichment, ItemType.SECONDARY_ENRICHMENT, db);
		setMessage("Successfully updated secondary enrichment." );
		String sql =  "spEMRE_updateSecondaryEnrichment ";
		ArrayList<String> params = new ArrayList<String>();
		params.add(itemId);
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("OxygenConcentration")))
			params.add(null);
		else
			params.add(getServerItemValue("OxygenConcentration"));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("CO2Concentration")))
			params.add(null);
		else
			params.add(getServerItemValue("CO2Concentration"));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("pH")))
			params.add(null);
		else
			params.add(getServerItemValue("pH"));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("EnrichmentResult")))
			params.add(null);
		else
			params.add(getServerItemValue("EnrichmentResult"));	
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Flocculence")))
			params.add(null);
		else
			params.add(getServerItemValue("Flocculence"));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
			params.add(null);
		else
			params.add(getServerItemValue("Comment"));
		
		dbHelper.callStoredProc(db, sql, params, false, false);
	}
	
	/**overriden to validate file inputs
	 * 
	 */
	@Override
	public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		super.verifyItems(request, response, user, db);
		
		//validate pH
		String pH = getServerItemValue("pH");
		this.validatePH(pH);
		
		//validate CO2
		String co2 = getServerItemValue("CO2Concentration");
		this.validateCO2(co2);
		
		//validate O2
		String o2 = getServerItemValue("OxygenConcentration");
		this.validateO2(o2);
		
		//validate flocculence
		String floc = getServerItemValue("Flocculence");
		validateFlocculence(floc,true, db);
		
		String er = getServerItemValue("EnrichmentResult");
		validateEnrichmentResult(er,true, db);
		
	}
	
	public String validatePH(String PH)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(PH))
				return null;
			float ph = Float.parseFloat(PH);
			if(ph < 0 || ph > 14)
				throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a pH value between 0 and 14 and then try again.");
		}
		return PH;
	}
	 
	public String validateCO2(String co2)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(co2))
				return null;
			float fCO2 = Float.parseFloat(co2);
			//if(af < 0 || af > 50)
			//	throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric CO2 value and then try again.");
		}
		return co2;
	}
	
	public String validateO2(String o2)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(o2))
				return null;
			float fO2 = Float.parseFloat(o2);
			//if(af < 0 || af > 50)
			//	throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric O2 value and then try again.");
		}
		return o2;
	}

}
