package com.sgsi.emre.servlet;

import javax.servlet.http.HttpServletRequest;

import com.wildtype.linx.task.Task;


/**
 * 
 * SvPhotoStrainCollection
 *
 * Overridden to handle custom actions to retrieve
 * past uploaded data or to print a label for a 
 * saved strain. Super handles all methods.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 6/2008
 */
public class SvPhotoStrainCollection extends SvStrainCollection
{

	/**
	 * Returns the name of stored proc to use
	 * to retrieve this type of strain. 
	 * @return name of sp to get strain report for this type of strain
	 */
    protected String getStrainReportSQL()
	{
		return "exec spMet_GetPhotoEColiStrainCollection";
	}

    /**
     * Number of rows to show in the Location UI table
     * on initial display, for defining new strain.
     * @return 3
     */
	protected int getLocationRowCount()
	{
		return 3;
	}   
    
}
