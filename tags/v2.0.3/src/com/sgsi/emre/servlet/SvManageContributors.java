package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.view.RowsetView;

/**
 * 
 * SvManageContributors
 *
 * Overridden to show existing contributors
 * in UI table and allow drilling for info on 
 * a contributor.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 7/2007
 */
public class SvManageContributors extends EMREServlet
{
  String CONTRIB_TABLE = "Contributors";
  int COL_CONTRIB = 1;
  
  
  
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
    super.preprocessTask(request, task, user, db);
    task.setMessage("");
 
    RowsetView.cleanupSessionViews(request);     
    RowsetView.addViewToSessionViews(request, getContributorsView(request, db));

  }

    /**
   * Returns a rowset view of contributors currently 
     * defined in the database.
   * @param request
   * @param db
   * @return rowset view of clones
   */
  protected RowsetView getContributorsView(HttpServletRequest request, Db db)
  {
    // show UI table of existing clones
    String sql = "exec spMet_GetContributorsForManageContributors";
    RowsetView view = getSQLRowsetView(request, sql, "Contributor", CONTRIB_TABLE, 
    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
    view.setWidget(COL_CONTRIB,LinxConfig.WIDGET.LINK);
    //view.setScroll(true);
    view.setHideNavControls(false);
    view.setStartRow(1);
    view.setMessage("Click on a Contributor to download details.");
    return view;
  }

  

  /** 
   * When user clicks on a contributor name in the UI table,
   * shows a report about that contributor (content tbd).  
   * @param request
   * @param response
   * @param task
   * @param user
   * @param db
   * @return ALL_DONE
   */
  @Override
  protected boolean doTaskWorkOnGet(HttpServletRequest request, HttpServletResponse response, Task task, User user, Db db)
  {
    if(request.getParameter("selVal") != null)
    {
      String id = request.getParameter("selVal");
      String sql = "exec spMet_GetContributorDetails '" + id + "'";
      writeToExcel(request, response, sql, db);
      
      return ALL_DONE;
    }
    return super.doTaskWorkOnGet(request, response, task, user, db);
  }

}
