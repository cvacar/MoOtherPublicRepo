package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.EMREStrings.GeneDiscovery;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.EMREStrings.FileType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;

public class SecondaryEnrichment extends EMRETask 
{
	private String originItem = null;
	private boolean bHaveFile = false;
	
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		String fileId = getServerItemValue(FileType.SECONDARY_ENRICHMENT_IMPORT_FILE);
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
		String item = getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
		if(WtUtils.isNullOrBlank(item))
		{
			throw new LinxUserException("New LIMS ID cannot be null");
		}
		boolean bItemExists = dbHelper.isItemExisting(item, ItemType.SECONDARY_ENRICHMENT, db);
		originItem = getServerItemValue(ItemType.ORIGIN_LIMS_ID);
		if(WtUtils.isNullOrBlank(originItem))
		{
			throw new LinxUserException("Origin LIMS ID cannot be null");
		}
		//lets see if the origin item is of one of the known types
		boolean bOriginExists = dbHelper.isEMREItemExisting(originItem, db);
		if(!bOriginExists)
		{
			throw new LinxUserException("The Origin LIMS ID '" + originItem + "' does not exist in the database.");
		}
		boolean bValidOriginItemType = false;
		for(String prefix : GeneDiscovery.prefix)
		{
			if(originItem.startsWith(prefix))
			{
				bValidOriginItemType = true;
				break;
			}
		}// next valid origin ID type
		if(!bValidOriginItemType)
		{
			throw new LinxUserException("Please enter a valid Origin LIMS ID.  Valid types start with 'SI'.");
		}
		//doEMRETaskWorkPreSave(request, false, db);
		String domOriginType = getOriginIdType();
		String originIdType = getDbOriginIdType(originItem, db, bHaveFile);
		//now manipulate the dom
		if(!originIdType.equalsIgnoreCase(ItemType.SECONDARY_ENRICHMENT))
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
		if(originIdType.equalsIgnoreCase(ItemType.SECONDARY_ENRICHMENT))
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
			     
			     if(domItemType.equalsIgnoreCase(EMREStrings.ItemType.SECONDARY_ENRICHMENT))
			     {
			    	 //we've found the item id 
			    	 //we need to check for addAsContent or addAsData to the OriginLIMSID
			    	 //we need to do this manually for items of the same type
				    if(!WtUtils.isNullOrBlank(item) && bItemExists)
					{
				    	NodeList lsContent = it.getAddAsContentsNodes();
				    	this.addContentForOriginId(lsContent, it.getItemElement(), item, ItemType.SECONDARY_ENRICHMENT,
				    			originItem, originIdType, db);
				    	NodeList lsData = it.getAddAsDataNodes();
				    	this.addDataForOriginId(lsData, it.getItemElement(), item, 
				    			ItemType.SECONDARY_ENRICHMENT, originItem, originIdType, db);
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
				    	eData.setAttribute("dbItemType", ItemType.SECONDARY_ENRICHMENT);
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
	/**
	* Updates custom table enrichment with new strain enrichment,  
	* 
	* @param request
	* @param response
	* @param user
	* @param db
	*/
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		  //updateAppFilesWithAppliesTo(request, response, user, db); 
		  //server dom has been manipulated - lets update the custom tables
		  updateCustomTables(request, db);
			//reset the task DOM to it's original state
		  doEMRETaskWorkPostSave(request, false, db);
	}
	
	/**overriden to validate file inputs
	 * 
	 */
	@Override
	public void verifyItems(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		super.verifyItems(request, response, user, db);
		
		//validate pH
		String pH = getServerItemValue("pH");
		validatePH(pH, true);
		
		//validate CO2
		String co2 = getServerItemValue("CO2Concentration");
		validateCO2(co2, true);
		
		//validate O2
		String o2 = getServerItemValue("OxygenConcentration");
		validateO2(o2, true);
		
		//validate flocculence
		String floc = getServerItemValue("Flocculence");
		validateFlocculence(floc,true, db);
	}
	
	
	/**
	 * Inserts new or updates screen-based enrichment into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		try
		{
			String enrichment = getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
			String itemId = dbHelper.getItemId(enrichment, ItemType.SECONDARY_ENRICHMENT, db);
			setMessage("Successfully created new secondary enrichment(s).  Click on task name to autogenerate a New LIMS ID.");
			String originItemId = dbHelper.getItemId(originItem, this.getOriginIdType(), db);
			String sql = "spEMRE_insertSecondaryEnrichment ";
			ArrayList<String> params = new ArrayList<String>();
			params.add(itemId);
			params.add(originItemId);
			params.add(getServerItemValue(DataType.NOTEBOOK_REF));
			params.add(getServerItemValue("GrowthMedium"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("OxygenConcentration")))
				params.add(null);
			else
				params.add(getServerItemValue("OxygenConcentration"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("CO2Concentration")))
				params.add(null);
			else
				params.add(getServerItemValue("CO2Concentration"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("pH")))
				params.add(null);
			else
				params.add(getServerItemValue("pH"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Flocculence")))
				params.add(null);
			else
				params.add(getServerItemValue("Flocculence"));
			if(WtUtils.isNullOrBlankOrPlaceholder(getServerItemValue("Comment")))
				params.add(null);
			else
				params.add(getServerItemValue("Comment"));
			
			params.add(getTranId() + "");
			
			dbHelper.callStoredProc(db, sql, params, false, false);
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		
	}
	
}
