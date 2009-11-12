package jtella.examples;
/*
 * JTella SearchExample
 *
 * Copyright (c) 2000 Ken McCrary, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * KEN MCCRARY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. KEN MCCRARY
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
 
import com.kenmccrary.jtella.GNUTellaConnection;
import com.kenmccrary.jtella.SearchReplyMessage;
import com.kenmccrary.jtella.MessageReceiverAdapter;
import com.kenmccrary.jtella.SearchSession;


/**
 *  An example class for showing how to execute search queries on the
 *  GNUTella network using JTella. The application expects two command
 *  line parameters, the host name, and the port the servant is listening on
 *
 */
public class SearchExample
{
  private GNUTellaConnection conn;
  
  /**
   *  Constructs the example given a started network connection
   *
   */
  public SearchExample(GNUTellaConnection networkConnection)
  {
    this.conn = networkConnection;
  }

  /**
   *  Entry point for the search example application, host and port number are
   *  expected arguments
   *
   */
  public static void main(String[] args)
  {
    System.out.println("<--- JTella Search Example running --->\n");


    if (args.length != 2)
    {
      System.out.println("Usage: SearchExample host port");
      System.exit(1);
    }

    try
    {
      System.out.println("Connecting to Gnutella Network...");
      
      //-------------------------------------------------------------
      // Start a network connection, wait, then execute searches
      // A proper application would check if Node connections
      // exist prior to searching
      //-------------------------------------------------------------
      GNUTellaConnection c = new GNUTellaConnection(args[0],
                                  Integer.decode(args[1]).intValue());
      c.start();
      
      System.out.println("Sending search requests");
      SearchSession search1 = c.createSearchSession("elvis", 
                                                     100, 
                                                     0, 
                                                     new TestReceiver());

      SearchSession search2 = c.createSearchSession("madonna", 
                                                     100, 
                                                     0, 
                                                     new TestReceiver());

      SearchSession search3 = c.createSearchSession("santana", 
                                                     100, 
                                                     0, 
                                                     new TestReceiver());
                                                     
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  /**
   *  Test class for receiving search responses
   *
   */
  static class TestReceiver extends MessageReceiverAdapter
  {

    /**
     *  For the example, just print the search response info to the console
     *
     */
    public void receiveSearchReply(SearchReplyMessage searchReply)
    {
      
      System.out.println("****Test receiver session received reply****");
      System.out.println("Port: " + searchReply.getPort());
      System.out.println("IP Address:" + searchReply.getIPAddress());
      System.out.println("Host Speed: " + searchReply.getDownloadSpeed());
      System.out.println("FileCount:" + searchReply.getFileCount());
      System.out.println("Vendor Code: " + searchReply.getVendorCode());
      System.out.println("ID: " + searchReply.getClientIdentifier().toString());
      
      for (int i = 0 ; i <  searchReply.getFileCount(); i++) 
      {
        SearchReplyMessage.FileRecord fileRecord = searchReply.getFileRecord(i);
        System.out.println("FileRecord: " +
                            i +
                            ", name: " +
                            fileRecord.getName() +
                            ", size: " +
                            fileRecord.getSize() +
                            ", index: " + 
                           fileRecord.getIndex());
      }

      System.out.println("****END Search session received reply****");
  }

  }
}
