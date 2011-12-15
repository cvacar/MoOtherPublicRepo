package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.GeneDiscovery;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.util.PlateLayout;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;

public class Isolates extends EMRETask 
{
	 private int COLUMN_LOCATION = 2;
	 private String originItem = null;
	 private String ROWSET = "Location";
	 private boolean bHaveFile = false;
	 
	
	 public void doTaskWorkPreSave(HttpServletRequest request,
				HttpServletResponse response, User user, Db db)
	{
		 String fileId = getServerItemValue(FileType.ISOLATES_IMPORT_FILE);
			if (!WtUtils.isNullOrBlank(fileId))
			{
				bHaveFile = true;
			}
		 if(getCleanDOM() == null)
			{
				DefaultTask cleanTask = new DefaultTask(this);
				setCleanDOM(cleanTask.getTaskDOM());
			}
			//manipulate the server dom so we know what our item types are
			String item = getServerItemValue(ItemType.ISOLATE);
			if(WtUtils.isNullOrBlank(item))
				throw new LinxUserException("LIMS ID cannot be null");
			boolean bItemExists = dbHelper.isItemExisting(item, ItemType.ISOLATE, db);
			originItem = getServerItemValue(ItemType.ORIGIN_LIMS_ID);
			if(WtUtils.isNullOrBlank(originItem))
				throw new LinxUserException("Origin LIMS ID cannot be null");
			boolean bOriginExists = dbHelper.isEMREItemExisting(originItem, db);
			if(!bOriginExists)
				throw new LinxUserException("The Origin LIMS ID '" + originItem + "' does not exist in the database.");
			//lets see if the origin item is of one of the known types
			boolean bValidOriginItemType = false;
			for(String prefix : GeneDiscovery.prefix)
			{
				if(originItem.startsWith(prefix))
				{
					bValidOriginItemType = true;
				}
			}
			if(!bValidOriginItemType)
				throw new LinxUserException("Please enter a valid Origin LIMS ID.  Valid types start with 'SI'.");
			
			String domOriginType = getOriginIdType();
			String originIdType = getDbOriginIdType(originItem, db, bHaveFile);
			//now manipulate the dom
			if(!originIdType.equalsIgnoreCase(ItemType.ISOLATE))
			{
				bSameItemType = false;
				//determine what types we are working with
				if(domOriginType.equalsIgnoreCase("OriginLIMSID"))
				{
					if(!WtUtils.isNullOrBlank(originItem))
					{
						this.manipulateDOMOriginLimsIdType(db);
					}
				}
			}
			if(originIdType.equalsIgnoreCase(ItemType.ISOLATE))
			{
				//we have two items of the same type
				//update the server DOM with the correct itemtype
				//we need to manually do the work for the origin id
				ListIterator itemsItor = this.getServerItems().listIterator();
				   while(itemsItor.hasNext())
				   {
				     // next item in server DOM
				     Item it = (Item)itemsItor.next();
				     if( WtDOMUtils.isNullOrBlankOrPlaceholder(it.getValue()))
				     {
				         continue;
				     }
				     String domItemType = it.getItemType();
				     
				     if(domItemType.equalsIgnoreCase(EMREStrings.ItemType.ISOLATE))
				     {
				    	 //we've found the item id 
				    	 //we need to check for addAsContent or addAsData to the OriginLIMSID
				    	 //we need to do this manually for items of the same type
					    if(!WtUtils.isNullOrBlank(item) && bItemExists)
						{
					    	NodeList lsContent = it.getAddAsContentsNodes();
					    	this.addContentForOriginId(lsContent, it.getItemElement(), item, ItemType.ISOLATE,
					    			originItem, originIdType, db);
					    	NodeList lsData = it.getAddAsDataNodes();
					    	this.addDataForOriginId(lsData, it.getItemElement(), item, 
					    			ItemType.ISOLATE, originItem, originIdType, db);
							//this.manipulateDOMLimsIdType(db);
							//this.manipulateDOMOriginLimsIdType(db);
							//System.out.println(WtDOMUtils.prettyPrintXML(eItems));
						}
					    else
					    {
					    	//here we have an item that doesn't exist and is of the same type as the origin item
					    	//we need to move the addAsContent and addAsData nodes from the item to a "data" node 
					    	//for post save processing 
					    	bSameItemType = true;
					    	Element eData = this.getServerDOM().getOwnerDocument().createElement("data");
					        //add item type to item element
					    	eData.setAttribute("dbItemType", ItemType.ISOLATE);
					    	eData.setAttribute("item", item);
					    	eData.setAttribute("dbOriginItemType", originIdType);
					    	eData.setAttribute("originItem", originItem);
					    	//Element newItem = (Element)it.getItemElement().cloneNode(true);
					    	//newItem.setAttribute("type", "container");
					    	String sItemType = WtDOMUtils.getAttributeWithoutFail(it.getItemElement(), "itemType", "fail");
					    	String sType = WtDOMUtils.getAttributeWithoutFail(it.getItemElement(), "type", "string");
					    	eData.setAttribute("itemType", sItemType);
					    	eData.setAttribute("type", sType);
					    	
					        NodeList nl = it.getAddAsContentsNodes();
					        for(int i = 0; i < nl.getLength(); i++)
					        {
					        	Node nc = nl.item(i);
					        	NamedNodeMap attrs = nc.getAttributes();
					        	Element eContent = getServerDOM().getOwnerDocument().createElement("addContent");
					        	eData.appendChild(eContent);
					        	
					        	for(int j = 0; j < attrs.getLength(); j++)
					        	{
					        		Node n = attrs.item(j);
					        		eContent.setAttribute(n.getNodeName(), n.getNodeValue());
					        	}
					        }
					        NodeList nl2 = it.getAddAsDataNodes();
					        for(int i = 0; i < nl2.getLength(); i++)
					        {
					        	Node nc = nl.item(i);
					        	NamedNodeMap attrs = nc.getAttributes();
					        	Element eD = getServerDOM().getOwnerDocument().createElement("addData");
					        	eData.appendChild(eD);
					        	
					        	for(int j = 0; j < attrs.getLength(); j++)
					        	{
					        		Node n = attrs.item(j);
					        		eD.setAttribute(n.getNodeName(), n.getNodeValue());
					        	}
					        }
					    	//eData.appendChild(newItem);
					        this.getServerDOM().appendChild(eData);
					        
					        //now we need to remove the addAsContent and addAsData nodes from the 
					        //"items" section so that they don't get processed
					        WtDOMUtils.removeChildren(it.getItemElement(), Item.ADD_AS_DATA);
					        WtDOMUtils.removeChildren(it.getItemElement(), Item.ADD_AS_CONTENT);

					        //this.manipulateDOMLimsIdType(db);
							
					    }
				     }
				   }
				   this.manipulateDOMOriginLimsIdType(db);
			}
			else
				this.manipulateDOMOriginLimsIdType(db);
	}
	 
	 /**overriden to validate file inputs
		 * 
		 */
		@Override
		public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
		{
			super.verifyItems(request, response, user, db);
			
			//make sure growth temperature is a number
			String temp = getServerItemValue("GrowthTemperature");
			validateTemperature(temp, true);
			
			String irr = getServerItemValue("GrowthIrradiance");
			validateIrradiance(irr, true);
			
			String isMeth = getServerItemValue("IsolationMethod");
			validateIsolationMethod(isMeth,false, db);
			
			String arcMeth = getServerItemValue("ArchiveMethod");
			validateArchiveMethod(arcMeth,false, db);
			
			String ves = getServerItemValue("VesselType");
			validateVesselType(ves, false, db);
		}
		
	
	/**
	* Updates custom table enrichment with new enrichment properties, 
	* 
	* @param request
	* @param response
	* @param user
	* @param db
	*/
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		updateAppFilesWithAppliesTo(request, response, user, db);
		updateCustomTables(request, db);
		//reset the limsid and origin lims id types
		doEMRETaskWorkPostSave(request, false, db);
	}

  
  /**
   * Inserts new isolate into custom table.
   * @param db
   */
  protected void updateCustomTables(HttpServletRequest request, Db db)
  {
	  try
		{
			String isolate = getServerItemValue(ItemType.ISOLATE);
			String plate = isolate.substring(0, isolate.lastIndexOf("-"));
			String alphaCoord = isolate.substring(isolate.lastIndexOf("-") + 1);
			PlateLayout.setBUseIsolateLayout(true);
			int numericCoord;
			try
			{
				numericCoord = PlateLayout.getNumericCoord(PlateLayout.CM_96WELL, alphaCoord);
				
			}
			catch(Exception e)
			{
				throw new Exception("Coordinate '" + alphaCoord + "' is not a valid coordinate for this plate.  Please enter a new coord and try again.");
			}
			String itemId = dbHelper.getItemId(isolate, ItemType.ISOLATE, db);
			setMessage("Successfully created new isolate(s).  Click on task name to autogenerate a New LIMS ID.");
			String originItemId = dbHelper.getItemId(originItem, this.getOriginIdType(), db);
			
			String sql = "spEMRE_insertIsolate ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(itemId);
			params.add(originItemId);
			params.add(plate);
			params.add(alphaCoord);
			params.add(numericCoord + "");
			params.add(getServerItemValue(DataType.NOTEBOOK_REF));
			params.add(getServerItemValue("VesselType"));
			params.add(convertDate(getServerItemValue("DateArchived")));
			params.add(getServerItemValue("ArchiveMethod"));
			params.add(getServerItemValue("IsolationMethod"));
			//make sure the optional values are checked before sending to SP
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthTemperature")))
				params.add(null);
			else
				params.add(validateTemperature(getServerItemValue("GrowthTemperature"), true));
			
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthMedium")))
				params.add(null);
			else
				params.add(getServerItemValue("GrowthMedium"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("GrowthIrradiance")))
				params.add(null);
			else
				params.add(validateIrradiance(getServerItemValue("GrowthIrradiance"),true));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
				params.add(null);
			else
				params.add(getServerItemValue("Comment"));
				
			params.add(getTranId() + "");
				
			dbHelper.callStoredProc(db, sql, params, false, false);
			
			//save the locations   
			
			List<String> lsLocations = getServerItemValues("Location");
			int numLocations = lsLocations.size();
			if(numLocations == 0)
			{
				//we have the UI 
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
					String rack = aLocs[1];
					String position = aLocs[2];
					//make sure position is 2 characters
					try
					{
						 int iPos = Integer.parseInt(position);
						 //now lets zero pad the position
						if(position.length() < 2)
						{
							position = EMRETask.zeroPad(iPos, 2);
						}
					}
					catch(Exception ex)
					{
					  throw new LinxUserException("The rack position must be a number between 01 and 36");
					}
					if(position.length() != 2)
						throw new LinxUserException("The rack position must be a number between 01 and 36.");
					try
					{
						int i = Integer.parseInt(position);
						if(i < 1 || i > 36)
							throw new Exception("The rack position must be a number between 01 and 36");
					}
					catch(Exception ex)
					{
						throw new LinxUserException("The rack position must be a number between 01 and 36.");
					}
					params.clear();
					params.add(isolate);
					params.add(freezer); 
					params.add(rack);
					params.add(position);
					params.add( idx +""); //location index
					params.add(ItemType.ISOLATE);
					params.add(getTranId()+"");
					 sql = "spEMRE_insertIsolateLocation";
					  dbHelper.callStoredProc(db, sql, params, false, true);
				}
			}
			
			else
			{
				//we have a file
				for(int i = 0; i < numLocations; i++)
				{
					String location = lsLocations.get(i);
					String [] aLocs = new String[3];
					
						aLocs = location.split(":");
						String freezer = aLocs[0];
						String rack = aLocs[1];
						String position = aLocs[2];
						
						try
						{
							 int iPos = Integer.parseInt(position);
							 //now lets zero pad the position
							if(position.length() < 2)
							{
								position = EMRETask.zeroPad(iPos, 2);
							}
						}
						catch(Exception ex)
						{
						  throw new LinxUserException(ex.getMessage());
						}
						if(position.length() != 2)
							throw new LinxUserException("The rack position must be a number between 01 and 36.");
						try
						{
							int j = Integer.parseInt(position);
							if(j < 1 || j > 36)
								throw new Exception("The rack position must be a number between 01 and 36");
						}
						catch(Exception ex)
						{
							throw new LinxUserException("The rack position must be a number between 01 and 36.");
						}
						params.clear();
						params.add(isolate);
						params.add(freezer); 
						params.add(rack);
						params.add(position);
						params.add( (i + 1) +""); //location index
						params.add(ItemType.ISOLATE);
						params.add(getTranId()+"");
						 sql = "spEMRE_insertIsolateLocation";
						  dbHelper.callStoredProc(db, sql, params, false, true);
				}
			}
				 
					
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}    
   }
  
}
