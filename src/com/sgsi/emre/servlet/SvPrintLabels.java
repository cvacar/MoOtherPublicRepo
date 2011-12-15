package com.sgsi.emre.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Parses delimited strain IDs from textarea widget
 * and appends to server-side item for processing
 * by task class.
 * @author TJS/Wt for SGI
 * @created 4/2011 for SGI-EMRE 2.1
 * @modified 7/2011 by TJS/Wildtype for EMRE 2.2 
 * 	-- eliminated short barcode in favor of full ITEM.item
 *
 */
public class SvPrintLabels extends EMREServlet 
{
	//String[] delims = {Strings.CHAR.COMMA,";",Strings.CHAR.TAB};
	String[] delims = {Strings.CHAR.NEWLINE, Strings.CHAR.COMMA,  
			" "};
	
	/* *
	 * Overridden to parse the textarea contents into individual
	 * strain IDs and set the server-side items for task processing. 
	 * We do this here vs task class bec choice of widget is not
	 * the task's concern.
	 * (non-Javadoc)
	 * @see com.wildtype.linx.task.servlet.SvTask_Default#doTaskWorkOnPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.wildtype.linx.task.Task, com.wildtype.linx.user.User, com.wildtype.linx.db.Db)
	 */
	@Override
	protected boolean doTaskWorkOnPost(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
	  if(!WtUtils.isNullOrBlank(task.getDisplayItemValue("MinBarcode")))
	  {
	  	// range widget takes precedent over subset text area widget
	  	return FINISH_FOR_ME;
	  }
		ArrayList<String> idList = new ArrayList<String>();

		// do some collection backflips to parse textarea contents,
		// then set values on server-side for selected itemtype
		String concat = task.getDisplayItemValue("Items");
		if(WtUtils.isNullOrBlank(concat))
		{
			throw new LinxUserException("Please enter a LIMS ID in at least one of the fields.");
		}
		// check for usual delim suspects
		String delim = delims[0]; // newline is default
		for(int i = 0; i < delims.length; i++)
		{
			if(concat.indexOf(delims[i]) > 0)
			{
				delim = delims[i];
				break;
			}
		}// next delim
		//at exit, we tried to detect a delimiter; may be a single ID
		String[] ayIds = concat.split(delim);
		for(int i = 0; i < ayIds.length; i++)
		{
			String id = ayIds[i];
			if(id.length() > 3) // skip empty tokens
			{
				idList.add(trim(id));
			}
		}// next strain ID
		// at exit, idList contains all tokens from textarea
		String itemType = task.getServerItemValue("ItemType");
		task.getServerItem(itemType).setValues(idList);
		// ready for standard processing
		return FINISH_FOR_ME;
	}

}
