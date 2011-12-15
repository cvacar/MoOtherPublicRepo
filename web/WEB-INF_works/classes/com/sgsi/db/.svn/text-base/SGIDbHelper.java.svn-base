package com.sgsi.db;

import java.util.ArrayList;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.db.NormDbHelper;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtDOMUtils;

/**
 * SGIDbHelper
 *
 * 
 * @author TJ Stevens/Wildtype for SGI
 */
public class SGIDbHelper extends NormDbHelper
{
  
  /**
   * Calls init() on parent class, so norm schema helpers are instantiated.
   *
   */
  public SGIDbHelper()
  {
      super.init();
  }

  /**
   * Creates a new COMMENT record of the given commentType
   * and sets target item if provided.
   * Assumes comment is not null, so sending a null will 
   * store a comment with the string 'null'. 
   * @param commentType
   * @param comment
   * @param tranId
   * @param db
   * @return id of new comment record
   */
  public String addComment(String commentType, String comment, String targetItem, String targetType, long tranId, Db db)
  {
    if( WtDOMUtils.isNullOrBlankOrPlaceholder(comment))
    {
      return null;
    }
    try
    {
      ArrayList params = new ArrayList();
      params.add(targetItem);
      params.add(targetType);
      params.add(comment);
      params.add(commentType);
      params.add(tranId + "");
      String id = callStoredProc(db, "spLinx_InsertCommentForItem", params, true);
      return id;
    }
    catch(Exception ex)
    {
      throw new LinxDbException("Unable to insert into COMMENT: " + ex.getMessage());
    }
  }
  
  
 
}
