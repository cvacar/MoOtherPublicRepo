package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.EMRETask;

import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvDefineCultureProtocol
 *
 * Overridden to show existing culture protocols
 * in UI table and allow drilling for info on 
 * a protocol (todo: implement drilling).
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 8/2007
 */
public class SvDefineProtocol extends EMREServlet
{
  String PROTOCOL_TABLE = "Protocols";
  int COL_SOP = 1;
  private RowsetView view = null;
  
  
  
  /** 
   * Overridden to display a UI table of existing
   * contributors with drill-down links.
   * @param request
   * @param task
   * @param user
   * @param db
   */
  @Override
  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
  {
   
      
        RowsetView.cleanupSessionViews(request);     
        RowsetView.addViewToSessionViews(request, getProtocolsView(request, task, db));
        task.setMessage("");

    }

  /** 
   * NOT IN USE - per Zieler Lab 5/17/08, will use Word or other docs
   * to track protocol conditions.
   * Handles user's selection of an existing 
   * protocol, loading the protocol's properties 
   * into screen widgets.
   * @param request
   * @param response
   * @param task
   * @param user
   * @param db
   * @return FINISH_FOR_ME
   */
  @Override
  protected boolean doTaskWorkOnGet(HttpServletRequest request, HttpServletResponse response, Task task, User user, Db db)
  {
    ((EMRETask)task).setDb(db);
    task.setMessage("");
 
    RowsetView.cleanupSessionViews(request);     
    RowsetView.addViewToSessionViews(request, getProtocolsView(request, task, db));
    

    // has user selected a clone from list of existing clones?
    if(request.getParameter("selCoord") != null
    	&& request.getParameter("selCoord").startsWith(PROTOCOL_TABLE)
        && request.getParameter("selVal") != null)
    {
        // user has selected an Protocol, so populate property fields on UI
        String selProtocol = request.getParameter("selVal");
        task.getDisplayItem(ItemType.PROTOCOL).setValue(selProtocol);
        setSelectedProtocolType(selProtocol, task, db);
        ((EMRETask)task).populateDisplayItemsWithProtocolProperties(task, selProtocol, db); 
        
      }
      
    task.setMessage("Edit Protocol values as needed, enter data, then click Save.");
    forwardToPg(Strings.DEFAULT.pgMASTER_DEFAULT_JSP, request, response, "");
    return ALL_DONE; 
  }
  
    
  /**
   * If this is the base class Define Protocol, 
   * not a subclass, looks up and sets the ProtocolType
   * display item (only parent class shows it; ProtocolType is
   * assumed in subclasses.) 
   * @param selProtocol 
   * @param task 
   * @param db 
   */
  protected void setSelectedProtocolType(String selProtocol, Task task, Db db)
  {
    if(task.getTaskName().equalsIgnoreCase("Define Protocol"))
    {
      String protocolType = db.getHelper().getDbValue("exec spMet_GetProtocolTypeByProtocolId '" + selProtocol + "'", db);
      task.getDisplayItem("ProtocolType").setSelectedValue(protocolType);
    }
  }

  /**
   * Returns a rowset view of Protocols defined for the given 
   * task (protocol type).
   * @param request
   * @param task 
   * @param db
   * @return rowset view of Protocols
   */
  protected RowsetView getProtocolsView(HttpServletRequest request, Task task, Db db)
  {
    // show UI table of existing clones
    // -- using task names to map to protocol types
    String protocolType = "ALL";
    if(!task.getTaskName().equalsIgnoreCase("Define Protocol"))
    {
      protocolType = task.getTaskName();
    }
    String sql = "exec spMet_GetProtocolsByType '" + protocolType + "'";
    RowsetView view = this.getSQLRowsetView(request, sql, "Protocol", PROTOCOL_TABLE, 
    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
    view.setWidget(1,LinxConfig.WIDGET.LINK);
    view.setScroll(true);
    view.setScrollSize("small");
    view.setStartRow(1);
    if(task.getTaskName().equalsIgnoreCase("Define Protocol"))
    {
      view.setMessage("Click on an Protocol to start with its properties.");
    }
    else
    {
      view.setMessage("Click on an Protocol to start with its properties [to define a new Protocol, run task 'Define Protocol']");
    }
    return view;
  }

}
