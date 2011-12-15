package com.sgsi.emre.task;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;

/**
 * UpdateStrain
 *
 * Eff v2.1
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 4/2011
 */
public class UpdateStrain extends CreateStrain
{
	
	 /**
   * Inserts new or updates screen-based STRAIN into custom table.
   * @param db
   */
  protected void updateCustomTables(HttpServletRequest request, Db db)
  {
	  String strain = getServerItemValue(ItemType.STRAIN);
			Code.info("Updating custom table record for strain " + strain);
			setMessage("Successfully updated properties of strain " + strain);

			// update editable values only (brute force method here)
			ArrayList<String> params = new ArrayList<String>();
		    params.add(strain);
		    params.add(ItemType.PROJECT);
		    params.add(getServerItemValue(ItemType.PROJECT));
		    params.add(getTranId()+"");

		    db.getHelper().callStoredProc(db, "spEMRE_UpdateStrain", params, false, true);
		    
		    params.clear();
		    params.add(strain);
		    params.add(DataType.GENUS);
		    params.add(getServerItemValue(DataType.GENUS));
		    params.add(getTranId()+"");

		    db.getHelper().callStoredProc(db, "spEMRE_UpdateStrain", params, false, true);

		    params.clear();
		    params.add(strain);		    
		    params.add(DataType.SPECIES);
		    params.add(getServerItemValue(DataType.SPECIES));
		    params.add(getTranId()+"");

		    db.getHelper().callStoredProc(db, "spEMRE_UpdateStrain", params, false, true);

		    params.clear();
		    params.add(strain);	
		    params.add(DataType.NOTEBOOK_REF);
		    params.add(getServerItemValue(DataType.NOTEBOOK_REF));
		    params.add(getTranId()+"");

		    db.getHelper().callStoredProc(db, "spEMRE_UpdateStrain", params, false, true);

		    params.clear();
		    params.add(strain);			    
		    params.add(DataType.COMMENT);
		    params.add(getServerItemValue(DataType.COMMENT));
		    params.add(getTranId() + "");
		    
		    db.getHelper().callStoredProc(db, "spEMRE_UpdateStrain", params, false, true);
		      	    
  }
  
  

}
