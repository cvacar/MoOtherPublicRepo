package com.sgsi.emre.task;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class DNARNAPrep extends EMRETask 
{
	private String sampleType = null;
	private String ROWSET = "Location";
	private int COLUMN_LOCATION = 2;
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		sampleType = null;
		getServerItem("DNA").clearValues();
		getServerItem("RNA").clearValues();
		//do we have a DNA or RNA?
		String sampleType = getServerItemValue("SampleType");
		if(WtUtils.isNullOrBlankOrPlaceholder(sampleType))
			throw new LinxUserException("Please select a sample type and try again.");
		setSampleType(sampleType);
		if(getSampleType().equalsIgnoreCase(ItemType.DNA))
		{
			String dna = getServerItemValue("LIMSID");
			getServerItem(ItemType.DNA).setValue(dna);
		}
			
		else if (getSampleType().equalsIgnoreCase(ItemType.RNA))
		{
			String rna = getServerItemValue("LIMSID");
			getServerItem(ItemType.RNA).setValue(rna);
		}
		//what type of input sample do we have?
		//did it come from a file?
		String fileId = getServerItemValue("OriginIDFile");
		if (!WtUtils.isNullOrBlank(fileId))
		{
			// yes
			//lets read the file and add the IDs to the server dom
			File inFile = this.getFile(fileId, db);
			if(!inFile.canRead())
				throw new LinxUserException("Cannot read origin id file.");
			try
			{
				char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
				String columnKey = "Origin LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
				String[] headers = new String[1];
				headers[0] = "Origin LIMS IDs";
				XLSParser fileData = new XLSParser(inFile, "DNA or RNA Preparation",
						delim, columnKey, headers, true);
				ArrayList<String> ids = new ArrayList<String>();
				if(fileData.gotoFirst())
				{
					do
					{
						String originLimsId = fileData.getRequiredProperty("Origin LIMS ID");
						ids.add(originLimsId);
					}
					while(fileData.gotoNext());
				}
				if(ids.size() == 0)
					throw new LinxUserException("There must be at least one Origin LIMS ID in your file");
				Item oIds = getServerItem("OriginLIMSID");
				oIds.setValues(ids);
			}
			catch(Exception ex)
			{
				throw new LinxUserException(ex.getMessage());
			}
			return;
		}
		List<String> lsOriginSample = getServerItemValues("OriginLIMSID");
		String validTypes = "";
		for(SampleType s : SampleType.values())
		{
			validTypes += s + ",";
		}
		for(String samp : lsOriginSample)
		{
			ResultSet rs = dbHelper.getItemType(samp, db);
			boolean bValidType = false;
			try
			{
				while(rs.next())
				{
					String originSampleType = rs.getString(1);
					for(SampleType s : SampleType.values())
					{
						if(originSampleType.equalsIgnoreCase(s.toString()))
						{
							bValidType = true;
						}
						if(bValidType)
							break;
					}
					//if we're here we have one itemtype for the origin sample - lets see if it's a valid one
					if(!bValidType)
					{
						throw new LinxUserException("Origin LIMS ID " + samp + " is not a valid type.\r\nValid types are: " + validTypes);
					}
				}
			}
			catch(Exception ex)
			{
				throw new LinxDbException(ex.getMessage());
			}
			
		}
		//now lets set the type on the server
		//getServerItem(originSampleType).setValue(originSample);
	}
	
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		updateAppFilesWithAppliesTo(request, response, user, db);
		  updateCustomTables(request, db);
	}

	/**
	 * Inserts new or updates into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		//the only reason we're distinguishing the two here if for insert into the nucleicAcid table
		String nucleicAcid = null;
		String itemId = null;
		String itemType = null;
		if(getSampleType().equalsIgnoreCase(ItemType.DNA))
		{
			nucleicAcid = getServerItemValue(ItemType.DNA);
			itemId = dbHelper.getItemId(nucleicAcid, ItemType.DNA, db);
			itemType = ItemType.DNA;
		}
		else if (getSampleType().equalsIgnoreCase(ItemType.RNA))
		{
			nucleicAcid = getServerItemValue(ItemType.RNA);
			itemId = dbHelper.getItemId(nucleicAcid, ItemType.RNA, db);
			itemType = ItemType.RNA;
		}
		else
			throw new LinxUserException("Unknown sample type.  Valid sample types are 'DNA' and 'RNA'.  Please notify LIMS support.");
		//String originItemId = dbHelper.getItemId(originSample, originSampleType, db);
		String vendor = getServerItemValue("Vendor");
		String protocolFileId = getServerItemValue("Protocol");
		String notebookPg = getServerItemValue("NotebookRef");
		
		ArrayList<String> params = new ArrayList<String>();
		String sql = "spEMRE_insertNucleicAcid";
		params.add(itemId); //dna
		params.add(notebookPg);
		params.add(vendor);
		params.add(protocolFileId);
		params.add(getTranId() + "");
			
		String neId = dbHelper.callStoredProc(db, sql, params, true, true);
		  
		setMessage("Successfully saved " + getSampleType() + ": " + nucleicAcid);
		//now update the nucleicAcidOrigin table
		List<String> lsOrigSamples  = getServerItemValues("OriginLIMSID");
		int idx = 1;
		for(String originSample : lsOrigSamples)
		{
			ResultSet rs = dbHelper.getItemType(originSample, db);
			String originSampleType = null;
			try
			{
				while(rs.next())
				{
					originSampleType = rs.getString(1);
					boolean bValidType = false;
					for(SampleType s : SampleType.values())
					{
						if(originSampleType.equalsIgnoreCase(s.toString()))
						{
							bValidType = true;
						}
						if(bValidType)
							break;
					}
				}
				rs.close();
				rs = null;
			}
			catch(Exception ex)
			{
				throw new LinxDbException(ex.getMessage());
			}
			if(WtUtils.isNullOrBlankOrPlaceholder(originSampleType))
				throw new LinxUserException("Origin LIMS ID " + originSample + " doesn't exist." +
						"  Please enter a valid Origin LIMS ID and try again.");
			String originItemId = dbHelper.getItemId(originSample, originSampleType, db);
			//now that we have a sample type lets add content
			dbHelper.addContent(nucleicAcid, itemType, 
					originSample, originSampleType, "0", null, null, getTranId(), db);
			
			//now insert the nucleicAcidOrigin table
			params = new ArrayList<String>();
			sql = "spEMRE_insertNucleicAcidOrigin";
			params.add(neId);
			params.add(idx + "");
			params.add(originItemId);
			params.add(getTranId() + "");
				
			dbHelper.callStoredProc(db, sql, params, false, true);
			idx++;
		}
		
		//now that we've inserted into nucleicAcid lets insert the locations
		TableDataMap rowMap = new TableDataMap(request, ROWSET);
		sql = "spEMRE_insertNucleicAcidLocation";
		String tranid = String.valueOf(this.getTranId());
		dbHelper.insertLocations(itemId, rowMap, tranid, request, COLUMN_LOCATION, ROWSET, sql, db);
		
		
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}
	
	private enum SampleType
	{
		EnvironmentalSample,
		PrimaryEnrichment,
		SecondaryEnrichment,
		Isolate,
		Strain
	}
}
