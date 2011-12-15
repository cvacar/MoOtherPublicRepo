package com.sgsi.emre.task;



/**
 * Parses Excel file submitted by user and
 * updates SAMPLINGTIMEPOINT  custom tables in database 
 * with edited sampling data.
 * Can be run multiple times on the same sampling ID;
 * task will preserve a copy of each pre-update record
 * in SAMPLINGTIMEPOINTHISTORY table. Effective EMRE 2.0.
 * @author TJS/Wildtype for SGI
 * @date 3/2011
 */
public class EditGrabData extends SubmitGrabData 
{

	
	/**
	 * Returns true if a samplingTimepoint record doesn't
	 * already exist. Edit Grab Data returns false.
	 * @return true/false
	 */
	public boolean createNewTimepoints()
	{
		return false;
	}
	

}
