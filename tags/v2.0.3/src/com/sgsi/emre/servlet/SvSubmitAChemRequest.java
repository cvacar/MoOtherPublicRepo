package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.SubmitAChemRequest;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvSubmitAChemRequest extends EMREServlet 
{
	
	/**
	 * Overridden to handle custom action to get next work request id
	 * for user to add to his or her file before submission
	 * (doesn't have to; will be validated as new in any case.)
	 * Unusual in that this handler commits the table update for 
	 * nextId, generating TASKHISTORY and COMMENT records too.
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return FINISH_FOR_ME
	 */
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		if (request.getAttribute("Next Request ID") != null)
		{
			// find last request ID of this type and increment
		    String requestType = task.getServerItemValue("RequestType");
		    if(WtUtils.isNullOrBlankOrPlaceholder(requestType))
		    {
		    	throw new LinxUserException(
		    			"Please select the Request Type from dropdown list, then try again.");
		    }
		   
			String nextID = ((SubmitAChemRequest)task).getNextRequestID(requestType, db );
			task.getDisplayItem("RequestID").setValue(nextID);
			db.getHelper().addComment("TASK_COMMENTTYPE", "Generated new request ID for " + requestType + " submission.", null, task.getTranId(), db);
			// make sure this work request id is 'burned'
			// -- also preserves TASKHISTORY and COMMENT records
			commitDb(db);
			
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}






	
}
