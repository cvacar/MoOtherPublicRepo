package com.sgsi.emre.task;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sgsi.emre.util.S4MSmallBarcode;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Called by Print Culture Labels task to turn the user's list of Culture IDs
 * into labels. Checks that Culture IDs exist in LIMS. Uses its own print service
 * specified in APPVALUE table as 'Print Culture Labels'. Eff v1.15.0.
 * 
 * @author TJS/Wt for SGI
 * @date 2/2011
 * 
 */
public class PrintCultureLabels extends EMRETask
{

	/**
	 * There is no other work in this task (no container items)
	 * aside from printing labels for the entered Culture IDs.
	 */
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		String itemType = getServerItemValue("ItemType");
		if (WtUtils.isNullOrBlankOrPlaceholder(itemType))
		{
			throw new LinxUserException(
					"Please select a label type from the dropdown list.");
		}

		// user may have entered up to 12 culture IDs
		String sql = "spEMRE_isCultureExisting";
		ArrayList<String> cultures = (ArrayList<String>) getServerItemValues("CultureID");
		if (cultures.size() < 1)
		{
			throw new LinxUserException("Please enter at least one Culture ID,"
					+ "then try again.");
		}

		ListIterator<String> itor = cultures.listIterator();
		while (itor.hasNext())
		{
			String culture = (String) itor.next();
			ArrayList<String> params = new ArrayList<String>();
			params.add(culture);
			params.add(itemType);
			String exists = dbHelper.getDbValueFromStoredProc(db, sql, params);
			if (Boolean.parseBoolean(exists) == false)
			{
				throw new LinxUserException("Culture ID " + culture
						+ " has not been saved in LIMS or is not of type " + itemType 
						+ ". Please check this entry, then try again.");
			}
		}// next cultureId
		// by here, all culture IDs exist in LIMS

		// in v1.15.0, Culture ID is only text on label
		// -- may add username later per Vidya
		// lets loop and print
		S4MSmallBarcode printer = new S4MSmallBarcode();
		PrintService printService = null;
		printService = dbHelper.getPrintServiceForTask(
					this.getTaskName(), db);
		Code.debug("Printing labels on printer: " + printService);
		dbHelper.updateTaskHistoryComment(getTranId(), "Printer: " + printService.getName(), true/*append*/, db);

		// refresh the itor
		itor = cultures.listIterator();
		try
		{
			while (itor.hasNext())
			{
				String cultureId = (String)itor.next();
				ArrayList<String> alRow = new ArrayList<String>();
				// one additional row under the culture ID
				//per vidya - do not need to print the itemtype on the label 
				//just print the item on the label 02/18/2011
				//alRow.add(itemType.substring(0,1)); // I, E, S etc.
				if(cultureId.length() > 14)//only 14 characters fit on a line - so split the id
				 {
					 int numIterations = 1;
					 char[] chars = cultureId.toCharArray();
					 String line = "";
					 for(int i = 0; i < chars.length; i++)
					 {
						 do
						 {
							 do
							 {
								 line += chars[i];
								 i++;
							 }
							 while(i < chars.length && i < numIterations * 14);
							 alRow.add(line);
							 line = "";
							 numIterations++;
						 }
						 while(numIterations <= 2);//only going to print 2 lines - culture id should fit on two lines
						 
					 }
					
				 }
				 else
				 {
					 alRow.add(cultureId);
				 }
				String label = printer.getZPLforLabelNoBarcode(alRow);
				Code.debug("Printing label: " + label);
				S4MSmallBarcode.print(printService, label);
				alRow = null;
				// Lets make sure these things print in order
				Thread.sleep(20);
			}// next label
		} 
		catch (Exception ex)
		{
			throw new LinxUserException(
					"Error occurred during label printing: "
							+ ex.getMessage());
		}
		this.setMessage("Successfully printed culture labels.");

	}

}
