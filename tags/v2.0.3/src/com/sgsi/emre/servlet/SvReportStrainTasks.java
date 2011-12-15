package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvReportStrainTasks extends EMREServlet 
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
	    	String taskName = task.getDisplayItemValue("TaskType");
		    if(WtUtils.isNullOrBlankOrPlaceholder(taskName))
		    	throw new LinxUserException("Please select a report task and try again.");
	    	task = this.getTaskObject(taskName);

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
	    return super.handleCustomAction(task, user, db, request, response);
	  }
}
