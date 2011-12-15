package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateIsolates extends EMRETask 
{

	/**overriden to validate file inputs
	 * 
	 */
	@Override
	public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		super.verifyItems(request, response, user, db);
		
		//make sure growth temperature is a number
		String temp = getServerItemValue("GrowthTemperature");
		validateTemperature(temp, true);
		
		String irr = getServerItemValue("GrowthIrradiance");
		validateIrradiance(irr, true);
	}

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

	updateCustomTables(request, db);
}


/**
* Inserts new isolate into custom table.
* @param db
*/
protected void updateCustomTables(HttpServletRequest request, Db db)
{
  try
	{
		String isolate = getServerItemValue(ItemType.ISOLATE);
		String itemId = dbHelper.getItemId(isolate, ItemType.ISOLATE, db);
		setMessage("Successfully updated isolate(s).");
		
		String sql = "spEMRE_updateIsolate ";
		ArrayList<String> params = new ArrayList<String>();
		params.add(itemId);
		//make sure the optional values are checked before sending to SP
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthTemperature")))
			params.add(null);
		else
			params.add(validateTemperature(getServerItemValue("GrowthTemperature"), true));
		
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthMedium")))
			params.add(null);
		else
			params.add(getServerItemValue("GrowthMedium"));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthIrradiance")))
			params.add(null);
		else
			params.add(validateIrradiance(getServerItemValue("GrowthIrradiance"), true));
		if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
			params.add(null);
		else
			params.add(getServerItemValue("Comment"));
			
		dbHelper.callStoredProc(db, sql, params, false, false);
		
		
	}
	catch(Exception ex)
	{
		throw new LinxUserException(ex.getMessage());
	}    
}
}
