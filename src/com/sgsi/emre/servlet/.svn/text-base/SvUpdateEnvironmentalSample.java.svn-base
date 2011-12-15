package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.UpdateAnimalSample;
import com.sgsi.emre.task.UpdateAquaticSample;
import com.sgsi.emre.task.UpdateCompositeSample;
import com.sgsi.emre.task.UpdatePlantSample;
import com.sgsi.emre.task.UpdateSubsurfaceSample;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Shows 
 * @author tjs
 *
 */
public class SvUpdateEnvironmentalSample extends EMREServlet 
{

	/** 
	   * Overridden to forward to correct task screen
	   * depending on Sample Type selected by user
	   * from dropdown.
	   * @param task
	   * @param user
	   * @param db
	   * @param request
	   * @param response
	   * @return ALL_DONE or FINISH_FOR_ME
	   */
	  @Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
	    if(request.getParameter("Go")!= null)
	    {
	      String sampleType = task.getDisplayItemValue("SampleType");
	      // throws error if task is misnamed or not yet supported
	      task = this.getTaskObject("Update Environmental " + sampleType );

	      // -- replace cached instance
	      task.populateSQLValues(user, db);
	      request.getSession().setAttribute(Strings.TASK.TASK, task);
	      request.setAttribute(Strings.TASK.TASK_NAME, task.getTaskName());
	      
	      // show the screen for the correct sample logging task
	      String taskPg = task.getTaskPg(getServletContext());
	      request.getSession().setAttribute(Strings.TASK.TASK_PAGE, taskPg);
	      this.forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, " ");
	      return ALL_DONE;
	      
	    }
	    else if(request.getParameter("FindData")!= null)
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
			if(task instanceof UpdateAnimalSample)
			{
				UpdateAnimalSample myTask = new UpdateAnimalSample();
				myTask.setDisplayItemValues(sample, task, user, request, db);
			}
			else if(task instanceof UpdateAquaticSample)
			{
				UpdateAquaticSample myTask = new UpdateAquaticSample();
				//myTask.setDisplayItemValues(sample, task, user, request, db);
			}
			else if(task instanceof UpdateCompositeSample)
			{
				UpdateCompositeSample myTask = new UpdateCompositeSample();
				myTask.setDisplayItemValues(sample, task, user, request, db);
			}
			else if(task instanceof UpdatePlantSample)
			{
				UpdatePlantSample myTask = new UpdatePlantSample();
				myTask.setDisplayItemValues(sample, task, user, request, db);
			}
			else if(task instanceof UpdateSubsurfaceSample)
			{
				UpdateSubsurfaceSample myTask = new UpdateSubsurfaceSample();
				myTask.setDisplayItemValues(sample, task, user, request, db);
			}
			return FINISH_FOR_ME;
	      
	    }
	    return super.handleCustomAction(task, user, db, request, response);
	}
	  
	  
}
