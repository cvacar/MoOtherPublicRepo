package com.sgsi.emre.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvDNARNAPreparation extends EMREServlet 
{
	protected String LOCATION_TABLE = "Location";
	protected RowsetView locationsView = null;
	protected TableDataMap locsRowMap = null;
	int COL_PRINT = 1;
	int COL_LOC = 2;
	protected int rowCount = 2;
	@Override
	protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
	  {
		task.getDisplayItem("OriginLIMSID").setScroll(false);
		
	    //autogenerate the lims id
	    String sample = task.getDisplayItemValue("LIMSID");
	    String sampleType = task.getDisplayItemValue("SampleType");
	    if(WtUtils.isNullOrBlank(sample))
	    {
	    	populateUI(request, task, db);
	    	reserveLocations(request, task, db);
	    }
	    else if(!db.getHelper().isItemExisting(sample, sampleType, db))
	    {
	    	// in case of error, 
	    	populateLocationsView(locationsView, COL_PRINT, COL_LOC, rowCount);
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
		if (request.getAttribute("PrintLabel") != null)
		{
	    	String sample = task.getServerItemValue("LIMSID");
			if(WtUtils.isNullOrBlankOrPlaceholder(sample))
			{
				throw new LinxUserException("Please enter a value for LIMS ID.");
			}
			else if( !db.getHelper().isItemExisting(sample, ItemType.DNA, db))
			{
				//lets check for RNA
				if(!db.getHelper().isItemExisting(sample, ItemType.RNA, db))
				throw new LinxUserException("Sample " + sample + " does not exist in this LIMS database."
					 + " Please check the entry, then try again.");
			}
			String spName = "spEMRE_getNucleicAcidLocations";
	    	((EMRETask)task).printDNALabels(request, sample, spName, db);
			return FINISH_FOR_ME;
		}
	    else if(request.getAttribute("ExportButton") != null)
		{
			// user wants to download the entire strain collection to Excel
			writeToExcel(request, response, "exec spEMRE_reportDNAPrep", db);

			return ALL_DONE;
		}
	    return super.handleCustomAction(task, user, db, request, response);
	}
	
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			
			super.save(task, user, db, request, response);
			//populate the ui with the next sample
			populateUI(request, task, db);
		}
		catch(Exception ex)
		{
			// redraw Locations table
			String limsId = task.getServerItemValue("LIMSID");
			task.getDisplayItem("LIMSID").setValue(limsId);
			reserveLocations(request, task, db);
		}
	}
	
	 protected void populateUI(HttpServletRequest request, Task task, Db db)
	 {
		 RowsetView.cleanupSessionViews(request);
		 String limsId = getNextNucleicAcid(db);
		  if(limsId == null)
				  throw new LinxSystemException("There is no DNA or RNA returned from the database.");
		  task.getDisplayItem("LIMSID").setValue(limsId);
		  
	 }
	 
	 /**
	  * reserves freezer locations for nucleic acids.
	  * @param request
	  * @param task
	  * @param db
	  */
	 protected void reserveLocations(HttpServletRequest request, Task task, Db db)
	 {
		 try
		 {
			  String freezerLocSP = "spEMRE_getCurrentDNABoxAndPosition";
		   	  ArrayList<String> alFrz = reserveFreezerLocations(freezerLocSP, this.getBoxPrefix(db),db);
			  locationsView = super.populateLocationsView(alFrz, 
					  locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE, db);
			  task.getDisplayItem(LOCATION_TABLE).setVisible(true);
			  RowsetView.addViewToSessionViews(request, locationsView);
		 }
		 catch(Exception ex)
		 {
			 throw new LinxDbException(ex.getMessage());
		 }
	 }
	 
	 protected void populateLocationsView(String sample, HttpServletRequest request, 
			 Task task, Db db)
		{
		    // show UI table of locations for selected sample
	  		RowsetView.cleanupSessionViews(request);
	  		task.getDisplayItem("LIMSID").setValue(sample);
		    //lets also populate the locations for this new sample
			String sql = "exec spEMRE_getNucleicAcidLocationsChecked '" + sample + "'";
			ArrayList<String> alLocations = new ArrayList<String>();
			locationsView = super.populateLocationsView(alLocations, 
					  locationsView, COL_PRINT, COL_LOC, LOCATION_TABLE, db);
			locationsView.setMessage("Showing freezer locations for " + sample);
		   	RowsetView.addViewToSessionViews(request, locationsView);
		 }
	 
	 protected String getNextNucleicAcid(Db db)
		{
			String s = null;
			try
			{
				s = dbHelper.getDbValue("exec spMet_getNextNucleicAcid ", db);
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
			String prefix = dbHelper.getApplicationValue(db, "DNA Prep Box Prefix", 
					"Box");
			return prefix;
		}
}
