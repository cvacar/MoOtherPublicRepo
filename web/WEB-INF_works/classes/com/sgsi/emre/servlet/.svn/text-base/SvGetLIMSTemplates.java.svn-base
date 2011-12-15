package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * 
 * SvGetLIMSTemplates
 *
 * Overridden to download latest *.xls template
 * for bulk import file selected by user
 * from dropdown.
 * 
					<value>Strain Collection bulk import</value>
					<value>Log Sample bulk import</value>
					<value>Toledo Lab tasks bulk import</value>
					<value>Analytical Chemistry Run Sheet</value>
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 5/2008
 */
public class SvGetLIMSTemplates extends EMREServlet
{

  /**
	 * Overridden to download latest *.xls template for bulk import file
	 * selected by user from dropdown.
	 * 
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
        String selItem = task.getDisplayItemValue("TemplateType");
        if(WtUtils.isNullOrBlankOrPlaceholder(selItem))
        {
            throw new LinxUserException("Please select the Template Type from the list, then try again.");
        }
        // look for path value (may not have been provided)
        String path = dbHelper.getApplicationValue(db, "FILETYPE", selItem);
        if(WtUtils.isNullOrBlank(path))
        {
            // a fairly common case
            throw new LinxUserException("No path to the latest version has been stored for Template Type " + selItem);
        }
        // file is known
        File file = new File(path);
        this.returnDownloadAsByteStream(response, file, "Template_" + selItem + ".xls", "application/vnd.ms-excel", false);
        return ALL_DONE;
      
    }
    return super.handleCustomAction(task, user, db, request, response);
  }
  
  
}
