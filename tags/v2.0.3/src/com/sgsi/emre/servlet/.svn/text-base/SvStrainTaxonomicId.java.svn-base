package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;



public class SvStrainTaxonomicId extends EMREServlet 
{
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if(request.getParameter("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
			writeToExcel(request, response, "exec spMet_reportStrainTaxonomicId", db);

			return ALL_DONE;
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
}
