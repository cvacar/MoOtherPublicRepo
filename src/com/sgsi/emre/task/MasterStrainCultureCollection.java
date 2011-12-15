package com.sgsi.emre.task;

import com.sgsi.emre.EMREStrings.ItemType;


public class MasterStrainCultureCollection extends CultureCollection 
{


	/**
	 * Returns the header expected in the first column
	 * of the data rows. 
	 * @return header in first column above data rows
	 */
	public String getColumnKey()
	{
		return "Strain ID*";
	}
	

	
	public String getCultureIDHeader()
	{
		return "Strain Culture ID";
	}
	
	public String getCultureType()
	{
		return ItemType.STRAINCULTURE;
	}





}
