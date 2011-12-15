package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvUpdateSecondaryEnrichment extends EMREServlet 
{

	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if (request.getAttribute("ImportButton") != null)
		{
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task.getServerItemValue(FileType.UPDATE_SECONDARY_ENRICHMENT_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			importRowsFromFile(fileId, task, user, db, request,response);
	    	commitDb(db);
	        return FINISH_FOR_ME;    	
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		XLSParser data = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);

		// create a list of objects while importing manifest
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "LIMS ID"; //the unique identifier that lets me know i've reach the column data in the file
		data = new XLSParser(inFile, FileType.UDPATESECONDARYENRICHMENT_SUBMISSION,
				delim, columnKey, EMREStrings.Enrichments.requiredUpdateSecondaryColumnHeaders, true);

		//loop though the file and insert each line
		String err = "";
		int row = 1;
		try
		{
			
			if(data.gotoFirst())
			{
				do
				{				
					String limsId = data.getRequiredProperty("LIMS ID");
					String o2Conc = data.getProperty("Oxygen Concentration");
					String co2Conc = data.getProperty("CO2 Concentration");
					String pH = data.getProperty("pH");
					String result = data.getProperty("Enrichment Result");
					String flocculence = data.getProperty("Flocculence");
					String comment = data.getProperty("Comments");
					
					task.getServerItem(ItemType.SECONDARY_ENRICHMENT).setValue(limsId);
					task.getServerItem("OxygenConcentration").setValue(o2Conc);
					task.getServerItem("CO2Concentration").setValue(co2Conc);
					task.getServerItem("pH").setValue(pH);
					task.getServerItem("EnrichmentResult").setValue(result);
					task.getServerItem("Flocculence").setValue(flocculence);
					task.getServerItem("Comment").setValue(comment);
					
					// call std processing
					save(task, user, db, request, response);
					row++;
				}
				while(data.gotoNext());
			}//at end we have validated all of the inputs in the file
		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if(!err.equals(""))
			throw new LinxUserException(err);
	}
}
