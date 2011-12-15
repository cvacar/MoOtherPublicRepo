package com.sgsi.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.project.Strings;
import com.sgsi.db.SGIDbHelper;
import com.sgsi.model.ResultSetDataModel;
import com.sgsi.emre.view.DefaultTableView;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.CSVDataModel;
import com.wildtype.linx.task.servlet.SvTask_Default;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
import com.wildtype.linx.util.WtUtils;



public class SGIServlet extends SvTask_Default
{
    // file.separator   \
    public String fileSeparator = System.getProperty("file.separator");
    
	public final String OK = "OK";
    public final static String BLANK_PAGE = "jspTasks/PgBlank.jsp";
	protected SGIDbHelper dbHelper = new SGIDbHelper();
    protected boolean isFirstRequest = true;
    
    public final String AS_EXCEL_FILE = "application/vnd.ms-excel";
	public final String AS_TEXT_FILE = "text/txt";
    

  
  /**
   * Returns the 'action' attribute's value from a servlet request.
   * Use other method signature if XML request action is wanted.
   * @param request
   * @return action usually save or verify
   */
	public String getAction(HttpServletRequest request)
  {
      if( request.getAttribute("action") != null)
      {
        return (String)request.getAttribute("action");
      }
      else if( request.getParameter("action") != null)
      {
        return request.getParameter("action");
      }
      throw new LinxSystemException("Either this is an XML request, "
          + "or action is missing from sv request.");
      
  }
  
	/**
	 * Linx 2 requires certain elements under any XML client request,
	 * but ignores additional custom nodes. Robot uses a custom 'data'
	 * node to pass custom data between client and LIMS server. 
	 * This convenience method returns the value of the first child 
	 * found under 'data' with dataChildName. So, if the tree looks like:
	 * <pre>
	 * ...rest of Linx request
	 * <data>
	 *   <SampleBatch>985</SampleBatch>
	 * </data>
	 * </pre>
	 * this method will return '985' as a String.
	 * @param domData
	 * @param dataChildName
	 * @return
	 */
	protected String getDataChildValue(Element domData, String dataChildName)
	{
		Element eData = (Element)domData.getElementsByTagName("data").item(0);
		if( eData == null)
		{
			throw new LinxUserException(
			 "No '" + dataChildName + "' child element found under given DOM. "
				+ Strings.MSG.ALERT_LIMS_ADMIN);
		}
		String value = WtDOMUtils.getElementValue(eData, dataChildName);
		
		return value;
	}
	
	/**
	 * Creates and returns a fresh 'data' node, for creating custom reply XML.
	 * @return 'data' node
	 */
	protected Element createDataNode()
	{
	  Document doc = WtDOMUtils.getDocumentWithoutProcessingInstructions(WtDOMUtils.getBuilder());
		Element eData = doc.createElement("data");

		return eData;
	}

  /**
   * Does the main work of updating workflow state by recording in the database
   * new and changed items, contents, and queues affected during this task.
   * 
   * @param request
   * @param response
   * @param task
   * @param user
   * @param db
   *
  protected void save(Task task, User user, Db db, HttpServletRequest request, HttpServletResponse response)
  {
     if( task == null )
    {
      throw new LinxSystemException("Cannot execute save() on null task."
          + Strings.MSG.ALERT_LIMS_ADMIN);
    }
    
    Code.event("Running task " + task.getTaskName() + " with tranId = "
        + task.getTranId());
		
		// catch single-item verifies
		if( task.getServerItems().size() > 1 
				&& task.isSingleItemVerify() )
		{
				 task.verifyItems(request, response, user, db);
				 // single-item verify successful (throws on error)
				 return;
		}
		
    // default message can be overwritten by any method
    task.recordTaskHistory(request, response, getWorkflow(), user, db);
    request.getSession().setAttribute("lastTranId", String.valueOf(task.getTranId()));
    
    task.doTaskWorkPreSave(request, response, user, db);
    
    task.verifyItems(request, response, user, db);
    task.createAnyNewItems(request, response, user, db);
    task.createAnyNewContents(request, response, user, db);
    task.createAnyNewData(request, response, user, db);
    //task.createAnyNewConditions(request, response, user, db);
    task.createAnyNewComments(request, response, user, db);
    task.copyContents(request, response, user, db);
    task.dequeueItems(request, response, user, db);
    task.queueItems(request, response, user, db);
    task.recordItemHistory(request, response, user, db);
 
    task.doTaskWorkPostSave(request, response, user, db);

  }*/
	
  /**
   * Excutes a sql statement and writes out the results to an excel spreadsheet
   * @param request
   * @param response
   * @param sql
   */
  public void writeToExcel(HttpServletRequest request, HttpServletResponse response, String sql, Db db )
  {
    if(WtUtils.isNullOrBlank(sql))
    {
      throw new LinxUserException("Unable to write to MS Excel.\r\n" +
          "The data-retrieval SQL statement cannot be null.");
    }
    
    ResultSet rs = dbHelper.getResultSet(sql,db);
    //response.setContentType("application/octet-stream");
    response.addHeader("Pragma", "Public");
    response.addHeader("Content-Disposition", 
          "attachment; filename=Report.xls");
    response.setContentType("application/vnd.ms-excel; Name=Excel");
   try
    {
      //write column names to file
      ResultSetMetaData metaData = rs.getMetaData();
      OutputStream out = response.getOutputStream();
      int numCols = metaData.getColumnCount();
      char delim = Strings.CHAR.TAB.charAt(0);
      String crlf = "\r\n";
      String columns = "";
      for(int i = 1; i < numCols; i++)
      {
        columns += metaData.getColumnLabel(i) + delim;
      }
      columns += metaData.getColumnLabel(numCols) + crlf;
      
      out.write(columns.getBytes());
      out.flush();
      columns = "";
      //write data to file
      while(rs.next())
      {
        for (int j = 1; j < numCols; j++)
		{
			String val = rs.getString(j);
			if (WtUtils.isNullOrBlank(val))
			{
				val = " "; // prevent text 'null' in Excel cell
			}
			columns += "\"" + val + "\"" + delim;
		}
			String val = rs.getString(numCols);
			if (WtUtils.isNullOrBlank(val))
			{
				val = " "; // prevent text 'null' in Excel cell
			}
        columns += "\"" + val + "\"" +  crlf;
        out.write(columns.getBytes());
        out.flush();
        columns = "";
      }
      rs.close();
      rs = null;
      metaData = null;
      out.close(); 
    }
    catch(Exception ex)
    {
      throw new LinxUserException("Error occurred while attempting to write to MS Excel:  " + ex);
    }
    
  }
  
  /**
   * Excutes a sql statement and writes out the results to an excel spreadsheet
   * @param request
   * @param response
   * @param sql
   */
  public void writeToExcelWithInlineHeaders(HttpServletRequest request, HttpServletResponse response, 
  		String sql, Hashtable headerValues, Db db )
  {
    if(WtUtils.isNullOrBlank(sql))
    {
      throw new LinxUserException("Unable to write to MS Excel.\r\n" +
          "The data-retrieval SQL statement cannot be null.");
    }
    
    ResultSet rs = dbHelper.getResultSet(sql,db);
    //response.setContentType("application/octet-stream");
    //response.addHeader("Pragma", "Public");
    response.addHeader("Content-Disposition", 
          "attachment; filename=Report.xls");
    response.setContentType("application/vnd.ms-excel; Name=Excel");
   try
    {
      //write column names to file
      ResultSetMetaData metaData = rs.getMetaData();
      OutputStream out = response.getOutputStream();
      int numCols = metaData.getColumnCount();
      char delim = Strings.CHAR.TAB.charAt(0);
      String crlf = "\r\n";
      String columns = "";
      for(int i = 1; i < numCols; i++)
      {
        columns += metaData.getColumnLabel(i) + delim;
      }
      columns += metaData.getColumnLabel(numCols) + crlf;
      
      out.write(columns.getBytes());
      out.flush();
      columns = "";
      //write data to file
      while(rs.next())
      {
        for (int j = 1; j < numCols; j++)
		{
			String val = rs.getString(j);
			if (WtUtils.isNullOrBlank(val))
			{
				val = " "; // prevent text 'null' in Excel cell
			}
			columns += "\"" + val + "\"" + delim;
		}
			String val = rs.getString(numCols);
			if (WtUtils.isNullOrBlank(val))
			{
				val = " "; // prevent text 'null' in Excel cell
			}
        columns += "\"" + val + "\"" +  crlf;
        out.write(columns.getBytes());
        out.flush();
        columns = "";
      }
      rs.close();
      rs = null;
      metaData = null;
      out.close(); 
    }
    catch(Exception ex)
    {
      throw new LinxUserException("Error occurred while attempting to write to MS Excel:  " + ex);
    }
    
  }
  

  /**
   * Returns the input number reformatted for the given
   * number of decimal places.
   * @param sNum
   * @param numDecimalPlaces
   * @return input number to numDecimalPlaces
   */
  public String formatNumber(String sNum, int numDecimalPlaces)
  {
    String sOut = sNum;
    try
    {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(true);
      nf.setMaximumFractionDigits(numDecimalPlaces);
      nf.setMinimumFractionDigits(numDecimalPlaces);
      Number num = nf.parse(sNum);
      sOut = nf.format(num);
    }
    catch(Exception ex)
    {
      throw new LinxUserException("Value " + sNum + " could not be formatted: " + ex.getMessage());
    }
    return sOut;
  }
  
  /**
   * Excutes a sql statement and writes out the results 
   * to a uniquely named file using RChart delimiter,
   * returning the data file name.
   * @param request
   * @param response
   * @param sql
   * @return name of data file
   */
  public String writeToChartSourceFile(String sql, Db db )
  {
    if(sql == null || sql.equals(""))
      throw new LinxUserException("Unable to write to file.\r\n" +
          "SQL statement cannot be null.");
    
    ResultSet rs = dbHelper.getResultSet(sql,db);
    String chartFilename = "chartData" + System.currentTimeMillis()+ ".dat";
    File chartFile = null;

    try
    {
      chartFile = new File(chartFilename);
      
      //write column names to file
      ResultSetMetaData metaData = rs.getMetaData();
      FileOutputStream out = new FileOutputStream(chartFile);
      int numCols = metaData.getColumnCount();
      char delim = '|';
      String crlf = "\r\n";
      String columns = "";
      for(int i = 1; i < numCols; i++)
      {
        columns += metaData.getColumnLabel(i) + delim;
      }
      columns += metaData.getColumnLabel(numCols) + crlf;
      
      out.write(columns.getBytes());
      out.flush();
      columns = "";
      //write data to file
      while(rs.next())
      {
        for(int j = 1; j < numCols; j++)
        {
          columns += "\"" + rs.getString(j) + "\"" + delim;
        }
        columns += "\"" + rs.getString(numCols) + "\"" +  crlf;
        out.write(columns.getBytes());
        out.flush();
        columns = "";
      }
      rs.close();
      rs = null;
      metaData = null;
      out.close(); 
    }
    catch(Exception ex)
    {
      throw new LinxUserException("Error occurred while attempting to write to excel.  " + ex);
    }
    return chartFile.getName();
  }
  
 
  
  
  
  /**
   * Sends the contents of the given file back to the client
   * as a byte stream, usually prompting a 'Save As...' dialog
   * from the browser. See also version that takes a StringBuffer.
   * @param response
   * @param file any character-based file
   * @param strTargetName suggested file name to show user
   * @param strContentType usually 'text/txt' or other content-type recognizable to client
   * @param bDeleteFileOnExit true if the given file can be safely deleted after transfer
   */
  protected void returnDownloadAsByteStream(HttpServletResponse response, File file, 
      String strTargetName, String strContentType, boolean bDeleteFileOnExit)
  {
    String strSourceName = "";

    try
    {
      // missing file -- a serious error
      if (file == null)
      {
        Code.warning("File was null at returnDownload()");
        throw new LinxUserException("No file was given to download."
            + Strings.MSG.ALERT_LIMS_ADMIN);
      }

      // add processing instructions if missing
      // -- usually missing only if an exception made it to here
      // print on the way OUT
      Code.event("DOWNLOADING RAW DATA OR FILE");

      strSourceName = file.getName();
      if (WtDOMUtils.isNullOrBlank(strContentType))
      {
        // text is default -- user will get a 'Save target as' dialog
        strContentType = "text/txt";
      }
      response.setContentType(strContentType);
      response.addHeader("Content-Disposition", "attachment;filename=\""
          + strTargetName + "\"");

      // -- send file contents over output stream to client
      // -- if browser, will show a 'Save As...' dialog with strTargetName
      Code.debug("Transferring file " + file.getName()
          + " to client via byte stream...");
      long len = 0;
      OutputStream out = response.getOutputStream();
      FileInputStream fin = new FileInputStream(file);
      byte[] buf = new byte[1024]; // size determined by trial and error
      int red = fin.read(buf, 0, 1024);
      len += red;
      while (red > -1)
      {
        out.write(buf, 0, red);
        out.flush();
        red = fin.read(buf, 0, 1024);
        len += red;
      }
      out.flush();
      out.close();
      fin.close();

      Code.debug("Sent " + len + " bytes to client.");

      // cleanup
      // -- free up resources if possible, altho task may elect to keep file
      if (bDeleteFileOnExit)
      {
        file.delete();
        file = null;
      }
    }
    catch(Exception ex)
    {
      Code.error(ex, "At returnDownload(" + strSourceName + ")");
    }
  }
  
  /**
   * Defines the file type, and its current location,
   * input path, and output path.
   */
  public class FileWrapper
  {
      
      private String path = null;
      private File outFile = null;
      private String type = null;
      /**
       * Simple wrapper class to hold file information for Accession Product.
       * @param outPath
       * @param file
       * @param fileType
       */
      public FileWrapper(String outPath, File file, String fileType)
      {
          path = outPath;
          outFile = file;
          type = fileType;
      }

      /**
       * @return Returns the outFile.
       */
      public File getOutFile()
      {
          return outFile;
      }

      /**
       * @return Returns the path.
       */
      public String getPath()
      {
          return path;
      }

      /**
       * @return Returns the type.
       */
      public String getType()
      {
          return type;
      }
      
  }
  
  /**
   * Creates a row map using the submitted table data.
   * 
   * @param request
   *          The current request
   * @param sTable
   *          The table to create the map for
   * @return Returns a map of the table cell values
   */

  public HashMap createRowMap(HttpServletRequest request, String sTable)
  {
    HashMap rowMap = new HashMap();
    int maxRow = 0;
    boolean useAttr = false;
    Enumeration e = request.getParameterNames();
    if( !e.hasMoreElements())
    {
      e = request.getAttributeNames();
      useAttr = true;
    }
    while (e.hasMoreElements())
    {
      String s = (String)e.nextElement();
      if( s.startsWith(sTable) ) // only want to use the selected table
      // data
      {
        StringTokenizer sToks = new StringTokenizer(s, ".");
        String temp = sToks.nextToken();
        if( sToks.hasMoreTokens() )
        {
          String sRow = sToks.nextToken();
          if( sToks.hasMoreTokens() )
          {
            String sCol = sToks.nextToken();
            if( useAttr )
            {
              String val = (String)request.getAttribute(s);
              rowMap.put(sRow + sCol, val);             
            }
            else // params
            {
              rowMap.put(sRow + sCol, request.getParameter(s));
            }
          }
        }
      }
    }

    return rowMap;
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
   *  Gets the result set as a CSV file as a view which the 
   * rowsetView widget can deal with.
   * @param request The current request
   * @param task The current task
   * @param sql The sql to populate the rowset with
   * @param sRefHeader The first header expected in the file
   * @param sTableName The name of the table  -- may be null
   * @param maxRows acts like 'top n' in SQL
   * @return Returns a populated rowsetView
   */
  public DefaultTableView getSQLRowsetView(HttpServletRequest request,
      String sql, String sRefHeader, String sTableName, int maxRows, Db db)
  {

    // -- writes result set directly from db to file
    // -- saves memory on large datasets
    ResultSetDataModel rsDataModel = new ResultSetDataModel();
    rsDataModel.setReferenceHeader(sRefHeader);
    rsDataModel.writeCSVResultSetMultiThreaded(sql, db);
   //new Delay(2500);

    // offers JSP convenient accessors such as getRowset(),
    // -- based on underlying data in dataModel
    DefaultTableView view = new DefaultTableView(rsDataModel);
    
    view.setStartRow(1);
    view.setMaxRowsetSize(maxRows);
    view.setName(sTableName);
    view.setMessage(getMessage(view));
    
    return view;
  }

  /**
   *  Gets the result set as a CSV file as a view which the 
   * rowsetView widget can deal with.
   * @param request The current request
   * @param task The current task
   * @param sql The sql to populate the rowset with
   * @param sRefHeader The first header expected in the file
   * @param sTableName The name of the table  -- may be null
   * @param maxRows acts like 'top n' in SQL
   * @return Returns a populated rowsetView
   */
  public DefaultTableView getSQLRowsetView(HttpServletRequest request,
      String procName, ArrayList<String> params, String sRefHeader, String sTableName, int maxRows, Db db)
  {

    // -- writes result set directly from db to file
    // -- saves memory on large datasets
    ResultSetDataModel rsDataModel = new ResultSetDataModel();
    rsDataModel.setReferenceHeader(sRefHeader);
    rsDataModel.writeCSVResultSetMultiThreaded(procName, params, db);
   //new Delay(2500);

    // offers JSP convenient accessors such as getRowset(),
    // -- based on underlying data in dataModel
    DefaultTableView view = new DefaultTableView(rsDataModel);
    
    view.setStartRow(1);
    view.setMaxRowsetSize(maxRows);
    view.setName(sTableName);
    view.setMessage(getMessage(view));
    
    return view;
  }

  /**
   * Uses a CSV file to create a rowsetView table for display
   * from a JSP.
   * @param request The current request
   * @param file The file containing data to build the table from
   * @param headerNames A list of headers names in the file
   * @param db The 
   * @return
   */
  public DefaultTableView getFileRowsetView(File file, String sRefHeader, 
      String sTableName, List widgets, Vector colHeaders, int maxRows )
  {
    DefaultTableView view = null;
    if (file == null)
    {
      throw new LinxUserException("File was null at getFileRowsetView(). "
          + Strings.MSG.ALERT_LIMS_ADMIN);
    }
    try
    {
      CSVDataModel csvDataModel = new CSVDataModel(file, sRefHeader);
      csvDataModel.setColumnHeaders(colHeaders);

      view = new DefaultTableView(csvDataModel);
      view.setStartRow(1);
      view.setMaxRowsetSize(maxRows);
      view.setName(sTableName);

      if(widgets != null)
      {
      ListIterator itor = widgets.listIterator();
      while( itor.hasNext())
      {
        view.setWidget(itor.nextIndex(), (String)itor.next());
      }// next widget
      // at exit, widgets for any widget cells are set
      }
      return view;


    }
    catch(Exception ex)

    {
      throw new LinxUserException("Unable to process file "
          + file.getAbsolutePath());
    }

  }
  
  /**
   * Uses a CSV file to create a rowsetView table for display
   * from a JSP.
   * @param request The current request
   * @param file The file containing data to build the table from
   * @param headerNames A list of headers names in the file
   * @param db The 
   * @return view
   */
  public DefaultTableView getFileRowsetView(File file, String sRefHeader, 
      String sTableName, List widgets, String[] colHeaders, int maxRows )
  {
    Vector v = new Vector();
    for(int i = 0; i < colHeaders.length; i++)
    {
      v.addElement(colHeaders[i]);
    }// next header
    // at exit, v is a list of colHeaders
    return getFileRowsetView(file, sRefHeader, sTableName, widgets, v, maxRows);
  }
  

  
  /**
   * A dummy source file for drawing numSectors number of rows
   * on the UI, with colHeaders in format "Header1,Header2,Header3,...".
   * @param numRows
   * @param colHeaders
   * @return file to use as view datasource
   */
  public File getSourceFile(int numRows, String[] colHeaders)
  {
    // overwrite old versions (no harm done)
    File file = new File("temp_" + numRows + ".csv");
    if( file.exists())
    {
      file.delete();
      file = new File("temp_" + numRows + ".csv");
    }    
    try
    {
      FileWriter writer = new FileWriter(file);
      for(int i = 0; i < colHeaders.length; i++)
      {
        writer.append(colHeaders[i]);
        if( colHeaders.length > i+1)
        {
          writer.append(",");
        }
      }
      writer.append(Strings.CHAR.NEWLINE);
      for(int i = 0; i < numRows; i++)
      {
        for(int j = 0; j < colHeaders.length; j++)
        {
          writer.append(" ");
          if( colHeaders.length > j+1)
          {
            writer.append(",");
          }
        }
        writer.append(Strings.CHAR.NEWLINE);
      }
      writer.flush();
      // at exit, writer has a csv representation of table JSP can use

    }
    catch (IOException e)
    {
      throw new LinxSystemException(e.getMessage());
    }    
    return file;
  }
  
  /**
   * A dummy source file for drawing numSectors number of rows
   * on the UI, with colHeaders in format "Header1,Header2,Header3,...".
   * @param numRows
   * @return file to use as view datasource
   */
  public File getSourceFile(int numRows, Vector colHeaders)
  {
    String[] ayHeaders = new String[colHeaders.size()];
    for(int i = 0; i < colHeaders.size(); i++)
    {
      ayHeaders[i] = (String)colHeaders.elementAt(i);
    }
    return getSourceFile(numRows, ayHeaders);
  }
  
  /**
   * A dummy source file for drawing numSectors number of rows
   * on the UI, with colHeaders in format "Header1,Header2,Header3,...".
   * @param numRows
   * @return file to use as view datasource
   */
  public File getSourceFile(int numRows, String colHeaders)
  {
    // overwrite old versions (no harm done)
    File file = new File("temp_" + numRows + ".csv");
    if( file.exists())
    {
      file.delete();
      file = new File("temp_" + numRows + ".csv");
    }
    try
    {
      FileWriter writer = new FileWriter(file);
      writer.append(colHeaders + Strings.CHAR.NEWLINE);
      for(int i = 0; i < numRows; i++)
      {
        writer.append(Strings.ALPHABET[i] + ",  ,  " + Strings.CHAR.NEWLINE);
      }
      writer.flush();
      // at exit, writer has a csv representation of table JSP can use

    }
    catch (IOException e)
    {
      throw new LinxSystemException(e.getMessage());
    }    
    return file;
  }
  
  /**
   * Returns the options for a pass/fail dropdown.
   * @param db
   */
   public List getOptionsList()
   {
     List list = new ArrayList();
     list.add("(Select)");
     list.add("Pass");
     list.add("Fail");
     return list;
   }
  	
}
