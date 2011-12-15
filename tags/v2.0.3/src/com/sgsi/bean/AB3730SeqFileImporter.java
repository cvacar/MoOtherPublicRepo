package com.sgsi.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import com.wildtype.linx.util.LimsImporter;
import com.wildtype.linx.util.LimsTokenizer;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Wraps a file in the format expected back
 * from the sequencing service when 3730 
 * sequencing is requested.
 * 
 * AB3730SeqFileImporter
 *
 * 
 * @author TJ Stevens/Wildtype for Synthetic Genomics
 * @created 7/2007
 */
public class AB3730SeqFileImporter extends LimsImporter
{
  String filename = null;

  /**
   * Allows import of files without inline headers
   * and without column headers, so, data rows only.
   * @param bLooseRules
   * @throws com.wildtype.linx.util.LinxUserException
   * @throws java.lang.Exception
   *
  public void importData(boolean bLooseRules) throws LinxUserException,
      Throwable
  {
    importDataP(bLooseRules);
    super.importData(bLooseRules);
  }
  */

  /**
   * @param bLooseRules
   * @throws LinxUserException
   * @throws Exception
   */
  private void importDataP(boolean bLooseRules) throws LinxUserException,
      Exception
  {
    // Validate first.
    // We must have data, a delim, and reference header
    if (dataContents == null)
      throw new LinxUserException(
          "An attempt was made to import without having a data file.");
    LimsTokenizer tk = null;
    try
    {
      BufferedReader reader = new BufferedReader(new StringReader(dataContents));
      String line = reader.readLine();
      // Check for Inline Data.
      while (line != null)
      {
        // process inline headings until we get to a column header
        if ((line.toUpperCase().indexOf(refHeading.toUpperCase()) > -1))
        {
          break;
        }
        // Get the first Token.
        tk = new LimsTokenizer(line, '=');
        int tkCount = tk.getTokenCount();
        // Lets make sure we have a pair of tokens.
        if (tkCount > 1)
        {
          String s = tk.getTokenAt(0);
          hshInline.put(tk.getTokenAt(0), tk.getTokenAt(1));
        }
        tk = null;
        line = reader.readLine();
      }// next line
    }
    catch (LinxUserException e)
    {
      bImported = false;
      throw new LinxUserException(e.getMessage());
    }
  }

  /**
   * Overridden to capture file name for use in error msgs.
   * 
   * @param data
   * @throws Exception
   */
  @Override
  public void setData(File data) throws Exception
  {
    filename = data.getName();
    super.setData(data);
  }

  /**
   * Overridden to throw error instead of returning null if header is missing.
   * 
   * @param prop
   * @return header value, or throws if missing
   */
  @Override
  public Object getInlineProperty(String prop)
  {
    String h = (String) super.getInlineProperty(prop);
    if (WtUtils.isNullOrBlank(h))
    {
      throw new LinxUserException("AB3730 file " + filename
          + " is missing required inline header " + prop + ".");
    }
    return h;
  }
  
}
