package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateSamplingTimepoints extends EMRETask 
{
	private String ROWSET = "Timepoints";
	private int COLUMN_TIMEPOINT = 1;
	private String culture = null;
	private String cultureType = null;
	
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		//what type of culture do we have?
		try
		{
			culture = getServerItemValue(ItemType.STRAINCULTURE);
			if(culture.startsWith("EX"))
			{
				//we have an experimental culture
				//remove the culture from the server dom and add it back as an experimental culture
				getServerItem(ItemType.STRAINCULTURE).clearValues();
				getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(culture);
				cultureType = ItemType.EXPERIMENTALCULTURE;
			}
			else
				cultureType = ItemType.STRAINCULTURE;
		}
		catch(Exception ex)
		{
			//it might be an experimental culture
			culture = null;
		}
		if (WtUtils.isNullOrBlank(culture))
		{
			culture = getServerItemValue(ItemType.EXPERIMENTALCULTURE);
			if(culture.startsWith("EX"))
			{
				//we have an experimental culture
				//remove the culture from the server dom and add it back as an experimental culture
				getServerItem(ItemType.STRAINCULTURE).clearValues();
				getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(culture);
				cultureType = ItemType.EXPERIMENTALCULTURE;
			}
			else
				throw new LinxUserException("Unknown culture type.  Please ensure the culture id starts with either 'EX','WT',or 'PH'");
		}
		if (WtUtils.isNullOrBlank(culture))
		{
			throw new LinxUserException("Please enter a value for required item 'Orign LIMS ID'");
		}
		//does the culture exist?
		String cultureItemId = dbHelper.getItemId(culture, cultureType, db);
		if(WtUtils.isNullOrBlank(cultureItemId))
			throw new LinxUserException("Origin LIMS ID '" + culture + "' does not exist in LIMS.");
		//get the timepoints and add them to the dom
		//if they came in from a file we already have them - let's check
		List<String> lsTps = getServerItemValues("SamplingTimepoint");
		if(lsTps == null || lsTps.size() < 1)
		{
			Item tp = getServerItem("SamplingTimepoint");
			ArrayList<String> alTp = new ArrayList<String>();
			TableDataMap rowMap = new TableDataMap(request, ROWSET);
		    int numRows = rowMap.getRowcount();
		    for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
			{
		    	String timepoint = (String)rowMap.getValue(rowIdx, COLUMN_TIMEPOINT);
		    	if(!WtUtils.isNullOrBlank(timepoint))
		    	{
		    		timepoint.trim();
		    		if(!WtUtils.isNullOrBlank(timepoint))
		    			alTp.add(timepoint);
		    		else
		    			throw new LinxUserException("Timepoints cannot be null.");
		    	}	
			}
		    tp.setValues(alTp);
		}
		//check for dups
		List<String> lsDups = new ArrayList<String>();
		for(String s: lsTps)
		{
			if(!lsDups.contains(s))
				lsDups.add(s);
			else
				throw new LinxUserException("Duplicate timepoints exist for culture '" + culture + "',");
		}
	}


	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		//time to update sampling timepoints
		if(WtUtils.isNullOrBlank(culture)) 
		{
			culture = getServerItemValue(ItemType.STRAINCULTURE);
			if(WtUtils.isNullOrBlank(culture)) 
			{
				culture = getServerItemValue(ItemType.EXPERIMENTALCULTURE);
			}
		}
		//we need the itemid for the culture
		String cultureItemId = dbHelper.getItemId(culture, cultureType, db);
		if(WtUtils.isNullOrBlank(cultureItemId))
			throw new LinxUserException("Origin LIMS ID '" + culture + "' does not exist in LIMS.");
		//now get it's cooresponding cultureCollectionId
		long cultureCollectionId = dbHelper.getCultureCollectionId(cultureItemId, db);
		
		//get the timepoints
		List<String> lsTimepoints = getServerItemValues("SamplingTimepoint");
		for(String tp : lsTimepoints)
		{
			//does timepoint already exist?  if not,
			//add them to the db
			String samplingTimepointId = dbHelper.getDbValue("exec spEMRE_getSamplingTimepointId " + 
					cultureCollectionId + ",'" + tp + "'", db);
			if(WtUtils.isNullOrBlank(samplingTimepointId))
			{
				try
				{
					dbHelper.executeSQL("exec spEMRE_insertSamplingTimepoint " + cultureCollectionId 
							+ ",'" + tp + "'," + getTranId(), db);
				}
				catch(Exception ex)
				{
					throw new LinxUserException("Error saving timepoint for culture '" + culture 
							+ "':" + ex.getMessage());
				}
			}
			else
			{
				throw new LinxUserException("The sampling timepoint '" + tp 
						+ "' already exists for culture '" + culture + "'.");
			}
		}
		
	}
}
