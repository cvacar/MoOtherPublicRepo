package com.sgsi.emre.view;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import com.sgsi.emre.db.EMREDbHelper;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.model.DataModel;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.servlet.SvTask_Default;
import com.wildtype.linx.view.RowsetView;
import com.project.Strings;

/**
 * DefaultTableView
 * 
 * Helpful for preserving user's data on error
 * in a complex table.
 * 
 * @author TJS/Wildtype
 * @date 1/2006
 */
public class DefaultTableView extends RowsetView
{
  protected SvTask_Default sv = null;
  protected Db db = null;
  protected EMREDbHelper dbHelper = new EMREDbHelper();
  
  protected TableDataMap rowMap = null;

 
  /**
   * Default constructor takes POPULATED data model,
   * or at least a model that has begun to retrieve
   * results on another thread.
   * @param model 
   */
  public DefaultTableView(DataModel model)
  {
    super();
    setDataModel(model);
  }

  /**
   * Need a handle to column constants, currently
   * stored in task sv.
   * @param sv
   */
  public void setSv(SvTask_Default sv, Db db)
  {
    this.sv = sv;
    this.db = db;
  }
  
  /**
   * Sets a ref to the user's submitted table data.
   * Helps preserve the user's table data by checking
   * for entries whenever the table is re-drawn,
   * e.g. on error.
   * @param rowMap
   */
  public void setRowMap(TableDataMap rowMap)
  {
    this.rowMap = rowMap;
  }

  
  /**
   * Returns the value returned by SQL, which might
   * have been masked in the UI table. Useful for
   * avoiding another RT to db when user is drilling
   * in UI table.
   * @param row
   * @param col
   * @return
   */
  public String getOriginalValue(int row, int col)
  {
    return super.getValue(row, col);    
  }
  
  /**
   * Removes any rowset views from the session, so that new
   * views will be found consistently.
   * @param request
   */
  public static void cleanupSessionViews(HttpServletRequest request)
  {
    List list = RowsetView.getSessionViews(request);
    if( list != null)
    {
	    ListIterator itor = list.listIterator();
	    while(itor.hasNext())
	    {
	      RowsetView view = (RowsetView)itor.next();
	      if( view != null) 
	      {
	        view.cleanup();
	        view = null;
	      }
	     }// next view
    }
    RowsetView view = null;
    try
    {
      view = RowsetView.getSessionView(request);
    }
    catch (RuntimeException e)
    {
      ; // ignore
    }
    if( view != null)
    {
      try
      {
        view.cleanup();
      }
      catch (RuntimeException e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      view = null;
    }
    request.getSession().removeAttribute(Strings.WIDGET.ROWSET_VIEWS);
    request.getSession().removeAttribute(Strings.WIDGET.ROWSET_VIEW);
  }
 
    
  /**
   * Also loads the Column collection, 
   * for use by JSPs.
   * @param model
   */
  public void setDataModel(DataModel model)
  {
    super.setDataModel(model);
    
    // overwrite superclass' column list
    super.getColumns().clear();
    Vector colNames = getDataModel().getColumnHeaders();
    for(int i = 0; i < colNames.size(); i++)
    {
      String colName = (String)colNames.get(i);
      super.getColumns().add(i, new DefaultColumn(this,(i +1), colName));
    }// next col header
  }
  
 
  
  /***************DefaultColumn****************/
  
  public class DefaultColumn extends RowsetView.Column
  {
    
    int minLength = 20;
    int maxLength = 50;

    
    public DefaultColumn(RowsetView view, int index, String name)
    {
      super(view, index, name);
    }
    
    
    
    /**
     * Overridden to look up and return last known value of cell,
     * if any.
     */
    public String getCellValue()
    {
      if( rowMap != null)
      {
        if(this.getWidgetType().equalsIgnoreCase(LinxConfig.WIDGET.CHECKBOX))
        {
          return String.valueOf(rowMap.isKeyPresent(getCurrentRowIndex(), getCurrentColIndex()));
        }
        String val = rowMap.getValue(getCurrentRowIndex(), getCurrentColIndex());
        if( val != null)
        {
          if( val.trim().toLowerCase().startsWith("cb") && val.indexOf("-") > 0)
          {
            // a kluge, but tidier from user's pov
             return val.trim().toUpperCase();
          }
          return val.trim();
        }
      }
      return super.getCellValue();
    }
    
   
    /**
     * @return Returns the maxLength.
     */
    public int getMaxLength()
    {
      return maxLength;
    }
    /**
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength)
    {
      this.maxLength = maxLength;
    }
    /**
     * @return Returns the minLength.
     */
    public int getMinLength()
    {
      return minLength;
    }
    /**
     * @param minLength The minLength to set.
     */
    public void setMinLength(int minLength)
    {
      this.minLength = minLength;
    }
  }



//public String getSelectedCoord() {
//	return selectedCoord;
//}
//
//public void setSelectedCoord(String selectedCoord) {
//	this.selectedCoord = selectedCoord;
//}


  
  
  
  
}
