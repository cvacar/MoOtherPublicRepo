package com.sgsi.emre.bean;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * CloneDataBean
 *
 * Used by SG Met LIMS task Manage Clone Collection
 * to display and process values in the custom
 * JSP.
 * 
 * @author TJ Stevens/Wildtype for SGI
 * @created 12/2007
 */
public class CloneDataBean
{
  /**
   * Maps property names to values for this clone.
   */
  private HashMap map = new HashMap();
 
     
  /**
   * Models a culture in the culture collection, 
   * populated from values entered into a 
   * custom JSP.
   */
  public CloneDataBean()
  {
    
  }
  
  /**
   * Inserts new Clone record into custom db
   * table CLONE. Requires knowledge of names
   * of fields and stored proc params. Update
   * this method whenever clone property names
   * are altered.
   * @param tranId
   * @param db
   */
  public void updateCustomTables(long tranId, Db db)
  {
    
    // update CLONE table (will insert if new clone)
    ArrayList params = new ArrayList();
    // params are alphabetical, to make sync'g easier
    params.add(getAntibioticResistance());
    params.add(getCatalogNumber());
    params.add(getCloneId());
    params.add(getClone());
    params.add(getCloneType());
    params.add(getComments());
    params.add(getCulture());
    params.add(getDnaSequenceFile());
    params.add(getDnaSequence());
    params.add(getHostOrganism());
    params.add(getLibraryId());
    params.add(getLocation());
    params.add(getMedium());
    params.add(getNotebookRef());
    params.add(getORI());
    params.add(getParentalVector());
    params.add(getPlasmid());
    params.add(getProject());
    params.add(getRestricted());
    params.add(getTemplateId());
    params.add(getVectorMap());
    params.add(getVendor());
    params.add(tranId+"");
    
    // will insert if new
    db.getHelper().callStoredProc(db, "spMet_UpdateClone", params, false, true);
  }



  /**
   * @return name.
   */
  public String getClone()
  {
    return (String)map.get("CLONE");
  }

  
  /**
   * @return culture
   */
  public String getCulture()
  {
    return (String)map.get("CULTURE");
  }
  

  /**
   * @return abResistance.
   */
  public String getAntibioticResistance()
  {
    return (String)map.get("ANTIBIOTICRESISTANCE");
  }


  /**
   * @return catalogNum.
   */
  public String getCatalogNumber()
  {
    return (String)map.get("CATALOGNUMBER");
  }

  /**
   * @return cloneID.
   */
  public String getCloneId()
  {
    return (String)map.get("CLONEID");
  }


  /**
   * @return cloneType.
   */
  public String getCloneType()
  {
    return (String)map.get("CLONETYPE");
  }

  /**
   * @return location.
   */
  public String getLocation()
  {
    return (String)map.get("LOCATION");
  }
  /**
   * @return hostOrganism.
   */
  public String getHostOrganism()
  {
    return (String)map.get("HOSTORGANISM");
  }


  /**
   * @return libraryID.
   */
  public String getLibraryId()
  {
    return (String)map.get("LIBRARYID");
  }

  /**
   * @return medium.
   */
  public String getMedium()
  {
    return (String)map.get("MEDIUM");
  }

  /**
   * @return notebook.
   */
  public String getNotebookRef()
  {
    return (String)map.get("NOTEBOOKREF");
  }


  /**
   * @return oRI.
   */
  public String getORI()
  {
    return (String)map.get("ORI");
  }

  /**
   * @return parentalVector.
   */
  public String getParentalVector()
  {
    return (String)map.get("PARENTALVECTOR");
  }

  /**
   * @return plasmid.
   */
  public String getPlasmid()
  {
    return (String)map.get("PLASMID");
  }

  /**
   * @return project.
   */
  public String getProject()
  {
    return (String)map.get("PROJECT");
  }
  /**
   * @return restricted.
   */
  public String getRestricted()
  {
    return (String)map.get("RESTRICTED");
  }
  
  /**
   * @return templateID.
   */
  public String getTemplateId()
  {
    return (String)map.get("TEMPLATEID");
  }

  /**
   * @return vendor.
   */
  public String getVendor()
  {
	String s = (String)map.get("VENDOR");
	if(WtUtils.isNullOrBlankOrPlaceholder(s))
	{
		return null;
	}
    return s;
  }


  /**
   * @return comments.
   */
  public String getComments()
  {
    return (String)map.get("COMMENT");
 }

  /**
   * @return dnaSeq file 
   */
  public String getDnaSequenceFile()
  {
    return (String)map.get("DNASEQUENCEFILE");
 }

  /**
   * @return dna sequence if previously pasted into screen
   */
  public String getDnaSequence()
  {
	String s = (String)map.get("DNASEQUENCE");
    return s;
 }
  /**
   * @return vectorMap.
   */
  public String getVectorMap()
  {
    return (String)map.get("VECTORMAP");
 }

  
  /**
   * Called by Manage Culture Collection
   * task class to populate this bean from 
   * database result set. This is the generic
   * approach, allowing new fields to be added
   * by updating 1) SQL, 2) this bean class,
   * and the 3) custom JSP, PgTask_Manage_Cultures.jsp. 
   * (The custom JSP calls specific
   * methods on this bean, so can't avoid updating
   * it when new fields are necessary.)
   * @param fieldName
   * @param value
   */
  public void setProperty(String fieldName, String value)
  {
    Code.info("Setting clone property  " + fieldName + " = " + value );
    map.put(fieldName.toUpperCase(), value);
  }
  
  /**
   * Hits db to populate properties of data bean for
   * use by custom JSP. Called by task sv.
   * @param cloneId
   * @param db
   */
  public void populatePropertiesFromDb(String cloneId, Db db)
  {
    // not task class local bean; belongs to task sv
    
    // look up culture 
    String sql = "exec spMet_GetCloneProperties '" + cloneId + "'";
    ResultSet rs = db.getHelper().getResultSet(sql, db);
    try
    {
      ResultSetMetaData rsMeta = rs.getMetaData();
      int colCount = rsMeta.getColumnCount();
      while(rs.next())
      {
        for(int colIdx = 1; colIdx < colCount+1; colIdx++)
        {
          String colName = rsMeta.getColumnName(colIdx).toUpperCase();
          String value   = rs.getString(colIdx);
          // alphabetical, to help sync with sp
          setProperty(colName, value);
        }

      }// should be only one row
      rs.close();
      rs = null;
      
    }
    catch (SQLException e)
    {
      throw new LinxDbException("At populateCloneBeanFromDb(): " + e.getMessage());
    }
    // at exit, have populated bean with data for this culture
    if(WtUtils.isNullOrBlankOrPlaceholder(getCloneId()))
    {
      throw new LinxUserException("Could not find a clone with ID " + cloneId);
    }
    
  }
  
  /**
   * Uses server-side item values to set properties on the bean,
   * so bean can be used by subsequent methods without knowledge
   * of how values were obtained (i.e., from custom JSP).
   * @param task 
   * @param db
   */
  public void populatePropertiesFromUserData(Task task, Db db)
  {
      // get new clone properties from server-side items set by JSP values
      // -- use column names to find items to keep this as generic as possible
      String sql = "exec spMet_GetCloneProperties '0'";
      ResultSet rs = db.getHelper().getResultSet(sql, db);
      try
      {
        ResultSetMetaData rsMeta = rs.getMetaData();
        int colCount = rsMeta.getColumnCount();
          for(int colIdx = 1; colIdx < colCount+1; colIdx++)
          {
            // col names could be alphabetical, to help sync with sp
            String colName = rsMeta.getColumnName(colIdx);
            String value = task.getServerItemValue(colName);
            //colName = colName.replace('_', ' ');
            setProperty(colName.toUpperCase(), value);

          }// should be only one row
      }
      catch (SQLException e)
      {
        throw new LinxDbException("At populateCloneBeanFromUserData(): " + e.getMessage());
      }
      // at exit, have populated bean with data for this new culture      
    }
  
}
