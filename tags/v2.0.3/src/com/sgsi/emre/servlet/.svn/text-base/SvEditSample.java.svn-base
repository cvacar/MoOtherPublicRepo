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
 * SvEditSample
 *
 * Overridden to show existing samples
 * in UI table and to aid in showing
 * or collecting data from user.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 2/2008
 */
public class SvEditSample extends EMREServlet
{
  String SAMPLE_TABLE = "Samples";

 

  /** 
   * @param request
   * @param task
   * @param user
   * @param db
   */
  @Override
  protected void preprocessTask(HttpServletRequest request, Task task, User user, Db db)
  {
    RowsetView.cleanupSessionViews(request);     
    //RowsetView.addViewToSessionViews(request, getSamplesView(request, "0", db));

  }



  /** 
   * Handles user's selection of an existing 
   * sample, loading its properties into the
   * widgets for update or addition.
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

    RowsetView.cleanupSessionViews(request);     
    RowsetView.addViewToSessionViews(request, getSamplesView(request, db));
    /**

    // has user selected a sample from list of existing samples?
    if(request.getParameter("selCoord") != null 
        && request.getParameter("selCoord").startsWith(SAMPLE_TABLE)
          && request.getParameter("selVal") != null)
      {

    	
        // user has selected a sample, so populate property fields on UI
        String selItem = request.getParameter("selVal");
        task.getDisplayItem("Sample").setValue(selItem);
        List list = task.getDisplayItems();
        ListIterator itor = list.listIterator();
        while(itor.hasNext())
        {
        	DisplayItem ditem = (DisplayItem)itor.next();
        	String ditemType = ditem.getItemType();
        	if(ditemType.equalsIgnoreCase(anotherString))
        }
 
        task.populateSQLValues(user, db);
      }
      **/
    return FINISH_FOR_ME;
  }
  
  
  
  /**
   * Returns a rowset view of items currently defined
   * in LIMS
   * @param request
   * @param db
   * @return rowset view of items
   */
  protected RowsetView getSamplesView(HttpServletRequest request, Db db)
  {
    // show UI table of existing items
    String sql = "exec spMet_GetSamplesForEditSample";
    RowsetView view = this.getSQLRowsetView(request, sql, "Sample", SAMPLE_TABLE, 
    		Integer.parseInt(Strings.WIDGET.CLIENT_SORT_MAX_ROWS), db);
    view.setWidget(1,LinxConfig.WIDGET.LINK);
    view.setScroll(true);
    view.setScrollSize("small");
    view.setStartRow(1);
    view.setMessage("Click on a Sample to edit its properties. "
    		+ " [To log a new Sample, run one of the Log Sample tasks.]");
    return view;
  }
  
}
