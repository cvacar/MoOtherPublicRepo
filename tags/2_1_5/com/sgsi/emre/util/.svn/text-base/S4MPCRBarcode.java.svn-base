package com.sgsi.emre.util;

import java.util.ArrayList;

import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Class to set up the print coordinates and ZPL for a 2 inch long by 0.25 inch high barcode in Code128
 * All of the class variables are initialized to valid values for this barcode type/printer combo
 * but can be changed if needed
 * @author bobby jo steeke for WT
 * @date = 10.17.2008
 */
public class S4MPCRBarcode extends PrintBarcode 
{

	public static String preString = "^XA";
	//^FO16,06^ACN,18,10^BY1^BCN,20,Y,N,N,N^FD"; this is the font string that works for this barcode
	public static String labelLineFeed = "^FS";
	public static String postString = "^XZ";
	
	//set the initial coordinates and fonts for the small barcode
	private int fontHeight = 12;
	private int fontWidth = 8;
	private int startPrintXCoord = 14;
	private int startPrintYCoord = 14;
	private int barcodeHeight = 18;
	private String fontType = "B";
	
	@Override
	public String buildBarcodeFontString() 
	{
		String rtn = "";
		try
		{
			//lets try to build the font string
			if(startPrintXCoord < 0 )
				throw new Exception("The x coordinate for the location to start printing must be a positive integer.");
			if(startPrintYCoord < 0)
				throw new Exception("The y coordinate for the location to start printing must be a positive integer.");
			if(WtUtils.isNullOrBlank(fontType))
				throw new Exception("The fontType cannot be null.");
			if(barcodeHeight < 0)
				throw new Exception("The barcode height must be a positive integer.");
			if(fontHeight < 0)
				throw new Exception("The font height must be a positive integer.");
			if(fontWidth < 0)
				throw new Exception("The fontWidth must be a positive integer.");
			
			rtn += "^FO" + getStartPrintXCoord() + "," + getStartPrintYCoord() + "^A" 
				+ getFontType() + "N," + getFontHeight() + "," + getFontWidth() + "^BY1^BCN," 
				+ getBarcodeHeight() + ",Y,N,N,N^FD";
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred when trying to build the ZPL for the font string: " 
					+ "\r\n" + ex.getMessage());
		}
		return rtn;
	}

	@Override
	public String getZPLforLabel(String barcode, ArrayList<String> alLabelRows) 
	{
		String sRtn = "";
    	try
    	{
    		//lets set the fonts
    		sRtn += S4MSmallBarcode.preString + this.buildBarcodeFontString() + barcode 
    			+ S4MSmallBarcode.labelLineFeed;
    		int sCurrentYCoord = getStartPrintYCoord();
    		//we want the text to be a little smaller than the barcode so it's font type is "B"
    		setFontType("B");
    		for(String sLine : alLabelRows)
    		{
    			//now lets set up the font and the print coordinates for the next line
    			//magic number 6 is to start 6 pixels below the current location
    			sCurrentYCoord = sCurrentYCoord + getBarcodeHeight() + getFontHeight() + 6; 
    			//we want the font size to be smaller than the barcode so font size will be 12,8
        		setFontHeight(12);
        		setFontWidth(8);
    			String fontString = buildFontString(40, sCurrentYCoord, getFontType(), getFontHeight(), getFontWidth());
    			sRtn += fontString + sLine + S4MSmallBarcode.labelLineFeed;
    		}
    		//and finally we need to tell the printer that this is the end of the label
    		sRtn += S4MSmallBarcode.postString;
    	}
    	catch(Exception ex)
    	{
    		throw new LinxUserException(ex.getMessage());
    	}
    	return sRtn;
	}
	
	/**
	 * we need to set the font for the ZPL for the rows that do not contain the barcode
	 * it should look something like this
	 * "^FO45,50^ABN,12,8^FD"
	 * @return
	 */
	public String buildFontString(int xCoord, int yCoord, String fontType, int fontHeight, int fontWidth)
	{
		String rtn = "";
		try
		{
			//lets try to build the font string
			if(xCoord < 0)
				throw new Exception("The x coordinate for the location to start printing must be a positive integer.");
			if(yCoord < 0)
				throw new Exception("The y coordinate for the location to start printing must be a positive integer.");
			if(WtUtils.isNullOrBlank(fontType))
				throw new Exception("The fontType cannot be null.");
			if(fontHeight < 0)
				throw new Exception("The font height must be a positive integer.");
			if(fontWidth < 0)
				throw new Exception("The fontWidth must be a positive integer.");
			
			rtn += "^FO" + xCoord + "," + yCoord + "^A" 
				+ fontType + "N," + fontHeight + "," + fontWidth + "^FD";
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred when trying to build the ZPL for the font string: " 
					+ "\r\n" + ex.getMessage());
		}
		return rtn;
	}

	public static String getPreString() {
		return preString;
	}

	public static void setPreString(String preString) {
		S4MPCRBarcode.preString = preString;
	}

	public static String getLabelLineFeed() {
		return labelLineFeed;
	}

	public static void setLabelLineFeed(String labelLineFeed) {
		S4MPCRBarcode.labelLineFeed = labelLineFeed;
	}

	public static String getPostString() {
		return postString;
	}

	public static void setPostString(String postString) {
		S4MPCRBarcode.postString = postString;
	}

	public int getFontHeight() {
		return fontHeight;
	}

	public void setFontHeight(int fontHeight) {
		this.fontHeight = fontHeight;
	}

	public int getFontWidth() {
		return fontWidth;
	}

	public void setFontWidth(int fontWidth) {
		this.fontWidth = fontWidth;
	}

	public int getStartPrintXCoord() {
		return startPrintXCoord;
	}

	public void setStartPrintXCoord(int startPrintXCoord) {
		this.startPrintXCoord = startPrintXCoord;
	}

	public int getStartPrintYCoord() {
		return startPrintYCoord;
	}

	public void setStartPrintYCoord(int startPrintYCoord) {
		this.startPrintYCoord = startPrintYCoord;
	}

	public int getBarcodeHeight() {
		return barcodeHeight;
	}

	public void setBarcodeHeight(int barcodeHeight) {
		this.barcodeHeight = barcodeHeight;
	}

	public String getFontType() {
		return fontType;
	}

	public void setFontType(String fontType) {
		this.fontType = fontType;
	}

}
