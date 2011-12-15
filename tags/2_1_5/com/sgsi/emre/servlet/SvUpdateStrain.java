package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.StrainPOIBulkImporter;
import com.sgsi.emre.task.UpdateStrain;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvUpdateStrain
 *
 * Overridden to handle custom actions to retrieve
 * past uploaded data or to print a label for a 
 * saved strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2008
 * @modified 4/2011 TJS/Wt -- switched parsers 
 */
public class SvUpdateStrain extends SvStrainCollection
{
	
	/**
	 * Overrides custom action handler for Import via bulk import file.
	 * Calls superclass for remaining custom actions.
	 * 
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 * @return ALL_DONE
	 */
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		
		if (request.getAttribute("ImportButton") != null)
		{
			// clear out old values
			setDisplayItemValues(null/* no strain */, (UpdateStrain) task, user,
					request, db);
			task.getDisplayItem(ItemType.STRAIN).clearValues();
			task.getServerItem(ItemType.STRAIN).clearValues();
			task.createAnyNewAppFiles(request, response, user, db);
			String fileId = task.getServerItemValue(FileType.STRAIN_IMPORT_FILE);
			if (WtUtils.isNullOrBlank(fileId))
			{
				throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
			}
			importRowsFromFile(fileId, task, user, db, request, response);
			task.recordTaskHistory(request, response, getWorkflow(), user, db);
			task.createAnyNewData(request, response, user, db);
			task.dequeueItems(request, response, user, db);
			task.queueItems(request, response, user, db);
			task.recordItemHistory(request, response, user, db);
			commitDb(db);
			task.setMessage("Successfully imported edited properties for " +
					task.getServerItemValues(ItemType.STRAIN).size() +
				  " strains.");
			return FINISH_FOR_ME;
		}
		return super.handleCustomAction(task, user, db, request, response);
	}
	
	/**
	 * Overridden to use new POI-based parser instead
	 * of LIMSImporter2. Eff EMRE v2.1.
	 */
	 protected void importRowsFromFile(String fileId, Task task,
				User user, Db db, HttpServletRequest request,
				HttpServletResponse response)
	 {
			// open and read an xls worksheet (user's bulk import file)
		  File file = this.getFile(fileId, db);
			HSSFWorkbook wb = null;
			try
			{
				POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
				wb = new HSSFWorkbook(fs);
			}
			catch (FileNotFoundException ex)
			{
				throw new LinxSystemException("While opening Excel workbook: " + ex.getMessage());
			}
			catch (IOException ex)
			{
				throw new LinxSystemException("While opening Excel workbook: " + ex.getMessage());
			}
			StrainPOIBulkImporter parser = new StrainPOIBulkImporter();
			parser.updateDataValues(wb, (UpdateStrain)task, dbHelper,db);
	 }
  
  
  
  	/**
  	 * Overridden only to re-draw Locations UI table on error.
  	 * @param task
  	 * @param user
  	 * @param db
  	 * @param request
  	 * @param response
  	 */
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		String strain = task.getServerItemValue(ItemType.STRAIN);
		//try
		//{
			super.save(task, user, db, request, response);
		//}
		//catch(Exception ex)
		//{
			// redraw Locations table
		//	preprocessTask(request, task, user, db);
		//	throw new LinxUserException(ex);
		//}
			// save clears values, so reset strain ID
			task.getServerItem(ItemType.STRAIN).setValue(strain);
			findStrainDataByStrain(request, (UpdateStrain)task, user, db);
			task.setMessage("Successfully updated data for Strain " 
					+ task.getDisplayItemValue(ItemType.STRAIN));

	}
	
	

	/**
	 * Returns the name of stored proc to use
	 * to retrieve this type of strain. 
	 * @return name of sp to get strain report for this type of strain
	 */
    protected String getStrainReportSQL()
	{
		return "exec spMet_GetStrainCollection";
	}

  
  /**
	 * Sets properties for this strain in the UI widgets, if this is an existing
	 * strain.
	 * 
	 * @param strain
	 * @param db
	 */
	protected void setDisplayItemValues(String strain, DefaultTask task, User user, HttpServletRequest request,
			Db db)
	{
		task.cleanupTask(request, getWorkflow());
		task.getDisplayItem(DataType.COMMENT).setValue("");
		
		RowsetView.cleanupSessionViews(request);
		initLocationsView(db);
		RowsetView.addViewToSessionViews(request, locationsView);
		if (WtUtils.isNullOrBlank(strain))
		{
			// we are importing a file or defining a new single strain, 
			// so just clear values and exit
			task.getDisplayItem(ItemType.STRAIN).setValue("");
			return;
		}
		// in case new Project has been created
		task.populateSQLValues(user, "(Select)", db);
		// set values for the search item
		ArrayList params = new ArrayList();
		params.add(strain);

		try
		{
			// retrieves newest comments first
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
					"spMet_GetStrainProperties", params, true);
			while (rs.next())
			{
				task.getDisplayItem("StrainName").setValue(rs.getString(1));
				task.getDisplayItem(ItemType.PROJECT).setSelectedValue(
						rs.getString(2));
				task.getDisplayItem(DataType.NOTEBOOK_REF).setValue(
						rs.getString(3));
				task.getDisplayItem("OldComment").setValue(rs.getString(4));
				task.getDisplayItem(ItemType.STRAIN).setValue(rs.getString(5));
				task.getDisplayItem("Genus").setValue(rs.getString(6));
				task.getDisplayItem("Species").setValue(rs.getString(7));
			}// expecting only one row, but updated comments can
				// complicate things
			while(rs.next())
			{
				rs.getString(1);
				rs.getString(2);
				rs.getString(3);
				rs.getString(4);
				rs.getString(5);
				rs.getString(6);
				rs.getString(7);
			}
			rs.close();
			rs = null;
			// at exit, have set UI properties for this strain, if known

			
			// populate a rowset with strain locations 
			populateLocationsView(strain, request, task, db);
			
			// populate a rowset table with any existing data file paths
			//task.getDisplayItem(FILE_TABLE).setVisible(true);
			dataFilesView = populateFilesView(strain, request, db);
			if (dataFilesView.getRowcount() < 1)
			{
				task.getDisplayItem(FILE_TABLE).setVisible(false);
				task.setMessage("No data files have been imported for strain "
						+ strain);
			}
			else
			{
				RowsetView.addViewToSessionViews(request, dataFilesView);
			}
		}
		catch (SQLException e)
		{
			throw new LinxSystemException("While retrieving strain properties: "
					+ e.getMessage());
		}
	}
  
 
	  /**
	 * Number of rows to show in the Location UI table
	 * on initial display, for defining new strain.
	 * @return 4 for Brown Lab
	 */
  protected int getLocationRowCount()
  {
	  return 4;
  }

}
