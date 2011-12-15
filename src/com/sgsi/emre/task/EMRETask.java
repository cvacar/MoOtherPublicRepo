package com.sgsi.emre.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.project.Strings;
import com.sgsi.emre.EMREStrings;
import com.sgsi.emre.bean.POIParser;
import com.sgsi.emre.bean.XLSParser;
import com.sgsi.emre.db.EMREDbHelper;
import com.sgsi.emre.util.S4MPCRBarcode;
import com.sgsi.emre.util.S4MSmallBarcode;
import com.sgsi.task.SGITask;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.db.DbHelper;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.task.ItemType;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;

/**
 * MetTask 
 * Parent class for all Met task classes.
 * 
 * Overrides doTaskWorkPreSave() to check inventory
 * when appropriate.
 * 
 * @author TJS/Wildtype 
 * @date 5/2006
 */
public class EMRETask extends SGITask
{
  EMREDbHelper dbHelper = new EMREDbHelper();
  Db db = null;
  private String limsIdType = "LIMSID";
  private String originIdType = "OriginLIMSID";
  Element cleanDOM = null;
  protected boolean bSameItemType = false;
   
  
//  /**
//   * Overriden to handle new="true" for file type items
//   * If new="true" then we ensure that the same filename doesn't exist in the db.
//   */
//  public void createAnyNewAppFiles(HttpServletRequest request, 
//		  HttpServletResponse response, User user, Db db)
//  {
//	  ListIterator itemsItor = this.getServerItems().listIterator();
//	  while(itemsItor.hasNext())
//	  {
//		 Item item = (Item)itemsItor.next();
//		 String type = item.getType();
//		 if(type.equalsIgnoreCase("file"))
//		 {
//			 String itemType = item.getItemType();
//			 boolean bNew = false;
//			 try
//			 {
//				 bNew = item.isNew();
//			 }
//			 catch(Exception ex)
//			 {
//				//new attribute doesn't exist - lets move on
//				continue;
//			 }
//			 if(bNew)
//			 {
//					 //lets make sure that we have a new file
//					 String newFileName = getServerItemValue(itemType);
//					 if(!WtUtils.isNullOrBlank(newFileName))
//					 {
//						 //are we supposed to copy the file to a server location?
//						 NodeList nodes = item.getAddAsAppFileNodes();
//						 String copyFileTo = null;
//						 if(nodes.getLength() > 0)
//						 {
//							 for(int i = 0; i < nodes.getLength(); i++)
//						     {
//								 Element eFileNode = (Element)nodes.item(i);
//								 try
//								 {
//									copyFileTo = eFileNode.getAttribute(EMREStrings.DOMAttribute.COPYFILETO);
//								 }
//								 catch(Exception ex)
//								 {
//									 //no copyFileTo attribute - ignore
//								 }
//								 if(!WtUtils.isNullOrBlank(copyFileTo))
//								 {
//									//we have a place to copy the file
//									if(copyFileTo.equalsIgnoreCase("lookup"))
//									{
//										//lets get the value from the db
//										copyFileTo = dbHelper.getApplicationValue(db, "FileType", itemType);
//									}
//								 }
//								 File f = new File(newFileName);
//								 String parent = f.getParent();
//								 String fileName = f.getName();
//								 boolean bExists = false;
//								 if(!WtUtils.isNullOrBlank(copyFileTo))
//									bExists = dbHelper.doesFileExist(copyFileTo, fileName, db);
//								 else
//									bExists = dbHelper.doesFileExist(parent, fileName, db);
//								 if(bExists)
//									throw new LinxUserException("A file with the name: " + newFileName + 
//										 " already exists.  Please rename the file and try again.");
//						     }
//							 
//						 }
//						 
//					 }
//					 else
//					 {
//						 if( isOptional(item))
//					       {
//					         // skip empty optional items
//					         continue;
//					       }
//					       throw new LinxUserException("Please provide a value for " 
//					            + getItemLabel(item) + ", then try again.");
//					 }
//				 }
//			 }
//	  }
//	  //lets call the super implementation
//	  super.createAnyNewAppFiles(request, response, user, db);
//  }
  
  /**
   * Creates new applicationFile entries in APPLICATIONFILE table for the items 
   * with addAsAppFile directive nodes.
   * 
   * In an itemType node with "type=file", flags can be set as follows:
   *   new=true -> core throws an error if file already exists
   *   new=optional -> core will not complain if file already exists and uses existing file and
   *   							will create a new appFileId in the DB for the file for each row's data
   *   							but all pointing to the same original file.
   *   new=oncePerTran -> file must be new at start of transaction but all subsequent uses of file
   *   							during the transaction will refer to the newly created appFileID 
   *   							(e.g. useful in processing rows of a batch file)
   *   new (missing) -> Same as "optional"
   * 
   * The addAsAppFile element may contain the flag copyFileTo=lookup or
   * (less common) a literal directory path ending with slash, copyFileTo=someDir/
   * If this flag is present, new=true/new=oncePerTran will check only 
   * if the destination file exists, not the src file, because different file types
   * often have different destination directories in which same-named files don't conflict.
   *   
   * This routine will get appFileIds from the Linx DB for the server items of type "file"
   * and replace the original server item's value of file/path with the DB assigned appFileId.
   * After this routine runs, the item.getValue() method will return the appFileId.
   *   
   * @param request
   * @param response
   * @param user
   * @param db
   */
  @Override
	public void createAnyNewAppFiles(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		String ONCE_PER_TRAN = "oncePerTran";

		// super implementation
		String mask = null;
		String itemType = null;

		// check each item in this server dom for addAsAppFile command nodes
		ListIterator itemsItor = this.getServerItems().listIterator();
		while (itemsItor.hasNext())
		{
			// next item in server DOM
			Item item = (Item) itemsItor.next();
			// System.out.print(WtDOMUtils.prettyPrintXML(item.getItemElement()));
			if (WtUtils.isNullOrBlankOrPlaceholder(item.getValue())
					|| item.getAddAsAppFileNodes().getLength() < 1)
			{
				// no file value provided or no addAsAppFile command node
				continue;
			}

			// found an addAsAppFile command
			// -- supporting only one addAsAppFile node per itemType
			NodeList nodes = item.getAddAsAppFileNodes();
			Element eCommand = (Element) nodes.item(0);

			// get itemType
			itemType = WtUtils.getAttributeWithoutFail(eCommand,
					Item.FILE_TYPE, item.getItemType());

			// ----- check for optional flag: new=oncePerTran -----
			// -- (do first because item value might be an appfileid)
			String newFlag = WtUtils.getAttributeWithoutFail(
					item.getItemElement(), Item.NEW, "optional");
			if (newFlag.equalsIgnoreCase(ONCE_PER_TRAN)
					&& isAppFileId(item.getValue(), db))
			{
				// appFileId already assigned; we're done here
				continue;
			}

			// not an appfileId; look for a src file
			// -- might be a fully qualified file name or just a file name
			// update item value with fully qualified path
			item.setValue(getFullyQualifiedPath(request, item.getValue()));
			// by here, we should have enough info to instantiate a src File
			// object

			// ----- check for optional flag: validate=true -----
			if (Boolean.parseBoolean(WtUtils.getAttributeWithoutFail(eCommand,
					Item.VALIDATE_FILE, "false")))
			{
				// will throw exc if file can't be read
				validateAppFile(item.getValue(), db);
			}

			// ------- check for optional flag: copyFileTo -------
			// -- (a destination, not a source path)
			boolean bCopyFileToNet = false;
			String copyFileTo = eCommand.getAttribute(Item.COPY_FILE);
			if (!WtUtils.isNullOrBlank(copyFileTo))
			{
				bCopyFileToNet = true;
			}

			// here's our src File obj
			// -- we didn't try to validate unless flag is set
			File fileObj = new File(item.getValue());

			// ------- check for optional flag: copyFileTo -------
			// -- (destination, not source path)
			File storeFile = fileObj; // the default is to store just file path
			if (bCopyFileToNet == true)
			{
				// ----- if copyFileTo=lookup, look up configured path in db
				if (copyFileTo.equalsIgnoreCase("lookup"))
				{
					// look up configured parent dir by file type
					copyFileTo = db.getHelper().getParentPathByFileType(itemType, db);
				}
				// else there's a literal dest path here (unusual)

				// ----- check for optional flag: mask ------
				mask = WtUtils.getAttributeWithoutFail(eCommand, Item.MASK,
						Placeholder.NONE_PROVIDED);
				String maskedName = getMaskedFilename(mask, storeFile);
				// check dest path for newness
				if ((newFlag.equalsIgnoreCase(Item.NEW) || newFlag
						.equalsIgnoreCase(ONCE_PER_TRAN))
						&& isFileExisting(new File(copyFileTo + maskedName), db))
				{
					throw new LinxUserException(
							"LIMS requires files of type "
									+ itemType
									+ " to be new, "
									+ "however a file named "
									+ copyFileTo
									+ maskedName
									+ " already exists in the destination directory."
									+ " Please rename your file, then try again.");
				}

				// ----- copy file to configured destination path ------
				storeFile = copyFileToNet(item, copyFileTo, itemType, mask, db);
			}
			else // don't copy file; just store name
			{
				// check file name for newness
				if ((newFlag.equalsIgnoreCase(Item.NEW) || newFlag
						.equalsIgnoreCase(ONCE_PER_TRAN))
						&& isFileExisting(storeFile, db))
				{
					throw new LinxUserException(
							"LIMS requires files of type "
									+ itemType
									+ " to be new, "
									+ "however a file named "
									+ storeFile.getAbsolutePath()
									+ " already exists in this LIMS database."
									+ " Please rename your file, then try again.");
				}
			}
			// by here, storeFile obj is either the src file or copied file

			// -------- save to db as app file ----------
			// --- look up app file id if one exists
			// -- must use flag new=true or =oncePerTran to prevent a substitution
			String appFileId = db.getHelper().getAppFileId(storeFile.getParent(), storeFile.getName(), db);
			if (WtUtils.isNullOrBlank(appFileId))
			{
				// no such appfile exists in LIMS
				// --- CREATE A NEW APPFILE RECORD IN LIMS
				String fileTypeId = db.getHelper().getTypeId(DbHelper.FILETYPE,
						itemType, getTranId(), db);
				appFileId = db.getHelper().addApplicationFile(storeFile.getParent(),
						storeFile.getName(), fileTypeId, getTranId(), db);
			}

			// -------- replace file path info with appFileId as item's value
			item.setValue(appFileId);
		}// next item with addAsAppFile node
		// at exit, new app files have been created

	}
  
  /**
   * If there's a valid mask, returns masked filename
   * for comparison to existing files, otherwise returns
   * storeFile's filename. Default suffix is .txt.
   * @param mask
   * @param storeFile
   * @return masked filename, or orig filename if no mask flag
   */
	protected String getMaskedFilename(String mask, File storeFile)
	{
		String srcFilename = storeFile.getName();
		// if masking filename, assemble new filename
		if (!WtUtils.isNullOrBlankOrPlaceholder(mask))
		{
			String prefix = mask.substring(0, mask.indexOf("."));
			if (WtUtils.isNullOrBlank(prefix))
			{
				prefix = srcFilename;
			}
			String suffix = mask
					.substring(mask.lastIndexOf("."), mask.length());
			if (WtUtils.isNullOrBlank(suffix))
			{
				suffix = ".txt";
			}
			srcFilename = prefix + "_" + getTranId() + suffix;
		}
		return srcFilename;
	}

/**
   * Returns true if a file with the same path and 
   * name exists in LIMS in application file table.
   * @param fqPath
   * @param db
   * @return true if file exists in LIMS, otherwise false
   */
  protected boolean isFileExisting(File fileObj, Db db)
  {
  	String s = db.getHelper().getAppFileId(fileObj.getParent(), fileObj.getName(), db);
  	if(!WtUtils.isNullOrBlank(s))
  	{
  		return true;
  	}
  	else if( fileObj.exists())
  	{
  		// tbd: does an orphan file count?
  		Code.debug("At isFileExisting(): destination file exists with no APPLICATIONFILE record: " + fileObj.getAbsolutePath());
  	}
  	return false;
  }
  
  /**
   * Returns true if the value under this file item
   * is already an applicationFileId. Called by
   * createAnyNewAppFiles() to handle new=oncePerTran.
   * TODO: look up actual appfileId in db?
   * @param fileValue
   * @return true if appfileid vs a file path or name
   */
  protected boolean isAppFileId(String fileValue, Db db)
	{
	    try
	    {
	    	// app file id's are integers; file names are not
	    	Integer.parseInt(fileValue);
	    }
	    catch(Exception ex)
	    {
	    	return false;
	    }
		return true;
	}

/**
   * Updates applies_to_itemId field in core table APPLICATIONFILE
   * for one target item per file itemType with an addAsAppFile node.
  * @param request
  * @param response
  * @param user
  * @param db
   *
   */
 public void updateAppFilesWithAppliesTo(HttpServletRequest request, HttpServletResponse response, User user,
     Db db)
 {
   String targetType = null;
   String fileType = null;
   String coordinate = null;
   String itemType = null;

   // check each item in this server dom for addAsAppFile command nodes
   ListIterator itemsItor = this.getServerItems().listIterator();
   while(itemsItor.hasNext())
   {
     // next item in server DOM
     Item item = (Item)itemsItor.next();
     if( WtDOMUtils.isNullOrBlankOrPlaceholder(item.getValue()))
     {
       if( isOptional(item))
       {
         // skip empty optional items
         continue;
       }
       throw new LinxUserException("Please provide a value for " 
            + getItemLabel(item) + ", then try again.");
     }
     itemType = item.getItemType();

     // any addAsAppFile nodes for this item?
     NodeList nodes = item.getAddAsAppFileNodes();
     if(nodes.getLength() < 1)
     {
       // no addAsAppFile nodes on this item
       continue;
     }
     
     // found an addAsAppFile node on this file item
     // get value of this app file item (should be db's application_file_id)
     // -- only one file is supported per file item element...
     String appFileId = item.getValue();
     // -- but support addAsAppFile nodes for multiple targets
     for(int i = 0; i < nodes.getLength(); i++)
     {
       Element eCommand = (Element)nodes.item(i);
       targetType = WtUtils.getAttributeWithoutFail(eCommand, Item.APPLIES_TO, "task");    
       // filetype string may differ from itemType, but usually doesn't
       fileType = WtUtils.getAttributeWithoutFail(eCommand, Item.FILE_TYPE, itemType);

       if( targetType == null || targetType.equalsIgnoreCase("task"))
       {
     	 // -- core added app files for generic case earlier
          continue;
       }
       // get values for targets
       Item target = this.getServerItem(targetType);

         // ...update appliesTo value for each item of correct target type
         ListIterator targetValuesItor = target.getValues().listIterator();
         while(targetValuesItor.hasNext())
         {
           String targetItem = (String)targetValuesItor.next();
           if( WtDOMUtils.isNullOrBlankOrPlaceholder(targetItem))
           {
             continue;
           }
           // go through custom db helper
            dbHelper.updateAppFileAppliesTo(targetItem, targetType, appFileId, getTranId(), db);
            
         }// next target value
       }// next addAsAppFile node on this file item
     }// next item in server DOM
   // at exit, have updated appliesTo for target types for each addAsAppFile in server DOM
 }
  
  /** 
   * Overridden to check inventory before allowing Save.
   * @param request
   * @param response
   * @param user
   * @param db
   */
  public void doTaskWorkPostSave(HttpServletRequest request,
      HttpServletResponse response, User user, Db db)
  {
    checkInventory(db);
    super.doTaskWorkPostSave(request, response, user, db);
  }
  
    /**
   * Creates new COMMENTS table records from the merged values
   * in the server items, if the task def includes
   * an item of type=comment, as opposed to type=container or type=string. 
   * 
   * @param request 
   * @param response
   * @param user 
   * @param db
   */
  public void createAnyNewComments(HttpServletRequest request, HttpServletResponse response,
      User user, Db db)
  {

    // check each item in this server dom type=comment
    ListIterator itemsItor = this.getServerItems().listIterator();
    while(itemsItor.hasNext())
    {
      // next item in server DOM
      Item item = (Item)itemsItor.next();
      if( item.getType().equalsIgnoreCase(ItemType.TYPES.COMMENT))
      {
        String commentType = item.getItemType();
        String comment     = item.getValue();
        if(WtUtils.isNullOrBlank(comment))
        {
          continue;
        }
        // make SQL-safe
        comment.replace('\'', '"');
        // targettype may be null
        String targetType  = item.getItemElement().getAttribute(Item.APPLIES_TO);
        if(!WtUtils.isNullOrBlank(targetType))
        {
        	ListIterator targetItor = getServerItemValues(targetType).listIterator();
        	while(targetItor.hasNext())
        	{
        		String targetItem = (String)targetItor.next();
        		dbHelper.addComment(commentType, comment, targetItem, targetType,getTranId(), db);
        	}// next target item
        	// at exit, have added comment for each target item of targetType
        }
        else
        {
        	db.getHelper().addComment(commentType, comment, targetType, getTranId(), db);
        }
      }
    }// next item in server DOM
    // at exit, have added comments to COMMENTS table 
    // -- for each comment item in server DOM
  }
  
  /**
   * Dequeues any items now exhausted(?)
   * TODO: design and implement
   * @param db
   */
  public void checkInventory(Db db)
  {
    // todo: design and implement
  }
  
  /**
   * Provide a way for task to lookup
   * list values upon call from custom JSP.
   * @param db
   */
  public void setDb(Db db)
  {
    this.db = db;
  }
  

  /**
   * When user selects an Protocol from the UI table,
   * populates the input widgets, ready for editing
   * or using as starting point for a new Protocol.
   * Called by tasks that must display Protocols.
   * @param task 
   * @param selProtocol
   * @param db
   */
  public void populateDisplayItemsWithProtocolProperties(Task task, String selProtocol, Db db)
  {
      // sp expects itemId, not the usual item.item
      // -- bec it's unlikely users will bother to name every protocol variation
    String sql = "exec spMet_GetProtocolProperties '" + selProtocol + "'";
    
    ResultSet rs = dbHelper.getResultSet(sql, db);
    try
    {
      // check each column for an itemType match
        // -- should match all, altho number of cols differs between Protocols
      ResultSetMetaData metaRs = rs.getMetaData();
      rs.next();
      for(int i=1; i < metaRs.getColumnCount()+1;i++)
      {
         DisplayItem ditem = null;
         String propName = metaRs.getColumnName(i);
         String value = rs.getString(i);
        try
        {
          ditem = getDisplayItem(propName);
        }
        catch (RuntimeException e)
        { // not all fields in table correspond, e.g. protocolId
          // create this display item on the fly to the client side (in memory)
          if(!(WtUtils.isNullOrBlank(value)))
          {
            Element eItem = task.getClientDOM().getOwnerDocument().createElement("displayItem");
            eItem.setAttribute("itemType", propName);
            eItem.setAttribute("widget", LinxConfig.WIDGET.TEXTBOX);
            eItem.setAttribute("label", propName);
            task.getClientDOM().appendChild(eItem);
            ditem = new DisplayItem(eItem);
          }
        }
        // set the new or old widget's value
        if(ditem != null)
        {
          if(ditem.getWidget().equals(LinxConfig.WIDGET.DROPDOWN))
          {
            ditem.setSelectedValue(value);
          }
          else
          {
            ditem.setValue(value);
          }
        }
      }// next column                                              
      rs.close();
      rs = null;
      // at exit, each widget has been set with existing Protocol's properties
    }
    catch (SQLException e)
    {
      throw new LinxDbException("At getProtocolProperties(): " + e.getMessage());
    }
  }
  
  /** 
   * See Task interface comment.
   * @param request
   * @param response
   * @param user
   * @param db
   */
  public void createAnyNewItems(HttpServletRequest request, HttpServletResponse response,
      User user, Db db)
  {
    String itemType = null;
    
    Code.debug(WtDOMUtils.domToString(getServerDOM()));
    
    // check each item in this server dom for new flags
    ListIterator itemsItor = this.getServerItems().listIterator();
    while(itemsItor.hasNext())
    {
      // next item in server DOM
      EMREItem eMREItem = new EMREItem(((Item)itemsItor.next()).getItemElement());
      itemType = eMREItem.getItemType();

      // is item flagged as new=y or new=optional?
      if(!eMREItem.isNew())
      {
        continue; // not new
      }
       
      // new or optionally new item
      // -- relies on verifyItemTypes() to check for (non)existence
      ListIterator valuesItor = eMREItem.getValues().listIterator();
      while(valuesItor.hasNext())
      {
        String itemId = (String)valuesItor.next();
        if( WtUtils.isNullOrBlankOrPlaceholder(itemId) && eMREItem.isOptional())
        {
          continue;
        }
        if( eMREItem.isNewOptional() && db.getHelper().isItemExisting(itemId, itemType, db))
        {
          continue;
        }
        try
        {
          db.getHelper().addItem(itemId, itemType, getTranId(), db);
          db.getHelper().addItemHistory(itemId, itemType, null, null, null, null, getTranId(), db);
        }
        catch(LinxDbException ex)
        {
          throw new LinxUserException("Could not create new item " + itemId 
              + " of type " + itemType + ": " + ex.getMessage()); 
        }
        // next valid new item
      }// next new itemId
    }// next item in server dom
    // at exit, have created new ITEMS records for any new items among values
  }
  
  /**
   * Called by many tasks from createAnyNewData().
   * Handles many display-side itemtypes
   * that should be stored as attributes of the appliesTo
   * item, without having to add each to the server-side
   * task def. Allows easier maintenance of task def when
   * requirements are changing rapidly.
   * @param appliesToItemType
   * @param db
   */
  public void createAnyNewData(String appliesToItemType, Db db)
  {

      String sitem = getServerItemValue(appliesToItemType);
      // use client-side display items to record item properties
      List ditems = this.getDisplayItems();
      ListIterator itor = ditems.listIterator();
      while(itor.hasNext())
      {
          DisplayItem ditem = (DisplayItem)itor.next();
          String itemType = ditem.getItemType();
          if(ditem.getWidget().equals(LinxConfig.WIDGET.BUTTON)
              || ditem.getWidget().equalsIgnoreCase("SAVEBUTTON")
              || ditem.getWidget().equalsIgnoreCase("VERIFYBUTTON")
              || ditem.getWidget().equals("rowsets")
              || ditem.getItemType().indexOf("Placeholder") > -1)
          {
              // skip buttons and UI tables
              continue;
          }
          try
          { 
            getServerItem(itemType);
            continue; // not interested in display items with server-side items
          }
          catch (RuntimeException e)
          { // we *are* interested in display items *without* server-side items
            ; // ignore
          }
          // if we get to here, this display item has no server-side pair
          // eliminate buttons and tables
          String value = getDisplayItemValue(itemType);
          if(!WtUtils.isNullOrBlankOrPlaceholder(value))
          {
            // among many optional itemTypes, user has filled out this one
            db.getHelper().addData(sitem, appliesToItemType, "0", value, itemType, getTranId(), db);
          }
          
      }// next displayItem
      // at exit, have added new data to appliesToItem
  }
   /**
	 * Creates a bulk insert file and executes the input sp
	 * 
	 * @param sb
	 *            StringBuffer containing the data for bulk insert
	 * @param spName
	 *            name of stored proc
	 * @param tranId
	 * @param db
	 */
	public void bulkInsert(StringBuffer sb, String spName, long tranId, Db db)
	{
		String PARENTTYPE = "System Properties";
		String APPVALUETYPE = "Bulk Insert Local Dir";

		// get the local path for the bulk insert file known to SQL Server
		String sqlServBulkPath = dbHelper.getApplicationValue(db, PARENTTYPE,
				APPVALUETYPE);
		if (sqlServBulkPath == null)
		{
			throw new LinxSystemException(
					"Local SQL Server path for bulk insert cannot be found. "
							+ "Please notify the LIMS administrator to set APPVALUE "
							+ "'" + PARENTTYPE + "'/'" + APPVALUETYPE + "'.");
		}
		File f = new File(sqlServBulkPath);

		boolean bMade = false;
		if (!f.exists())
		{
			bMade = f.mkdir();
			if (!bMade)
			{
				throw new LinxSystemException("Cannot create the directory: "
						+ sqlServBulkPath
						+ ".  Please notify the LIMS administrator.");
			}
		}
		if (!f.canRead())
		{
			throw new LinxSystemException("Cannot read directory: "
					+ f.getAbsolutePath());
		}
		if (!f.canWrite())
		{
			throw new LinxSystemException("Cannot write to directory: "
					+ f.getAbsolutePath());
		}
		f = null;
		// keep sync'd with what SQL expects
		String sFullLocalPath = sqlServBulkPath + tranId + ".csv";
		File output = new File(sFullLocalPath);
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(output);
			fos.write(sb.toString().getBytes());
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			throw new LinxUserException("At bulk insert: " + e.getMessage());
		}
		catch (IOException e)
		{
			throw new LinxUserException("At bulk insert: " + e.getMessage());
		}
		fos = null;
		sb = null;
		output = null;
		ArrayList<String> params = new ArrayList<String>();
		params.add(sFullLocalPath);
		params.add(tranId + "");
		String s = db.getHelper().callStoredProc(db, spName, params, false,
				true);
		// now clean up the file
		File sqlFile = new File(sFullLocalPath);
		if (sqlFile.exists())
		{
			sqlFile.delete();
		}
		sqlFile = null;
		// throw new LinxUserException("Got thru sp");

	}
  
 
  /**
	 * Validate that the inline headings in the file match the required headings
	 * for this file type.
	 * 
	 * @param reqHeads
	 * @param actualHeads
	 */
	public void validateFileHeaders(String[] reqHeads, Vector actualHeads)
	{
		try
		{
			String errs = "Missing required file headers: ";
			for(String reqHead : reqHeads)
			{
				boolean bFound = false;
				for(Object s : actualHeads)
				{
					String sHead = (String)s;
					if(reqHead.equalsIgnoreCase(sHead.trim()))
					{
						bFound = true;
						break;
					}		
				}
				if(!bFound)
					errs += reqHead + ", ";
			}
			if(!errs.equals("Missing required file headers: "))
			{
				errs.replace(errs.charAt(errs.lastIndexOf(',')), ' ');
				throw new Exception(errs);
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred during file validation: " + ex.getMessage());
		}
	}
	
	/**
	 * validate that the inline headings in the file match the required headings for this file type
	 * @param reqHeads
	 * @param actualHeads
	 */
	public void validateColumnHeaders(String[] reqHeads, Vector actualHeads)
	{
		try
		{
			String errs = "Missing required column headers: ";
			for(String reqHead : reqHeads)
			{
				boolean bFound = false;
				for(Object s : actualHeads)
				{
					String sHead = (String)s;
					if(reqHead.equalsIgnoreCase(sHead.trim()))
					{
						bFound = true;
						break;
					}		
				}
				if(!bFound)
					errs += reqHead + ", ";
			}
			if(!errs.equals("Missing required column headers: "))
			{
				errs.replace(errs.charAt(errs.lastIndexOf(',')), ' ');
				throw new Exception(errs);
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred during file validation: " + ex.getMessage());
		}
	}

	
	/**
	   * copies a file from one path to another
	   * @author bobbyjo
	   * @param sourcePath
	   * @param destPath
	   */
	  public void copyFile(String sourcePath, String destPath)
	  {
		  try
	      {
	          FileInputStream fis = new FileInputStream(sourcePath);
	          FileOutputStream fos = new FileOutputStream(destPath);
	          FileChannel source = fis.getChannel();
	          FileChannel dest = fos.getChannel();
	          int maxCount = 32 * 1024 * 1024;
	          long size = source.size();
	          long position = 0;
	          while (position < size) 
	          {
	              position += source.transferTo(position, maxCount, dest);
	              Thread.sleep(10);
	          }
	          if(dest.isOpen())
	              dest.close();
	          if(source.isOpen())
	              source.close();
	          try
	          {
	              fis.close();
	              fos.close();
	              fis = null;
	              fos = null;
	          }
	          catch(Exception ex)
	          {
	              // ignore.
	          }
	      }
	      catch(Exception e)
	      {
	    	  e.printStackTrace();
	    	  throw new LinxUserException("Unable to copy file " + sourcePath + 
	    			  " to " + destPath + "\r\n" + e.getMessage());
	      }
	  }
	  /**
	   * zero pads a string to the appropriate length
	   * @param strToPad
	   * @param numDigits
	   * @return
	   */
	  public static String zeroPad(int strToPad, int numDigits)
	  {
		//lets zero pad the string
		  String rtn = String.valueOf(strToPad);
		  do
		  {
			  if(rtn.length() < numDigits)
			    {
			    	rtn = "0" + rtn;
			    }
		  }
		  while(rtn.length() < numDigits);
		    
		  return rtn;
	  }
	  
  public String zeroPadPosition(String position)
  {
	//lets zero pad the new position
	    if(position.length() < 2)
	    {
	    		position = "0" + position;
	    }
	    return position;
  }
  
    public String zeroPadPosition(int position)
  {
	//lets zero pad the new position
	    if(position < 10)
	    {
	    		return "0" + position;
	    }
	    return position + "";
  }
  
  public boolean doesLocationExist(String freezer, String box,
			String position, Db db)
	throws Exception
	{
		try
		{
			boolean bRtn = false;
			String sql = "exec spMet_doesStrainLocationExist '" + freezer + 
				"','" + box + "','" + position + "'";
			String rtn = dbHelper.getDbValue(sql, db);
			if(WtUtils.isNullOrBlank(rtn))
				rtn = "false";
			bRtn = Boolean.valueOf(rtn);
			return bRtn;
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
  
  public String validateFeedMedium(String feedMed)
  {
		if(WtUtils.isNullOrBlankOrPlaceholder(feedMed))
			throw new LinxUserException("Please select a feed medium and then try again.");
		return feedMed;
  }
  
  public String validateFeedRate(String feed)
  	throws Exception
  {
	  try
	  {
		  if(WtUtils.isNullOrBlankOrPlaceholder(feed))
				throw new Exception();
		  float feedRate = Float.parseFloat(feed);
		  if(feedRate < 0 || feedRate > 9999.99)
			  throw new Exception();
		  feed = String.valueOf(feedRate);
	  }
	  catch(Exception ex)
	  {
		  throw new LinxUserException("Please enter a numeric value between 0 and 10000 for feed rate.");
	  }
		return feed;
  }
  
  public String validateIrradiance(String irr, boolean bAllowNull)
	{
	  String sIrr = null;
	  try
		{
		  if(WtUtils.isNullOrBlankOrPlaceholder(irr))
		  {
			  if(!bAllowNull)
				  throw new Exception();
		  }
		  else
		  {
			  BigDecimal bd = new BigDecimal(irr);
			  int iIrr = bd.intValue();
			  if(iIrr < 0 || iIrr > 5000)
					throw new Exception();
			  sIrr = String.valueOf(iIrr);
		  }
			
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter an integer value between 0 and 5000 for irradiance and then try again.");
		}
		return sIrr;
	}
  
  public String validateTemperature(String temp, boolean bAllowNull)
	{
	  String sTemp = null;
	  try
		{
		  
		  if(WtUtils.isNullOrBlankOrPlaceholder(temp))
		  {
			  if(!bAllowNull)
				  throw new Exception();
		  }
		  else
		  {
			  BigDecimal bd = new BigDecimal(temp);
			  int iTemp = bd.intValue();
			  if(iTemp < 0 || iTemp > 100)
				throw new Exception();
			  sTemp = String.valueOf(iTemp);
		  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter an integer value between 0 and 100 for temperature and then try again.");
		}
		return sTemp;
	}
  
  public String validatePH(String pH, boolean bAllowNull)
	{
		try
		{
			 if(WtUtils.isNullOrBlankOrPlaceholder(pH))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
				  float ph = Float.parseFloat(pH);
				  if(ph < 0 || ph > 14)
					  throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a pH value between 0 and 14 and then try again.");
		}
		return pH;
	}
  
  public String validateCO2(String co2, boolean bAllowNull)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(co2))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
				  float fCO2 = Float.parseFloat(co2);
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric CO2 value and then try again.");
		}
		return co2;
	}
	
	public String validateO2(String o2, boolean bAllowNull)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(o2))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
				  float fO2 = Float.parseFloat(o2);
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric O2 value and then try again.");
		}
		return o2;
	}
	
	public String validateFlocculence(String floc, boolean bAllowNull, Db db)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(floc))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
					ArrayList<String> lsFloc = new ArrayList<String>();
					String sql = "exec spEMRE_getFlocculence";
					lsFloc = dbHelper.getListEntries(sql, db);
					
					if(!lsFloc.contains(floc))
						throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please check the flocculence dropdown for valid entries and try again.");
		}
		return floc;
	}
	
	public String validateIsolationMethod(String isoMeth, boolean bAllowNull, Db db)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(isoMeth))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
					ArrayList lsIso = new ArrayList();
					String sql = "exec spEMRE_getIsolationMethod";
					lsIso = db.getHelper().getListEntries(sql, db);
					
					if(!lsIso.contains(isoMeth))
						throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please check the isolation method dropdown for valid entries and try again.");
		}
		return isoMeth;
	}
	
	public String validateArchiveMethod(String arcMeth, boolean bAllowNull, Db db)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(arcMeth))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
					ArrayList<String> lsIso = new ArrayList<String>();
					String sql = "exec spEMRE_getArchiveMethod";
					lsIso = db.getHelper().getListEntries(sql, db);
					
					if(!lsIso.contains(arcMeth))
						throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please check the archive method dropdown for valid entries and try again.");
		}
		return arcMeth;
	}
	
	public String validateVesselType(String ves, boolean bAllowNull, Db db)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(ves))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
					ArrayList<String> lsIso = new ArrayList<String>();
					String sql = "exec spEMRE_getVesselType";
					lsIso = db.getHelper().getListEntries(sql, db);
					
					if(!lsIso.contains(ves))
						throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please check the vessel type dropdown for valid entries and try again.");
		}
		return ves;
	}
	
	public String validateEnrichmentResult(String er, boolean bAllowNull, Db db)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(er))
			  {
				  if(!bAllowNull)
					  throw new Exception();
			  }
			  else
			  {
					ArrayList<String> lsIso = new ArrayList<String>();
					String sql = "exec spEMRE_getEnrichmentResult";
					lsIso = db.getHelper().getListEntries(sql, db);
					
					if(!lsIso.contains(er))
						throw new Exception();
			  }
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please check the enrichment result dropdown for valid entires and try again.");
		}
		return er;
	}
  
  public String validateDO(String DO)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(DO))
				throw new Exception();
			int dO = Integer.parseInt(DO);
			if(dO < -1 || dO > 100)
				throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a DO value between -1 and 100 and then try again.");
		}
		return DO;
	}
  
  
  public String validateInitialAgitation(String initAg)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(initAg))
				throw new Exception();
			int dO = Integer.parseInt(initAg);
			if(dO < 0 || dO > 1500)
				throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter initial agitation value between 0 and 1500 and then try again.");
		}
		return initAg;
	}
  
  
  public String validateAirflow(String airflow)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(airflow))
				throw new Exception();
			float af = Float.parseFloat(airflow);
			if(af < 0 || af > 50)
				throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric airflow rate between 0 and 50 and then try again.");
		}
		return airflow;
	}
  
  
  public String validateInitialVolume(String vol)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(vol))
				throw new Exception();
			float iv = Float.parseFloat(vol);
			if(iv < 1)
				throw new Exception();
			if(iv > 99999.99999)
				throw new Exception();
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric value between 1 and 99,999 for initial volume and then try again.");
		}
		return vol;
	}
  
  public String validateSupplementConc(String item)
	{
		try
		{
			if(WtUtils.isNullOrBlankOrPlaceholder(item))
				return item;
			float conc = Float.parseFloat(item);
		}
		catch(Exception ex)
		{
			throw new LinxUserException("Please enter a numeric value for " + item + " and then try again.");
		}
		return item;
	}
  
  
  /** 
   * Overridden to handle special case of different placeholder (Any) for 
   * report task.
   * Runs the SQL queries that are defined in any 'sql' elements 
   *  under any display item elements.  db.getHelper().getXMLResultSet is used to get
   *  the results from the database and append <values> nodes to items.  
   *  dom is unchanged if nothing is to be done.
   *  
   *  See com.wildtype.linx.db.getHelper()#getXMLResultSet
   * @param user
   * @param db
   */
//  public void populateSQLValues(User user, Db db)
//  {
//	if(this.getTaskName().equalsIgnoreCase("Run Culture Report A"))
//	{
//   		populateSQLValues(user, "(Any)",db);
//	}
//	else
//	{
//	    //WtDOMUtils.setPlaceholderValue(this, Strings.WIDGET.DROPDOWN_PLACEHOLDER_VALUE, db);
//	    populateSQLValues(user, Strings.WIDGET.DROPDOWN_PLACEHOLDER_VALUE,db);
//	}
//    // at exit, items with sql nodes have query results added as values
//  }
  /**
   * used in the AChem tasks to add -Acid and -Base to FAME requests
   * @param file
   * @return
   */
  public File processFAMERequest(File file, XLSParser fileData)
	{
		 FileInputStream fis = null;
		 FileOutputStream stream = null;
		 HSSFWorkbook wb = null;
		 //File out = null;
		 try
		 {
			 fis = new FileInputStream(file);
			 POIFSFileSystem fs = new POIFSFileSystem(fis);
		     wb = new HSSFWorkbook(fs);
		     //String outfile = file.getParent() + "\\AchemRequest_" + getTranId() + ".xls";
		     //out = new File(outfile);
		     stream = new FileOutputStream(file);
		     String login = (String)fileData.getInlineProperty("Requester");
		     if(WtUtils.isNullOrBlank(login))
		    	 login = "";
			 for (int k = 0; k < wb.getNumberOfSheets(); k++)
	         {
	             HSSFSheet sheet = wb.getSheetAt(k);
	             int       rows  = sheet.getPhysicalNumberOfRows();
	             System.out.println("Sheet " + k + " \""
	                                + wb.getSheetName(k) + "\" has "
	                                + rows + " row(s).");

	             boolean bFoundColumns = false;
	             for (int m = 0; m < rows; m++)
	             {
	                 HSSFRow row   = sheet.getRow(m);
	             
	                 if (row == null)
	                 {
	                     continue;
	                 }
	                 
	                 HSSFCell cell = row.getCell((short)0);
	                 String cellVal = cell.getStringCellValue();
	                
	                 if(cellVal.equalsIgnoreCase("Submission ID"))
	                 {
	                	 bFoundColumns = true;
	                	 rows = m;
	                	 break;
	                 }
	              }
	             Date date = new Date();
				 SimpleDateFormat converted = new SimpleDateFormat("yyyyMMdd");
				 String yyyyMMdd =  converted.format(date);
				 String yyMMdd = yyyyMMdd.substring(2);
				 
				 String requestId = (String)fileData.getInlineProperty("Request ID");
	             //now lets write out the data
	             int nextRow = rows + 1;
	             int nextSubmitId = 1;
	             //lets loop through the samples 
	             if(fileData.gotoFirst())
	             {
	            	 do
	            	 {
	            		//String submitId = fileData.getProperty("Submission ID");
	            		String sample = fileData.getProperty("LIMS ID");
	            		if(sample.indexOf("-Acid") > 0 || sample.indexOf("-Base") > 0)
	            		{
	            			//these rows already have -acid and -base on them for FAMe - just continue
	            			continue;
	            		}
	            		String limsIdType = fileData.getProperty("LIMS ID Type");
	            		String samplingTimepoint = fileData.getProperty("Sampling Timepoint");
	            		String dilution = fileData.getProperty("Dilution");
	            		String comment = fileData.getProperty("Comment");
	            		String submissionId = yyMMdd + "_" + requestId + "_" + EMRETask.zeroPad(nextSubmitId, 3);
	            		HSSFRow row  = sheet.createRow(nextRow);
	            		
	            		 HSSFCell cell = row.createCell((short)0);
		            	 HSSFCell cell1 = row.createCell((short)1);
		            	 HSSFCell cell2 = row.createCell((short)2);
		            	 HSSFCell cell3 = row.createCell((short)3);
		            	 HSSFCell cell4 = row.createCell((short)4);
		            	 HSSFCell cell5 = row.createCell((short)5);
		            	 
		            	 cell.setCellValue(submissionId);
		            	 cell1.setCellValue(limsIdType);
		            	 if(sample.indexOf("-Acid") < 0)
		            		 cell2.setCellValue(sample + "-Acid");
		            	 else
		            		 cell2.setCellValue(sample);
		            	 cell3.setCellValue(samplingTimepoint);
		            	 cell4.setCellValue(dilution);
		            	 cell5.setCellValue(comment);
		            	 
		            	 nextRow++;
		            	 nextSubmitId++;
		            	 submissionId = yyMMdd + "_" + requestId + "_" + EMRETask.zeroPad(nextSubmitId, 3);
		            	 row  = sheet.createRow(nextRow);
		            		
	            		 cell = row.createCell((short)0);
		            	 cell1 = row.createCell((short)1);
		            	 cell2 = row.createCell((short)2);
		            	 cell3 = row.createCell((short)3);
		            	 cell4 = row.createCell((short)4);
		            	 cell5 = row.createCell((short)5);
		            	 
		            	 cell.setCellValue(submissionId);
		            	 cell1.setCellValue(limsIdType);
		            	 if(sample.indexOf("-Base") < 0)
		            		 cell2.setCellValue(sample + "-Base");
		            	 else
		            		 cell2.setCellValue(sample);
		            	 cell3.setCellValue(samplingTimepoint);
		            	 cell4.setCellValue(dilution);
		            	 cell5.setCellValue(comment);
		            	 nextRow++;
		            	 nextSubmitId++;
	            	 }
	            	 while(fileData.gotoNext());
	             }
	             wb.write(stream);
	             stream.close();
	             fis.close();
	         }
		 }
		 catch(Exception ex)
		 {
			 try
			 {
				 wb.write(stream);
				 fis.close();
				 fis = null;
				 stream.close();
				 stream = null;
			 }
			 catch(Exception ignoreMe)
			 {
				 
			 }
			 throw new LinxUserException("Error occurred when writting sample sheet:" + ex.getMessage());
		 }
		return file;
	}
  /**
	 * validates that all of the data in the 'LIMS ID' column of the file 
	 * exists as items in the db and are of the same item type
	 * @param data
	 * @return the item type
	 */
	public void validateIds(XLSParser data, ArrayList<String> alValidItemTypes, Db db)
	{
		try
		{
			String errMsg = "";
			if(data.gotoFirst())
			{
				do
				{
					String id = data.getRequiredProperty("LIMS ID");
					String type = data.getRequiredProperty("LIMS ID Type");
					if(!alValidItemTypes.contains(type))
						throw new LinxUserException("Invalid LIMS ID Type.  Please ensure that all rows have a valid 'LIMS ID Type' from the dropdown.");
					//for StrainCulture and ExperimentalCulture column 'SamplingTimepoint is required
					if(type.equalsIgnoreCase("StrainCulture") || type.equalsIgnoreCase("ExperimentalCulture"))
					{
						String timepoint = data.getRequiredProperty("Sampling Timepoint");
						//make sure the timepoint is the correct date format
						String mask = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2} (AM|PM)";
						Pattern p = Pattern.compile(mask);
						Matcher m = p.matcher(timepoint);
						if(!m.matches())
							throw new Exception("Sampling Timepoint '" + timepoint + "' is not of the correct pattern " + mask);
					}
					else
					{
						//we can't have sampling timepoints for items other than cultures
						String timepoint = data.getProperty("Sampling Timepoint");
						if(!WtUtils.isNullOrBlank(timepoint))
						{
							throw new LinxUserException("Sampling Timepoint needs to be blank for items other than StrainCulture or ExperimentalCulture");
						}
					}
					if(WtUtils.isNullOrBlank(id))
					{
						errMsg += "At line " + (data.currentRow + 1) + ": LIMS ID cannot be blank." + Strings.CHAR.NEWLINE;
						continue;
					}
					if(type.equalsIgnoreCase("lims id type"))
					{
						errMsg += "At line " + (data.currentRow + 1) + ": Please select a Lims ID Type from the dropdown." + Strings.CHAR.NEWLINE;
						continue;
					}
					try
					{
						//lets make sure this ID is of the correct type
						String itemid = null;
						try
						{
							itemid = dbHelper.getItemId(id, type, db);
						}
						catch(Exception ex)
						{
							//if this a fame request that is being edited it'll have -acid or -base on the end
							//need to remove that -acid and -base to see if item exists
							String acid = "-acid";
							String base = "-base";
							if(id.toLowerCase().indexOf(acid) > 0)
							{
								id = id.substring(0,id.length() - acid.length());
								itemid = dbHelper.getItemId(id, type, db);
							}
							else if(id.toLowerCase().indexOf(base) > 0)
							{
								id = id.substring(0,id.length() - base.length());
								itemid = dbHelper.getItemId(id, type, db);
							}
						}
						if(WtUtils.isNullOrBlankOrPlaceholder(itemid))
						{
							errMsg += "At line " + (data.currentRow + 1) + ": LIMS ID doesn't exist in the database or is not of type: " + type + "." + Strings.CHAR.NEWLINE;
							continue;
						}
					}
					catch(Exception ex)
					{
						throw new LinxDbException(ex.getMessage());
					}
				}
				while(data.gotoNext());
				//we've validated all of the lines in the file
				//lets see if we have any errors
				if(!WtUtils.isNullOrBlank(errMsg))
				{
					throw new LinxUserException("Errors occur in the file:\r\n" + errMsg);
				}
			}
		}
		catch(Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}
	
	/**
	 * loops through the file rows to check to see if any LIMS ID types are set as 'StrainCulture'
	 * or 'ExperiementalCulture'  If so, throw an error
	 * This method called when LIMS Tracking is set to no
	 * ExperimentalCulures and StrainCuluters are only allowed to be submitted when Lims Tracking = 'yes'
	 * @param data
	 */
	public void validateCultureType(XLSParser data)
	{

			String errMsg = "";
			
			if(data.gotoFirst())
			{
				do
				{
					String id = data.getRequiredProperty("LIMS ID");
					String type = data.getRequiredProperty("LIMS ID Type");
					if(type.equalsIgnoreCase("ExperimentalCulture") || 
							type.equalsIgnoreCase("StrainCulture"))
						throw new LinxUserException("For 'LIMS ID Types' of StrainCulture or ExperimentalCulture LIMS Tracking needs to be set to 'yes'");
					//only ExperimentalCultures and StrainCultures can have sampling timepoints so if we're here the timepoint needs to be null
					String timepoint = data.getProperty("Sampling Timepoint");
					if(!WtUtils.isNullOrBlank(timepoint))
					{
						throw new LinxUserException("Sampling Timepoint needs to be blank for items other than StrainCulture or ExperimentalCulture");
					}
				}
				while(data.gotoNext());
				//we've validated all of the lines in the file
			}

	}
	
	/**
	 * clears any text from dropdown, textarea, datepicker, etc  
	 * @param task
	 */
	public void clearDisplayValues()
	{
		try
		{
			List ditems = getDisplayItems();
		    ListIterator itor = ditems.listIterator();
		    while(itor.hasNext())
		    {
		    	DisplayItem ditem = (DisplayItem)itor.next();
		        String itemType = ditem.getItemType();
		        if(ditem.getWidget().equals(LinxConfig.WIDGET.BUTTON)
		              || ditem.getWidget().equalsIgnoreCase("SAVEBUTTON")
		              || ditem.getWidget().equalsIgnoreCase("VERIFYBUTTON")
		              || ditem.getWidget().startsWith("rowsets")
		              || ditem.getItemType().indexOf("Placeholder") > -1)
		        {
		              // skip buttons and UI tables
		              continue;
		        }
		       
		        String value = getDisplayItemValue(itemType);
		        if(!WtUtils.isNullOrBlankOrPlaceholder(value))
		        {
		        	if(ditem.getWidget().equalsIgnoreCase(Strings.WIDGET.DROPDOWN))
		        	{
		        		getDisplayItem(itemType).setSelectedValue("(Select)");
		        	}
		        	else
		        	{
		        		getDisplayItem(itemType).clearValues();
		        	}
		           
		        }
		          
		      }// next displayItem
		}
		catch(Exception ex)
		{
			throw new LinxSystemException(ex.getMessage());
		}
	}
	/**
	 * clears all server item values  
	 * @param task
	 */
	public void clearServerValues()
	{
		try
		{
			List sitems = getServerItems();
		    ListIterator itor = sitems.listIterator();
		    while(itor.hasNext())
		    {
		    	Item sitem = (Item)itor.next();
		        String itemType = sitem.getItemType();
		        getServerItem(itemType).clearValues();
		    }
		}
		catch(Exception ex)
		{
			throw new LinxSystemException(ex.getMessage());
		}
	}
	
	public String convertDate(String dateStarted) 
	{
		String cd = null;
		if(WtUtils.isNullOrBlank(dateStarted))
		{
			throw new LinxUserException("Date cannot be null.");
		}
		if(dateStarted.indexOf('/') > 0)
		{
			throw new LinxUserException(" Date " + dateStarted + " is not in expected format 'yymmdd'.");
		}
		try
		{
			SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
			Date d = df.parse(dateStarted);
			
			SimpleDateFormat converted = new SimpleDateFormat("yyyy-MM-dd");		
			cd = converted.format(d);
		}
		catch(Exception ex)
		{
			throw new LinxUserException("  Date is not in expected format yyMMdd: " + ex.getMessage());
		}
		return cd;

	}
	
	public void insertLocation(HttpServletRequest request, String tableName, 
			int locationColumn, String item, String sql, Db db)
	{
		//save the locations   
	    TableDataMap rowMap = new TableDataMap(request, tableName);
	    int numRows = rowMap.getRowcount();
		for(int rowIdx = 1; rowIdx <= numRows; rowIdx++)
		{
			String location = (String)rowMap.getValue(rowIdx, locationColumn);
			if(location.indexOf(":") < 1)
			{
			  throw new LinxUserException("Please provide a location in the format FRZ:BOX:POS,"
					  + " then try again.");
			}
			String[] alLocs = location.split(":");
			String freezer = alLocs[0];
			String box = alLocs[1];
			String coord = alLocs[2];
			 
			try
			{
				  int idxLastColon = location.lastIndexOf(':');
				  String pos = location.substring(idxLastColon + 1);
				  int iPos = Integer.parseInt(pos);
				  //now lets zero pad the position
				  if(pos.length() < 2)
				  {
					  coord = EMRETask.zeroPad(iPos, 2);
				  }
			}
			catch(Exception ex)
			{
				  throw new LinxUserException(ex.getMessage());
			}
			ArrayList<String> params = new ArrayList<String>();
			params.add(item);
			params.add(freezer); 
			params.add(box);
			params.add(coord);
			params.add(rowIdx+""); //location index
			params.add(getTranId()+"");
			
			dbHelper.callStoredProc(db, sql, params, false, true);
		}// next loc index
		// at exit, have updated locations  
	}
	
	/**
	 * Prints small (1" X 0.5 ") barcodes on the S4M printer.
	 * @param request
	 * @param barcode
	 * @param spName
	 * @param db
	 */
	 public void printLibraryLabels(HttpServletRequest request, String barcode, String spName, Db db)
	  {
		  //lets make sure we did a save first.
		  String notebook = null;
		  String location = null;
		  try
		  {
			  //we need to update the library table with the barcode
			  String libraryId = dbHelper.getDbValue("exec spEMRE_getLibraryId '" + barcode + "'", db);
			  //lets remove the "SGI-E" so the barcode fits on the label
			  String shortbarcode = barcode;
			  shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			  dbHelper.executeSQL("exec spEMRE_updateBarcode 'library'," + libraryId + ",'" + shortbarcode + "'", db);
			  //now print labels
			  EMREDbHelper dbHelper = new EMREDbHelper();
			  PrintService printService = dbHelper.getPrintServiceForTask(getTaskName(), db);
			  String sql = "exec " + spName + " '" + barcode + "'";
			  ResultSet rs = db.getHelper().getResultSet(sql, db);
			  while(rs.next())
			  {
				  notebook = rs.getString(2);
				  location = rs.getString(3);
				  
				  if(WtUtils.isNullOrBlank(notebook))
				  {
					  throw new LinxUserException("Please save the locations before printing labels (click [Save]).");
				  }
				  //add NB as a prefix to the notebook page
				  notebook = "NB" + notebook;
				  S4MSmallBarcode print = new S4MSmallBarcode();
				  location = "FZ" + location;
				  ArrayList<String> alrows = new ArrayList<String>();
				  alrows.add(notebook);
				  alrows.add(location);
				  String label = print.getZPLforLabel(shortbarcode, alrows);
				  S4MSmallBarcode.print(printService, shortbarcode, label);
				  Thread.sleep(200);
				  alrows.clear();
				  alrows = null;
			  }
			  rs.close();
			  rs = null;
		  }
		  catch(Exception ex)
		  {
			  throw new LinxDbException(ex.getMessage());
		  }
		  setMessage("Successfully printed barcodes.");
	  }
	 
	 public void printSampleLabels(HttpServletRequest request, String barcode, String spName, Db db)
	 {
		 //lets make sure we did a save first.
		  String notebook = null;
		  String location = null;
		  String internalId = null;
		  try
		  {
			  //we need to update the sample table with the new barcode
			  String sampleId = dbHelper.getDbValue("exec spEMRE_getSampleId '" + barcode + "'", db);
			  //lets remove the "SGI-E" so the barcode fits on the label
			  String shortbarcode = barcode;
			  shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			  dbHelper.executeSQL("exec spEMRE_updateBarcode 'sample'," + sampleId + ",'" + shortbarcode + "'", db);
			  //now print the barcode
			  EMREDbHelper dbHelper = new EMREDbHelper();
			  PrintService printService = dbHelper.getPrintServiceForTask("Environmental Sample Logging", db);
			  String sql = "exec " + spName + " '" + barcode + "'";
			  ResultSet rs = db.getHelper().getResultSet(sql, db);
			  while(rs.next())
			  {
				  notebook = rs.getString(2);
				  location = rs.getString(3);
				  internalId = rs.getString(4);
				  if(WtUtils.isNullOrBlank(notebook))
				  {
					  throw new LinxUserException("Please save the locations before printing labels (click [Save]).");
				  }
				  //add NB as a prefix to the notebook page
				  notebook = "NB:" + notebook;
				  S4MSmallBarcode print = new S4MSmallBarcode();
				  location = "FZ:" + location;
				  internalId = "InID:" + internalId;
				  ArrayList<String> alrows = new ArrayList<String>();
				  alrows.add(notebook);
				  alrows.add(location);
				  alrows.add(internalId);
				  print.setFontType("R");
				  print.setStartPrintYCoord(30);
				  String label = print.getZPLforBoldBCLabel(shortbarcode, alrows);
				  S4MSmallBarcode.print(printService, shortbarcode, label);
				  Thread.sleep(200);
				  alrows.clear();
				  alrows = null;
			  }
			  rs.close();
			  rs = null;
		  }
		  catch(Exception ex)
		  {
			  throw new LinxDbException(ex.getMessage());
		  }
		  setMessage("Successfully printed barcodes.");
	 }
	 
	 public void printDNALabels(HttpServletRequest request, String barcode, String spName, Db db)
	 {
		 //lets make sure we did a save first.
		  String notebook = null;
		  String location = null;
		  try
		  {
			//we need to update the nucleicAcid table with the barcode
			  String dnaId = dbHelper.getDbValue("exec spEMRE_getNucleicAcidId '" + barcode + "'", db);
			  //lets remove the "SGI-E" so the barcode fits on the label
			  String shortbarcode = barcode;
			  shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			  dbHelper.executeSQL("exec spEMRE_updateBarcode 'nucleicAcid'," + dnaId + ",'" + shortbarcode + "'", db);
			  //now print labels
			  EMREDbHelper dbHelper = new EMREDbHelper();
			  PrintService printService = dbHelper.getPrintServiceForTask("Environmental Sample Logging", db);
			  String sql = "exec " + spName + " '" + barcode + "'";
			  ResultSet rs = db.getHelper().getResultSet(sql, db);
			  while(rs.next())
			  {
				  notebook = rs.getString(2);
				  location = rs.getString(3);
				  
				  if(WtUtils.isNullOrBlank(notebook))
				  {
					  throw new LinxUserException("Please save the locations before printing labels (click [Save]).");
				  }
				  //add NB as a prefix to the notebook page
				  notebook = "NB" + notebook;
				  S4MSmallBarcode print = new S4MSmallBarcode();
				  location = "FZ" + location;
				  ArrayList<String> alrows = new ArrayList<String>();
				  alrows.add(notebook);
				  alrows.add(location);
				  String label = print.getZPLforLabel(shortbarcode, alrows);
				  S4MSmallBarcode.print(printService, shortbarcode, label);
				  Thread.sleep(200);
				  alrows.clear();
				  alrows = null;
			  }
			  rs.close();
			  rs = null;
		  }
		  catch(Exception ex)
		  {
			  throw new LinxDbException(ex.getMessage());
		  }
		  setMessage("Successfully printed barcodes.");
	 }
	 
	 public void printPCRLabels(String barcode, Db db)
	 {
		 try
		 {
			 S4MPCRBarcode printer = new S4MPCRBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Archive Culture", db);
			 ArrayList<String> alRow = new ArrayList<String>();
			 
			 //only one barcode to print
			 String label = printer.getZPLforLabel(barcode, alRow);
			 S4MPCRBarcode.print(printService, barcode, label); 
			 
		 }
		 catch(Exception ex)
		 {
			 throw new LinxSystemException(ex.getMessage());
		 }
	 }
	 
	 public void printVialLabels(String barcode, String isolate, Db db)
	 {
		 try
		 {
			 //we need to update the isolate table with the barcode
			  String isolateId = dbHelper.getDbValue("exec spEMRE_getIsolateId '" + isolate + "'", db);
			  //lets remove the "SGI-E" so the barcode fits on the label
			  String shortbarcode = barcode;
			  //shortbarcode = shortbarcode.replace("-SGI-E", ""); 
			  dbHelper.executeSQL("exec spEMRE_updateBarcode 'isolate'," + isolateId + ",'" + shortbarcode + "'", db);
			 //now print labels
			 S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Isolates", db);
			 ArrayList<String> alRow = new ArrayList<String>();
			 String sql = "exec spEMRE_getInfoForBulkPrint '" + barcode + "','','Isolate'";
			 ResultSet rs = dbHelper.getResultSet(sql, db);
			 while(rs.next())
			 {
				 alRow.add(rs.getString(2));
				 alRow.add(rs.getString(3));
			 }
			 rs.close();
			 rs = null;
			 //only one barcode to print 
			 printer.setFontType("R");
			 printer.setStartPrintYCoord(20);
			 String label = printer.getZPLforBoldBCLabel(shortbarcode, alRow);
			 S4MPCRBarcode.print(printService, shortbarcode, label); 
			 
		 }
		 catch(Exception ex)
		 {
			 throw new LinxSystemException(ex.getMessage());
		 }
	 }
	 
	 public void validateWell(String well)
	  {
		//lets make sure the well is an int and that it's 2 digits
			 int iWell = 0;
			 try
			 {
				 iWell = Integer.parseInt(well);
			 }
			 catch(Exception ex)
			 {
				 throw new LinxUserException("Well Number must be an integer.");
			 }
			 if(iWell > 96 || iWell < 1)
				 throw new LinxUserException("Well Number must be an integer between 1 and 96.");
		
	  }
	  
	 /**
	   * returns the fully qualified path of the file.
	   * @param request
	   * @param filePath
	   * @return
	   */
	  public String getFullyQualifiedPath(HttpServletRequest request, String filePath)
	  {
	      File srcFile = new File(filePath);
	      String srcFilename = srcFile.getName();
	      String srcPathOnly = srcFile.getParent();
	      if(WtUtils.isNullOrBlank(srcPathOnly))
	      {
	          // may be null if browser client
	          srcPathOnly = "";
	      }
	      String remoteComputerName = null;
	      String fullQualPathOnly = srcPathOnly;
	      String fullQualFilePath = null;
	      
	      // If path starts with "\\" we'll assume the user properly browsed to a 
	      // network computer and has a fully qualified path (e.g. \\MyComputer\Myfolder\Myfile.txt)
	      // otherwise
	      // lookup the remote computer name & manually build a fully qualified path
	      
	      if (srcPathOnly.startsWith("\\") == false)
	      {
	          try
	          {
	              // Remove everything from the end of the first single "\" to end of ":"
	              // Get remote computer's name
	              remoteComputerName = request.getRemoteHost();
	              
	              if (remoteComputerName.indexOf(".") >= 0)
	              {
	                  // ComputerName is IP address, see if we can resolve an actual DNS name
	                  // If we can't (server doesn't have reverse DNS lookup capability) the
	                  // IP address will be used for the computer name (which may lead to problems
	                  // working with the file later if IP addresses are dynamically assigned).
	                  String ip = remoteComputerName;
	                  String hostName = InetAddress.getByName(ip).getHostName();
	                  remoteComputerName = hostName;
	                  if(remoteComputerName.equalsIgnoreCase("localhost"))
	                  {
	                	  java.net.InetAddress i = java.net.InetAddress.getLocalHost();
		            	  remoteComputerName = i.getHostName();
	                  }
	              }
	             
	              srcPathOnly = "\\\\" + remoteComputerName + "\\" + srcPathOnly;

	              // Remove any explicit hard drive names from the path
	              // (e.g. \\MyComputer\C:\MyFolder becomes \\MyComputer\MyFolder)
	              int startOfFirstSlash = srcPathOnly.indexOf("\\", 2);
	              int startOfColon = srcPathOnly.indexOf(":");

	              if (startOfFirstSlash >= 0 && startOfColon >= 0)
	              {
	                  // Remove everything from the end of the first single "\" to end of ":"
	                  String firstPart = srcPathOnly.substring(0, startOfFirstSlash);
	                  String lastPart = srcPathOnly.substring(startOfColon + 1);
	                  fullQualPathOnly = firstPart + lastPart;
	                  
	                  if (fullQualPathOnly.endsWith("\\"))
	                  {
	                      // Last char in path is a "\", remove it as it will be added below
	                      fullQualPathOnly = fullQualPathOnly.substring(0, (fullQualPathOnly.length() - 1));
	                  }
	              }
	          }
	          catch (UnknownHostException ex)
	          {
	              Logger.getLogger(DefaultTask.class.getName()).log(Level.SEVERE, null, ex);
	          }
	      }
	      
	      fullQualFilePath = fullQualPathOnly + "\\" + srcFilename;
	      
	      return fullQualFilePath;
	  }
	  
	  public String getDbLimsIdType(String item, Db db, boolean bHaveFile)
	  {
		  String itemType = "TypeUnknown";
		  try
		  {
			  
			  boolean bItemExists = dbHelper.isEMREItemExisting(item, db);
			  //lets get it's type
			  String type = getServerItemValue("ItemType");
			  if(!WtUtils.isNullOrBlank(type))
			  {
				  setLimsIdType(type);
				  itemType = type;
			  }
			  else if(bItemExists)
			  {
				  if(WtUtils.isNullOrBlank(type))
				  {
					  List<String> lsTypes = getLimsIdTypes(item, this, db);
					  validateSingleItemType(item, lsTypes, bHaveFile);
						  
					  itemType = lsTypes.get(0);
					  setLimsIdType(itemType);
				  }
				  else
				  {
					  //item corresponds to the "LIMSID"
					  setLimsIdType(type);
					  itemType = type;
				  }
			  }
			  else
				  setLimsIdType(itemType);
		  }
		  catch(Exception ex)
		  {
			  throw new LinxSystemException(ex.getMessage());
		  }
		  return itemType;
	  }
	  
	  public String getDbLimsIdTypeReactorSetup(HttpServletRequest request, String column,
			  String item, Db db)
	  {
		  String itemType = "TypeUnknown";
		  try
		  { 
			  //lets get it's type
			  String type = request.getParameter(column + "ItemTypeArray");
			  if(WtUtils.isNullOrBlankOrPlaceholder(type))
				  throw new LinxUserException("Please select an item type from the dropdown menu.");
			
			  //item corresponds to the "LIMSID"
			  setLimsIdType(type);
			  itemType = type;
		  }
		  catch(Exception ex)
		  {
			  throw new LinxSystemException(ex.getMessage());
		  }
		  return itemType;
	  }
	  
	  public String getDbOriginIdType(String originItem, Db db, boolean bHaveFile)
	  {
		  String itemType = "TypeUnknown";
		  try
		  {
			  boolean bItemExists = dbHelper.isEMREItemExisting(originItem, db);
			  //lets get it's type
			  String type = getServerItemValue("OriginItemType");
			  if(!WtUtils.isNullOrBlank(type))
			  {
				  setOriginIdType(type);
				  itemType = type;
			  }
			  else if(bItemExists)
			  {
				  if(WtUtils.isNullOrBlank(type))
				  {
					  List<String> lsTypes = getOriginLimsIdTypes(originItem, this, db);
					  validateSingleOriginItemType(originItem, lsTypes, bHaveFile);
						  
					  itemType = lsTypes.get(0);
					  setOriginIdType(itemType);
				  }
				  else
				  {
					  //item corresponds to the "LIMSID"
					  setOriginIdType(type);
					  itemType = type;
				  }
			  }
			  else
			  {
				  setOriginIdType(itemType); 
			  }
				  
		  }
		  catch(Exception ex)
		  {
			  throw new LinxSystemException(ex.getMessage());
		  }
		  return itemType;
	  }
	  
	  
	  /**
	   * retrieves the item type(s) for the LIMSID item from the database 
	   * @param task
	   * @param db
	   * @return
	   */
	  public List<String> getLimsIdTypes(String item, Task task, Db db)
		{
			ArrayList<String> vals = new ArrayList<String>();
			try
			{
				vals = dbHelper.getListEntries("exec spEMRE_getItemTypes '" + item + "'", db);
			}
			catch(Exception e)
			{
				throw new LinxDbException(e.getMessage());
			}
			return vals;
		}
	  
	  /**
	   * retrieves the item type(s) for the OriginLIMSID item from the database 
	   * @param task
	   * @param db
	   * @return
	   */
	  public List<String> getOriginLimsIdTypes(String item, Task task, Db db)
		{
			ArrayList<String> vals = new ArrayList<String>();
			try
			{
				vals = dbHelper.getListEntries("exec spEMRE_getItemTypes '" + item + "'", db);
			}
			catch(Exception e)
			{
				throw new LinxDbException(e.getMessage());
			}
			return vals;
		}
	  
	  
	  /**
	   * determines if there is more than one item type and throws the appropriate error
	   * @param lsVals
	   * @throws Exception
	   */
	  public void validateSingleItemType(String item, List<String> lsVals, boolean bHaveFile)
	  	throws Exception
	  {
		  String types = "";
		  try
		  {
			  if(lsVals.size() > 1)
			  {
				  for(String s : lsVals)
				  {
					  types += "'" + s + "'" + Strings.CHAR.COMMA;
				  }
				  setLimsIdType("LIMSID");
				  setOriginIdType("OriginLIMSID");
				  throw new Exception("More than one item exists.");
			  }
		  }
		  catch(Exception ex)
		  {
			  
			  if(bHaveFile)
			  {
				  throw new Exception("More than one item type exists for the LIMS ID '" + item 
						  + "'.  Please choose an item type from the list ['" 
						  + types + "] and enter it into the optional 'LIMSIDType' field in the bulk import file and try again.");
			  }
			  else
			  {
				  getDisplayItem("ItemType").setValues(lsVals);
				  getDisplayItem("ItemType").setVisible(true);
				  throw new Exception("More than one item exists for the item '" + item 
						  + "'.  Please choose an item type from the dropdown.");
			  }
				  
		  }
	  }
	  
	  /**
	   * determines if there is more than on item type for the origin item and throws the appropriate error
	   * @param lsVals
	   * @throws Exception
	   */
	  public void validateSingleOriginItemType(String originItem, List<String> lsVals, boolean bHaveFile)
	  	throws Exception
	  {
		  String types = "";
		  try
		  {
			  if(lsVals.size() > 1)
			  {
				  for(String s : lsVals)
				  {
					  types += "'" + s + "'" + Strings.CHAR.COMMA;
				  }
				  setLimsIdType("LIMSID");
				  setOriginIdType("OriginLIMSID");
				  throw new Exception("More than one origin item exists.");
			  }
		  }
		  catch(Exception ex)
		  {
			  if(bHaveFile)
			  {
				  throw new Exception("More than one origin item type exists for the Origin LIMS ID '" + originItem 
						  + "'.  Please choose an origin item type from the list ['" 
						  + types + "] and enter it into the optional 'OriginIDType' field in the bulk import file and try again.");
			  }
			  else
			  {
				  getDisplayItem("OriginItemType").setValues(lsVals);
				  getDisplayItem("OriginItemType").setVisible(true);
				  throw new Exception("More than one origin item exists with the name '" 
						  + originItem + "'.  Please choose an origin item type from the dropdown.");
			  }
		   }
	  }
	  
	  
	  /**
	   * finds all instances of the LIMSID in the server DOM and replaces it with the appropriate itemtype
	   * also maps the LIMSID type to the correct type for use in subsequent processing
	   * @param request
	   * @param response
	   * @param user
	   * @param db
	   */
	  public void manipulateDOMLimsIdType(Db db)
	  {
		  //setLimsIdType("LIMSID");
		  try
		  {
			  Element req = getServerDOM();
			  Element eItems = (Element)req.getElementsByTagName(EMREStrings.DOMElement.ITEMS).item(0);
			  //System.out.println(WtDOMUtils.prettyPrintXML(eItems));

			  NodeList nlItems = eItems.getElementsByTagName(EMREStrings.DOMElement.ITEM);
				int numNodes = nlItems.getLength();
				for(int i = 0; i < numNodes; i++)
				{
					Element eItem = (Element)nlItems.item(i);//item node
					String domItemType = eItem.getAttribute(EMREStrings.DOMAttribute.ITEMTYPE);
					if(domItemType.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
					{
						//we found the item!
						eItem.setAttribute(EMREStrings.DOMAttribute.TYPE, EMREStrings.DOMAttribute.CONTAINER);
						//set the item type for this item
						eItem.setAttribute(EMREStrings.DOMAttribute.ITEMTYPE,getLimsIdType());
					}
					String appliesTo = eItem.getAttribute(EMREStrings.DOMAttribute.APPLIESTO);
					if(appliesTo != null && appliesTo.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
						eItem.setAttribute(EMREStrings.DOMAttribute.APPLIESTO, getLimsIdType());
					String contentType = eItem.getAttribute(EMREStrings.DOMAttribute.CONTENTTYPE);
					if(contentType != null && contentType.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
						eItem.setAttribute(EMREStrings.DOMAttribute.CONTENTTYPE, getLimsIdType());
					
					ArrayList<Element> alChildren = getChildren(eItem, null);//content, data, or file nodes
					int numChildren = alChildren.size();
					for(int j = 0; j < numChildren; j++)
					{
						Element eRow = alChildren.get(j);
						appliesTo = eRow.getAttribute(EMREStrings.DOMAttribute.APPLIESTO);
						if(appliesTo != null && appliesTo.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
							eRow.setAttribute(EMREStrings.DOMAttribute.APPLIESTO, getLimsIdType());
						contentType = eRow.getAttribute(EMREStrings.DOMAttribute.CONTENTTYPE);
						if(contentType != null && contentType.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
							eRow.setAttribute(EMREStrings.DOMAttribute.CONTENTTYPE, getLimsIdType());
					}
						  
				}
				 //System.out.println(WtDOMUtils.prettyPrintXML(eItems));
		  }
		  catch(Exception ex)
		  {
			  throw new LinxSystemException(ex.getMessage());
		  }
	  }
	  
	  /**
	   * finds all instances of the OriginLIMSID in the server DOM and replaces it with the appropriate itemtype
	   * also maps the OriginLIMSID type to the correct type for use in subsequent processing
	   * @param request
	   * @param response
	   * @param user
	   * @param db
	   */
	  public void manipulateDOMOriginLimsIdType(Db db)
	  {
		  //setOriginIdType("OriginLIMSID");
		  try
		  {
			  Element req = getServerDOM();
			  Element eItems = (Element)req.getElementsByTagName(EMREStrings.DOMElement.ITEMS).item(0);
			  //System.out.println(WtDOMUtils.prettyPrintXML(eItems));

			  NodeList nlItems = eItems.getElementsByTagName(EMREStrings.DOMElement.ITEM);
				int numNodes = nlItems.getLength();
				for(int i = 0; i < numNodes; i++)
				{
					Element eItem = (Element)nlItems.item(i);//item node
					String domItemType = eItem.getAttribute(EMREStrings.DOMAttribute.ITEMTYPE);
					if(domItemType.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
					{
						//we found the item!
						eItem.setAttribute(EMREStrings.DOMAttribute.TYPE, EMREStrings.DOMAttribute.CONTAINER);
						//set the item type for this item
						eItem.setAttribute(EMREStrings.DOMAttribute.ITEMTYPE,getOriginIdType());
					}
					String appliesTo = eItem.getAttribute(EMREStrings.DOMAttribute.APPLIESTO);
					if(appliesTo != null && appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
						eItem.setAttribute(EMREStrings.DOMAttribute.APPLIESTO, getOriginIdType());
					String contentType = eItem.getAttribute(EMREStrings.DOMAttribute.CONTENTTYPE);
					if(contentType != null && contentType.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
						eItem.setAttribute(EMREStrings.DOMAttribute.CONTENTTYPE, getOriginIdType());
					ArrayList<Element> alChildren = getChildren(eItem, null);
					int numChildren = alChildren.size();
					for(int j = 0; j < numChildren; j++)
					{
						Element eRow = alChildren.get(j);
						appliesTo = eRow.getAttribute(EMREStrings.DOMAttribute.APPLIESTO);
						if(appliesTo != null && appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
							eRow.setAttribute(EMREStrings.DOMAttribute.APPLIESTO, getOriginIdType());
						contentType = eRow.getAttribute(EMREStrings.DOMAttribute.CONTENTTYPE);
						if(contentType != null && contentType.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
							eRow.setAttribute(EMREStrings.DOMAttribute.CONTENTTYPE, getOriginIdType());
					}
						  
				}
				 //System.out.println(WtDOMUtils.prettyPrintXML(eItems));
		  }
		  catch(Exception ex)
		  {
			  throw new LinxSystemException(ex.getMessage());
		  }
	  }
	  
	  /**
	   * returns the child nodes of a particular element
	   * @param parent
	   * @param tagName
	   * @return
	   */
	  public static ArrayList<Element> getChildren(Element parent,String tagName)
	  {
		  ArrayList<Element>eRtn = new ArrayList<Element>();
	      NodeList list = parent.getChildNodes();

	      for (int i = 0; i < list.getLength(); i++)
	      {
	          if (list.item(i) instanceof Element)
	          {
	              Element elem = (Element)list.item(i);
	              if (tagName != null)
	              {
	                  if (elem.getNodeName().equals(tagName))
	                  {
	                      eRtn.add(elem);
	                  }
	              }
	              else
	              {
	                  eRtn.add(elem);
	              }
	          }
	      }
	      return eRtn;
	  }
	  
	  public void doEMRETaskWorkPreSave(HttpServletRequest request, boolean bHaveFile, Db db)
		{
			//get the unaltered task dom 
			if(getCleanDOM() == null)
			{
				DefaultTask cleanTask = new DefaultTask(this);
				setCleanDOM(cleanTask.getTaskDOM());
			}
			//reset the boolean for items of the same type
			bSameItemType = false;
			
			//retrieve the map of types
			//determine what types of items we really have
			String item = getServerItemValue(EMREStrings.ItemType.LIMS_ID);
			
			if(WtUtils.isNullOrBlankOrPlaceholder(item))
				throw new LinxUserException("Please enter a LIMS ID");
			
			//is the item retired?
			if(dbHelper.isRetired(item, db))
				throw new LinxUserException("LIMS ID '" + item + "' is retired and cannot be used.");
			String domLimsType = getLimsIdType();
			String limsIdType = getDbLimsIdType(item, db, bHaveFile);	
			boolean bItemExists = dbHelper.isItemExisting(item, limsIdType, db);
			String originItem = null;
			try
			{
				originItem = getServerItemValue(EMREStrings.ItemType.ORIGIN_LIMS_ID);
			}
			catch(Exception ignoreMe)
			{
				//there's no originItem in the server DOM - just carry on
			}
			if(!WtUtils.isNullOrBlankOrPlaceholder(originItem))
			{
				//is the origin item retired?
				if(dbHelper.isRetired(originItem, db))
					throw new LinxUserException("Origin LIMS ID '" + originItem + "' is retired and cannot be used.");
				String domOriginType = getOriginIdType();
				String originIdType = getDbOriginIdType(originItem, db, bHaveFile);
				
				//if both the origin and the lims id are of the same type then we need to 
				//do all of the processing of the origin id in custom code.
				if(!limsIdType.equalsIgnoreCase(originIdType))
				{
					bSameItemType = false;
					//determine what types we are working with
					if(domLimsType.equalsIgnoreCase("LIMSID"))
					{
						if(!WtUtils.isNullOrBlank(item))
						{
							this.manipulateDOMLimsIdType(db);
						}
					}
					if(domOriginType.equalsIgnoreCase("OriginLIMSID"))
					{
						if(!WtUtils.isNullOrBlank(originItem))
						{
							this.manipulateDOMOriginLimsIdType(db);
						}
					}
				}
				else
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
					     
					     if(domItemType.equalsIgnoreCase(EMREStrings.ItemType.LIMS_ID))
					     {
					    	 //we've found the item id 
					    	 //we need to check for addAsContent or addAsData to the OriginLIMSID
					    	 //we need to do this manually for items of the same type
						    if(!WtUtils.isNullOrBlank(item) && bItemExists)
							{
						    	NodeList lsContent = it.getAddAsContentsNodes();
						    	this.addContentForOriginId(lsContent, it.getItemElement(), item, limsIdType,
						    			originItem, originIdType, db);
						    	NodeList lsData = it.getAddAsDataNodes();
						    	this.addDataForOriginId(lsData, it.getItemElement(), item, 
						    			limsIdType, originItem, originIdType, db);
								this.manipulateDOMLimsIdType(db);
								this.manipulateDOMOriginLimsIdType(db);
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
						    	eData.setAttribute("dbItemType", limsIdType);
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

						        this.manipulateDOMLimsIdType(db);
								this.manipulateDOMOriginLimsIdType(db);
						    }
					     }
					   }
				}
			}
			else
			{
				//we just need to manipulate the item 
				//origin item doesn't exist
				if(domLimsType.equalsIgnoreCase("LIMSID"))
				{
					if(!WtUtils.isNullOrBlank(item))
					{
						this.manipulateDOMLimsIdType(db);
					}
				}
			}
			Element serverDom = getServerDOM();
			//Element eItems = (Element)serverDom.getElementsByTagName(MetStrings.DOMElement.ITEMS).item(0);
			System.out.println(WtDOMUtils.prettyPrintXML(serverDom));
		}
		
		public void doEMRETaskWorkPostSave(HttpServletRequest request, boolean bHaveFile, Db db)
		{
			//do we have a data node to process?
			if(bSameItemType)
			{
				try
				{
					Element serverDom = getServerDOM();
					Element eData = (Element)serverDom.getElementsByTagName(EMREStrings.DOMElement.DATA).item(0);
					String domItemType = eData.getAttribute("itemType");
					String idType = eData.getAttribute("dbItemType");
					String item = eData.getAttribute("item");
					String originType = eData.getAttribute("dbOriginItemType");
					String originItem = eData.getAttribute("originItem");
					NodeList nlContent = eData.getElementsByTagName("addContent");
					if(nlContent != null)
					{
						int numNodes = nlContent.getLength();
						for(int i = 0; i < numNodes; i++)
						{
							Element e = (Element)nlContent.item(i);
							e.setAttribute(EMREStrings.DOMAttribute.TYPE, EMREStrings.DOMAttribute.CONTAINER);
							this.addContentForOriginId(e, eData, item, idType,
								   			originItem, originType, db);
						}
						
					}
					
					NodeList nlData = eData.getElementsByTagName("addData");
					if(nlData != null)
					{
						int numNodes = nlData.getLength();
						for(int i = 0; i < numNodes; i++)
						{
							Element e = (Element)nlData.item(i);
							e.setAttribute(EMREStrings.DOMAttribute.TYPE, EMREStrings.DOMAttribute.CONTAINER);
							this.addDataForOriginId(e, eData, item, 
					   			idType, originItem, originType, db);
						}
					}
				}
				catch(Exception ex)
				{
					throw new LinxUserException("Error in processing items of the same type: " + ex.getMessage());
				}
				
					
			}
			
			//reset the task DOM to it's original state
			setTaskDOM(getCleanDOM());
			//reset the limsid and origin lims id types
			setLimsIdType("LIMSID");
			setOriginIdType("OriginLIMSID");
			//we may not have an originItem or item
			try
			{
				//hide the itemtype boxes when done with them and reset the server item types too
				getDisplayItem("ItemType").clearValues();
				getDisplayItem("ItemType").setVisible(false);
				getServerItem("ItemType").clearValues();
				getDisplayItem("OriginItemType").clearValues();
				getDisplayItem("OriginItemType").setVisible(false);
				getServerItem("OriginItemType").clearValues();
			}
			catch(Exception ignoreMe)
			{
				//ignore since we probably don't have an origin item type
			}
			//reset the clean dom
			if(!bHaveFile)
				setCleanDOM(null);
		}
		
		public void addContentForOriginId(NodeList nlContent, Element eItem, String item,
				String itemType, String originItem, String dbOriginItemType, Db db)
		{
			try
			{
			    if(nlContent.getLength() < 1)
			    {
			        // no nodes on this item
			    	return;
			    }
			    for(int i = 0; i < nlContent.getLength(); i++)
			    {
			    	//we need to add content
			    	Element eContent = (Element)nlContent.item(i);
			    	String contentType = eContent.getAttribute(Item.CONTENT_TYPE);
			    	String coord = eContent.getAttribute(Item.COORDINATE);
			    	String appliesTo = eContent.getAttribute(Item.APPLIES_TO);
			    	if(WtUtils.isNullOrBlank(coord))
			    	  coord = "0";
			    	if(appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
			    	{
			    		db.getHelper().addContent(db, item, itemType, originItem, dbOriginItemType,
			    				coord, null, null, getTranId());
			    	}
			    	else if(contentType.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
			    	{
			    		db.getHelper().addContent(db, originItem, dbOriginItemType, item, itemType,
			    				coord, null, null, getTranId());
			    	}
		    	 }
			  //remove addAsContent because we already added the content
		    	WtDOMUtils.removeChildren(eItem, Item.ADD_AS_CONTENT);
			}
			catch(Exception ex)
			{
				throw new LinxSystemException(ex.getMessage());
			}
			
		}
		
		public void addContentForOriginId(Element eContent, Element eItem, String item,
				String itemType, String originItem, String dbOriginItemType, Db db)
		{
			try
			{
			    	//we need to add content
					String contentType = eContent.getAttribute(Item.CONTENT_TYPE);
			    	String coord = eContent.getAttribute(Item.COORDINATE);
			    	String appliesTo = eContent.getAttribute(Item.APPLIES_TO);
			    	if(WtUtils.isNullOrBlank(coord))
			    	  coord = "0";
			    	if(appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
			    	{
			    		db.getHelper().addContent(db, item, itemType, originItem, dbOriginItemType,
			    				coord, null, null, getTranId());
			    	}
			  //remove addAsContent because we already added the content
		    	WtDOMUtils.removeChildren(eItem, Item.ADD_AS_CONTENT);
			}
			catch(Exception ex)
			{
				throw new LinxSystemException(ex.getMessage());
			}
			
		}
		
		public void addDataForOriginId(NodeList nlData, Element eItem, String item,
				String itemType, String originItem, String dbOriginItemType, Db db)
		{
			try
			{
				    if(nlData.getLength() < 1)
				    {
				        // no nodes on this item
				    	return;
				    }
				    for(int i = 0; i < nlData.getLength(); i++)
				    {
				    	//we need to add data
				    	Element eData = (Element)nlData.item(i);
				    	String dataType = eData.getAttribute(Item.DATA_TYPE);
				    	String coord = eData.getAttribute(Item.COORDINATE);
				    	String appliesTo = eData.getAttribute(Item.APPLIES_TO);
				    	if(WtUtils.isNullOrBlank(coord))
				    	  coord = "0";
				    	if(appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
				    	{
				    		db.getHelper().addData(item, itemType, coord, 
				    				originItem, dbOriginItemType, getTranId(), db);
				    	}
				    }
				  //remove addAsContent because we already added the content
			    	WtDOMUtils.removeChildren(eItem, Item.ADD_AS_DATA);
			}
			catch(Exception ex)
			{
				throw new LinxSystemException(ex.getMessage());
			}
		}
		
		public void addDataForOriginId(Element eData, Element eItem, String item,
				String itemType, String originItem, String dbOriginItemType, Db db)
		{
			try
			{
				    String dataType = eData.getAttribute(Item.DATA_TYPE);
				    String coord = eData.getAttribute(Item.COORDINATE);
				    String appliesTo = eData.getAttribute(Item.APPLIES_TO);
				    if(WtUtils.isNullOrBlank(coord))
				    	 coord = "0";
				    if(appliesTo.equalsIgnoreCase(EMREStrings.ItemType.ORIGIN_LIMS_ID))
				    {
				    	db.getHelper().addData(item, itemType, coord, 
				    			originItem, dbOriginItemType, getTranId(), db);
				    }
				  //remove addAsContent because we already added the content
			    	WtDOMUtils.removeChildren(eItem, Item.ADD_AS_DATA);
			}
			catch(Exception ex)
			{
				throw new LinxSystemException(ex.getMessage());
			}
		}

		public Element getCleanDOM() {
			return cleanDOM;
		}

		public void setCleanDOM(Element cleanDOM) {
			this.cleanDOM = cleanDOM;
		}

	public String getLimsIdType() {
		return limsIdType;
	}

	public void setLimsIdType(String limsIdType) {
		this.limsIdType = limsIdType;
	}

	public String getOriginIdType() {
		return originIdType;
	}

	public void setOriginIdType(String originIdType) {
		this.originIdType = originIdType;
	}
	  
	/**
	 * Given <culture>_<serial number> (the name format for a SamplingTimepoint item), 
	 * returns serial number + 1. Not zero-padded.
	 * @param lastSTP
	 * @return serial number at end of sampling timepoint's name
	 */
	public int getNextSerialNumberByCulture(String culture, String cultureType, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		params.add(culture);
		params.add(cultureType);
		String lastSTP = db.getHelper().getDbValueFromStoredProc(
							db, "spEMRE_getLastSamplingTimepoint", params);
		String suffix = lastSTP.substring(lastSTP.indexOf("_")+1);
		int sn = Integer.parseInt(suffix);
		return sn + 1;
	}
	
	/**
	 * <culture>_<serial number> (the name format for a SamplingTimepoint item),
	 * or null if this combo doesn't exist yet.
	 * @param lastSTP
	 * @return item.item for SamplingTimepoint or null if one doesn't exist
	 */
	public String getSamplingTimepointByCulture(String culture, String cultureType, String timepoint, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		params.add(culture);
		params.add(timepoint);
		String stp = db.getHelper().getDbValueFromStoredProc(
							db, "spEMRE_getSamplingTimepoint", params); // returns null if none
		if(WtUtils.isNullOrBlank(stp))
		{
			// create new
			int sn = getNextSerialNumberByCulture(culture, cultureType, db);
			stp = culture + "_" + sn;
		}
		return stp;
	}
	
	/**
	 * Parses file only enough to extract a list of new items to add to
	 * server-side item for core processing. Strategy is to let core do its
	 * validation on items prior to performing bulk insert of custom data.
	 * Parsing twice is the reasonable cost.
	 * 
	 * @param inFile
	 * @param tab worksheet name
	 * @parma ID_COLUMN column containing item names
	 * @return list of items from user's file
	 */
	protected ArrayList<String> getIdsFromImportFile(File inFile, String tab, String ID_COLUMN)
	{
		String WORKSHEET = tab;
		
		ArrayList<String> ids = new ArrayList<String>();
		// open user's import file
		POIParser parser = new POIParser();
		HSSFRow row = null;
		HSSFRow headerRow = null;
		try
		{
			// find worksheet tab named "Primary Enrichment"
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);

			HSSFSheet sheet = wb.getSheet(WORKSHEET);
			if (sheet == null)
			{
				throw new LinxUserException(
						"Could not find a worksheet tab named '" + WORKSHEET + "'."
						+ " Please compare your import file with the current template," 
						+ " then try again.");
			}
			// find the header row
			Iterator rowItor = sheet.rowIterator();
			while (rowItor.hasNext())
			{
				// walk the file by rows, looking for header row
				row = (HSSFRow) rowItor.next();
				if (parser.isHeaderRow(row, ID_COLUMN))
				{
					headerRow = row;
					break;
				}
			}// next row
			if (headerRow == null)
			{
				throw new LinxUserException(
						WORKSHEET + " worksheet tab should start with column '" + ID_COLUMN + "'."
						+ " Please compare your import file with the current template, then try again.");
			}
			// by here, at header row

			// walk rows, taking only LIMS ID (Sample name) value
			while (rowItor.hasNext())
			{
				row = (HSSFRow) rowItor.next();
				String limsId = parser.getValueAsString(row.getCell(0));
				if(WtUtils.isNullOrBlank(limsId))
				{
					break;
				}
				ids.add(limsId);
			}// next row
			// at exit, list contains all Sample names in file
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			throw new LinxUserException(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new LinxUserException(e);
		}
		return ids;
	}
	
	/**
	 * Returns a reference to a new tab-delimited bulk insert *.txt file created
	 * from the imported file. No data validation is done app-side due to
	 * frequent changes in requirements. See db stored proc spEMRE_bulkInsert*
	 * for task logic and business rules.
	 * 
	 * @param inFile
	 * @param tab worksheet name
	 * @parma ID_COLUMN column containing item names
	 * @return ref to *.txt file for bulk insert
	 */
	protected File createBulkInsertFile(File inFile, String tab, String ID_COLUMN, Db db)
	{
		String WORKSHEET = tab;

		String DELIM = Strings.CHAR.TAB; // commas often occur in data

		// create empty output file
		String path = dbHelper.getApplicationValue(db, "System Properties", "Bulk Insert Local Dir");
		String filename = "bi_" + getTranId() + ".txt";
		File biFile = new File(path + filename);
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(biFile);
		}
		catch (IOException e1)
		{
			throw new LinxUserException(e1);
		}
		int biRowCount = 0;

		// open user's import file
		POIParser parser = new POIParser();
		HSSFRow row = null;
		HSSFRow headerRow = null;
		try
		{
			// find worksheet tab named "Primary Enrichment"
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);

			HSSFSheet sheet = wb.getSheet(WORKSHEET);
			if (sheet == null)
			{
				throw new LinxUserException("Could not find a worksheet tab named '" + WORKSHEET + "'."
						+ " Please compare your import file with the current template," + " then try again.");
			}
			// find the header row
			Iterator rowItor = sheet.rowIterator();
			while (rowItor.hasNext())
			{
				// walk the file by rows, looking for header row
				row = (HSSFRow) rowItor.next();
				if (parser.isHeaderRow(row, ID_COLUMN))
				{
					headerRow = row;
					break;
				}
			}// next row
			// at header row
			if (headerRow == null)
			{
				throw new LinxUserException(WORKSHEET + " worksheet tab should start with column '" + ID_COLUMN + "'."
						+ " Please compare your import file with the current template, then try again.");
			}

			// construct output file, one line per column in import file
			while (rowItor.hasNext())
			{
				row = (HSSFRow) rowItor.next();
				biRowCount++;
				int cellCount = row.getLastCellNum();
				String limsId = parser.getValueAsString(row.getCell(0));
				if(WtUtils.isNullOrBlank(limsId))
				{
					break; // end of rows
				}

				// walk each row by cells,
				// adding tab-delim'd lines to output file
				for (int cellIdx = 1; cellIdx <= cellCount; cellIdx++)
				{
					String key = parser.getValueAsString(headerRow.getCell(cellIdx));
					String value = parser.getValueAsString(row.getCell(cellIdx));
					if(WtUtils.isNullOrBlank(key))
					{
						break; // end of columns
					}
					String line = limsId + DELIM + key + DELIM + value + DELIM + getTranId();
					writer.write(line + EMREStrings.CHAR.CRLF);
					//System.out.println(line);
				}// next cell
			}// next row
			// at exit, biFile contains one line per sample + property
			writer.flush();
			writer.close();
			writer = null;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			throw new LinxUserException(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new LinxUserException(e);
		}

		return biFile;
	}


	/**
	 * For SubmitGrabData and EditGrabData tasks
	 * to override appropriately.
	 * @return false
	 */
	public boolean createNewTimepoints()
	{
		// 
		return false;
	}
}
