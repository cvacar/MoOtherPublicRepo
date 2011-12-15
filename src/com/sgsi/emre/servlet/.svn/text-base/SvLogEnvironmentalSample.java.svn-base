package com.sgsi.emre.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.view.RowsetView;

/**
 * Handles task Environmental Aquatic Sample Logging
 * in EMRE LIMS v2.2 by accepting bulk import file.
 * @author TJS/Wildtype for SGI
 * @modified 6/2011 by TJS/Wt under SOW 004
 * 	-- eliminated Print Label custom action handler (lab uses Print Labels task)
 *
 */
public class SvLogEnvironmentalSample extends EMREServlet 
{
	
	protected String LOCATION_TABLE = "Location";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	protected int rowCount = 4;
	
	/** 
	   * Overridden to forward to correct task screen
	   * depending on Sample Type selected by user
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
	      // throws error if task is misnamed or not yet supported
		  String sampleType = task.getDisplayItemValue("SampleType");
	      
		  task = this.getTaskObject("Environmental " + sampleType + " Logging");

	      // -- replace cached instance
	      task.populateSQLValues(user, db);
	      request.getSession().setAttribute(Strings.TASK.TASK, task);
	      request.setAttribute(Strings.TASK.TASK_NAME, task.getTaskName());
	      
	      //we're drilled down into a specific kind of sample
		  //lets get the next available LIMS ID
		  String limsId = getNextEnvironmentalSample(db);
		  if(limsId == null)
				  throw new LinxSystemException("There is no sample returned from the database.");
		  task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(limsId);
		  //lets also populate the locations for this new sample
		  populateUI(request, task, db);
	      // show the screen for the correct sample logging task
	      String taskPg = task.getTaskPg(getServletContext());
	      request.getSession().setAttribute(Strings.TASK.TASK_PAGE, taskPg);
	      this.forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, " ");
	      return ALL_DONE;
	      
	    }
	    /** NOT IN USE -- lab uses Print Labels task exclusively per Jay 7/2011**
	    else if (request.getParameter("PrintLabel") != null)
		{
	    	String sample = task.getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
			if(WtUtils.isNullOrBlankOrPlaceholder(sample))
			{
				throw new LinxUserException("Please enter a value for New LIMS ID.");
			}
			else if( !db.getHelper().isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
			{
				throw new LinxUserException("Sample " + sample + " does not exist in this LIMS database."
					 + " Please check the entry (save first), then try again.");
			}
			String spName = "spMet_GetSampleLocations";
			
	    	((EMRETask)task).printSampleLabels(request, sample, spName, db);
			return FINISH_FOR_ME;
		}
		**/
	    return super.handleCustomAction(task, user, db, request, response);
	}
	  
	  /**
	   * Populates UI widgets with next available LIMS ID 
	   * and next available Locations. Calls db.
	   * @param request
	   * @param task
	   * @param db
	   */
	  protected void populateUI(HttpServletRequest request, Task task, Db db)
	  {
		  String limsId = getNextEnvironmentalSample(db);
		  if(limsId == null)
				  throw new LinxSystemException("No next sample ID was retrieved from the database.");
		  task.getDisplayItem(ItemType.ENVIRONMENTAL_SAMPLE).setValue(limsId);
		  //lets also populate the locations for this new sample
		  String sql = "exec spMet_GetSampleLocationsChecked '" + limsId + "'";
		  ArrayList<String> alLocations = new ArrayList<String>();
		  String freezerLocSP = "spMet_getCurrentSampleBoxAndPosition";
		  alLocations = reserveFreezerLocations(freezerLocSP, this.getBoxPrefix(db),db);
	   	  ArrayList<String> alFrz = reserveFreezerLocations(freezerLocSP, this.getBackupBoxPrefix(db),db);
	   	  for(String s : alFrz)
	   		  alLocations.add(s);
		  locationsView = super.populateFreezerLocationsView(locationsView, alLocations, rowCount, LOCATION_TABLE, 
					COL_PRINT, COL_LOC, sql, limsId, request, task, db);
		  locationsView.setStartRow(1);
		  
		  try
		{
			task.getDisplayItem(LOCATION_TABLE).setVisible(true);
		}
		catch (Exception e)
		{
			// ignore for Subsurface Sample Logging
		}
		  RowsetView.addViewToSessionViews(request, locationsView);
	  }
	  
	 
	 
	protected String getNextEnvironmentalSample(Db db)
	{
		String s = null;
		try
		{
			s = dbHelper.getDbValue("exec spMet_getNextEnvironmentalSample ", db);
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return s;
	}
	
	/**
	* Returns the correct prefix for the standard archive box.
	* @param taskName
	* @return
	*/
	protected String getBoxPrefix(Db db)
	{
		String prefix = dbHelper.getApplicationValue(db, "Environmental Sample Logging Box Prefix", 
				"Box");
		return prefix;
	}
		
	/**
	* Returns correct prefix for boxes storing backup archives,
	* @return
	*/
	protected String getBackupBoxPrefix(Db db)
	{
		String prefix = dbHelper.getApplicationValue(db, "Environmental Sample Logging Box Prefix", 
				"BackupBox");
		return prefix;
	}
	  
	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	  
}
