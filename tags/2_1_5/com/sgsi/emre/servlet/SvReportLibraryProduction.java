package com.sgsi.emre.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvReportLibraryProduction extends EMREServlet 
{

	protected RowsetView view = null;
	final String DATA_TABLE = "Results";

	
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
	     request.getSession().setAttribute(Strings.TASK.TASK_PAGE, task.getTaskPg(getServletContext()));
	    	
	     if( request.getParameter(Strings.WIDGET.ACTION.GO_TO_ROW) != null)
	     {
	     	handleGoToRowRequest(request, response);
	     	forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
	     	return(ALL_DONE);
	     }
	     else if( request.getParameter(Strings.WIDGET.ACTION.EXPORT) != null)
	     {
	     	return handleExportRequest(request, response, DATA_TABLE, "LibraryData_"+task.getTranId(db) +".csv");
	     	
	     }
	     else if (request.getParameter("selCoord") != null
					&& request.getParameter("selCoord").startsWith(DATA_TABLE)
					&& request.getParameter("selVal") != null)
	     {
	    	 //hyperlink to file selected - download the file
	    	 String f = (String)request.getParameter("selVal");
	    	 File file = new File(f);
	    	 this.returnDownloadAsByteStream(response, file, 
	 				"ProtocolFileDownload.doc", "application/vnd.ms-word", false);
	    	 return ALL_DONE;
	     }
		 return FINISH_FOR_ME;
	}
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
	
	    if(request.getParameter("Run")!= null)
	    {
	    	try
	    	{
	    		//build the reporting table
	    		RowsetView.cleanupSessionViews(request);
	        	String sampType = task.getDisplayItemValue("SampleType"); 
	            String limsId  = task.getDisplayItemValue("LIMSID"); 
	            String originId  = task.getDisplayItemValue("OriginLIMSID"); 
	            String vendor  = task.getDisplayItemValue("Vendor"); 
	            String library  = task.getDisplayItemValue("Library"); 
	            String size  = task.getDisplayItemValue("Size"); 
	            String vector  = task.getDisplayItemValue("Vector"); 
	            String vectorName  = task.getDisplayItemValue("VectorName"); 
	            String prepBy  = task.getDisplayItemValue("PreparedBy"); 
	            String linkers  = task.getDisplayItemValue("Linkers"); 
	            String notebook  = task.getDisplayItemValue("NotebookRef"); 
	            String dnaConc  = task.getDisplayItemValue("DNAConcentration"); 
	            String primSamp  = task.getDisplayItemValue("PrimarySample"); 
	            String insertSize  = task.getDisplayItemValue("QCdInsertSize"); 
	            String primer5  = task.getDisplayItemValue("QCPrimer5Prime"); 
	            String primer3  = task.getDisplayItemValue("QCPrimer3Prime"); 
	            String location  = task.getDisplayItemValue("Location"); 
	            if(WtUtils.isNullOrBlankOrPlaceholder(sampType))
	            	sampType = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(limsId))
	            	limsId = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(originId))
	            	originId = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(vendor))
	            	vendor = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(library))
	            	library = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(size))
	            	size = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(vector))
	            	vector = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(vectorName))
	            	vectorName = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(prepBy))
	            	prepBy = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(linkers))
	            	linkers = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(notebook))
	            	notebook = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(dnaConc))
	            	dnaConc = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(primSamp))
	            	primSamp = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(insertSize))
	            	insertSize = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(primer5))
	            	primer5 = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(primer3))
	            	primer3 = "";
	            if(WtUtils.isNullOrBlankOrPlaceholder(location))
	            	location = "";
	            
	            String sql = "exec spEMRE_reportLibraryProduction '";
	        	sql += sampType + "','";
	        	sql += limsId + "','";
	        	sql += originId + "','";
	        	sql += vendor + "','";
	        	sql += library + "','";
	        	sql += size + "','";
	        	sql += vector + "','";
	        	sql += vectorName + "','";
	        	sql += prepBy + "','";
	        	sql += linkers + "','";
	        	sql += notebook + "','";
	        	sql += dnaConc + "','";
	        	sql += primSamp + "','";
	        	sql += insertSize + "','";
	        	sql += primer5 + "','";
	        	sql += primer3 + "','";
	        	sql += location + "'";
	    		System.out.println(sql+Strings.CHAR.NEWLINE);

	          // show UI table with culture results
	          view = getSQLRowsetView(request, sql, "SampleType", DATA_TABLE, 
	        		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
	          view.setName(DATA_TABLE);
	          view.setWidget(18, LinxConfig.WIDGET.LINK);
	          task.setMessage("Showing library production data returned from query.");
	          task.getDisplayItem(DATA_TABLE).setVisible(true);
	          RowsetView.addViewToSessionViews(request, view);
	         
	          return FINISH_FOR_ME;
	    	}
	    	catch(Exception ex)
	    	{
	    		throw new LinxDbException(ex.getMessage());
	    	}
	    }
	    else
	    {
	    	return super.handleCustomAction(task, user, db, request, response);
	    }
	  }
}
