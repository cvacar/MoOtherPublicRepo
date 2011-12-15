package com.sgsi.emre.task;

import java.sql.ResultSet;
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

public class LibraryConstruction extends EMRETask 
{

	private String originSampleType = null;
	private int COLUMN_LOCATION = 2;
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		originSampleType = null;
		//do we have a DNA or RNA?
		//what type of input sample do we have?
		String originSample = getDisplayItemValue("OriginLIMSID");
		if(WtUtils.isNullOrBlankOrPlaceholder(originSample))
			throw new LinxUserException("Please enter a value for Origin LIMS ID");
		ResultSet rs = dbHelper.getItemType(originSample, db);
		boolean bValidType = false;
		String validTypes = "";
		try
		{
			while(rs.next())
			{
				originSampleType = rs.getString(1);
				for(SampleType s : SampleType.values())
				{
					if(originSampleType.equalsIgnoreCase(s.toString()))
					{
						bValidType = true;
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		
		if(!bValidType)
		{
			for(SampleType s : SampleType.values())
			{
				validTypes += s + ",";
			}
			throw new LinxUserException("Origin LIMS ID " + originSample + " is not a valid type.\r\nValid types are: " + validTypes);
		}
			
		//now lets set the type on the server
		getServerItem(originSampleType).setValue(originSample);
	}
	
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		  updateCustomTables(request, db);
	}

	/**
	 * Inserts new or updates into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		String originSample = getServerItemValue(originSampleType);
		String originItemId = dbHelper.getItemId(originSample, originSampleType, db);
		String nucleicAcidId = dbHelper.getDbValue("exec spMet_getNucleicAcidId " + originItemId, db);
		String library = getServerItemValue(ItemType.LIBRARY);
		String libraryItemId = dbHelper.getItemId(library, ItemType.LIBRARY, db);
		String size = getServerItemValue("Size");
		String vector = getServerItemValue("Vector");
		String vectorName = getServerItemValue("VectorName");
		String preparedBy = getServerItemValue("PreparedBy");
		String linkers = getServerItemValue("Linkers");
		String notebook = getServerItemValue(DataType.NOTEBOOK_REF);
		String dnaConc = getServerItemValue("DNAConcentration");
		String primarySample = getServerItemValue("PrimarySample");
		String qcInsertSize = getServerItemValue("QCdInsertSize");
		String qc5Primer = getServerItemValue("QCPrimer5Prime");
		String qc3Primer = getServerItemValue("QCPrimer3Prime");
		
		ArrayList<String> params = new ArrayList<String>();
		String sql = "spMet_insertLibrary";
		//the only reason we're distinguishing the two here if for insert into the library table
		
		params.add(nucleicAcidId); 
		params.add(libraryItemId); 
		params.add(size);
		params.add(vector);
		params.add(vectorName);
		params.add(preparedBy);
		params.add(linkers);
		params.add(notebook);
		params.add(dnaConc);
		params.add(primarySample);
		params.add(qcInsertSize);
		params.add(qc5Primer);
		params.add(qc3Primer);
		params.add(getTranId() + "");
			
		dbHelper.callStoredProc(db, sql, params, false, true);
			//lets add the DNA as content of the library
		if (originSampleType.equalsIgnoreCase(ItemType.DNA))
		{
			dbHelper.addContent(library, ItemType.LIBRARY, 
				originSample, ItemType.DNA, "0", null, null, getTranId(), db);
		}
		else if (originSampleType.equalsIgnoreCase(ItemType.RNA))
		{
			//lets add the RNA as content of the library
			dbHelper.addContent(library, ItemType.LIBRARY, 
					originSample, ItemType.RNA, "0", null, null, getTranId(), db);
		}
		
		//now lets insert the locations
		sql = "spMet_insertLibraryLocation";
		insertLocation(request, "Location", COLUMN_LOCATION, library, sql, db);
		  
	}
	
	private enum SampleType
	{
		DNA,
		RNA
	}
}
