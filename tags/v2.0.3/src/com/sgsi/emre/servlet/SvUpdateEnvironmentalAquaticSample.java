package com.sgsi.emre.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.PhotoHostStrainCollection;
import com.sgsi.emre.task.PhotoStrainCollection;
import com.sgsi.emre.task.StrainCollection;
import com.sgsi.emre.task.UpdateAnimalSample;
import com.sgsi.emre.task.UpdateAquaticSample;
import com.sgsi.emre.task.UpdateCompositeSample;
import com.sgsi.emre.task.UpdatePlantSample;
import com.sgsi.emre.task.UpdateSubsurfaceSample;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtTaskUtils;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvUpdateEnvironmentalAquaticSample extends EMREServlet 
{

	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		
		try
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
				save(task, user, db, request,response);
		    	commitDb(db);
		        return FINISH_FOR_ME;    	
			}
			else if(request.getAttribute("FindData")!= null)
			{
				String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
				if(WtUtils.isNullOrBlank(sample))
				{
					throw new LinxUserException("Please enter a sample, then try again.");
				}
				else if( !db.getHelper().isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
				{
					throw new LinxUserException("Sample " + sample + " does not exist in this LIMS database."
						 + " Please check the entry, then try again.");
				}
				// set this sample's values in UI widgets
				UpdateAquaticSample myTask = new UpdateAquaticSample();
				myTask.setDisplayItemValues(sample, task, user, request, db);
				return FINISH_FOR_ME;
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
}
