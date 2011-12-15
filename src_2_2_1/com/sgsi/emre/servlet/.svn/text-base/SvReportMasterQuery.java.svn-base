package com.sgsi.emre.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvReportMasterQuery extends EMREServlet 
{
	protected RowsetView view = null;
	protected RowsetView itemType = null;
	protected RowsetView analytical = null;
	final String DATA_TABLE = "Results";
	final String ITEM_TABLE = "ItemType";
	final String ANALYTICAL_TABLE = "AnalyticalData";
	String currItem = null;
	
	
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

		try
		{
			 request.getSession().setAttribute(Strings.TASK.TASK_PAGE, task.getTaskPg(getServletContext()));
			 if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
		     {
		     	handleGoToRowRequest(request, response);
		     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
		     	return(ALL_DONE);
		     }
		     else if (request.getParameter("selCoord") != null
						&& request.getParameter("selCoord").startsWith(DATA_TABLE)
						&& request.getParameter("selVal") != null)
				{
					// user has selected an item type, so get the detail back
					try
					{
						RowsetView.cleanupSessionViews(request);
						String returntype = request.getParameter("selVal");
						String limsid = task.getDisplayItemValue("LIMSID");
						String inputtype = task.getDisplayItemValue("ItemType");
						String sql = getItemSQL(limsid,inputtype, returntype);
						itemType = getSQLRowsetView(request, sql, "ItemType", ITEM_TABLE, 
					        		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
						itemType.setName(ITEM_TABLE);
						itemType.setScrollSize("medium");  
						itemType.setWidget(2,LinxConfig.WIDGET.LINK);
						itemType.setStartRow(1);
						view.setStartRow(1);
						task.setMessage("Showing query results for item: " + limsid);
					    RowsetView.addViewToSessionViews(request, view);
					    RowsetView.addViewToSessionViews(request, itemType);
					    task.getDisplayItem("ItemType").clearValues();
				    	task.getDisplayItem("ItemType").setVisible(false);
				    	//type = null;
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
						&& request.getParameter("selCoord").startsWith(ITEM_TABLE)
						&& request.getParameter("selVal") != null)
				{
					// user has selected an item, so get the analytical data back
					try
					{
						RowsetView.cleanupSessionViews(request);
						String item = request.getParameter("selVal");
						String type = itemType.getValue(1, 1);
						String sql = getAnalyticalSQL(item, type);
//						Element eParent = task.getDisplayItem("ItemType").getItemElement();
//						eParent = dbHelper.getHTMLResultSet(sql, eParent, db);
//				      	StringBuffer sb = new StringBuffer(eParent.getTextContent());
//				        returnDownloadAsHTML(response, sb);
//				        return ALL_DONE;
						analytical = getSQLRowsetView(request, sql, "LIMSID", ANALYTICAL_TABLE, 
					        		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
						analytical.setName(ANALYTICAL_TABLE);
						analytical.setScrollSize("medium"); 
						//analytical.setWidget(2,LinxConfig.WIDGET.LINK);
						analytical.setStartRow(1);
						itemType.setStartRow(1);
						view.setStartRow(1);
						task.setMessage("Showing query results for item: " + item);
					    RowsetView.addViewToSessionViews(request, view);
					    //RowsetView.addViewToSessionViews(request, itemType);
					    RowsetView.addViewToSessionViews(request, analytical);
					    task.getDisplayItem("ItemType").clearValues();
				    	task.getDisplayItem("ItemType").setVisible(false);
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
		     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
		     {
		    	 RowsetView.addViewToSessionViews(request, view);
		    	 String sTable = request.getParameter(Strings.WIDGET.TABLE.TABLE);
		    	 return handleExportRequest(request, response, sTable, "MasterQueryData_"+task.getTranId(db) +".csv");
		     }
			 return FINISH_FOR_ME;
		}
		catch(Exception ex)
		{
			//keep the rowset as is
			view.setStartRow(1);
			if(itemType != null) 
			{
				itemType.setStartRow(1);
			}
			if(analytical != null)
			{
				analytical.setStartRow(1);
			}
			if(ex instanceof LinxUserException)
			{
				throw (LinxUserException)ex;
			}
			else if(ex instanceof LinxDbException)
			{
				if(ex.getMessage().contains("Query did not return columns"))
				{
					task.setMessage("No results were returned from the query.");
				}
				else if(ex.getMessage().contains("More than one item exists with this name"))
				{
					task.setMessage("More than one item exists with this name.  Please notify LIMS support.");
				}
				else
					throw (LinxDbException)ex;
			}
			throw new LinxUserException(ex.getMessage());
		}
	      
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {

	    if(request.getParameter("Run")!= null)
	    {
	    	try
	    	{
	    		runQuery(request, task, db);
	    	}
	    	catch(LinxDbException dbe)
	    	{
	    		if(dbe.getMessage().indexOf("The LIMS item does not exist in the database") >= 0)
    				throw new LinxDbException("The LIMS ID does not exist in the database.");
    			else if (dbe.getMessage().indexOf("More than one item exists with this name") >= 0)
    			{
    				task.setMessage("More than one item exists with this name.  Please choose an item type from the dropdown.");
	    			List<String> values = getItemTypes(task,db);
	    		    task.getDisplayItem("ItemType").setValues(values);
	    			task.getDisplayItem("ItemType").setVisible(true);
    			}
    			else
    				throw new LinxDbException(dbe.getMessage());
	    	}
	    	catch(LinxSystemException se)
	    	{
	    		if(se.getMessage().indexOf("The LIMS item does not exist in the database") >= 0)
    				throw new LinxDbException("The LIMS ID does not exist in the database.");
    			else if (se.getMessage().indexOf("More than one item exists with this name") >= 0)
    			{
    				task.setMessage("More than one item exists with this name.  Please choose an item type from the dropdown.");
	    			List<String> values = getItemTypes(task,db);
	    		    task.getDisplayItem("ItemType").setValues(values);
	    			task.getDisplayItem("ItemType").setVisible(true);
    			}
    			else
    				throw new LinxDbException(se.getMessage());
	    	}
	    	catch(Exception ex)
	    	{
	    		ex.printStackTrace();
	    		throw new LinxUserException(ex.getMessage());	
	    	}
	    	return FINISH_FOR_ME;
	    }
	    else
	    {
		    	 runQuery(request, task, db);
		    	 return FINISH_FOR_ME;
	    }
	  }
	
	private void runQuery(HttpServletRequest request, Task task, Db db)
	{
		try
		{
			RowsetView.cleanupSessionViews(request);
    		String sql = getSql(request, task, db);
	    	 // show UI table with results
    		view = getSQLRowsetView(request, sql, "ItemType", DATA_TABLE, 
		        	Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
    		
	        view.setName(DATA_TABLE);
	        view.setScrollSize("medium");  
	        view.setWidget(1,LinxConfig.WIDGET.LINK);
	        view.setStartRow(1);
	        task.getDisplayItem(DATA_TABLE).setVisible(true);
	        RowsetView.addViewToSessionViews(request, view);
		}
		catch(Exception ex)
		{
			if(ex.getMessage().indexOf("The LIMS item does not exist in the database") >= 0)
				throw new LinxDbException("The LIMS ID does not exist in the database.");
			else if (ex.getMessage().indexOf("More than one item exists with this name") >= 0)
			{
				task.setMessage("More than one item exists with this name.  Please choose an item type from the dropdown.");
    			List<String> values = getItemTypes(task,db);
    		    task.getDisplayItem("ItemType").setValues(values);
    			task.getDisplayItem("ItemType").setVisible(true);
			}
			else
				throw new LinxDbException(ex.getMessage());
		}
	}
	
	private List<String> getItemTypes(Task task, Db db)
	{
		ArrayList<String> vals = new ArrayList<String>();
		try
		{
			String item = task.getDisplayItemValue("LIMSID");
			vals = dbHelper.getListEntries("exec spMet_getItemTypes '" + item + "'", db);
		}
		catch(Exception e)
		{
			throw new LinxDbException(e.getMessage());
		}
		return vals;
	}
	
	private String getSql(HttpServletRequest request, Task task, Db db)
	 {
		 String sql = null;
		 try
		 {
			 String limsid = task.getDisplayItemValue("LIMSID");
			 if(WtUtils.isNullOrBlank(limsid))
				 throw new LinxUserException("Please enter a LIMS ID and try again.");
			 if(!limsid.equalsIgnoreCase(currItem))//we have a new query
			 {
				//lets set the value of the ItemType == null
				 task.getDisplayItem("ItemType").clearValues();
				 task.getDisplayItem("ItemType").setSelectedValue(null);
				 task.getServerItem("ItemType").clearValues();
				 task.getServerItem("ItemType").setValue(null);
				 //now set the new item to be the current item
				 currItem = limsid;
			 }
			 if(WtUtils.isNullOrBlank(limsid))
				 throw new Exception("The LIMS ID cannot be blank.");
			 String type = task.getServerItemValue("ItemType");
			 if(WtUtils.isNullOrBlank(type))
			 {
				 List<String> lsTypes = this.getItemTypes(task, db);
				 
				 if(lsTypes.size() == 0)
				 {
					 type = null;
					 sql = "exec spMet_reportMasterHistory '" + limsid + "'," + type;
					 task.getDisplayItem("ItemType").setValues(new ArrayList<String>());
		    		 task.getDisplayItem("ItemType").setVisible(false);
				 }
				 else if(lsTypes.size() == 1)
				 {
					 type = (String)lsTypes.get(0);
					 sql = "exec spMet_reportMasterHistory '" + limsid + "','" + type +"'";
					 task.getDisplayItem("ItemType").setValues(new ArrayList<String>());
					 task.getDisplayItem("ItemType").setSelectedValue(type);
		    		 task.getDisplayItem("ItemType").setVisible(false);
				 }
				 else
				 {
		    		 task.getDisplayItem("ItemType").setValues(lsTypes);
		    		 task.getDisplayItem("ItemType").setVisible(true);
		    		 throw new Exception("More than one item exists with this name.  Please choose an item type from the dropdown.");
				 }
			 }
			 else
				 sql = "exec spMet_reportMasterHistory '" + limsid + "','" + type +"'";
			 task.setMessage("Showing query results for item: " + limsid);
		 }
		 catch(Exception ex)
		 {
			throw new LinxDbException(ex.getMessage()); 
		 }
		
		 return sql;
	 }
	
	private String getItemSQL(String limsid, String inputtype, String returntype)
	{
		String sql = null;
		try
		{
			sql = "exec spEMRE_reportMasterItemDetail '" + limsid + "','" 
			+ inputtype + "','" + returntype + "'";
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return sql;
	}
	private String getAnalyticalSQL(String limsid, String type)
	{
		String sql = null;
		try
		{
			sql = "exec spMet_reportMasterAnalyticalData '" + limsid + "','" + type + "'";
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return sql;
	}

}
