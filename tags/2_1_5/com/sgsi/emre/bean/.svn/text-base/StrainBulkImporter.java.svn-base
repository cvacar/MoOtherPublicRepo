package com.sgsi.emre.bean;

import org.apache.poi.hssf.usermodel.HSSFCell;

import com.wildtype.linx.util.WtUtils;


/**
 *  
 * StrainBulkImporter
 * 
 * Imports an XLS file in Gena Lee's 
 * strain bulk import format, with fields
 * matching the Strain Collection task screen's fields.
 * 			
 * @author TJ Stevens/Wildtype for SGI
 * @created 4/2008
 */
public class StrainBulkImporter extends SampleManifestImporter
{
    /**
   * Returns the numeric cell's value as String. Overridden
   * to eliminate ".0" added by POI Excel parsing library. 
   * @param cell 
   * @return number in cell as string
   */
  protected String getNumericValueAsString(HSSFCell cell)
  {
    // primitive parser has no 'getObjectCellValue()' option
    String s = cell.toString();
    if(!WtUtils.isNullOrBlank(s) && s.endsWith(".0"))
    {
    	// eliminate decimal value added by third-party parsing library
    	s = s.substring(0, s.lastIndexOf(".0"));
    }
    return s;
  }
 
}
