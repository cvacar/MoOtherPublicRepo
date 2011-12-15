package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class UpdateEnvironmentalSample extends EMRETask 
{

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
	        	  || ditem.getWidget().equals(LinxConfig.WIDGET.FILE_SUBMIT)
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
	      params.add(getTranId()+"");
	      String sample = getServerItemValue(ItemType.ENVIRONMENTAL_SAMPLE);
	      String sampleType = dbHelper.getSampleType(sample, db);
	      if(!WtUtils.isNullOrBlank(sampleType))
	      {
	    	  if(this.getTaskName().replace(" ", "").indexOf(sampleType) < 0)
		    	  throw new LinxUserException("Sample '" + sample + "' is of type '" 
		    			  + sampleType + "'.\r\nPlease select the correct Update Environmental Sample task and try again.");
		     
	      }
	      else
	    	  throw new LinxUserException("Unknown sample type.  Please enter the appropriate sample type and try again.");
	        
	      try
			{
				String sql = "spEMRE_updateEnvironmentalSample_" 
									+ sampleType.replace(" ", "");
				db.getHelper().callStoredProc(db, 
						sql, params, false, true);
				sql = ""; 
			}
			catch (Exception e)
			{
				throw new LinxUserException(e.getMessage());
			}
	  }

}
