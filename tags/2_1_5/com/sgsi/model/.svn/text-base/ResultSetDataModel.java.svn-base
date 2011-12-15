/**
 * 
 */
package com.sgsi.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;

import com.project.Strings;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtTaskUtils;

/**
 * @author TJS
 *
 */
public class ResultSetDataModel extends
		com.wildtype.linx.model.ResultSetDataModel
{
	
	protected String procName = null;
	protected ArrayList<String> params = null;
	
	public void setParams(ArrayList<String> sqlParams)
	{
		params = sqlParams;
	}
	
	public void setProcName(String sqlProcName)
	{
		this.procName = sqlProcName;
	}
	
  /**
   * Calls super class's writeCSVResultSetToTempFile() method to write 
   * the results of the SQL query to a CSV file, returning the new
   * temp file name. At exit, the result set has been written to disk
   * under the returned absolute path. No rows remain in memory.
   * @param sql
   * @param db
   */
  public void writeCSVResultSetMultiThreaded(String procName, 
  		ArrayList<String> sqlParams, Db db)
  {
    this.setDelim(',');  
    this.setDb(db);
    this.setSql(procName);/*ignored - for compatibility only*/
    this.setProcName(procName);
    this.setParams(sqlParams);
    t = new Thread(this);
    t.setName("query runner thread");
    t.start();
  }
  
  
  /**
   * Executes a SQL stored procedure, writes to a uniq filename in CSV format,
   * and returns filename. Also sets class members with headers and rowcount.
   * First row is column names. If no rows are retrieved, returns new Node (not null).
   * Caveat: all db field values in the SQL must be retrievable as Strings.
   * @param sql
   * @param delim
   * @param withHeader
   * @param db
   * @return comma separated result set, each row on separate line
   */
  public String writeDelimitedResultSetToTempFile(String sql/*ignored*/, char delim, boolean withHeader, Db db)
  {
    ResultSet rs = null;
    Vector colNames = new Vector();
    this.setDelim(delim);
    try
    {
	    File file = File.createTempFile("rs_",".csv");
	    FileWriter writer = new FileWriter(file);
	    
	    // prepend a header to the file?
	    if( withHeader )
	    {
	      String date = GregorianCalendar.getInstance().getTime().toString();
	      writer.write("Reported on " + date + Strings.CHAR.NEWLINE);
	    }

      // execute SQL and format results as comma-separated values
      int iRowCounter = 0;
      rs = db.getHelper().getResultSetFromStoredProc(db, procName, params, true);

      // write column headers to file
      java.sql.ResultSetMetaData meta = rs.getMetaData();
      int iColumnCount = meta.getColumnCount();
      if( withHeader )
      {
        for (int i = 1; i <= iColumnCount; i++)
        {
          writer.write(meta.getColumnLabel(i));
          colNames.add(meta.getColumnLabel(i));
          if( i < iColumnCount )
          {
            writer.write(this.getDelim());
          }
        }// next column
       writer.write(Strings.CHAR.NEWLINE);
      }
      writer.flush();
      // at exit, column headers are written to file and local list

      // while we have rows, delim the data and write to file
      iRowCounter = 0;
      while(rs.next())
      {
        iRowCounter++;
        for (int i = 1; i <= iColumnCount; i++)
        {
          String value =  rs.getString(i);
          if( WtDOMUtils.isNullOrBlank(value) )
          {
            value = " ";
          }
          else
          {
            value = value.trim();
            if( meta.getColumnType(i) == java.sql.Types.DATE || meta.getColumnType(i) == java.sql.Types.TIMESTAMP)
            {
              value = WtTaskUtils.formatDateForDisplay(value);
            }
          }
          writer.write(value);
          if( i < iColumnCount )
          {
            writer.write(String.valueOf(this.getDelim()));
          }
        }// next field
        writer.write(Strings.CHAR.NEWLINE);
        writer.flush();
      } // next row
      // at exit, sb contains comma-separated result set
      writer.flush();
      writer.close();
      writer = null;
      
      // init class members 
      // -- accepting multiple effects of this method 
      // -- to achieve better performance on big files
      setColumnHeaders(colNames);
      setCurrentRowcount(iRowCounter);
      setData(file);
      file = null;
      
      return getFilePath();
    }
    catch (SQLException e)
    {
      throw new LinxSystemException("Problem encountered running SQL stored procedure\n" + procName,e);
    }
    catch(IOException ioe)
    {
      throw new LinxSystemException("Problem encountered running SQL stored procedure\n" + procName, ioe);     
    }
    catch(Exception exc)
    {
    	throw new LinxSystemException("Problem encountered running SQL stored procedure\n" + procName, exc);
    }
    finally{
      if(rs != null){
        try{ rs.close(); } catch(java.sql.SQLException se) {}; // ignore
      }
    }
  }
  
  

}
