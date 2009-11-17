package jtella.examples;
/*
 * JTella MonitorExample
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
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.MessageReceiverAdapter;
import com.kenmccrary.jtella.SearchMonitorSession;

/**
 *  An example class for showing how to monitor search queries on the
 *  GNUTella network using JTella. The application expects two command
 *  ine parameters, the host name, and the port the servant is listening on
 *
 */
public class MonitorExample
{
  private GNUTellaConnection conn;

  /**
   *  Constructs the example given a started network connection
   *
   */
  public MonitorExample(GNUTellaConnection networkConnection)
  {
    this.conn = networkConnection;
  }

  /**
   *  Main entrypoint for the example
   *
   */
  public static void main(String[] args)
  {
    System.out.println("<--- JTella MonitorExample running --->\n");

    if (args.length != 2)
    {
      System.out.println("Usage: MonitorExample host port");
      System.exit(1);
    }

    try
    {
      System.out.println("Connecting to Gnutella Network...");
      
      //-------------------------------------------------------------
      // Start a network connection and listen for succesful connection
      //-------------------------------------------------------------
      GNUTellaConnection c = new GNUTellaConnection(args[0],
                                   Integer.decode(args[1]).intValue());
      c.getSearchMonitorSession(new TestReceiver());
      c.start();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  /**
   *  Test class for monitoring query messages, prints out queries to the console
   *
   */
  static class TestReceiver extends MessageReceiverAdapter
  {
    /**
     *  Receives Search messages from the network, this example just
     *  prints the search criteria to the console
     *
     *  @param searchMessage a search message received on the network
     */
    public void receiveSearch(SearchMessage searchMessage)
    {
      System.out.println("Search Session received: " +
                         searchMessage.getSearchCriteria());  
    }
  }
}
