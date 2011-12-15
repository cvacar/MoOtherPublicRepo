package com.sgsi.emre.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.project.Strings;
import com.sgsi.emre.EMREStrings.FileType;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.task.CultureCollection;
import com.sgsi.emre.task.EMRETask;
import com.wildtype.linx.config.LinxConfig;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;
import com.wildtype.linx.view.RowsetView;



/**
 * Handles custom actions for bulk import of cultures, returning next ID
 * available for new cultures, and exporting culture collection.
 * 
 * @author BJS/Wildtype for SGI
 * @modified 4/2011 for EMRE v2.1
 * 
 */

@SuppressWarnings("unused")
public class SvPrintCultureBarcodes extends SvCultureSelection
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
