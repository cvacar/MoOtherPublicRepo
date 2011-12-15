package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.user.User;

/**
 * RemoveStrainVial
 *
 * Overridden to validate Location for a given strain
 * and remove that location for the strain if found.
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 6/2008
 */
public class RemoveStrainVial extends EMRETask
{

	/**
	 * Overridden to validate Location for a given strain
	 * and to remove that location if found. Throws an error
	 * if the strain is not at that location.
	 * @param request
	 * @param response
	 * @param user
	 * @param db
	 */
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		
		String strain = getServerItemValue(ItemType.STRAIN);
		String location = getServerItemValue(DataType.LOCATION);
		
		// update strain location
		ArrayList params = new ArrayList();
		params.add(strain);
		params.add(location);
		
		// throws an error if strain is not in this location
		// todo: list locations on screen and allow user to choose one?
		String sql = "spMet_DeleteStrainLocation";
		db.getHelper().callStoredProc(db, sql, params,false,false);
		
	}




    

}
