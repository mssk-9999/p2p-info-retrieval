/*
 * Copyright (C) 2000-2001  Ken McCrary
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Email: jkmccrary@yahoo.com
 */
package com.kenmccrary.jtella;

/**
 *  Provides a linkage between the Servant and JTella. ConnectionInformation is used
 *  by <code>GNUTellaConnection</code> and others in determining current parameters settable
 *  by the Servant application.
 *
 */
public class ConnectionData
{
  private int outgoingConnectionCount = 3;
  private int incommingConnectionCount = 3;
  private int incommingConnectionPort = 6346;
  private int sharedFileCount = 0;
  private int sharedFileSize = 0;
  private String connectionGreeting = "GNUTELLA CONNECT/0.4\n\n";
  private String vendorCode;


  /**
   *  Returns the requested number of outgoing connections
   *
   *  @return requested outgoing connection count
   */
  public int getOutgoingConnectionCount()
  {
    return outgoingConnectionCount;
  }

  /**
   *  Set the requested number of outgoing connections.
   *  Defaults to two connections
   *
   *  @param count count of desired output connections
   */
  public void setOutgoingConnectionCount(int count)
  {
    outgoingConnectionCount = count;
  }

  /**
   *  Get the requested number of incomming connection
   *
   *  return requested incomming connection count
   */
  public int getIncommingConnectionCount()
  {
    return incommingConnectionCount;
  }


  /**
   *  Set the requesting number of incomming connections.
   *  Defaults to four connections
   *
   *  @param count count of requested incomming connections
   */
  public void setIncommingConnectionCount(int count)
  {
    incommingConnectionCount = count;
  }

  /**
   *  Get the port to used for incoming connections
   *  Defaults to 6346.
   *  This property must be set appropriately before constructing
   *  <code>NetworkConnection</code>
   *
   *  @return port number
   */
  public int getIncomingPort()
  {
    return incommingConnectionPort;
  }

   /**
   *  Set the port to used for incomming connections.
   *  The port number defaults to 6346.
   *  This property must be set appropriately before constructions
   *  <code>NetworkConnection</code>
   *
   *  @return port number
   */
  public void setIncomingPort(int port)
  {
    incommingConnectionPort = port;
  }

  /**
   *  Get the value for shared file count
   *
   *  @return shared file count
   */
  public int getSharedFileCount()
  {
    return sharedFileCount;
  }

  /**
   *  Set the value for current number of shared files
   *  Defaults to zero
   *
   *  @param count number of shared files
   */
  public void setSharedFileCount(int count)
  {
      sharedFileCount = count;
  }

   /**
   *  Get the value for shared file size, this is the total size of shared files
   *
   *  @return shared file size in KB
   */
  public int getSharedFileSize()
  {
    return sharedFileSize;
  }

  /**
   *  Set the value for shared file size
   *  Defaults to zero
   *
   *  @param size size of shared files in KB
   */
  public void setSharedFileSize(int size)
  {
    sharedFileSize = size;
  }
  
  /**
   *  Get the connection handshake greeting
   *
   *  @return connection greeting
   */
  public String getConnectionGreeting()
  {
    return connectionGreeting;
  }
  
  /**
   *  Set the connection handshake greeting. Using alternate greetings can
   *  create private greetings. The default value is 
   *  GNUTELLA CONNECT/0.4\n\n
   *
   *  @param greeting
   */
  public void setConnectionGreeting(String greeting)
  {
    connectionGreeting = greeting;
  }
  
  /**
   *  Get Vendor code for use in QueryReply messages. Vendor code is a
   *  4 character code that must be registered with the GDF
   *
   *  @return vendor code
   */
  public String getVendorCode()
  {
    return vendorCode;
  }
  
  /**
   *  Set Vendor code for use in QueryReply messages. Vendor code is a
   *  4 character code that must be registered with the GDF
   *
   *  @param vendorCode 4 character code
   */
  public void setVendorCode(String vendorCode)
  {
    this.vendorCode = vendorCode;
  }
}



