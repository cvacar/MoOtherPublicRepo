package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.task.PrintSubmissionLabels;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvPrintSubmissionLabels extends EMREServlet 
{
	static final String REQUEST_ID = "Request";
	static final String PRINTER = "Printer";
	static final String GET_REQUEST = "ShowRequest";
	static final String PRINT_BUTTON = "PrintLabels";
	static final String MIN_ID = "Min";
	static final String MAX_ID = "Max";
	static final String LABEL_TABLE = "Labels";
	PrintSubmissionLabels mytask = new PrintSubmissionLabels();
	RowsetView labelView = null;
	TableDataMap labelMap = null;
	EMREDbHelper dbHelper = new EMREDbHelper();
	
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
    	String requestId = task.getDisplayItemValue(REQUEST_ID);
		if(WtUtils.isNullOrBlank(requestId))
		{
			throw new LinxUserException("Please enter a Request ID in the 'Request ID' field.");
		}

		// eff v1.15, allow user to enter a range or single label to print
		if(request.getParameter(GET_REQUEST) != null)
		{
			 
			 labelView = getSubmissionsRowsetView(request, response,
					 requestId, task, db);
			 RowsetView.addViewToSessionViews(request, labelView);
			 
			 // show min and max submission IDs
			 String minSubId = mytask.getMinOrMaxSubmissionID(requestId, "min", db);
			 String maxSubId = mytask.getMinOrMaxSubmissionID(requestId, "mnx", db);
			 
			 task.getDisplayItem(MIN_ID).setValue(minSubId);
			 task.getDisplayItem(MAX_ID).setValue(maxSubId);
			 
			 task.getDisplayItem(PRINT_BUTTON).setVisible(true);
			 task.getDisplayItem(LABEL_TABLE).setVisible(true);
			 task.getDisplayItem(MIN_ID).setVisible(true);
			 task.getDisplayItem(MAX_ID).setVisible(true);
			 task.getDisplayItem("Placeholder").setVisible(true);
			 
		     this.forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, " ");
		     return ALL_DONE;
		}
		else if(request.getParameter(PRINT_BUTTON) != null)
	    {
	    	// print labels, handle lots of potential error conditions
	    	try
			{
	    		
	    		// moved later to handle errors better
				 String zebraPrinter = task.getDisplayItemValue(PRINTER);
				 if(WtUtils.isNullOrBlankOrPlaceholder(zebraPrinter))
				 {
					 
					 throw new LinxUserException(
							 "Please select a Zebra printer from the list, then try again.");
				 }
				 
				 // get user's selected submission ID range
				 // -- per VA, last 3 digits only is also acceptable
				 String minID = task.getDisplayItemValue(MIN_ID);
				 String maxID = task.getDisplayItemValue(MAX_ID);
				 if(WtUtils.isNullOrBlank(maxID))
				 {
					 maxID = minID;
				 }
				 
	    		// try to print
	    		mytask.printLabels(requestId, minID, maxID, zebraPrinter, db);
	    		mytask.setMessage("Successfully printed labels.");
			}
			catch (Exception e)
			{
				// handle problem with request ID 
				// by hiding table & Print button
				if(e.getMessage().indexOf("does not contain Request ID") > 0)
				{
					cleanupOnError(request, task);
					throw new LinxUserException(e.getMessage());
				}
					
				// handle problem with printer 
				// by preserving user's selections 
				// -- fetch workingLabel db table contents
				RowsetView.cleanupSessionViews(request);
				labelView = getSubmissionsRowsetView(request, response,
						requestId, task, db);
				RowsetView.addViewToSessionViews(request, labelView);
				
				task.setMessage(e);
				this.forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, " ");
			    return ALL_DONE;	
			}
	        return FINISH_FOR_ME;    	
		}
		return super.handleCustomAction(task, user, db, request, response);
	}

	/** 
	 * Returns a rowset view containing all potential barcode rows for
	* given request, ready to display to user, eff v1.15.
	*/
	public RowsetView getSubmissionsRowsetView(HttpServletRequest request,
			HttpServletResponse response, String requestId, Task task, Db db)
		{
		    
			// fetch the table data rows
			RowsetView view = this.getSQLRowsetView(request, response, task, 
					 "exec spEMRE_getSubmissionsForBulkPrint '" + requestId + "', 'NA', 'NA'", 
					 "Barcode", LABEL_TABLE, db);

			if(view.getRowcount() < 1)
		   	{
				this.cleanupOnError(request, task);
		   		throw new LinxUserException("Could not find any submission IDs "
		   				+ "associated with Request ID [" + requestId + "].");
		   				
		   	}

		    view.setHideNavControls(false); 
			view.setStartRow(1);
		    return view;
		 }
	
	/**
	 * Handles special case of errors in the request ID after
	 * the label table is already displayed. 
	 * @param request
	 * @param task
	 */
	protected void cleanupOnError(HttpServletRequest request, Task task)
	{
		// prep for special case of new request ID entered
		// while another request's label table is still displayed
		 RowsetView.cleanupSessionViews(request);
		 task.getDisplayItem(LABEL_TABLE).setVisible(false);
		 task.getDisplayItem(PRINT_BUTTON).setVisible(false);
		 task.getDisplayItem(MIN_ID).setVisible(false);
		 task.getDisplayItem(MAX_ID).setVisible(false);
		 task.getDisplayItem("Placeholder").setVisible(false);
		 

	}
	
}
