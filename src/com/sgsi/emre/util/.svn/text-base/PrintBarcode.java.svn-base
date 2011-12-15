package com.sgsi.emre.util;


import com.wildtype.linx.log.Code;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtDOMUtils;
//import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;



/**
 * <p>Title: PrintBarcode</p>
 * <p>Description: Utility for printing barcodes to Zebra Z4M printer,
 * via PrintServerII/ZPL interface.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Wildtype Informatics LLC</p>
 * @author Stu Shannon/ILMN (as "com.ilmn.app.opmPnlMakeOPABarcode.java") used by permission
 * @revised TJS/Wildtype for Linx core and network printing
 * @version 1.0
 */

public abstract class PrintBarcode
{

  //private static String preBarcodeString = "^XA^LH30,30^FO160,10^MD8^BCN,45,Y,N,N,N^AC,N,20,8^FD";
  //private static String postBarcodeString = "^FS^FO200,100^AAN,27,10^FDFirst line^FS^FO180,140^AAN,27,10^FDSecond line^FS" +
  //                                  "^FO197,178^AAN,25,10^FDThird line^FS^FO115,210^AAN,25,10^FDFourth line^FS";
 
  //private static String preSizeString = "^FO90,248^AAN,30,13^FD";
  //private static String postLotString = "^FS^ISR:EXERPROG.GRF,N^XZ^XA^ILR:EXERPROG.GRF,N^XZ";
  //private static String testString = "^XA^LH30,30^FO160,10^MD8^BCN,45,Y,N,N,N^AC,N,20,8^FDGX0000001-XXX^FS^FO200,100^AAN,27,10^FDStore at -20 Degrees C^FS^FO180,140^AAN,27,10^FDUse in  Pre-PCR Area Only^FS^FO197,178^AAN,25,10^FDFor Research Use Only^FS^FO115,210^AAN,25,10^FDNot for Use in Diagnostic Procedures^FS^FO90,248^AAN,30,13^FD1.2 mL Lot# 9999999-1-1^FS^ISR:EXERPROG.GRF,N^XZ^XA^ILR:EXERPROG.GRF,N^XZ";
  //private static final String HOST_IDENTIFICATION = "~HI";
  //private static final String HOST_STATUS_RETURN = "~HS";


  /**
   * Returns a handle on the print service named sPrinter.
   * @return reference to sPrinter print service
   */
  public static PrintService getPrintService(String sPrinter)
  {
    // get a handle on the barcode printer configured in config.xml
    PrintService pservices[] = null;
    PrintService pservice = null;
    DocFlavor flavor = null;

    try
    {
      // get the default printer
      flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
      pservices = PrintServiceLookup.lookupPrintServices(flavor, null);
      int numOfServices = pservices.length;
      //int iSelectedIdx = -1;
      for(int i = 0; i < numOfServices; i++)
      {
        if(pservices[i].getName().equalsIgnoreCase(sPrinter))
        {
          pservice = pservices[i];
          break;
        }
      }// next printing service on network that will handle this flavor

      if(pservice == null)
      {
        throw new LinxUserException("Could not locate a barcode printer named " + sPrinter + " on the network."
                                    + " Please alert your LIMS administrator.");
      }
    }
    catch(Exception ex)
    {
      throw new LinxUserException("While locating barcode printer " + sPrinter + " on the network: " + ex.getMessage());

    }
    // at exit, pservice is the printer service caller asked for, or error was thrown
    return pservice;

  }

  /**
   * Returns a list of print services visible to the LIMS host,
   * all those with flavor = DocFlavor.INPUT_STREAM.AUTOSENSE.
   * @return list of visible print services
   */
  public static List<PrintService> getPrintServices()
  {
    // get a handle on the barcode printer configured in config.xml
    List<PrintService> lstServices = new ArrayList<PrintService>(); // for return
    try
    {
      // get the default printer
      DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
      PrintService[] pservices = PrintServiceLookup.lookupPrintServices(flavor, null);
      int numOfServices = pservices.length;
      if(numOfServices < 1)
      {
        throw new LinxUserException("Could not locate any local or network printers visible to the LIMS host."
                                    + " Please alert your LIMS administrator.");
      }

      for(int i = 0; i < numOfServices; i++)
      {
        lstServices.add(pservices[i]);
      }// next printing service on network that will handle this flavor
      // at exit, lstServices holds a ref to each visible printer
    }
    catch(Exception ex)
    {
      throw new LinxUserException("While locating printers visible to LIMS host: " + ex.getMessage());

    }
    // at exit, pservice is the printer service caller asked for, or error was thrown
    return lstServices;

  }

  /**
   * Uses PrintServices lookup to find the configured barcode printer
   * on the network (config.xml 'barcodePrinter'), and sends the print job
   * as string returned by getZpl(). Call setBarcode() first.
   * @param printer
   * @param sBC
   * @param sType
   */
  public static void print(PrintService printer, String sBC, String label)
  {
    final String sBarcode = sBC;
    final String sLabel = label;
    final PrintService pservice = printer;

    if( WtDOMUtils.isNullOrBlank(sBarcode) )
    {
      throw new LinxUserException("Please provide a non-null barcode, then try again.");
    }
    else if( printer == null)
    {
      throw new LinxUserException("Please provide a non-null print service, then try again.");
    }

    Thread makeRunner = new Thread()
    {
      DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

      public void run()
      {
        //String sLabel = null;
        try
        {
          Code.debug("Printing barcode " + sBarcode);
          //sLabel = getZPLforBarcodeType(sBarcode,sBarcodeType);
          Code.debug("Instructions to printer: " + sLabel);

          DocPrintJob pj = pservice.createPrintJob();
          //PrinterJob printerJob = PrinterJob.getPrinterJob();
          ByteArrayInputStream ba = new ByteArrayInputStream(sLabel.getBytes());

          // Create the Document to print
          Doc doc = new SimpleDoc(ba, flavor, null);

          // Print The Document
          Code.debug("" + doc.getPrintData());
          pj.print(doc, aset);

          // Release the ByteArrayInputStream
          ba.close();

          Thread.sleep(500l);
        }
        catch(Exception ex)
        {
          throw new LinxUserException("Error while attempting to print barcode " + sLabel + ": " + ex.getMessage());
        }
      }
    }; // end thread.run()
    makeRunner.start();

  }
  
  /**
   * Uses PrintServices lookup to find the configured barcode printer
   * on the network (config.xml 'barcodePrinter'), and sends the print job
   * as string returned by getZpl(). 
   * This method prints a label without a barcode
   * @param printer
   * @param sBC
   * @param sType
   */
  public static void print(PrintService printer, String label)
  {
    final String sLabel = label;
    final PrintService pservice = printer;

    if( printer == null)
    {
      throw new LinxUserException("Please provide a non-null print service, then try again.");
    }

    Thread makeRunner = new Thread()
    {
      DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

      public void run()
      {
        
        try
        {
          Code.debug("Instructions to printer: " + sLabel);

          DocPrintJob pj = pservice.createPrintJob();
          //PrinterJob printerJob = PrinterJob.getPrinterJob();
          ByteArrayInputStream ba = new ByteArrayInputStream(sLabel.getBytes());

          // Create the Document to print
          Doc doc = new SimpleDoc(ba, flavor, null);

          // Print The Document
          Code.debug("" + doc.getPrintData());
          pj.print(doc, aset);

          // Release the ByteArrayInputStream
          ba.close();

          Thread.sleep(500l);
        }
        catch(Exception ex)
        {
          throw new LinxUserException("Error while attempting to print barcode " + sLabel + ": " + ex.getMessage());
        }
      }
    }; // end thread.run()
    makeRunner.start();

  }
  
  /**
   * returns the label to be printed in ZPL (Zebra Print Language)
   * @param sBArcode  The text of the barcode
   * @param alLabelRows  Anything other than the barcode text that needs to be on the label
   * @return
   */
  public abstract String getZPLforLabel(String sBarcode, ArrayList<String> alLabelRows);
  
  /**
   * builds the initial string that sets the fonts, print heights, etc.
   * @return
   */
  public abstract String buildBarcodeFontString();
 
  /**
   * Returns the default barcode text, encoded for the Z4M
   * barcode printer. Override to customize layout and content
   * of barcode based on barcode type.
   * @param sBarcode
   * @param sBarcodeType
   * @return ZPL-encoded barcode text, with Zebra formatting instructions
   */
//  public static String getZPLforBarcodeType(String sBarcode, String sBarcodeType)
//  {
//	  return preString + sBarcode + postString;//+ postLotString;
//  }
}