package com.sgsi.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Sample
 *
 * Models a Sample from a sample manifest in the
 * LIMS project. Used by SampleManifestFileImporter class
 * as a member of a list available to Log Sample class
 * for accessioning new samples.
 * 
 * @author TJ Stevens/Wildtype for SGI
 * @created 3/2008
 */
public class Sample
{
  protected HashMap properties = new HashMap();
  protected List columnHeaders = new ArrayList();
  protected String cssBarcode = null;
  protected  String barcode = null;
  protected  String sampleType = null;
  
  /**
   * Creates a new Sample instance 
   * without any properties set.
   */
  public Sample()
  {
  }
 
  
  /**
   * @return index of sample manifest column containing SGI identifier for sample
   */
  public String getSGIBarcodeColumn()
  {
    return getProperty(LinxConfig.getConfig("SampleManifest", "BarcodeColumn"));
  }
  
  /**
   * Returns one of controlled vocabulary of supported
   * sample types.
   * @return type of sample, e.g. Aquatic, Environmental, etc.
   */
  public String getSampleType()
  {
    return sampleType;
  }
  
  /**
   * Stores the value of the given
   * property of this Sample under
   * the given key. Does not access the db.
   * @param key
   * @param value
   */
  public void setProperty(String key, String value)
  {
    properties.put(key, value);
  }
  
  /**
   * Returns the value of the given
   * property of this Sample, relying
   * on the caller having stored it 
   * previously under this key. 
   * Does not access the db.
   * @param key
   * @return value stored under given key, or null if none found
   */
  public String getProperty(String key)
  {
    return (String)properties.get(key);
  }

    /**
   * Returns the value of the given
   * property of this Sample, relying
   * on the caller having stored it 
   * previously under this key. 
   * Does not access the db.
   * @param key
   * @return value stored under given key, or null if none found
   */
  public String getRequiredProperty(String key)
  {
    String s = (String)properties.get(key);
    if(WtUtils.isNullOrBlank(s))
    {
    	throw new LinxUserException("Missing required field '" + key + "'.");
    }
    return s;
  }
  
    /**
   * Returns the value at the given
   * column index of this Sample, relying
   * on the caller having stored it 
   * previously under the column name key. 
   * Does not access the db.
   * @param colIdx column index
   * @return value stored under the col header, or null if none found
   */
  public String getProperty(ArrayList ayHeaders, int colIdx)
  {
	String key = (String)ayHeaders.get(colIdx);
    return (String)properties.get(key);
  }
  
  /**
   * Called by manifest importer to set
   * the sample type for this sample,
   * e.g. Aquatic, Enrichment, etc.
   * @param type
   */
  public void setSampleType(String type)
  {
	  this.sampleType = type;
  }
  /**
   * Called by manifest importer to set the
   * barcode for this Sample.
   * @param barcode
   */
  public void setSGIBarcode(String barcode)
  {
	  this.barcode = barcode;
  }
/**
 * Returns the barcode for this sample.
 * @return
 */
public String getSGIBarcode()
{
	return barcode;
}


/**
 * Returns the list of column headers
 * collected when this sample's manifest was parsed.
 * Keeps column names and order out of code. 
 * Done this way, developer needs
 * only to sync up the manifest columns with the stored procedure
 * params to change what's imported.
 * @return list of column headers for this type of sample
 */
public List getColumnHeaders()
{
	return columnHeaders;
}
  
public void setColumnHeaders(List cols)
{
	this.columnHeaders = cols;
}
  
}
