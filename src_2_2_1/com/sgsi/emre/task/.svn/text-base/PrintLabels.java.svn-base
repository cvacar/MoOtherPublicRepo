package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Prints labels for items specified by user at
 * EMRE LIMS task Print Labels in the Candidate Strain Isolation
 * workflow group. 
 * @author TJS/Wildtype for SGI
 * @modified 7/2011 TJS/Wt 
 * 	-- printing full item.item vs short barcode per Jay 7/2011
 *
 */
public class PrintLabels extends EMRETask
{

	@SuppressWarnings("unchecked")
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request,
			HttpServletResponse response, User user, Db db)
	{
		String itemType = getServerItemValue("ItemType");
		if (WtUtils.isNullOrBlankOrPlaceholder(itemType))
			throw new LinxUserException(
					"Please select a label type from the drop down list.");
		ArrayList<String[]> alData = null;
		if (!WtUtils.isNullOrBlank(getDisplayItemValue("MinBarcode")))
		{
			String minBarcode = getDisplayItemValue("MinBarcode");
			String maxBarcode = getDisplayItemValue("MaxBarcode");
			alData = getRangeLabelInfo(minBarcode, maxBarcode, itemType, db);
		}
		else // subset is entered into text area
		{
			ArrayList<String> barcodes = (ArrayList<String>) getServerItemValues(itemType);
			if(barcodes.isEmpty())
			{
				throw new LinxUserException("No IDs were found in textarea to print");
			}
			alData = getSubsetLabelInfo(barcodes, itemType, db);
		}
		// did we get anything from the DB?
		if (alData.size() < 1)
			throw new LinxUserException(
					"Could not find data needed to print for these strain IDs.");
		// by here, alData contains actual text for every label
		try
		{
			// now that we know we have valid strains lets loop and print
			S4MSmallBarcode printer = new S4MSmallBarcode();
			 PrintService printService = dbHelper.getPrintServiceForTask("Print Labels", db);
			 printer.setFontType("D");
			printer.setStartPrintXCoord(8);
			printer.setStartPrintYCoord(60);
			Code.debug("Printing labels on printer: " + printService.getName());
			dbHelper.updateTaskHistoryComment(getTranId(), "Printer: " + printService.getName(), true/*append*/, db);
			for (String[] ay : alData)
			{
				String s1 = ay[0];
				String s2 = ay[1];
				String s3 = ay[2];
				String s4 = ay[3];

				ArrayList<String> alRow = new ArrayList<String>();
				alRow.add(s2);
				alRow.add(s3);
				alRow.add(s4);
				//s1 = s1.replace("-SGI-E", "");
				String label = printer.getZPLforBoldBCLabel(s1, alRow);
				Code.debug("Printing label: " + label);
				S4MSmallBarcode.print(printService, s1, label);
				alRow = null;
				// Lets make sure these things print in order
				Thread.sleep(20);
			}// next label
			setMessage("Printed labels to " + printService.getName());

		}
		catch (Exception ex)
		{
			throw new LinxUserException("Error occurred during barcode printing: "
					+ ex.getMessage());
		}
	}

	/**
	 * Returns list of barcode label data falling between min and max barcode, 
	 * ready to print. Calls stored proc spEMRE_getInfoForRangePrint.
	 */
	protected ArrayList<String[]> getRangeLabelInfo(String minBarcode,
			String maxBarcode, String itemType, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		params.add(minBarcode);
		params.add(maxBarcode);
		params.add(itemType);

		ArrayList<String[]> alData = new ArrayList<String[]>();
		ResultSet rs = dbHelper.getResultSetFromStoredProc(db,
				"spEMRE_getInfoForRangePrint", params, false);
		try
		{
			while (rs.next())
			{
				String[] ay = new String[4];
				ay[0] = rs.getString(1);
				ay[1] = rs.getString(2);
				ay[2] = rs.getString(3);
				ay[3] = rs.getString(4);
				alData.add(ay);
				ay = null;
			}
			rs.close();
			rs = null;
		}
		catch (SQLException ex)
		{
			throw new LinxUserException("At spEMRE_getInfoForRangePrint:" + ex.getMessage());
		}
		return alData;
	}

	/**
	 * Returns list of barcode label data for a subset vs a defined range.
	 * Label text is ready to print. Calls stored proc
	 * spEMRE_getInfoForSubsetPrint.
	 */
	protected ArrayList<String[]> getSubsetLabelInfo(ArrayList<String> barcodes,
			String itemType, Db db)
	{
		ArrayList<String[]> alData = new ArrayList<String[]>();
		ArrayList<String> params = new ArrayList<String>();

		// call once for each strain
		ListIterator<String> itor = barcodes.listIterator();
		while (itor.hasNext())
		{
			params.clear();
			String barcode = itor.next();
			params.add(barcode);
			params.add(itemType);

			try
			{
				ResultSet rs = dbHelper.getResultSetFromStoredProc(db,
						"spEMRE_getInfoForSubsetPrint", params, false);

				while (rs.next())
				{
					String[] ay = new String[4];
					ay[0] = rs.getString(1);
					ay[1] = rs.getString(2);
					ay[2] = rs.getString(3);
					ay[3] = rs.getString(4);
					alData.add(ay);
					ay = null;
				}
				rs.close();
				rs = null;
			}
			catch (SQLException ex)
			{
				throw new LinxUserException("At spEMRE_getInfoForSubsetPrint: " + ex.getMessage());
			}
		}// next strain ID
		return alData;
	}
}
