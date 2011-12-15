package com.sgsi.emre.task;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.WtUtils;

/**
 * HostStrainCollection
 *
 * Handles custom action for Photo host strains
 * by printing a strain-specific barcode label
 * and updating custom table for strain.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 6/2008
 */
public class PhotoHostStrainCollection extends StrainCollection
{

  /** 
   * Returns the type of strain being handled by this task, 
   * one of Met E, Photo, and Photo E.
   * @return
   */
  	protected String getStrainType()
	{
		return "Photo H";
	}
  	
  		/**
	 * return the correct prefix for the strain based upon task.
	 * @param taskName
	 * @return
	 */
	public String getStrainPrefix(Db db)
	{
		String prefix = dbHelper.getDbValue("exec spMet_getStrainPrefix '" 
				+ getStrainType() + "'", db);
		String suffix = dbHelper.getDbValue("exec spMet_getIDSuffix", db);
		if(WtUtils.isNullOrBlank(suffix))
			suffix = "";
		else
			suffix = suffix + "-";
		return prefix + "-" + suffix;
	}
  	  
	/**
	 * Allows subclasses to return the name of the 
	 * stored proc they need to use to insert a new
	 * strain. Photo Host Strain requires nine locations
	 * vs three, for example.
	 * @return name of sp to use for file-based inserts
	 */
  protected String getFileInsertSQL()
	{
		return "spMet_InsertPhotoHostStrainFromFile";
	}
  
  	/**
	 * return the correct prefix for the strain based upon task.
	 * @param taskName
	 * @return
	 */
	protected String getStrainBoxPrefix(Db db)
	{
		String sql = "exec spMet_getStrainCollectionBoxPrefix '" + getStrainType() + " Strain'";
		String prefix = db.getHelper().getDbValue(sql, db);
		return prefix;
	}
	

	/**
	 * Returns correct prefix for boxes storing backup archives,
	 * currently 'BU' for all strains including Photo strains. 
	 * This changed as of 11.11.08 per Gena Lee
	 * Hi Bobby Jo,
	 *	To prevent future confusion, can we just name the back-up (BU) 
	 *  freezer boxes for PE and PH strains as PEBU and PHBU, respectively?
	 *Thanks!
	 *-Gena Lee
	 *
	 * @return
	 */
	protected String getStrainBackupBoxPrefix(Db db)
	{
		String sql = "exec spMet_getStrainCollectionBackupBoxPrefix '" + getStrainType() + " Strain'";
		String prefix = db.getHelper().getDbValue(sql, db);
		return prefix;
	}
	
    /**
     * Number of rows to show in the Location UI table
     * on initial display, for defining new strain.
     * @return 4
     */
	protected int getLocationRowCount()
	{
		return 4;
	}  
}
