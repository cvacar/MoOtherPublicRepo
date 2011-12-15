package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * CreateStrain
 * 
 * Overrides getStrainPrefix() and getNextStrainID() to query db for which of
 * the nine prefixes to use depending on organism and genetic background.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2011
 */
public class CreateStrain extends StrainCollection
{

	/**
	 * Returns the next strain ID to use based on the NEXTID table value (by
	 * appvalueId for ItemType/Strain). Pads ID to fill five integer places and
	 * adds prefix for the
	 * 
	 * @param db
	 */
	public String getNextStrainID(String organism, String strain, Db db)
	{
		String prefix = getStrainPrefix(organism, null, db);
		String nextId = dbHelper.getDbValueFromStoredProc(db,
				"spEMRE_getNextStrainID", "placeholder");
		db.commit(); // burn the ID

		// pad to create '00015'
		String paddedId = nextId;
		while (paddedId.length() < "00000".length())
		{
			paddedId = "0" + paddedId;
		}
		// add prefix to '00015' to make 'AA-SGI-E-00015'
		String newId = prefix + "-SGI-E-" + paddedId;
		return newId;
	}

	/**
	 * Inserts new file import or screen-based STRAIN into custom tables,
	 * one per save().
	 * 
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		String strain = getServerItemValue(ItemType.STRAIN);
		Code.info("Updating custom table record for strain " + strain);
		setMessage("Successfully created new strain(s)");

		String organism = getServerItemValue("StrainType");

		ArrayList<String> params = new ArrayList<String>();
		params.add(strain);
		params.add(getServerItemValue("StrainName"));
		params.add(getStrainPrefix(organism, strain, db));
		params.add(getServerItemValue(ItemType.PROJECT));
		params.add(getServerItemValue(DataType.NOTEBOOK_REF));
		params.add(getServerItemValue(DataType.COMMENT));
		params.add(getTranId() + "");

		db.getHelper().callStoredProc(db, "spEMRE_InsertStrain", params, false,true);

		params.clear();
		params.add(strain);
		params.add(DataType.GENUS);
		params.add(getServerItemValue(DataType.GENUS));
		params.add(getTranId() + "");

		db.getHelper().callStoredProc(db, "spEMRE_updateStrain", params, false,true);

		if (!WtUtils.isNullOrBlank(getServerItemValue(DataType.SPECIES)))
		{
			// optional
			params.clear();
			params.add(strain);
			params.add(DataType.SPECIES);
			params.add(getServerItemValue(DataType.SPECIES));
			params.add(getTranId() + "");

			db.getHelper().callStoredProc(db, "spEMRE_updateStrain", params, false,true);
		}
		if (!WtUtils.isNullOrBlank(getServerItemValue("OriginID")))
		{
			// optional
			params.clear();
			params.add(strain);
			params.add("OriginID");
			params.add(getServerItemValue("OriginID"));
			params.add(getTranId() + "");

			db.getHelper().callStoredProc(db, "spEMRE_updateStrain", params, false,true);
		}
		insertStrainLocationsTable(request, strain, params, db);

	}
	
	/**
	 * Parses locations from file field or from UI table 
	 * and splits into params to stored procedure spEMRE_insertStrainLocations
	 * to save strain locations in freezers.
	 * @param request
	 * @param strain
	 * @param params
	 * @param db
	 */
	protected void insertStrainLocationsTable(HttpServletRequest request,
			String strain, ArrayList<String> params, Db db)
	{
		// set multiple location values from rowset
		// warning: relies on specific 'location index' (position in LinxML)

		// lets get the straintype
		String strainType = getStrainType(strain);
		// long strainTypeId = dbHelper.getAppValueIdAsLong("StrainType",
		// strainType, null, false, true, this, db);
		List<String> locations = new ArrayList<String>();
		if (getServerItem(FileType.STRAIN_IMPORT_FILE).getValue() != null)
		{
			// locations should be set in the dom
			locations = getServerItemValues("Location");
		}
		else
		{
			TableDataMap rowMap = new TableDataMap(request, ROWSET);
			int numRows = rowMap.getRowcount();
			/*
			 * //do we have the correct number of locations if(numRows !=
			 * this.getLocationRowCount()) throw newLinxUserException(
			 * "Invalid number of freezer locations.  This task expects " +
			 * getLocationRowCount() + " rows.");
			 */
			for (int rowIdx = 1; rowIdx <= numRows; rowIdx++)
			{
				String location = (String) rowMap.getValue(rowIdx, COLUMN_LOCATION);
				if (location.indexOf(":") < 1)
				{
					throw new LinxUserException(
							"Please provide a location in the format FRZ:BOX:POS,"
									+ " then try again.");
				}
				locations.add(location);
			}
			getServerItem("Location").setValues(locations);
		}
		// if we're here we have all of the locations
		/*
		 * if(locations.size() != getLocationRowCount()) throw new
		 * LinxUserException(
		 * "Invalid number of freezer locations.  This task expects " +
		 * getLocationRowCount() + " rows.");
		 */
		int rowIdx = 1;
		for (String location : locations)
		{
			String[] alLocs = location.split(":");
			String freezer = alLocs[0];
			String box = alLocs[1];
			String coord = alLocs[2];

			try
			{
				int idxLastColon = location.lastIndexOf(':');
				String pos = location.substring(idxLastColon + 1);
				// now lets zero pad the position
				if (pos.length() < 2)
				{
					pos = zeroPadPosition(pos);
					coord = pos;
					location = location.substring(0, idxLastColon) + ":" + pos;
				}
			}
			catch (Exception ex)
			{
				throw new LinxUserException("Unable to parse location [" + location
						+ "]: " + ex.getMessage());
			}
			String boxType = box.substring(0, box.length() - 2); // assumes < 100
																														// boxes per freezer
			String transferPlan = 
					strainType + " Freezer " + freezer + " Box "	+ boxType;
			
			params.clear();
			params.add(transferPlan);
			params.add(coord);
			String transferPlanId = 
				db.getHelper().getDbValueFromStoredProc(db, "spEMRE_getTransferPlanId", params);

			// todo: refactor sp to take name of trf plan 
			// -- (to cut down on number of db calls per row)
			params.clear();
			params.add(strain);
			params.add(freezer);
			params.add(box);
			params.add(coord);
			params.add(rowIdx + ""); // location index
			params.add(strainType);
			params.add(transferPlanId);
			params.add(getTranId() + "");

			dbHelper.callStoredProc(db, "spEMRE_insertStrainLocation", params, false,
					true);
			rowIdx++;
		}// next loc index
		// at exit, have saved strain locations for this strain
	}



	/**
	 * Inserts custom table STRAIN with new strain properties.Handles
	 * screen-based definition as well as bulk file imported list of strains.
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{

			if(!WtUtils.isNullOrBlank(getServerItemValue(FileType.STRAIN_IMPORT_FILE)))
			{
				// file import
				updateAppFilesWithAppliesTo(request, response, user, db);
			}
			// file import or screen-based definition use same code
			updateCustomTables(request, db);

	}

	/**
	 * Returns T if a history record should be saved for pre-update records.
	 * 
	 * @return T
	 */
	public String createHistoryRecord()
	{
		return "T";
	}

}
