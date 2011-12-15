package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.task.PrintBulkLabels;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class SvPrintBulkLabels extends EMREServlet 
{
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
	    if(request.getParameter("PrintLabel") != null)
	    {
	    	String minStrain = task.getDisplayItemValue("MinStrain");
			if(WtUtils.isNullOrBlank(minStrain))
			{
				throw new LinxUserException("Please enter a strain ID in the 'Min Strain ID' field.");
			}
			String maxStrain = task.getDisplayItemValue("MaxStrain");//can be null
	    	// print labels
	    	try
			{
	    		((PrintBulkLabels)task).printLabels(minStrain, maxStrain, task, db);
	    		task.setMessage("Successfully printed labels.");
			}
			catch (Exception e)
			{
				throw new LinxUserException(e.getMessage());	
			}
	        return FINISH_FOR_ME;    	
		}
		return super.handleCustomAction(task, user, db, request, response);
	}
}
