package com.sgsi.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.sgsi.db.SGIDbHelper;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.DefaultTask;
import com.wildtype.linx.util.WtUtils;

/**
 * Base task for all SGI tasks. 
 * Contains helper methods that can be used from all tasks.  Extends the 
 * Linx2 Default Task Class.
 */
public class SGITask extends DefaultTask
{
  protected SGIDbHelper dbHelper = new SGIDbHelper();
  
  public final String ITEMTYPE_BATCH   = "Batch";
  public final String ITEMTYPE_LIBRARY_TYPE = "LibraryType";
  public final String ITEMTYPE_LIBRARY_ORDER   = "LibraryOrder";
  public final String ITEMTYPE_REQUESTER = "Requester";
  public final String ITEMTYPE_DATE_PROMISED = "DatePromised";
  public final String ITEMTYPE_LIBRARY = "Library";
  public final String ITEMTYPE_TARGET  = "Target";
  public final String ITEMTYPE_TSG     = "TSG";
  public final String ITEMTYPE_TSR     = "TSR";
  public final String ITEMTYPE_GPA     = "GPA";
  public final String ITEMTYPE_GPL     = "GPL";
  public final String ITEMTYPE_GP      = "GhostProbe";
  public final String ITEMTYPE_RP      = "ReporterProbe";
  public final String ITEMTYPE_GPTYPE  = "GhostProbeType";
  public final String ITEMTYPE_RPTYPE  = "ReporterProbeType";
  
  public final String ITEMTYPE_GP_ANCHOR  = "GP Anchor";
  public final String ITEMTYPE_GP_LIGATOR = "GP Ligator";
  public final String ITEMTYPE_RP_HOOK    = "RP Hook";
  public final String ITEMTYPE_RP_HOOK_LIGATOR = "RP Hook Ligator";
  
  // no custom tables
  public final String ITEMTYPE_LIGASE  = "Ligase";
  public final String ITEMTYPE_WATER   = "Water";
  public final String ITEMTYPE_TSP     = "TSP";
  public final String ITEMTYPE_SSPE    = "SSPE";
  public final String ITEMTYPE_LIGATION_BUFFER = "Ligation Buffer";
  
  
  
  

  
  /**
   * Subtracts given number of uses times amount per use (a lookup)
   * from the available inventory of the item, not complaining 
   * if the item's stock goes negative. Relies on a 'Reduction Per Use'
   * APPVALUE entry for each reagent type whose inventory is tracked.
   * @param lot
   * @param rgtType
   * @param useCount
   * @param db
   */
  public void updateInventory(String lot, String rgtType, int useCount, Db db)
  {
    String amount = null;
    useCount = Math.abs(useCount);
    try
    {
      amount = dbHelper.getApplicationValue(db, "Reduction Per Use", rgtType);
    }
    catch (RuntimeException e)
    {
      
      e.printStackTrace();
    }
    if( WtUtils.isNullOrBlankOrPlaceholder(amount))
    {
      //return; // no inventory control
      amount = "1";
    }    
    // by here, have all required properties
    
    double dAmount = Double.parseDouble(amount);
      // subtract from inventory
      // -- will not complain if stock goes negative
      //dbHelper.updateInventory(rgtType, lot, ((useCount * dAmount) * -1), getTranId(), db);
  }
  

  /**
   * Returns the file stored for this appFileId, provided
   * LIMS server can access its network location.
   * @param appFileId
   * @param db
   * @return handle to file stored under this appFileId
   */
  public File getFile(String appFileId, Db db)
  {
    String filepath = dbHelper.getDbValue("exec spLinx_getApplicationFileAndPath " + appFileId, db);
    return new File(filepath);
  } 
  
  /**
   * Returns a date in yyyy-MM-dd HH:mm:ss format.
   * @return date out to seconds
   */
  public String getDate()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return df.format(new Date());
  }
	
}
