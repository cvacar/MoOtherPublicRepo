package com.sgsi.emre.task;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.bean.DataType;
import com.sgsi.emre.bean.ProtocolTypeBean;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Workflow;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.WtUtils;


/**
 * Define Protocol
 *
 * Allows user to fill in info on protocol 
 * using existing protocol as template.
 * 
 * @author TJ Stevens/Wildtype for Synthetic Genomics
 * @created 1/2008
 */
public class DefineProtocol extends EMRETask
{
 private Db db = null;

  
  /** 
   * Overridden to create new PROTOCOL custom table
   * record.
   * @param request
   * @param response
   * @param user
   * @param db
   */
  @Override
  public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
  {
    
    super.doTaskWorkPostSave(request, response, user, db);
    
    updateCustomTables(db);
  }
  
  /**
   * Inserts new Protocol custom table record
   * for the given properties.
   * @param db
   */
  public void updateCustomTables(Db db)
  {
      // comment may be null
    String commentId = null;
    String comment = getDisplayItemValue(DataType.COMMENT);
    if(!WtUtils.isNullOrBlank(comment))
    {
       commentId = dbHelper.addComment("PROTOCOL_COMMENTTYPE", comment, getTranId(), db);
    }

      // todo: straighten out various id/names for Protocols
    //String protocolRef = getDisplayItemValue(ItemType.PROTOCOL);
    ProtocolTypeBean bean = new ProtocolTypeBean();
    bean.setProperties(getDisplayItems());
    
    // distinguish between use by a protocol-using task and a new Protocol definition
    if(getTaskName().equalsIgnoreCase("Define Protocol"))
    {
      // new definition
      //bean.save(protocolRef, getDisplayItemValue("ProtocolType"), commentId, getTranId(), db);
      bean.save(getDisplayItemValue("ProtocolType"), commentId, getTranId(), db);
      
    }
    else
    { // protocol is being used by a task run
      //bean.save(protocolRef, getTaskName(), commentId, getTranId(), db);
      bean.save(getTaskName(), commentId, getTranId(), db);
    }
    
  }

  /** 
   * Smother non-impactful error caused by 
   * dynamic task def xml.
   * @param request
   * @param wf
   */
  @Override
  public void cleanupTask(HttpServletRequest request, Workflow wf)
  {
    
    try
    {
      super.cleanupTask(request, wf);
    }
    catch (RuntimeException e)
    {
      if(e.getMessage().indexOf("no client DOM item Type") > 0)
      {
        //ignore
      }
    }
  }
  
  
  

  
  
  
}
