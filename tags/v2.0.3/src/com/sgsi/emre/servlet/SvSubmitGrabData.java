package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.task.SubmitGrabData;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;

public class SvSubmitGrabData extends EMREServlet 
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
		if (request.getAttribute("GetImportID") != null)
		{		   
			// currently, unique import ID is just this tranID
			// -- useful because we know it is unique
			long nextId = ((SubmitGrabData)task).getTranId(db);
			task.getDisplayItem("ImportID").setValue(String.valueOf(nextId));
			db.getHelper().addComment("TASK_COMMENTTYPE", 
					"Generated new Import ID " + String.valueOf(nextId) 
					+ "for sampling data submission.", null, task.getTranId(), db);
			// make sure this tranId is 'burned'
			// -- also preserves TASKHISTORY and COMMENT records
			commitDb(db);
			
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}


}
