package com.sgsi.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import com.project.Strings;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.db.NormDbHelper;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtTaskUtils;

/**
 * SGIDbHelper
 *
 * 
 * @author TJ Stevens/Wildtype for SGI
 */
public class SGIDbHelper extends NormDbHelper
{
	protected Vector colHeaders = null;
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
  
	/**
   * Executes a stored procedure with params, writes results to file in CSV format.
   * Returns file.
   * 
   * First row is column names. If no rows
   * are retrieved, returns new Node (not null). Caveat: all db field
   * values in the SQL must be retrievable as Strings.
   * @param procName name of stored procedure (no 'exec ' prepended)
   * @params 
   * @param db
   * @return file containing comma separated result set, each row on separate line
   */
  public File getFileResultSet(String procName, ArrayList<String> params, Db db)
  {
	if(colHeaders == null)
	{
		colHeaders = new Vector();
	}
	colHeaders.clear();
	// overwrite old versions (no harm done)
	File file = new File("temp_file.csv");
	if (file.exists())
	{
		file.delete();
		file = new File("temp_file.csv");
	}
    try
    {
	  FileWriter writer = new FileWriter(file);
	  ResultSet rs = null;
      // execute SQL and format results as comma-separated values
      int iRowCounter = 0;
      rs = getResultSetFromStoredProc(db, procName, params, true);
      
      // Get the column headers.
      java.sql.ResultSetMetaData meta = rs.getMetaData();
      int iColumnCount = meta.getColumnCount();
        for(int i = 1; i <= iColumnCount; i++)
        {
          colHeaders.add(meta.getColumnLabel(i));
          writer.write(meta.getColumnLabel(i));
          writer.write(Strings.CHAR.COMMA);
        }// next column
        writer.write(Strings.CHAR.NEWLINE);
        writer.flush();

      // While we have rows, get the data and add to the StringBuffer
      iRowCounter = 0;
      while (rs.next())
      {
        iRowCounter++;
        for(int i = 1; i <= iColumnCount; i++)
        {
          String value = rs.getString(i);
          if( WtDOMUtils.isNullOrBlank(value) )
          {
            value = " ";
          }
          else
          {
            value = value.trim();
            if( meta.getColumnType(i) == java.sql.Types.DATE
                || meta.getColumnType(i) == java.sql.Types.TIMESTAMP )
            {
              value = WtTaskUtils.formatDateForDisplay(value);
            }
          }
          writer.write(value);
          if( i < iColumnCount )
          {
        	  writer.write(Strings.CHAR.COMMA);
          }
        }// next field
        writer.write(Strings.CHAR.NEWLINE);
        writer.flush();
      } // next row
      writer.flush();
      writer.close();
      writer = null;
      // at exit, file contains comma-separated result set

      return file;
    }
    catch(IOException ioe)
    {
        throw new LinxDbException("Problem encountered running SQL query\n" + procName
                + ": " + ioe.getMessage());
   	
    }
    catch(SQLException e)
    {
      handleSQLException(e);
      throw new LinxDbException("Problem encountered running SQL query\n" + procName
          + ": " + e.getMessage());
    }

  }
  
  public Vector getColHeaders()
  {
	  return colHeaders;
  }
}
