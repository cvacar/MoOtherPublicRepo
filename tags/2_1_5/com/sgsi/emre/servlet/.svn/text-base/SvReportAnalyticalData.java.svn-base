package com.sgsi.emre.servlet;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.view.RowsetView;

public class SvReportAnalyticalData extends EMREServlet 
{
	protected RowsetView view = null;
	protected RowsetView itemType = null;
	protected RowsetView results = null;
	final String COMPOUND_TABLE = "Compounds";
	final String RESULT_TABLE = "Results";
	final String ITEM_TYPE = "ItemType";
	final static int COLUMN_CHECKBOX = 1;
	final static int COLUMN_METHOD = 2;
	final static int COLUMN_COMPOUND = 3;
	final static int COLUMN_MIN = 4;
	final static int COLUMN_MAX = 5;
	private ArrayList<String> alMethodsToReport = new ArrayList<String>();
	
	/**
	 * Handles the task GET requests ('goToRow' and 'export' currently supported)
	 * @param request The current request
	 * @param response The current response
	 * @param task The selected task
	 * @param user The logged in user
	 * @param db The db connection
	 */
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{

	     if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
	     {
	     		
	     	handleGoToRowRequest(request, response);
	     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
	     	return(ALL_DONE);
	     }
	     else if (request.getParameter("selCoord") != null
					&& request.getParameter("selCoord").startsWith(ITEM_TYPE)
					&& request.getParameter("selVal") != null)
			{
				// user has selected an item type, so get the detail back
				try
				{
					String itemtype = request.getParameter("selVal");
					getAnalyticalData(itemtype, request, task, db);
				    return FINISH_FOR_ME;
				}
				catch(NullPointerException ne)
				{
					task.setMessage("No results were returned from the query.");
				}
				catch(Exception ex)
				{
					throw new LinxDbException(ex.getMessage());
				}

		 }
	     else if (request.getParameter("selCoord") != null
					&& request.getParameter("selCoord").startsWith(RESULT_TABLE)
					&& request.getParameter("selVal") != null)
			{
				// user has selected a file to download
				try
				{
					String fileId = (String)request.getParameter("selVal");
			         if(fileId != null)
			         {
			             File file = new File(fileId);
			             String filename = file.getName();
			             this.returnDownloadAsByteStream(response, file, filename, AS_EXCEL_FILE, false);
			             return ALL_DONE;
			         }
				}
				catch(NullPointerException ne)
				{
					task.setMessage("No files were returned from the query.");
				}
				catch(Exception ex)
				{
					throw new LinxDbException(ex.getMessage());
				}

		 }
	     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
	     {
	    	 RowsetView.addViewToSessionViews(request, results);
	    	 String sTable = request.getParameter(Strings.WIDGET.TABLE.TABLE);
	    	 return handleExportRequest(request, response, sTable, "GetAnalyticalData_"+task.getTranId(db) +".csv");
	     }
		 return FINISH_FOR_ME;
		   
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
	
	    if(request.getParameter("GetCompounds")!= null)
	    {
	    	try
	    	{
	    		populateCompoundView(request, task, db);
	    	}
	    	catch(Exception ex)
	    	{
	    		throw new LinxUserException(ex.getMessage());
	    	}
	      return FINISH_FOR_ME;
	    }
	    else if(request.getParameter("Run")!= null)
	    {
	    	try
	    	{
	    		getItemsForMethods(request, task, db);
	    	}
	    	catch(Exception ex)
	    	{
	    		throw new LinxUserException(ex.getMessage());
	    	}
	      return FINISH_FOR_ME;
	    }
	    else
	    {
	    	return super.handleCustomAction(task, user, db, request, response);
	    }
	  }
	
	protected void populateCompoundView(HttpServletRequest request, Task task, Db db)
	{
	    // show UI table of locations for selected strain
  		RowsetView.cleanupSessionViews(request);
  		alMethodsToReport = new ArrayList<String>();
  		String[] params = request.getParameterValues("AnalysisMethod");
  		
  		int numMethods = params.length;
  		String methods = "";
  		for(int i = 0; i < numMethods; i ++)
  		{
  			if(params[i].equalsIgnoreCase("(Any)"))
  				continue;
  			if(i < numMethods -1)
 				methods += params[i] + ";";
  			else
  				methods += params[i];
  		}
	    String sql = "exec spMet_getAnalyticalCompoundsByMethod '" + methods + "'";
	    view = getSQLRowsetView(request, sql, "Select", COMPOUND_TABLE,
	    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	    view.setWidget(1,LinxConfig.WIDGET.CHECKBOX);
	    view.setWidget(2,LinxConfig.WIDGET.LABEL);
	    view.setWidget(3,LinxConfig.WIDGET.LABEL);
	    view.setWidget(4,LinxConfig.WIDGET.TEXTBOX);
	    view.setWidget(5,LinxConfig.WIDGET.TEXTBOX);
	    view.setStartRow(1); 
	    view.setMessage("Choose compounds to report for method(s): " + methods);
	    task.getDisplayItem("Results").setVisible(true);
  		RowsetView.addViewToSessionViews(request, view);

	 }
	
	protected void getAnalyticalData(String itemtype, HttpServletRequest request, Task task, Db db)
	{
		try
		{ 
			String sql = "exec spMet_reportAnalyticalGetResults '" + itemtype + "','";
			//now build the where clause
			String inputs = "";//will use this to add values to the sp
			int numRows = alMethodsToReport.size();
			
			for(int rowIdx = 0; rowIdx < numRows; rowIdx++)
			{
				String s = alMethodsToReport.get(rowIdx);
				String[] data = s.split(";");
				String method 	= data[0];
				String compound = data[1];
				String min 		= data[2];
				String max 		= data[3];
				inputs += method + ";" + compound;
				if(rowIdx == numRows)
					inputs += ";" + min + ";" + max;
				else
					inputs += ";" + min + ";" + max + ";";
				
			}
			 sql += inputs + "'";
			 
		      System.out.println(sql+Strings.CHAR.NEWLINE);
		      results = getSQLRowsetView(request, sql, "LIMS ID", RESULT_TABLE, 
		    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		      results.setName(RESULT_TABLE);
		      results.setScrollSize("medium");   
		      results.setWidget(6,LinxConfig.WIDGET.LINK);
		      task.setMessage("Showing results filtered by item type.");
		      if(view != null)
		      {
		    	  view.setStartRow(1);
		    	  RowsetView.addViewToSessionViews(request, view);
		      }
		    	 
		      if(itemType != null)
		      {
		    	  itemType.setStartRow(1);
		    	  RowsetView.addViewToSessionViews(request, itemType);
		      }
		      results.setStartRow(1);
		      RowsetView.addViewToSessionViews(request, results);
		      
		}
		catch(Exception ex)
		{
			throw new LinxSystemException(ex.getMessage());
		}
	}
	
	protected void getItemsForMethods(HttpServletRequest request, Task task, Db db)
	{
		try
		{
			alMethodsToReport = new ArrayList<String>();
			String sql = "exec spMet_reportGetAnalyticalItems '";
			//determine the analysis methods to report (if any)
			String[] params = request.getParameterValues("AnalysisMethod");
			int numMethods = params.length;
	  		String analysisMethods = "";
	  		for(int i = 0; i < numMethods; i ++)
	  		{
	  			if(params[i].equalsIgnoreCase("(Any)"))
	  				continue;
	  			if(i < numMethods -1)
	  				analysisMethods += params[i] + ";";
	  			else
	  				analysisMethods += params[i];
	  		}
	  		sql += analysisMethods + "','";
    		//now build the where clause
			String sqlArray = "";//will use this to add values to the sp
			TableDataMap rowMap = null;
			try
			{
				rowMap = new TableDataMap(request, COMPOUND_TABLE);
			}
			catch(Exception ex)
			{
				//must be null - move on
			}
	    	if(rowMap != null)
	    	{
	    		int numRows = rowMap.getRowcount();
	        	for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
	        	{
	        		if( rowMap.isCheckboxChecked(rowIdx, COLUMN_CHECKBOX))
	        		{
	        			//we need to report this row
	        			RowsetView view = getViewFromSession(request, COMPOUND_TABLE);
	        			String method = view.getValue(rowIdx, COLUMN_METHOD);
	        			String compound = view.getValue(rowIdx, COLUMN_COMPOUND);
	        			String min = (String)rowMap.getValue(rowIdx, COLUMN_MIN);
	        			String max = (String)rowMap.getValue(rowIdx, COLUMN_MAX);
	        			sqlArray += method + ";" + compound;
	        			if(rowIdx == numRows)
	        				  sqlArray += ";" + min + ";" + max;
	        			else
	        				  sqlArray += ";" + min + ";" + max + ";";
	        			//save the checked data for reporting later
	        			String concatenatedMethod = method + ";" + compound + ";" + min + ";" + max;
	        			if(!alMethodsToReport.contains(concatenatedMethod))
	        				alMethodsToReport.add(concatenatedMethod);
	        		}
	        	}
	        	 view.setStartRow(1);
	        	 RowsetView.addViewToSessionViews(request, view);
	    	}
			
    	  sql += sqlArray + "'";
	      
	      System.out.println(sql+Strings.CHAR.NEWLINE);
	      itemType = getSQLRowsetView(request, sql, "ItemType", ITEM_TYPE, 
	    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	      itemType.setName(ITEM_TYPE);
	      itemType.setScrollSize("medium");   
	      itemType.setWidget(1,LinxConfig.WIDGET.LINK);
	      task.setMessage("Showing results filtered by compound.");
	      itemType.setStartRow(1);
	      task.getDisplayItem("Results").setVisible(true);
	      RowsetView.addViewToSessionViews(request, itemType);
		}
		catch(Exception ex)
		{
			throw new LinxSystemException(ex.getMessage());
		}
	}
}
