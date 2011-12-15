package com.sgsi.emre.servlet;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;


public class SvUpdateSamplingTimepoints extends EMREServlet 
{
	private String TIMEPOINT_TABLE = "Timepoints";
	protected RowsetView timepointView = null;
	private int COLUMN_TIMEPOINT = 1;
	
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			
			if(request.getAttribute("GetTimepoints")!= null)
		    {
				int numTimepoints = -1;
				try
				{
					numTimepoints = Integer.parseInt(task.getDisplayItemValue("NumberTimepoints"));
				}
				catch(Exception ex)
				{
					throw new LinxUserException("Please enter an integer value for 'Number of Timepoints'.");
				}
				timepointView = getSQLRowsetView(request, response, task, 
						getRowsetSQL(task, numTimepoints, "Timepoint"), 
						"Timepoint", TIMEPOINT_TABLE , numTimepoints, db);
				timepointView = configureTimepointView(timepointView, numTimepoints);
		    	task.getDisplayItem("Timepoints").setVisible(true);
		    	RowsetView.addViewToSessionViews(request, timepointView);
		        return FINISH_FOR_ME;    	
			}
			else if (request.getAttribute("ImportButton") != null)
			{
				task.createAnyNewAppFiles(request, response, user, db);
				String fileId = task.getServerItemValue(FileType.SAMPLING_TIMEPOINT_IMPORT_FILE);
				if (WtUtils.isNullOrBlank(fileId))
				{
					throw new LinxUserException(
						"Please browse for a bulk import file, then try again.");
				}
			
				importRowsFromFile(fileId, task, user, db, request,response);

		    	commitDb(db);
		        return FINISH_FOR_ME;    	
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			if(ex instanceof LinxUserException)
			{
				throw new LinxUserException(ex.getMessage());
			}
			else if(ex instanceof LinxDbException)
			{
				throw new LinxDbException(ex.getMessage());
			}
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	/**
	 * loops through the rows of a file and calls save per row
	 * @param fileId
	 * @param task
	 * @param user
	 * @param db
	 * @param request
	 * @param response
	 */
	protected void importRowsFromFile(String fileId, Task task,
			User user, Db db, HttpServletRequest request,
			HttpServletResponse response)
	{
		XLSParser fileData = null;
		// import file
		// core has already validated file per task def
		File inFile = this.getFile(fileId, db);
		
		// create a list of objects while importing manifest
		//lets get the data from the file and put it into a data container object
		char delim = EMREStrings.CHAR.COMMA;//the data delimiter for the data container
		String columnKey = "Culture ID"; //the unique identifier that lets me know i've reach the column data in the file
		try
		{
			fileData = new XLSParser(inFile, task.getTaskName(),
					delim, columnKey, EMREStrings.GrowthRecovery.requiredUpdateTimepointsColumnHeaders,
					true);
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Error occurred during file parsing: " + ex.getMessage());
		}
		

		// ready for std save() processing
		task.setMessage("Successfully imported new timepoints from bulk import file.");
		
		
		//loop though the file and insert each line
		String err = "";
		int row = 1;
		//check for duplicate timpoints
		try
		{
			//check for dups
			List<String> lsDups = new ArrayList<String>();
			if(fileData.gotoFirst())
			{
				do
				{
					String culture = fileData.getRequiredProperty("Culture ID");
					String tp = fileData.getRequiredProperty("Sampling Timepoint");
					if(!lsDups.contains(tp))
						lsDups.add(tp);
					else
						throw new LinxUserException("Duplicate timepoints found for culture '" + culture + "'");
				}
				while(fileData.gotoNext());
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Error validating file: " + ex.getMessage());
		}
		try
		{
			if(fileData.gotoFirst())
			{
				do
				{			
					String limsId = fileData.getRequiredProperty("Culture ID");
					String timepoint = fileData.getRequiredProperty("Sampling Timepoint");
					//do we have an ExperimentalCulture or a StrainCulture?
					String itemType = null;
					try
					{
						ResultSet rs = dbHelper.getItemType(limsId, db);
						while(rs.next())//expecting only one
						{
							itemType = rs.getString(1);
							if(itemType.toLowerCase().startsWith("ex"))
								itemType = ItemType.EXPERIMENTALCULTURE;
							else
								itemType = ItemType.STRAINCULTURE;
							break;
						}
						rs.close();
						rs = null;
					}
					catch(Exception ex)
					{
						throw new LinxDbException(ex.getMessage());
					}
					if(WtUtils.isNullOrBlank(itemType))
						throw new LinxUserException("Culture ID '" + limsId + " does not exist in LIMS.");
					if(itemType.equalsIgnoreCase(ItemType.EXPERIMENTALCULTURE))
						task.getServerItem(ItemType.EXPERIMENTALCULTURE).setValue(limsId);
					else
						task.getServerItem(ItemType.STRAINCULTURE).setValue(limsId);
					
					task.getServerItem("SamplingTimepoint").setValue(timepoint);
					
					// call std processing
					save(task, user, db, request, response);
					row++;
				}
				while(fileData.gotoNext());
			}//at end we have saved each row of the file
			
		}
		catch (Exception e)
		{
			err += "Error occurred while parsing row " + row + ": " + e.getMessage();
		}
		if(!err.equals(""))
			throw new LinxUserException(err);
	}
	
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		ArrayList<String> alTp = new ArrayList<String>();
		try
		{
			//do we have a file?
			String fileId = task.getServerItemValue(FileType.SAMPLING_TIMEPOINT_IMPORT_FILE);
			if(WtUtils.isNullOrBlank(fileId))
			{
				//get the data in the timepoints table for repopulating later
				TableDataMap rowMap = new TableDataMap(request, TIMEPOINT_TABLE);
			    int numRows = rowMap.getRowcount();
			    Item tps = task.getServerItem("SamplingTimepoint");
			    for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
				{
			    	String timepoint = (String)rowMap.getValue(rowIdx, COLUMN_TIMEPOINT);
			    	if(!WtUtils.isNullOrBlank(timepoint))
			    	{
			    		timepoint.trim();
			    		if(!WtUtils.isNullOrBlank(timepoint))
			    		{
			    			alTp.add(timepoint);
			    		}
			    		else
			    			throw new LinxUserException("Timepoints cannot be null.");
			    	}	
				}
			    tps.setValues(alTp);
			}
			
			super.save(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			//reset the timepoints table
			try
			{
				timepointView = configureTimepointView(alTp, db, request);
			}
			catch(Exception e)
			{
				//Houston - we have a problem
				//can't repopulate the timepoints - ignore
			}
			throw new LinxUserException("Error occurred during save: " + ex.getMessage());
		}
	}
	
	private String getRowsetSQL(Task task, int numTimepoints, String colName)
	{
		return "exec spEMRE_getSQLRowsetView " + numTimepoints + ",'" + colName + "'";
	}
		
	protected RowsetView configureTimepointView(RowsetView view, int numRows)
	{
		view.setStartRow(1);
		view.setHideNavControls(true); 
		view.setRowcount(numRows);
		view.setWidget(1,LinxConfig.WIDGET.DATETIMEPICKER);	
		view.getColumn(0).setDateFormat("yyyy-mm-dd h:MM:ss TT");
		return view;
	}
	
	protected RowsetView configureTimepointView(ArrayList<String> alRows, Db db, HttpServletRequest request)
	{
		
		String[] colHeaders = {"Timepoint"};
		char delim = ',';
		File file = writeSingleColumnToFile(alRows, colHeaders, delim, db);
	    
		timepointView = this.getFileRowsetView(file, "Timepoint", 
	    	     TIMEPOINT_TABLE, null, colHeaders, alRows.size() );
		timepointView.setStartRow(1);
		timepointView.setHideNavControls(true); 
		timepointView.setRowcount(alRows.size());
		timepointView.setWidget(1,LinxConfig.WIDGET.DATETIMEPICKER);	
		timepointView.getColumn(0).setDateFormat("yyyy-mm-dd h:MM:ss TT");
		
		RowsetView.addViewToSessionViews(request, timepointView);
		return timepointView;
	}
	
	
}
