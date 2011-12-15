package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class MasterExperimentalCultureCollection extends EMRETask 
{

	/**
	* Updates custom table cultureCollection with new culture properties, 
	* 
	* @param request
	* @param response
	* @param user
	* @param db
	*/
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		//update appfile appliesTo
		
		updateCustomTables(request, db);
	}
	
	/**
	 * NOT IN USE -- UI has been replaced with bulk-import only 
	 * Inserts new screen-based culture into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		try
		{
			String strain = getServerItemValue(ItemType.STRAIN);
			String culture = getServerItemValue(ItemType.EXPERIMENTALCULTURE);
			String notebookpg = getServerItemValue(DataType.NOTEBOOK_REF);
			String startDate = convertDate(getServerItemValue("DateStarted"));
			
			String sql = "spEMRE_insertExperimentalCultureCollection ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(culture);
			params.add(ItemType.EXPERIMENTALCULTURE);
			params.add(strain);
			params.add(notebookpg);
			params.add(startDate);
			
			//make sure the optional values are checked before sending to SP
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Description")))
				params.add(null);
			else
				params.add(getServerItemValue("Description"));
				
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
				params.add(null);
			else
				params.add(getServerItemValue("Comment"));
				
			params.add(getTranId() + "");
				
			dbHelper.callStoredProc(db, sql, params, false, false);
			
			setMessage("Successfully created new culture(s).");
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}
	
	public String getCultureCollectionSQL()
	{
		return "spEMRE_reportExperimentalCultureCollection";
	}
	
	/**
	 * retrieves the next culture id from the database
	 * @param cultureType Currently either 'Culture' or 'ExperimentalCulture'
	 * @param db
	 * @return
	 */
	public String getNextCulture(String strain, String cultureType, String dateStarted, Db db)
	{
		String nextId = null;
		try
		{
			synchronized(this)
			{
				nextId = dbHelper.getDbValue("exec spEMRE_getNextExperimentalCulture '" +
						dateStarted + "'", db);
			}
		}
		catch(Exception ex)
		{
			throw new LinxDbException("Error occured when trying to get the next culture id: " + ex.getMessage());
		}
		return nextId;
	}
}
