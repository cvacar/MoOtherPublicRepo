package com.sgsi.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.util.LinxDbException;


/**
 * 
 * Box.java
 * 
 * Models a storage box for strain, cultures,
 * and samples with a hard-coded coordinate system,
 * usually contained in a Freezer object. 
 * Used by Met LIMS tasks * Strain Collection.
 * 
 * @author TJS/Wildtype for SGI
 * @date 10/2008
 */
public class Box
{
	// coord system, hard-coded for robustness/easy maintenance
	final String[] ayCoordsPadded = {"01","02","03","04","05","06","07","08","09",
					           "10","11","12","13","14","15","16","17","18",
					           "19","20","21","22","23","24","25","26","27",
					           "28","29","30","31","32","33","34","35","36",
					           "37","38","39","40","41","42","43","44","45",
					           "46","47","48","49","50","51","52","53","54",
					           "55","56","57","58","59","60","61","62","63",
					           "64","65","66","67","68","69","70","71","72",
					           "73","74","75","76","77","78","79","80","81"};			           
	
	Integer[] ayCoordsInt =  new Integer[81];
	HashMap mapContents = null;
	int MAX_POSITION = 81;
	String name = "unset";
	Freezer freezer = null;
	String strainType = null;
	
	
	public Box()
	{
		init();
	}
	
	/**
	 * Inits coord system.
	 */
	protected void init()
	{
		for(int i = 1; i < 82; i++)
		{
			ayCoordsInt[i] = i;
		}// next coord in box
		mapContents = new HashMap();
	}

		/**
	 * Inits coord system and populates itself 
	 * with max box for this straintype from db.
	 */
	protected void getCurrentForStrainType(String strainType, Db db)
	{
		init();
		this.strainType = strainType;
		String sql = "exec spMet_getCurrentBox '" + strainType + "'";
		try
		{
			ResultSet rs = db.getHelper().getResultSet(sql, db);
			while(rs.next())
			{
				this.name 			= rs.getString(1);
			    String paddedCoord 	= rs.getString(2);

			}
		}
		catch (LinxDbException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the freezer
	 */
	public Freezer getFreezer()
	{
		return freezer;
	}

	/**
	 * @param freezer the freezer to set
	 */
	public void setFreezer(Freezer freezer)
	{
		this.freezer = freezer;
	}

	/**
	 * @return the lastFilledPaddedPosition
	 */
	public String getLastFilledPaddedPosition()
	{
		return null;
	}


	/**
	 * @return the lastFilledPosition
	 */
	public int getLastFilledPosition()
	{
		return 0;
	}

	public void setStrainType(String type)
	{
		this.strainType = type;
	}
	
	
	public String getStrainType()
	{
		return strainType;
	}
		
	public boolean isFull()	
	{
		return (getLastFilledPosition() == MAX_POSITION);
	}
}
