package com.sgsi.emre.bean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;



public class AChemParser extends XLSParser 
{

	public AChemParser(File dataFile, String fileType, char delim, String startHeading, String[] headerNames)
	{
	    //call super implementation
		super(dataFile,fileType,delim,startHeading,headerNames);
	}
	
	
	/**
	   * Returns cell's value as string, calling
	   * getNumericValueAsString if necessary.
	   * @param cell
	   * @return cell's value as String
	   */
	  @Override
	  public String getValueAsString(HSSFCell cell, int cellNum)
		{
		  String value;
			try
			{
				//which cell is the Sampling timepoint in?
				int columnNum = 0;
				Vector<String> colNames  = this.getColumnNames();
				if(colNames != null)
				{
					for(int i = 0; i < colNames.size(); i++)
					{
						String col = colNames.get(i);
						if(col.equalsIgnoreCase("Sampling Timepoint"))
						{
							columnNum = i + 1;
							break;
						}
					}
					
					if(cellNum == columnNum)
					{
						//we've found the sampling timepoint
						//it's a date column so format the date correctly
						Date d = cell.getDateCellValue();
						String dv = d.toLocaleString();//Sep 7, 2010 4:43:00 PM
						SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyyy h:mm:ss a");
						Date nd = df.parse(dv);
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
						value = sdf.format(nd);
						return value;
						
					}
					else
					{
						value = cell.getRichStringCellValue().getString();
						return value;
					}
				}
				else
				{
					value = cell.getRichStringCellValue().getString();
					return value;
				}
			}
			catch (Exception e)
			{
				return getNumericValueAsString(cell);
			}
		}
}
