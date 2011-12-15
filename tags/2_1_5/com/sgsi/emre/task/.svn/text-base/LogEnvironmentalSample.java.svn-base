package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;


public class LogEnvironmentalSample extends EMRETask 
{
	protected String itemType = "EnvironmentalSample";
	private String ROWSET = "Location";
	  private int COLUMN_CHECKBOX = 1;
	  private int COLUMN_LOCATION = 2;
	  
	  /** 
	   * Overridden to generate new Sample ID(s) and
	   * add to server side itemtype 'Sample'.
	   * @param request
	   * @param response
	   * @param user
	   * @param db
	   */
	  @Override
	  public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	  {
		  String item = getServerItemValue(itemType);
		  setMessage("Successfully logged new sample " + item + 
				  ".  Click 'Print Labels' to print or click on task name to autogenerate new IDs." );
	  }
	  
		/**
		 * Overridden to update custom tables with 
		 * properties of new samples.
		 * @param request
		 * @param response
		 * @param user
		 * @param db
		 */
		public void doTaskWorkPostSave(HttpServletRequest request,
				HttpServletResponse response, User user, Db db)
		{
			updateAppFilesWithAppliesTo(request, response, user, db);
			//String fileId = getServerItemValue(FileType.SAMPLE_MANIFEST_FILE);
			//File file = getFile(fileId, db);
			//if (!WtUtils.isNullOrBlank(fileId))
			//{
			//	updateCustomTablesFromFile(file, db);
			//}
			//else
				updateCustomTables(request, db);
		}

		/**
		 * 
		 * @param db
		 */
		protected void updateCustomTables(HttpServletRequest request, Db db)
		{
			ArrayList<String> params = new ArrayList<String>();
			
			//iterate through the display items and add to list to sent to SP
			 List ditems = this.getDisplayItems();
		     ListIterator itor = ditems.listIterator();
		      while(itor.hasNext())
		      {
		          DisplayItem ditem = (DisplayItem)itor.next();
		          if(ditem.getWidget().equals(LinxConfig.WIDGET.BUTTON)
		              || ditem.getWidget().equalsIgnoreCase("SAVEBUTTON")
		              || ditem.getWidget().equalsIgnoreCase("VERIFYBUTTON")
		              || ditem.getWidget().equals("rowsets")
		              || ditem.getItemType().indexOf("Placeholder") > -1)
		          {
		              // skip buttons and UI tables
		              continue;
		          }
		          if(WtUtils.isNullOrBlankOrPlaceholder(ditem.getValue())){
		        	  params.add(null);
		          }
		          else
		        	  params.add(ditem.getValue());
		      }// next displayItem
		      // at exit, have added new data to appliesToItem
		      params.add(getTranId() + "");
		      String sample = getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
		      String exists = "false";
		      if(dbHelper.isItemExisting(sample, ItemType.ENVIRONMENTAL_SAMPLE, db))
		      {
		    	  exists = "true";
		      }
		      params.add(exists);
		      try
				{
					String sql = "spMet_InsertEnvironmentalSample_" 
										+ getTaskName().replace(" ", "");
					db.getHelper().callStoredProc(db, 
							sql, params, false, true);
					sql = ""; 
				}
				catch (Exception e)
				{
					throw new LinxUserException(e.getMessage());
				}
				
				//now that we've inserted into sample lets insert the locations
				 TableDataMap rowMap = new TableDataMap(request, ROWSET);
			      int numRows = rowMap.getRowcount();
			      int idx = 0;
				  for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
				  {
					String location = (String)rowMap.getValue(rowIdx, COLUMN_LOCATION);
					idx++;
					String[] aLocs = location.split(":");
					String freezer = aLocs[0];
					String box = aLocs[1];
					String position = aLocs[2];
					aLocs = null;
					params.clear();
					params.add(sample);
					params.add(freezer); 
					params.add(box);
					params.add(position);
					params.add(idx+""); //location index
					params.add(getTranId()+"");
					  
					String sql = "spMet_InsertOrUpdateSampleLocation";
					dbHelper.callStoredProc(db, sql, params, false, true);
				}
		  }
		
//		private void updateCustomTablesFromFile(File file, Db db)
//		{
//			try
//			{
//				char delim = ';';
//				String columnKey = "New LIMS ID";
//				XLSParser fileData = new XLSParser(file, getTaskName(),
//						delim, columnKey, reqColHeaders, true);
//				
//				if(fileData.gotoFirst())
//				{
//					do
//					{		
//						//insert the row of data
//						ArrayList<String> params = new ArrayList<String>();
//						params.add(fileData.getRequiredProperty("New LIMS ID"));		
//						params.add(fileData.getRequiredProperty("Physical Form"));
//						params.add(fileData.getRequiredProperty("Collection Date"));
//						params.add(fileData.getRequiredProperty("Notebook Ref"));
//						params.add(fileData.getProperty("Internal ID"));
//						params.add(fileData.getProperty("Field Name"));
//						params.add(fileData.getProperty("Description"));
//						params.add(fileData.getProperty("Volume (L)"));
//						params.add(fileData.getProperty("Weight (g)"));
//						params.add(fileData.getProperty("Temperature (degrees C)"));
//						params.add(fileData.getProperty("pH"));
//						params.add(fileData.getProperty("Dissolved Oxygen (mg/L)"));
//						params.add(fileData.getProperty("Conductivity (mS/m)"));
//						params.add(fileData.getProperty("Depth (m)"));
//						params.add(fileData.getProperty("Salinity (ppt)"));
//						params.add(fileData.getProperty("Latitude"));
//						params.add(fileData.getProperty("Longitude"));
//						params.add(fileData.getProperty("Altitude (m)"));
//						params.add(fileData.getProperty("Site Description"));
//						params.add(fileData.getProperty("Storage Method"));
//						params.add(fileData.getProperty("Closest Town"));
//						params.add(fileData.getProperty("City"));
//						params.add(fileData.getProperty("County"));
//						params.add(fileData.getProperty("State"));
//						params.add(fileData.getProperty("Country"));
//						params.add(fileData.getProperty("Archive Location"));
//						params.add(fileData.getProperty("Comment"));
//						params.add(getTranId() + "");
//						
//						String sql = "spMet_InsertEnvironmentalSample_" 
//							+ getTaskName().replace(" ", "");
//						db.getHelper().callStoredProc(db, sql, params, false, true);
//						params.clear();
//						params = new ArrayList<String>();
//						//now add freezer locations
//						String location = fileData.getProperty("Freezer Location");
//						int idx = 1;
//						String[] aLocs = location.split(":");
//						String freezer = aLocs[0];
//						String box = aLocs[1];
//						String position = aLocs[2];
//						aLocs = null;
//						params.clear();
//						params.add(fileData.getRequiredProperty("New LIMS ID"));
//						params.add(freezer); 
//						params.add(box);
//						params.add(position);
//						params.add(idx+""); //location index
//						params.add(getTranId()+"");
//						  
//						sql = "spMet_InsertOrUpdateSampleLocation";
//						dbHelper.callStoredProc(db, sql, params, false, true);
//						params = null;
//					}
//					while(fileData.gotoNext());
//				}//at end we have validated all of the inputs in the file
//			}
//			catch(Exception ex)
//			{
//				throw new LinxUserException(ex.getMessage());
//			}
//		}

}
