package com.sgsi.emre.bean;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.DisplayItem;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * ProtocolTypeBean
 * 
 * Models an Protocol defined in the Protocol custom database table,
 * for use by SG Met LIMS task Define Protocol when inserting
 * new Protocols.
 * 
 * @author TJS/Wildtype for Synthetic Genomics.
 * @date 1/2008
 */
public class ProtocolTypeBean 
{
   HashMap map = new HashMap();
   
   /**
    * Hashes values for those itemTypes
    * that have a value, for saving to Protocol table later.
    * @param items DisplayItem or server-side Item objects
    */
    public void setProperties(List items)
    {
        ListIterator itor = items.listIterator();
        while (itor.hasNext())
        {
            DisplayItem item = (DisplayItem) itor.next();
            String itemType = item.getItemType();
            String value = item.getValue();
            if(!WtUtils.isNullOrBlankOrPlaceholder(value))
            {
                // upcase to compare later to column names at save()
                setProperty(itemType.toUpperCase(), value);
            }
        }// next itemType
        // at exit, have hashed any non-null values by itemtype
    }
       
   /**
     * Called by setProperties() to hash each value against
     * its itemType. Allows LIMS to support Protocols for several
    * tasks, each with different properties and each with different
    * optional properties.
     * @param property
     * @param value
     */
    public void setProperty(String property, String value)
    {
        Code.info("Setting Protocol property " + property + "=" + value);
        map.put(property, value);
    }
    
    /**
     * Returns the property stored for this key,
     * or null if not found.
     * @param key
     * @return property
     */
    public String getProperty(String key)
    {
        if(map.containsKey(key))
        {
            return (String)map.get(key);
        }
        return null;
    }
    
    /**
     * Returns the map of property name/value pairs
     * for caller to use as needed.
     * @return map
     */
    public HashMap getProperties()
    {
        return this.map;
    }
    /**
     * Inserts custom db table Protocol with this instance
     * of the given ProtocolType. It's more efficient to construct this sql
     * on the fly to allow each task to use only those fields needed.
     * @param protocolRef 
     * @param commentId 
     * @param tranId 
     * @param db
     *
    public void save(String protocolRef, String commentId, long tranId, Db db)
    {
        // set up insert sql
        StringBuffer insertBuff = new StringBuffer();
        insertBuff.append("insert into protocol(");
        StringBuffer valuesBuff = new StringBuffer();
        valuesBuff.append(" values(");
            
        // for each Protocol field name, append a value if provided by user
        // -- relies on field names matching task def itemTypes
        // -- allows optional fields and multiple Protocol types to use same code
        String sql = "select top 1 * from protocol"; // just to get column names
        ResultSet rs = db.getHelper().getResultSet(sql, db);
        try
        {
            // check for a hashed value for each Protocol table column name
            ResultSetMetaData metaRs = rs.getMetaData();
            for(int i = 1; i < metaRs.getColumnCount()+1; i++)
            {
                String colName = metaRs.getColumnName(i).toUpperCase();
                if(map.containsKey(colName))
                {
                  if((!insertBuff.toString().endsWith("("))) insertBuff.append(",");
                  if((!valuesBuff.toString().endsWith("("))) valuesBuff.append(",");
                    insertBuff.append(colName);
                    valuesBuff.append("'" + map.get(colName) + "'");
                }
                
            }// next column name
            // at exit, have added any user-provided fields to INSERT stmt
        }
        catch(SQLException se)
        {
            throw new LinxDbException("At save Protocol: " + se.getMessage());
        }
        // finish up the INSERT stmt
        insertBuff.append(",tranId)");
        valuesBuff.append("," + tranId + ")");
        // stick the clauses together
        insertBuff.append(valuesBuff);
        
        // execute INSERT
        db.getHelper().executeSQL(insertBuff.toString(), db);
    }*/
    
    /**
     * Inserts custom db table Protocol with this instance
     * of the given ProtocolType. It's more efficient to construct this sql
     * on the fly to allow each task to use only those fields needed.
     * @param protocolRef 
     * @param protocolType 
     * @param bAddProtocolType - true if displayitems don't include itemType 'ProtocolType' 
     * @param commentId 
     * @param tranId 
     * @param db
     */
    //public void save(String protocolRef, String protocolType, String commentId, long tranId, Db db)
    public void save(String protocolType, String commentId, long tranId, Db db)
    
    {
        // set up insert sql
        StringBuffer insertBuff = new StringBuffer();
        StringBuffer valuesBuff = new StringBuffer();
        if(true /*bAddProtocolType*/)
        {  // used by protocol-using task - doesn't provide ProtocolType in display item values
          insertBuff.append("insert into protocol(protocolType");
          valuesBuff.append(" values('" + protocolType + "'");
        }
  
        // for each Protocol field name, append a value if provided by user
        // -- relies on field names matching task def itemTypes
        // -- this design allows optional fields and multiple Protocol types to use same code
        String sql = "exec spMet_GetProtocolProperties '0'"; // just to get column names
		String comment = null;
		String name = null;
		
       // String sql = "select top 1 * from protocol";
        ResultSet rs = db.getHelper().getResultSet(sql, db);
        try
        {
            // check for a hashed value for each Protocol table column name
            ResultSetMetaData metaRs = rs.getMetaData();
            for(int i = 1; i < metaRs.getColumnCount()+1; i++)
            {
                String colName = metaRs.getColumnName(i).toUpperCase();
                Code.info("Looking for value for Protocol property: " + colName);
                if(map.containsKey(colName))
                {
                    String value = (String)map.get(colName);
                    Code.info("Found Protocol value: " + value);
                    if(WtUtils.isNullOrBlankOrPlaceholder(value))
                    {
                      // catch (Template)
                      throw new LinxUserException("Please replace placeholder " 
                          + value + " with a value, then try again.");
                    }
                    else if(colName.equalsIgnoreCase("Comments")
                    		|| colName.equalsIgnoreCase("Comment"))
                    {
                    	// must add separately later
                    	comment = value;
                    	continue;
                    }
                    //value = value.replace("\r\n"," ");
                    //value = value.replace('\n',' ');
                    if((!insertBuff.toString().endsWith("("))) insertBuff.append(",");
                    if((!valuesBuff.toString().endsWith("("))) valuesBuff.append(",");
                    insertBuff.append(colName);
                    valuesBuff.append("'" + value + "'");
                }
                
            }// next column name
            // at exit, have added any user-provided fields to INSERT stmt
        }
        catch(SQLException se)
        {
            throw new LinxDbException("At save protocol: " + se.getMessage());
        }
        // finish up the INSERT stmt
        insertBuff.append(",tranId)");
        valuesBuff.append("," + tranId + ")");
        // stick the clauses together
        insertBuff.append(valuesBuff);
        
        // execute INSERT
        sql = insertBuff.toString();
        db.getHelper().executeSQL(sql, db);
        
        // update comment separately
        ArrayList params = new ArrayList();
        params.add(tranId +"");
        params.add(comment);
        db.getHelper().callStoredProc(db, "spMet_UpdateProtocolComment", params, false, true);
    }
}
