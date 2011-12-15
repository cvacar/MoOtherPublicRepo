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
 * SvManageProjects
 *
 * Overridden to show existing projects
 * in UI table and to allow drilling for info on 
 * a project.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 8/2007
 */
public class SvManageProjects extends EMREServlet
{
  String PROJECT_TABLE = "Projects";
  int COL_PROJECT = 1;
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
    
      super.preprocessTask(request, task, user, db);
 
      RowsetView.cleanupSessionViews(request);
      
      // show sample queueing option table
      String sql = "exec spMet_GetProjectsForManageProjects";
      view = getSQLRowsetView(request, sql, "Project", PROJECT_TABLE, 
    		  Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
      
      // user can drill on contributor's name to get a report
      view.setWidget(1, LinxConfig.WIDGET.LINK_OPEN_IN_NEW_WINDOW); // item.item=contrib name
   
      view.setMessage("Click on a project for details");
      
      RowsetView.addViewToSessionViews(request, view);

    }

  

  

  /** 
   * When user clicks on a project name in the UI table,
   * shows a report about that project (content tbd).  
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
      String sql = "exec spMet_GetProjectReport '" + id + "'";
      writeToExcel(request, response, sql, db);
      
      return ALL_DONE;
    }
    return super.doTaskWorkOnGet(request, response, task, user, db);
  }

}
