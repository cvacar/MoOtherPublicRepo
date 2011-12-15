package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtUtils;

public class UpdatePrimaryEnrichment extends EMRETask 
{

	
	/**
	* Updates custom table enrichment with new properties
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
	 * Inserts new screen-based enrichment into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		try
		{
			String enrichment = getServerItemValue(ItemType.PRIMARY_ENRICHMENT);
			String itemId = dbHelper.getItemId(enrichment, ItemType.PRIMARY_ENRICHMENT, db);
			setMessage("Successfully updated primary enrichment.");
			String sql =  "spEMRE_updatePrimaryEnrichment ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(itemId);
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("InternalID")))
				params.add(null);
			else
				params.add(getServerItemValue("InternalID"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("EnrichmentResult")))
				params.add(null);
			else
				params.add(getServerItemValue("EnrichmentResult"));	
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
				params.add(null);
			else
				params.add(getServerItemValue("Comment"));
				
			dbHelper.callStoredProc(db, sql, params, false, false);
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		
	}
	
	/**overriden to validate file inputs
	 * 
	 */
	@Override
	public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		super.verifyItems(request, response, user, db);
		
		String er = getServerItemValue("EnrichmentResult");
		validateEnrichmentResult(er,true, db);
	}
	
}
