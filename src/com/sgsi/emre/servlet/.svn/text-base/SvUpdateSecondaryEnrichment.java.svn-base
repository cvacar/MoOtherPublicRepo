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
 * Overridden to handle custom action 'Import' and bulk insert
 * contents of bulk import file containing updates to existing
 * PrimaryEnrichment items. Calls db stored procedure 
 * spEMRE_bulkUpdateSecondaryEnrichments.
 * @author TJS/Wildtype for SGI
 * @created 7/2011 for EMRE v2.2
 *
 */
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
			task.getServerItem(ItemType.SECONDARY_ENRICHMENT).clearValues();
			save(task, user, db, request,response);
	    	commitDb(db);
	        return FINISH_FOR_ME;    	
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	

}
