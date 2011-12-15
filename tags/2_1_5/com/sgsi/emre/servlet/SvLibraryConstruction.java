package com.sgsi.emre.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;
import com.sgsi.emre.task.StrainCollection;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvLibraryConstruction extends EMREServlet 
{
	protected String LOCATION_TABLE = "Location";
	protected RowsetView locationsView = null;
	protected int rowCount = 2;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	  {
	    task.setMessage("");
	    
	    // display a clean location rowset UI table 
	    // -- data file rowset is hidden until a strain is selected
	    String sample = task.getServerItemValue(ItemType.LIBRARY);
	    if(WtUtils.isNullOrBlank(sample))
	    {
	    	populateLocation(request, task, db);
	    }
	    else if(!db.getHelper().isItemExisting(sample, ItemType.LIBRARY, db))
	    {
	    	// in case of error, 
	    	populateLocationsView(locationsView, COL_PRINT, COL_LOC, getRowCount());
	    }
	    else
	    {
	    	// after an update, show the new location(s)
	        populateLocationsView(sample, request, task, db);
	    }
	  }
	
	@Override
	  protected boolean handleCustomAction(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
	  {
		if (request.getParameter("PrintLabel") != null)
		{
	    	String sample = task.getServerItemValue(ItemType.LIBRARY);
			if(WtUtils.isNullOrBlankOrPlaceholder(sample))
			{
				throw new LinxUserException("Please enter a value for New LIMS ID.");
			}
			else if( !db.getHelper().isItemExisting(sample, ItemType.LIBRARY, db))
			{
				throw new LinxUserException("Library " + sample + " does not exist in this LIMS database."
					 + " Please check the entry, then try again.");
			}
			String spName = "spMet_GetLibraryLocations";
	    	((EMRETask)task).printLibraryLabels(request, sample, spName, db);
	    	
			return FINISH_FOR_ME;
		}
		else if(request.getParameter("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
			writeToExcel(request, response, "exec spMet_reportLibraryProduction", db);

			return ALL_DONE;
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	
	 protected void save(Task task, User user, Db db,
				HttpServletRequest request, HttpServletResponse response)
		{
			try
			{
				String lib = task.getServerItemValue(ItemType.LIBRARY);
				super.save(task, user, db, request, response);
				task.setMessage("Successfully saved new library:" + lib +
						"  Click 'Print Labels' to print or click on task name to autogenerate new IDs." );
			}
			catch(Exception ex)
			{
				// redraw Locations table
				populateLocation(request, task, db);
				throw new LinxUserException(ex);
			}
		}
	 
	 protected String getNextLibrary(Db db)
		{
			String s = null;
			try
			{
				s = dbHelper.getDbValue("exec spMet_getNextLibrary ", db);
			}
			catch(Exception ex)
			{
				throw new LinxDbException(ex.getMessage());
			}
			return s;
		}
	
	 protected void populateLocation(HttpServletRequest request, Task task, Db db)
	 {
		 String limsId = getNextLibrary(db);
		  if(limsId == null)
				  throw new LinxSystemException("There is no Library returned from the database.");
		  task.getDisplayItem(ItemType.LIBRARY).setValue(limsId);
		  //lets populate the location for this new sample
		  String sql = "exec spMet_GetSampleLocationsChecked '" + null + "'";
		  ArrayList<String> alLocations = new ArrayList<String>();
		  String locSP = "spMet_getCurrentLibraryBoxAndPosition";
		  alLocations = reserveFreezerLocations(locSP, this.getBoxPrefix(db),db);
		  ArrayList<String> alFrz = reserveFreezerLocations(locSP, this.getBackupBoxPrefix(db),db);
			for(String s : alFrz)
				alLocations.add(s);
		   	locationsView = super.populateFreezerLocationsView(locationsView, alLocations, 
					2, LOCATION_TABLE, 
						COL_PRINT, COL_LOC, sql, "Library", request, task, db);
		   	locationsView.setStartRow(1);
			task.getDisplayItem(LOCATION_TABLE).setVisible(true);
	  		RowsetView.addViewToSessionViews(request, locationsView);
	 }
	 
	 protected void populateLocationsView(String strain, HttpServletRequest request, Task task, Db db)
		{
		    // show UI table of locations for selected strain
	  		RowsetView.cleanupSessionViews(request);
	  		task.getDisplayItem(ItemType.LIBRARY).setValue(strain);
		    String sql = "exec spMet_GetSampleLocationsChecked '" + strain + "'";
		   	locationsView = this.getSQLRowsetView(request, sql, "Print", LOCATION_TABLE, 9, db);
		   	ArrayList<String> alLocations = null;
		   	if(locationsView.getRowcount() < getRowCount())
		   	{
		   		alLocations = ((StrainCollection)task).reserveFreezerLocations(strain, db);
		   		locationsView = this.populateLocationsView(alLocations, locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE, db);
		   	}
		   	else
		   		locationsView = populateLocationsView(locationsView, COL_PRINT, COL_LOC, getRowCount());	
		    locationsView.setMessage("Showing freezer locations for " + strain);
		    
	  		RowsetView.addViewToSessionViews(request, locationsView);

		 }
	 
	/**
	* Returns the correct prefix for the standard archive box.
	* @param taskName
	* @return
	*/
	protected String getBoxPrefix(Db db)
	{
		String prefix = dbHelper.getApplicationValue(db, "Library Construction Box Prefix", "Library Box");
		return prefix;
	}
		
	/**
	* Returns correct prefix for boxes storing backup archives,
	* @return
	*/
	protected String getBackupBoxPrefix(Db db)
	{
		String prefix = dbHelper.getApplicationValue(db, "Library Construction Box Prefix", "Library BackupBox");
		return prefix;
	}
	 
	
	 
	 private int getRowCount()
	 {
		 return rowCount;
	 }
}
