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
 *  Contains the location of a host on the network
 *
 */
public class Host
{
  private String ipAddress;
  private int port;
  private int sharedFileCount;
  private int sharedFileSize;

  /**
   *  Construct a host record
   *
   *  @param ipAddress host address
   */
  /* 
  Host(String ipAddress, int port)
  {
    this.ipAddress = ipAddress;
    this.port = port;
  }
  */
  
  /**
   *  Constructs a Host
   *
   *  @param ipAddress IP address
   *  @param port port
   *  @param sharedFileCount count of shared files
   *  @param sharedFileSize total shared file size in KB 
   */
  Host(String ipAddress, int port, int sharedFileCount, int sharedFileSize)
  {
    this.ipAddress = ipAddress;
    this.port = port;
    this.sharedFileCount = sharedFileCount;
    this.sharedFileSize = sharedFileSize;
  }

  /**
   *  Constructs a Host using a Pong
   */
  Host(PongMessage pongMessage)
  {
    this(pongMessage.getIPAddress(),
         pongMessage.getPort(),
         pongMessage.getSharedFileCount(),
         pongMessage.getSharedFileSize());
  }
  
  /**
   *  Returns the address
   *
   */
  String getIPAddress()
  {
    return ipAddress;
  }

  /**
   * Returns the port
   *
   */
  int getPort()
  {
    return port;
  }

  /**
   *  Return shared file count
   *
   *  @return file count
   */
  int getSharedFileCount()
  {
    return sharedFileCount;
  }
  
  /**
   *  Reurn the shared file size
   *
   *  @return size in KB
   */
  int getSharedFileSize()
  {
    return sharedFileSize;
  }
  
  /**
    *  Equals comparison
    *
    *
    */
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof Host) ) 
    {
      return false;
    }

    Host rhs = (Host)obj;

    return getIPAddress().equals(rhs.getIPAddress()) &&
           getPort() == rhs.getPort();
  }

  /**
   *  Use the IP address for the hashcode
   */
  public int hashCode()
  {
    return getIPAddress().hashCode();
  }
     
  /**
   *  Get text based host information
   *
   */
  public String toString()
  {
    return new String(ipAddress + ":" + port);
  }    
}

