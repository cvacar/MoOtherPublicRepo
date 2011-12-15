package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Handles custom actions 'Import' and 'Find Data'
 * for Update Environmental Aquatic Sample task.
 * 
 * @author TJS/Wildtype for SGI
 * @modified 7/2011 for EMRE v2.2 -- eliminated screen input in favor of bulk insert
 *
 */
public class SvUpdateEnvironmentalAquaticSample extends EMREServlet 
{

	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		
			if(request.getAttribute("ImportButton")!= null)
		    {
		    	task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
							"Please browse for a bulk import file, then try again.");
				}
				task.getDisplayItem("EnvironmentalSample").clearValues();
				task.getServerItem("EnvironmentalSample").clearValues();
				save(task, user, db, request,response);
		    	commitDb(db);
		        return FINISH_FOR_ME;    	
			}
 			else if(request.getAttribute("FindData")!= null)
			{
				String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
				if(WtUtils.isNullOrBlank(sample))
				{
					throw new LinxUserException("Please enter a sample ID (SI1-), then try again.");
				}
				else if( !db.getHelper().isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
				{
					throw new LinxUserException("Sample " + sample + " does not exist in this LIMS database."
						 + " Please check the entry, then try again.");
				}
				// user wants to download the entire strain collection to Excel
				writeToExcel(request, response, "exec spEMRE_exportEnvironmentalSample '" + sample + "'",
						db);				
				return FINISH_FOR_ME;
			}
			return super.handleCustomAction(task, user, db, request, response);
	}
}
