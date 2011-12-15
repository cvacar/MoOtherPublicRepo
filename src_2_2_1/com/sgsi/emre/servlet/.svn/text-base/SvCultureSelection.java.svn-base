package com.sgsi.emre.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.CultureCollection;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvCultureSelection extends EMREServlet 
{
	String IMPORTS_TABLE = "Imports"; // for displaying imported files
	CultureCollection thisTask = new CultureCollection();
	/** 
	   * Overridden to forward to correct task screen
	   * depending on Culture Type selected by user
	   * from dropdown.
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
		    String whichType = task.getDisplayItemValue("CultureType");
		    if(WtUtils.isNullOrBlankOrPlaceholder(whichType))
		    {
		    	throw new LinxUserException("Please select the culture type, then try again.");
		    }
		    isFirstRequest = true;
	      // show the screen for the correct culture collection task
	      // -- throws error if task is misnamed or not yet supported
	      task = this.getTaskObject(whichType + " Collection");
	      task.populateSQLValues(user, db);
	      request.getSession().setAttribute(Strings.TASK.TASK, task);
	      request.setAttribute(Strings.TASK.TASK_NAME, task.getTaskName());
	      String taskPg = task.getTaskPg(getServletContext());
	      request.getSession().setAttribute(Strings.TASK.TASK_PAGE, taskPg);
	      
	      this.forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, " ");
	      return ALL_DONE;
	    }
	    else if (request.getAttribute("ExportButton") != null)
			{
	    	isFirstRequest = true;
				// user wants to download the entire culture collection to Excel
	    	String sql = getCultureCollectionSQL();
	    	String param = task.getDisplayItemValue("SearchString");
	    	if(!WtUtils.isNullOrBlank(param))
	    	{
	    		sql = "exec " + sql + " '" + param + "'";
	    	}
	      writeToExcel(request, response, sql, db);
				return ALL_DONE;
			}
			else if (request.getAttribute("NextIDButton") != null)
			{
		    isFirstRequest = true;
				setNextId(task, db);
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("ImportButton") != null)
			{
				// note: Strain Culture Collection task screen uses 'Save',
				// so behavior is in task class
				isFirstRequest = true;
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.CULTURE_COLLECTION_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException("Please browse for a bulk import file, then try again.");
				}
				importRowsFromFile(fileId, task, user, db, request,response);
		    commitDb(db);
		    return FINISH_FOR_ME;    	
			}
			else if(request.getAttribute("DataUploadButton") != null)
			{
		    isFirstRequest = true;

		    try
				{
					processShareDataFiles(ItemType.STRAINCULTURE, (EMRETask)task, user, db);
				}
				catch (IOException ex)
				{
					throw new LinxUserException(ex);
				}
		    String itemList = task.getMessage().getMessage();
				task.setMessage("Successfully added data files for the following "
						+ " strain culture(s): " + itemList);
				return FINISH_FOR_ME;
			}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	  
	
	  /**
	   * Overridden by subclasses to parse Excel bulk import files
	   * and create or edit cultures in database. Eff v2.1, screen def
	   * and editing of new cultures is no longer supported. 
	   * @param fileId
	   * @param task
	   * @param user
	   * @param db
	   * @param request
	   * @param response
	   */
	  protected void importRowsFromFile(String fileId, Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		// overridden by subclasses SvStrainCultureCollection
	  	// and SvExperimentalCultureCollection
		
	}

		/**
	   * Populates the UI widget 'Next ID' with the
	   * next available serial number of the culture id
	   * for the given culture type, strain and start date,
	   * e.g. EX-WT-SGI-E-02305-001. Calls getNextCultureId().
	   * @param task
	   * @param db
	   */
	  protected void setNextId(Task task, Db db)
	  {
			// generate next culture id
			// make sure we have a strain first
			String strain = task.getDisplayItemValue(ItemType.STRAIN);
			if (WtUtils.isNullOrBlankOrPlaceholder(strain))
			{
				String label = task.getDisplayItem(ItemType.STRAIN).getLabel();
				throw new LinxUserException("Please enter a value for '" + label + "'.");
			}
			String dateStarted = task.getDisplayItemValue("DateStarted");
			if (WtUtils.isNullOrBlankOrPlaceholder(dateStarted))
			{
				String label = task.getDisplayItem("DateStarted").getLabel();
				throw new LinxUserException("Please enter a value for '" + label + "'");
			}
			String nextId = 
				getNextCultureId(strain, getCultureItemType(), dateStarted, db);
			task.getDisplayItem(getCultureItemType()).setValue(nextId);
	  }
	  
		/**
		 * Constructs the next culture id from the date started
		 * and other params, then checks for the last used serial
		 * number in existing cultures and returns + 1.
		 * @param strain
		 * @param cultureType Currently either 'StrainCulture' or 'ExperimentalCulture'
		 * @param dateStarted
		 * @param db
		 * @return nextId constructed from culture type, strain, and date started + serial number
		 */
		public String getNextCultureId(String strain, String cultureType, String dateStarted, Db db)
		{

			ArrayList<String> params = new ArrayList<String>();
			// convert four-digit strains to new five digit 'WT-SGI-E-0947'
			params.add(strain);
			params.add(cultureType);
			params.add(dateStarted);
			String nextId = null;
			try
			{
				synchronized(this)
				{
					nextId = 
						dbHelper.getDbValueFromStoredProc(db, "spEMRE_getNextCulture",params);
				}
			}
			catch(Exception ex)
			{
				throw new LinxDbException(
						"Error occured when trying to get the next culture id: " 
						+ ex.getMessage());
			}
			return nextId;
		}
		
		
		@Override
		protected boolean doTaskWorkOnGet(HttpServletRequest request,
				HttpServletResponse response, Task task, User user, Db db)
		{
			// has user selected an import from a list of imported files?
			if (request.getParameter("selCoord") != null
					&& request.getParameter("selCoord").startsWith(IMPORTS_TABLE)
					&& request.getParameter("selVal") != null)
			{
				// user has selected an import, so get the file back
				try
				{
					String fileId = request.getParameter("selVal");
					getFile(fileId, task, request, response, user, db);
					return FINISH_FOR_ME;
				}
				catch(Exception ex)
				{
					task.setMessage(ex.getMessage());
					throw new LinxUserException(ex.getMessage());
				}

			}
			else //if(isFirstRequest)
			{
				isFirstRequest = false;
		    task.setMessage("");
		    RowsetView.cleanupSessionViews(request);     
		    RowsetView.addViewToSessionViews(request, getImportsView(request, user, db));

				//task.setMessage("Successfully downloaded results.");
				forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
			}
			return ALL_DONE;

		}
		
		protected RowsetView getImportsView(HttpServletRequest request, User user, Db db)
		  {
		    // show UI table of pending requests
		    String sql = 
		    	"exec spEMRE_getCultureCollectionImportsByUser '" + user.getName() + "'";
		    RowsetView view = 
		    	getSQLRowsetView(request, sql, "File ID", IMPORTS_TABLE, 
		    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
		    view.setWidget(1,LinxConfig.WIDGET.LINK);
		    //view.setScroll(true);
		    //view.setScrollSize("small");
		    view.setStartRow(1);
		    view.setMessage("");
		    return view;
		  }
		
		
		/** 
		 * For subclasses to override.
		 * @return
		 */
		public String getCultureCollectionSQL()
		{
			return "override";
		}
		
		/**
		 * For subclasses to override.
		 * @return
		 */
		public String getCultureItemType()
		{
			return "override";
		}
}
