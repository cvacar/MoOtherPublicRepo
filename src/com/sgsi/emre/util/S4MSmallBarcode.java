package com.sgsi.emre.util;

import java.util.ArrayList;

import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * Class to set up the print coordinates and ZPL for a 1 inch long by 0.5 inch high barcode in Code128
 * All of the class variables are initialized to valid values for this barcode type/printer combo
 * but can be changed if needed
 * @author bobby jo steeke for WT
 * @date = 10.10.2008
 */
public class S4MSmallBarcode extends PrintBarcode 
{
	public static String preString = "^XA";
	//^FO16,06^ACN,18,10^BY1^BCN,20,Y,N,N,N^FD"; this is the font string that works for this barcode
	public static String labelLineFeed = "^FS";
	public static String postString = "^XZ";
	
	//set the initial coordinates and fonts for the small barcode
	private int fontHeight = 10;
	private int fontWidth = 6;
	private int startPrintXCoord = 15;
	private int startPrintYCoord = 10;
	private int barcodeHeight = 30; // 2.2.2 changed from 15
	private String fontType = "P";
	
	public S4MSmallBarcode()
	{
	}
	
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
			
			/** original version
			rtn += "^FO" + getStartPrintXCoord() + "," + getStartPrintYCoord() + "^A" 
				+ getFontType() + "N," + getFontHeight() + "," + getFontWidth() + "^BY1^BCN," 
				+ getBarcodeHeight() + ",Y,N,N,N^FD";
			**/
			
			// test version 2.2.2 interpretation line
			rtn += "^FO" + getStartPrintXCoord() + "," + getStartPrintYCoord() + "^A" 
			+ 0 + "N," + 40 + "," + 20 + "^BY1^BCN," 
			+ getBarcodeHeight() + ",Y,N,N,N^FD";

			//2D barcode generator
			//rtn += "^FO" + getStartPrintXCoord() + "," + getStartPrintYCoord() + "^A" 
			//+ getFontType() + "N," + getFontHeight() + "," + getFontWidth() + 
			//"^BY1^B0R,2,N,0,N,1,0^FD";
			
			
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred when trying to build the ZPL for the font string: " 
					+ "\r\n" + ex.getMessage());
		}
		return rtn;
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
			
			/** original 2.1.17
			rtn += "^FO" + xCoord + "," + yCoord + "^A" 
				+ fontType + "N," + fontHeight + "," + fontWidth + "^FD";
			**/
			
			// subsequent lines 2.2.2
			rtn += "^FO" + xCoord + "," + yCoord + "^A" 
			+ fontType + "N," + 26 + "," + 10 + "^FD";
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred when trying to build the ZPL for the font string: " 
					+ "\r\n" + ex.getMessage());
		}
		return rtn;
	}
	/**
	 * we need to set the font for the ZPL for the rows that do not contain the barcode
	 * it should look something like this
	 * "^FO45,50^ABN,12,8^FD"
	 * @return
	 */
	public String buildFontStringForStrain(int xCoord, int yCoord, String fontType, int fontHeight, int fontWidth)
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
			
			/** original 2.1.17
			rtn += "^FO" + xCoord + "," + yCoord + "^A" 
				+ fontType + "N," + fontHeight + "," + fontWidth + "^FD";
			**/
			
			// subsequent lines 2.2.2
			rtn += "^FO" + xCoord + "," + yCoord + "^A" 
			+ fontType + "N," + 20 + "," + 10 + "^FD";
		}
		catch(Exception ex)
		{
			throw new LinxUserException("An error occurred when trying to build the ZPL for the font string: " 
					+ "\r\n" + ex.getMessage());
		}
		return rtn;
	}
	@Override
    public String getZPLforLabel(String sBarcode, ArrayList<String> alLabelRows)
    {
    	String sRtn = "";
    	try
    	{
    		//lets set the fonts
    		sRtn += S4MSmallBarcode.preString + this.buildBarcodeFontString() + sBarcode 
    			+ S4MSmallBarcode.labelLineFeed;
    		int sCurrentYCoord = getStartPrintYCoord();
    		//lets add the barcodeHeight to the currentYcoord
    		sCurrentYCoord = sCurrentYCoord + getBarcodeHeight() + 6;
    		for(String sLine : alLabelRows)
    		{
    			//now lets set up the font and the print coordinates for the next line
    			//magic number 6 is to start 6 pixels below the current location
    			sCurrentYCoord = sCurrentYCoord + getFontHeight() + 6; 
    			//we want the font size to be smaller than the barcode so font size will be 10,6
        		setFontHeight(10);
        		setFontWidth(6);
    			String fontString = buildFontString(getStartPrintXCoord(), sCurrentYCoord, getFontType(), getFontHeight(), getFontWidth());
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
	 * Used in Candidate Strain Isolation tasks to print barcodes 
	 * where human readable barcode is bold
	 */
	public String getZPLforBoldBCLabel(String sBarcode, ArrayList<String> alLabelRows)
    {
    	String sRtn = "";
    	this.setFontType("R");//bold font
    	try
    	{
    		//lets set the fonts
    		sRtn += S4MSmallBarcode.preString + this.buildBarcodeFontString() + sBarcode 
    			+ S4MSmallBarcode.labelLineFeed;
    		int sCurrentYCoord = getStartPrintYCoord();
    		//lets add the barcodeHeight to the currentYcoord
    		sCurrentYCoord = sCurrentYCoord + getBarcodeHeight() + 40;
    		for(String sLine : alLabelRows)
    		{
    			//now lets set up the font and the print coordinates for the next line
    			//magic number 15 is to start 15 pixels below the current location
    			setFontType("D");
    			sCurrentYCoord = sCurrentYCoord + getFontHeight() + 8 ; 
    			//we want the font size to be smaller than the barcode so font size will be 10,6
        		setFontHeight(20); // was 10
        		setFontWidth(6); // was 6
    			String fontString = buildFontString(getStartPrintXCoord(), sCurrentYCoord, getFontType(), getFontHeight(), getFontWidth());
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
	 * used in task Print Culture Labels.
	 * turns out the zebra printer prints a tiny "null" barcode on the label
	 * Set up for printing on the 203 dpi S4M printer
	 * @param alLabelRows
	 * @return
	 */
    public String getZPLforLabelNoBarcode(ArrayList<String> alLabelRows)
    {
    	String sRtn = "";
    	try
    	{
    		//lets set the fonts
    		sRtn += S4MSmallBarcode.preString + this.buildBarcodeFontString()
    			+ S4MSmallBarcode.labelLineFeed;
    		int sCurrentYCoord = getStartPrintYCoord();
    		//lets add the barcodeHeight to the currentYcoord
    		//sCurrentYCoord = sCurrentYCoord + getBarcodeHeight() + 6;
    		
    		for(String sLine : alLabelRows)
    		{
    			//now lets set up the font and the print coordinates for the next line
    			//magic number 6 is to start 6 pixels below the current location
    			sCurrentYCoord = sCurrentYCoord + getFontHeight() + 12; 
    			//we want the font size to be smaller than the barcode so font size will be 10,6
        		setFontType("D");
    			setFontHeight(8);
        		setFontWidth(3);
    			String fontString = buildFontString(getStartPrintXCoord(), sCurrentYCoord, getFontType(), getFontHeight(), getFontWidth());
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
	
	
    public String getZPLforLabel300DPI(String sBarcode, ArrayList<String> alLabelRows)
    {
    	String sRtn = "";
    	try
    	{
    		//lets set the fonts
    		sRtn += S4MSmallBarcode.preString + this.buildBarcodeFontString() + sBarcode 
    			+ S4MSmallBarcode.labelLineFeed;
    		int sCurrentYCoord = getStartPrintYCoord();
    		//lets add the barcodeHeight to the currentYcoord
    		sCurrentYCoord = sCurrentYCoord + getBarcodeHeight() + 8;
    		//reset the font type for the label portion (part other than barcode)
    		this.setFontType("C");
    		for(String sLine : alLabelRows)
    		{
    			//now lets set up the font and the print coordinates for the next line
    			//magic number 6 is to start 6 pixels below the current location
    			sCurrentYCoord = sCurrentYCoord + getFontHeight() + 10; 
    			//we want the font size to be smaller than the barcode so font size will be 10,6
        		setFontHeight(8);
        		setFontWidth(3);
    			String fontString = buildFontString(getStartPrintXCoord(), sCurrentYCoord, getFontType(), getFontHeight(), getFontWidth());
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
	  
	
	public static String getPreString() 
	{
		return preString;
	}
	public static void setPreString(String preString) 
	{
		S4MSmallBarcode.preString = preString;
	}
	public static String getLabelLineFeed() 
	{
		return labelLineFeed;
	}
	public static void setLabelLineFeed(String labelLineFeed) 
	{
		S4MSmallBarcode.labelLineFeed = labelLineFeed;
	}
	public static String getPostString() 
	{
		return postString;
	}
	public static void setPostString(String postString) 
	{
		S4MSmallBarcode.postString = postString;
	}
	public int getFontHeight() 
	{
		return fontHeight;
	}
	public void setFontHeight(int fontHeight) 
	{
		this.fontHeight = fontHeight;
	}
	public int getFontWidth() 
	{
		return fontWidth;
	}
	public void setFontWidth(int fontWidth) 
	{
		this.fontWidth = fontWidth;
	}
	public int getStartPrintXCoord() {
		return startPrintXCoord;
	}
	public void setStartPrintXCoord(int startPrintXCoord) 
	{
		this.startPrintXCoord = startPrintXCoord;
	}
	public int getStartPrintYCoord() 
	{
		return startPrintYCoord;
	}
	public void setStartPrintYCoord(int startPrintYCoord) 
	{
		this.startPrintYCoord = startPrintYCoord;
	}
	public int getBarcodeHeight() 
	{
		return barcodeHeight;
	}
	public void setBarcodeHeight(int barcodeHeight) 
	{
		this.barcodeHeight = barcodeHeight;
	}

	public String getFontType() {
		return fontType;
	}

	public void setFontType(String fontType) {
		this.fontType = fontType;
	}
	 
}
