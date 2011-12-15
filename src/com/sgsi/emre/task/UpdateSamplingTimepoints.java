package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateSamplingTimepoints extends EMRETask
{
	private String	ROWSET						= "Timepoints";
	private int			COLUMN_TIMEPOINT	= 1;
	private String	culture						= null;

	/**
	 * If user has entered values in UI screen vs a file, do some manipulations.
	 */
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		// what type of culture do we have?
		if (getServerItemValue(FileType.SAMPLING_TIMEPOINT_IMPORT_FILE) != null)
		{
			return; // file-based save skips this method
		}
		// file item is empty, so we have a
		/******* SCREEN-BASED SAVE *****/
		// put culture under correct itemtype
		// servlet already set timepoint strings on server-side item for us
		culture = getDisplayItemValue(ItemType.CULTURE);
		String cultureType = null;
		if (WtUtils.isNullOrBlank(culture))
		{
			throw new LinxUserException("Please enter a value for required item "
					+ getDisplayItem(ItemType.CULTURE).getLabel());
		}
		// user may enter either type of culture in the same field
		if (culture.startsWith("EX"))
		{
			// we have an experimental culture
			getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(culture);
			getServerItem(ItemType.STRAINCULTURE).clearValues();
			cultureType = ItemType.EXPERIMENTALCULTURE;
		}
		else
		{
			getServerItem(ItemType.STRAINCULTURE).setValue(culture);
			getServerItem(ItemType.EXPERIMENTALCULTURE).clearValues();
			cultureType = ItemType.STRAINCULTURE;
		}
		
		List<String> timepts = getServerItemValues("Timepoint");
		int numSTPs = timepts.size();
		int sn = getNextSerialNumberByCulture(culture, cultureType, db);
		for(int idx = 0; idx < numSTPs; idx++)
		{
			// get last serial number used for STP of this culture
			getServerItem(ItemType.SAMPLING_TIMEPT).appendValue(culture + "_" + sn);
			sn++;
		}// next timepoint
		// at exit, server-side items have matching number of entries
	}



	/**
	 * Overridden to add SamplingTimepoint records to the given cultures.
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		// time to update sampling timepoints
		String cultureType = ItemType.STRAINCULTURE;
		culture = getServerItemValue(ItemType.STRAINCULTURE);
		if(WtUtils.isNullOrBlank(culture))
		{
			culture = getServerItemValue(ItemType.EXPERIMENTALCULTURE);
			cultureType = ItemType.EXPERIMENTALCULTURE;
		}
		
		// record the timepoints, either from UI table or row in file
		ArrayList<String> params = new ArrayList<String>();
		List<String> lsTimepoints = getServerItemValues("Timepoint");
		List stplist = getServerItemValues(ItemType.SAMPLING_TIMEPT);
		int idx = 0;
		// -- may be only one timept if row in file
		for (String timept : lsTimepoints)
		{
			if(timept.startsWith("'"))
			{
				// remove Excel format flag
				timept = timept.substring(1);
			}
			// add any new timepoints to the custom db tables
			params.clear();
			params.add((String)stplist.get(idx)); // making assoc w/datetime now, so order is unimportant
			params.add(culture);
			params.add(timept);
			params.add(getTranId()+"");
			try
			{
				dbHelper.callStoredProc(db, "spEMRE_insertSamplingTimepoint", params, false);
			}
			catch (Exception ex)
			{
				if(ex.getMessage().indexOf("already exists") > 0)
				{
					throw new LinxUserException(ex.getMessage());
				}
				else if(ex.getMessage().indexOf("converting data type nvarchar to datetime") > 0)
				{
					throw new LinxUserException(
							"Please prepend a single quote to the timepoint value"
							+ "(for example: '2011-05-18 02:15 PM) to prevent MS Excel from converting it to a number,"
							+ " then try again");
				}
				throw new LinxUserException("Error saving timepoint " + timept
						+ " for culture '" + culture + "':" + ex.getMessage());
			}
			idx++;
		}
		setMessage("Successfully updated sampling timepoints");

	}
}
