package com.sgsi.emre.task;


/**
 * Parses incoming file of sampling data to create new SAMPLINGTIMEPOINT db
 * table records for Greenhouse, WoL, and SSTF tabs in MS Excel 2010 workbooks.
 * 
 * @created TJS/Wildtype for SGI
 * @date 3/2011
 * @version 2.0
 * 
 */
public class SubmitGrabData extends EMRETask
{
/**
	 * Returns true to allow caller to create new samplingTimepoint records 
	 * if they don't already exist. Edit Grab Data returns false.
	 * @return true
	 */
	public boolean createNewTimepoints()
	{
		return true;
	}

}
