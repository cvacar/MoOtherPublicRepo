package com.sgsi.emre.servlet;



/**
 * Handles custom actions for bulk import of cultures, returning next ID
 * available for new cultures, and exporting culture collection.
 * 
 * @author BJS/Wildtype for SGI
 * @modified 4/2011 for EMRE v2.1
 * 
 */
public class SvMasterStrainCultureCollection extends SvCultureSelection
{


	/**
	 * Returns name of stored procedure to retrieve
	 * culture collection.
	 */
	public String getCultureCollectionSQL()
	{
		return "spEMRE_reportCultureCollectionByList";
	}



}
