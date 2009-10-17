package examples;
/*
 * JTella Node
 *
 * Copyright (c) 2000-2001 Ken McCrary, All Rights Reserved.
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
import com.kenmccrary.jtella.ConnectionData;

/**
 *  A class for testing multiple-machine routing. Functions as a node 
 *  in between two other GNUTElla servants. To test the Node, start it by
 *  specifying a port number and direct the GNUTella clients to connect, 
 *  communication will then pass through the JTella Node.
 *
 */
public class Node
{
  private GNUTellaConnection conn;

  /**
   *  Constructs the example given a started network connection
   *
   */
  public Node(GNUTellaConnection networkConnection)
  {
    this.conn = networkConnection;
  }

  /**
   *  Main entrypoint for the example
   *
   */
  public static void main(String[] args)
  {
    System.out.println("<--- JTella Node running --->");

    if ( args.length != 1 )
    {
      System.out.println("Usage: Node inputport");
      System.exit(1);
    }

    try
    {
      ConnectionData connectionData = new ConnectionData();
      connectionData.setIncomingPort(Integer.decode(args[0]).intValue());
      connectionData.setOutgoingConnectionCount(0);
      connectionData.setIncommingConnectionCount(2);
      GNUTellaConnection c = new GNUTellaConnection(connectionData);
      c.start();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

}
