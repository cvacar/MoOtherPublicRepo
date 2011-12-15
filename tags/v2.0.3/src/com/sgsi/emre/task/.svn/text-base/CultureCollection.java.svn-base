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

public class CultureCollection extends EMRETask 
{
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		//make sure strain is active
		String strain = getServerItemValue(ItemType.STRAIN);
		boolean bQ = dbHelper.isItemOnQueue(strain, ItemType.STRAIN, "Available Strains", db);
		if(!bQ)
			throw new LinxUserException("Strain '" + strain + "' is not listed in the database as an available strain." +
					"  Perhaps it has been discarded or retired.  Please use a different strain and try again.");
	
		//make sure the strain and start date are contained within the new culture
		String startDate = getServerItemValue("DateStarted");
		String culture = null;
		try
		{
			culture = getServerItemValue("StrainCulture");
		}
		catch(Exception ex)
		{
			//it's possible we have an experimental culture here
			try
			{
				culture = getServerItemValue("ExperimentalCulture");
			}
			catch(Exception e)
			{
				throw new LinxUserException("Unable to determine culture type:" + ex.getMessage());
			}
		}
		if(!(culture.indexOf(startDate) >= 0) )
			throw new LinxUserException("The start date needs to be part of the culture ID.");
		if(!(culture.indexOf(strain) >= 0))
			throw new LinxUserException("The strain needs to be part of the culture ID.");
	}

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
		updateCustomTables(request, db);
	}
	
	/**
	 * Inserts new screen-based culture into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		try
		{
			String strain = getServerItemValue(ItemType.STRAIN);
			String strainItemId = dbHelper.getItemId(strain, ItemType.STRAIN, db);
			String culture = getServerItemValue(ItemType.STRAINCULTURE);
			String cultureItemId = dbHelper.getItemId(culture, ItemType.STRAINCULTURE, db);
			String notebookpg = getServerItemValue(DataType.NOTEBOOK_REF);
			String date = convertDate(getServerItemValue("DateStarted"));
			
			String sql = "spEMRE_insertCultureCollection ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(cultureItemId);
			params.add(ItemType.STRAINCULTURE);
			params.add(strainItemId);
			params.add(notebookpg);
			params.add(date);
			
			
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
			
			setMessage("Successfully created new culture.");
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}
	
	public String getCultureCollectionSQL()
	{
		return "spEMRE_reportCultureCollection";
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
				nextId = dbHelper.getDbValue("exec spEMRE_getNextCulture '" + strain + "','" 
						+ dateStarted + "'", db);
			}
		}
		catch(Exception ex)
		{
			throw new LinxDbException("Error occured when trying to get the next culture id: " + ex.getMessage());
		}
		return nextId;
	}
}
