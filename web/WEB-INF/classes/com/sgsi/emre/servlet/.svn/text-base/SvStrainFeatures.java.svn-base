package com.sgsi.emre.servlet;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.StrainFeatures;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;

public class SvStrainFeatures extends EMREServlet 
{
	private String TABLE = "GeneAdditionData";
	protected RowsetView view = null;
	String fileHeaders = "Species Origin of Transgene,Gene Annotation,Vector Backbone,Promoter,Affinity Tag";
	String[] colHeaders = {"Species Origin of Transgene","Gene Annotation",
    		"Vector Backbone","Promoter","Affinity Tag"};
	String colKey = "Species Origin of Transgene";
    
	
	protected boolean doTaskWorkOnGet(HttpServletRequest request,
			HttpServletResponse response, Task task, User user, Db db)
	{
	  if(request.getParameter("strain") != null)
	  {
		  //we have a request from the strain features query page
		  String strain = request.getParameter("strain");
		  getStrainFeatures(strain, task, request, db);
	  }
	  else if(request.getParameter(ACTION.EXPORT) != null)
	  {
		  // user wants the list of saved files (not an individual file)
		  String strain = task.getServerItemValue(ItemType.STRAIN);
		  this.exportAllTablesOnScreen(request, response, strain + " data files.txt");
	  }
	  return FINISH_FOR_ME;
	}
	
	@Override
	protected boolean handleCustomAction(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		
		try
		{

			if (request.getAttribute("GetAdditions") != null)
			{
				// user wants to add gene additions to this strain
				//populate the rowset
				populateView(request, task, db);
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("GetMarkers") != null)
			{
				// user wants to add markers to a plasmid
				//populate the rowset
				populateMarkerView(request, task, db);
				ArrayList<String> alAdditions = getGeneAdditionsFromUI(request);
				populateView(alAdditions,request, task, db);
				return FINISH_FOR_ME;
			}
			else if (request.getAttribute("NextID") != null)
			{
				// user wants to generate a new plasmid id
				//get the plasmid type
				String plasmidType = task.getServerItemValue("PlasmidType");
				if(plasmidType.trim().equalsIgnoreCase("none"))
					throw new LinxUserException("Please select a plasmid type and then click 'Next ID' to generate a new plasmid ID.");
				else if(plasmidType.trim().equalsIgnoreCase("exists"))
				{
					//check to see if the plasmid exists
					String plasmid = task.getServerItemValue(ItemType.PLASMID);
					if(!WtUtils.isNullOrBlank(plasmid))
					{
						if(dbHelper.isItemExisting(plasmid, ItemType.PLASMID, db))
						{
							task.setMessage("The plasmid '" + plasmid + "' already exists in the database.");
						}
						else
							throw new LinxUserException("Please enter an existing plasmid in the 'Plasmid ID' field or select a 'Plasmid Type' other than 'exists'");
					}
					else
						throw new LinxUserException("The selected plasmid type is 'exists' but there is no plasmid ID in the 'Plasmid ID' field.");
				}
				else 
				{
					String nextPlasmid = ((StrainFeatures)task).getNextPlasmidId(plasmidType, dbHelper, db);
					task.getDisplayItem(ItemType.PLASMID).setValue(nextPlasmid);
				}
				ArrayList<String> alAdditions = getGeneAdditionsFromUI(request);
				populateView(alAdditions,request, task, db);
				return FINISH_FOR_ME;
			}
			else if(request.getAttribute("ExportButton") != null)
			{
				// user wants to download the entire strain collection to Excel
				writeToExcel(request, response, "exec spEMRE_reportStrainFeatures", db);

				return ALL_DONE;
			}
			else if (request.getAttribute("FindButton") != null)
			{
				String strain = task.getServerItemValue(ItemType.STRAIN);
				if(WtUtils.isNullOrBlank(strain))
				{
					throw new LinxUserException("Please enter a Strain ID, then try again.");
				}
				else if( !db.getHelper().isItemExisting(strain, ItemType.STRAIN, db))
				{
					throw new LinxUserException("Strain " + strain + " does not exist in this LIMS database."
						 + " Please check the entry, then try again.");
				}
				else if( db.getHelper().isItemOnQueue(strain, ItemType.STRAIN, "Discarded Strains",db))
				{
					throw new LinxUserException("Strain " + strain + " has been discarded and cannot be queried."
						 + " Please enter a new strain, then try again.");
				}
				// set this strain's values in UI widgets
				getStrainFeatures(strain, task, request, db);
				return FINISH_FOR_ME;
			}
			return super.handleCustomAction(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			if(ex instanceof LinxUserException)
			{
				throw (LinxUserException)ex;
			}
			else if(ex instanceof LinxDbException)
			{
				throw (LinxDbException)ex;
			}
			throw new LinxUserException(ex.getMessage());
		}
	}
	/**
  	 * Overridden only to re-draw Locations UI table on error.
  	 * @param task
  	 * @param user
  	 * @param db
  	 * @param request
  	 * @param response
  	 */
	@Override
	protected void save(Task task, User user, Db db,
			HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			super.save(task, user, db, request, response);
		}
		catch(Exception ex)
		{
			// redraw Locations table
			//do we have a table on the UI?
			try
			{
				persistRowSetData(request, db);
			}
			catch(Exception e)
			{
				//ignore
			}
			
			throw new LinxUserException(ex);
		}
	}
	
	private void getStrainFeatures(String strain, Task task, HttpServletRequest request, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(strain);
			//get the features
			ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
					"spEMRE_getStrainFeatures", params, true);
			while (rs.next())
			{
				task.getDisplayItem("Strain").setValue(rs.getString(1));
				task.getDisplayItem("OriginStrain").setValue(
						rs.getString(2));
				task.getDisplayItem("HostSpecies").setValue(
						rs.getString(3));
				task.getDisplayItem("GeneDeletion").setValue(rs.getString(4));
				task.getDisplayItem("Vendor").setValue(rs.getString(5));
				task.getDisplayItem("VectorMapFile").setValue(rs.getString(6));
				task.getDisplayItem("OldComment").setValue(rs.getString(7));
			}
			rs.close();
			rs = null;
			//now we need the gene additions
			rs = db.getHelper().getResultSetFromStoredProc(db,
					"spEMRE_getGeneAdditions", params, true);
			ArrayList<String> alData = new ArrayList<String>();
			String s = "";
			while (rs.next())
			{
				s = rs.getString(2) + Strings.CHAR.COMMA + rs.getString(3) 
				+ Strings.CHAR.COMMA + rs.getString(4) 
				+ Strings.CHAR.COMMA + rs.getString(5)
				+ Strings.CHAR.COMMA + rs.getString(6);
				alData.add(s);
				s = "";
			}
			rs.close();
			rs = null;
			task.getDisplayItem("Additions").setSelectedValue(String.valueOf(alData.size()));
			populateView(alData, request, task, db);
			//now we need the plasmid data - if any
			rs = db.getHelper().getResultSetFromStoredProc(db,
					"spEMRE_getPlasmidData", params, true);
			int rowNum = 1;
			ArrayList<String> alMarkers = new ArrayList<String>();
			while (rs.next())
			{
				if(rowNum == 1)
				{
					task.getDisplayItem("PlasmidType").setSelectedValue(
							rs.getString(2));
					task.getDisplayItem("Plasmid").setValue(
							rs.getString(3));
					alMarkers.add(rs.getString(4));
				}
				else
				{
					alMarkers.add(rs.getString(4));
				}
			}
			rs.close();
			rs = null;
			task.getDisplayItem("PlasmidMarkers").setSelectedValue(String.valueOf(alMarkers.size()));
			int numMarkers = alMarkers.size();
			if(alMarkers != null && numMarkers > 0 && alMarkers.get(0) != null)
			{
				task.getDisplayItem("PlasmidMarkerData").setValues(alMarkers);
				task.getDisplayItem("PlasmidMarkerData").setScroll(false);
				task.getDisplayItem("PlasmidMarkerData").setVisible(true);
			}
				
		}
		catch(Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}

	}
	
	private ArrayList<String> getGeneAdditionsFromUI(HttpServletRequest request)
	{
		ArrayList<String> alData = new ArrayList<String>();
		try
		{
			TableDataMap map = null;
			RowsetView view = null;
			try
			{
				map = new TableDataMap(request, TABLE);
				view = RowsetView.getSessionView(request);
			}
			catch(Exception ex)
			{
				//no table on UI yet
				//create an empty array and return
				String data = " , , , , ";
				alData.add(data);
				return alData;
			}
			
			int numrows = map.getRowcount();
			int colcount = view.getColumns().size();
			
			for(int i = 1; i <= numrows; i++)
			{
				String row = "";
				for(int j= 1; j <= colcount; j++)
				{
					if( j < colcount )
						row += map.getValue(i, j) + EMREStrings.CHAR.COMMA;
					else
						row += map.getValue(i, j);
				}
				alData.add(row);
				row = "";
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return alData;
	}
	
	
	public void persistRowSetData(HttpServletRequest request, Db db)
	{
		try
		{
			ArrayList<String> alData = getGeneAdditionsFromUI(request);
			RowsetView view = RowsetView.getSessionView(request);
			int colcount = view.getColumns().size();
			int numRows = alData.size();
			//ok, we have all of the rows from the map into an arraylist. 
			//easiest way to get them back into the view is to populate the view from a file
			String cols = "";
			String[] colHeaders = new String[colcount];
			String colKey = view.getColumn(0).getName();
			//lets get the widgets for use later
			ArrayList<String> alWidgets = new ArrayList<String>();
			for(int x = 0; x < colcount; x++)
			{
				if( x < colcount - 1)
					cols += view.getColumn(x).getName() + EMREStrings.CHAR.COMMA;
				else
					cols += view.getColumn(x).getName();
				
				colHeaders[x] = view.getColumn(x).getName();
				alWidgets.add(view.getWidget(x));
			}
			StringBuffer sb = new StringBuffer();
	  		sb.append(cols);
			File file = writeToFile(sb, alData, db);
			view = this.getFileRowsetView(file, colKey, 
			    		TABLE, null, colHeaders, numRows );
			view.setStartRow(1);
			for(int x = 0; x < colcount; x++)
			{
				view.setWidget(x + 1, alWidgets.get(x));
			}
			   	
		  	RowsetView.addViewToSessionViews(request, view);
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	 protected void populateView(HttpServletRequest request, Task task, Db db)
	{
		 try
		 {	
			 // show UI table of locations for selected strain
		  		//RowsetView.cleanupSessionViews(request);
		  		//how many additions do we have?
		  		String sNumRows = task.getServerItemValue("Additions");
		  		if(WtUtils.isNullOrBlankOrPlaceholder(sNumRows))
		  			throw new Exception("Please select a number from the drop down before clicking the 'Get Additions' button.");
			    int numRows = Integer.parseInt(sNumRows);
		  		ArrayList<String> alLocations = getGeneAdditionsFromUI(request);
		  		int currentRowCount = alLocations.size();
		  		String data = " , , , , ";
			    for(int i = currentRowCount; i <= numRows; i++)
			    {
			    	//lets iterate through and create a row for the array list
			    	alLocations.add(data);
			    }
			    StringBuffer sb = new StringBuffer();
		  		sb.append(fileHeaders);
			    File file = writeToFile(sb, alLocations, db);
			    view = this.getFileRowsetView(file, colKey, 
			    		TABLE, null, colHeaders, numRows );
			   	view.setStartRow(1);
			   	view.setWidget(1, LinxConfig.WIDGET.TEXTBOX);
			   	view.setWidget(2, LinxConfig.WIDGET.TEXTBOX);
			   	view.setWidget(3, LinxConfig.WIDGET.TEXTBOX);
			   	view.setWidget(4, LinxConfig.WIDGET.TEXTBOX);
			   	view.setWidget(5, LinxConfig.WIDGET.TEXTBOX);
			   	
			    task.getDisplayItem("GeneAdditionData").setVisible(true);
		  		RowsetView.addViewToSessionViews(request, view);
		 }
		 catch(Exception ex)
		 {
			throw new LinxUserException(ex.getMessage()); 
		 }
		   
	}
	 
	 protected void populateView(ArrayList<String> alData, HttpServletRequest request, 
			 Task task, Db db)
		{
			 try
			 {	
				 // show UI table of locations for selected strain
			  		RowsetView.cleanupSessionViews(request);
			  		StringBuffer sb = new StringBuffer();
			  		sb.append(fileHeaders);
				    File file = writeToFile(sb, alData, db);
				    view = this.getFileRowsetView(file, colKey, 
				    		TABLE, null, colHeaders, alData.size() );
				   	view.setStartRow(1);
				   	view.setWidget(1, LinxConfig.WIDGET.TEXTBOX);
				   	view.setWidget(2, LinxConfig.WIDGET.TEXTBOX);
				   	view.setWidget(3, LinxConfig.WIDGET.TEXTBOX);
				   	view.setWidget(4, LinxConfig.WIDGET.TEXTBOX);
				   	view.setWidget(5, LinxConfig.WIDGET.TEXTBOX);
				   	
				    task.getDisplayItem("GeneAdditionData").setVisible(true);
			  		RowsetView.addViewToSessionViews(request, view);
			 }
			 catch(Exception ex)
			 {
				throw new LinxUserException(ex.getMessage()); 
			 }
			   
		}
	 
	 protected void populateMarkerView(HttpServletRequest request, Task task, Db db)
		{
			 try
			 {	
			  	//how many markers do we have?
			  	String sNumRows = task.getServerItemValue("PlasmidMarkers");
			  	if(WtUtils.isNullOrBlankOrPlaceholder(sNumRows))
			  		throw new Exception("Please select a marker number from the drop down before clicking the 'Get Markers' button.");
				int numRows = Integer.parseInt(sNumRows);
				task.getDisplayItem("PlasmidMarkerData").setVisible(true);
				DisplayItem di = task.getDisplayItem("PlasmidMarkerData");
				task.getDisplayItem("PlasmidMarkerData").setScroll(false);
			 }
			 catch(Exception ex)
			 {
				throw new LinxUserException(ex.getMessage()); 
			 }
			   
		}
	 
//	 protected File writeToFile(String fileHeaders, ArrayList<String> alLocations)
//	  {
//		  
//		  StringBuffer sb = new StringBuffer();
//		  //write out headers
//		  sb.append(fileHeaders + Strings.CHAR.NEWLINE);
//		  
//		  String PARENTTYPE = "System Properties";
//		  String APPVALUETYPE = "Temp File Dir";
//
//			// get the local path for the bulk insert file known to SQL Server
//			String sBulkPath = dbHelper.getApplicationValue(db, PARENTTYPE,
//					APPVALUETYPE);
//			if (sBulkPath == null)
//			{
//				throw new LinxSystemException(
//						"Local path for temp file directory cannot be found. "
//								+ "Please notify the LIMS administrator to set APPVALUE "
//								+ "'" + PARENTTYPE + "'/'" + APPVALUETYPE + "'.");
//			}
//			sBulkPath = sBulkPath + "\\" + "locationFile.csv";
//			
//			
//		  ListIterator<String> itor = alLocations.listIterator();
//		  while(itor.hasNext())
//		  {
//			  String loc = (String)itor.next();
//			  // write out a print placeholder and a reserved location
//			  String line = loc + Strings.CHAR.NEWLINE;
//			  sb.append(line);
//		  }
//
//		  // write rows to file
//		  // we're not writing back to client, so no need for output stream
//		  String fileName = "LimsLogs\\temp\\locationFile.csv";
//		  File file = new File(fileName);
//		  if(file.exists())
//		  {
//			  file.delete();
//		  }
//		  file = new File(fileName);
//		  try
//		  {
//			  FileWriter writer = new FileWriter(file);
//			  writer.write(sb.toString());
//			  writer.flush();
//			  writer.close();
//		  }
//		  catch(Exception ex)
//		  {
//			  throw new LinxUserException(ex.getMessage());
//		  }
//		  return file;
//	  }
}
