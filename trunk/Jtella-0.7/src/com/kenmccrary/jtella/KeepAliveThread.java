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

import java.util.List;
import java.util.ListIterator;
import java.net.Socket;
import java.io.IOException;

import com.kenmccrary.jtella.util.Log;

/**
 *  Maintains activity on live connections
 *
 */
class KeepAliveThread extends Thread
{
  private static final int SLEEP_TIME = 5000;
  private boolean shutdownFlag = false;
  private ConnectionList connectionList;

  /**
   *  Constructor
   *
   *  @param connectionList connection list
   */
  KeepAliveThread(ConnectionList connectionList)
  {
    super("KeepAliveThread");
    this.connectionList = connectionList;
  }
  
  /**
   *  Stops the thread
   *
   */
  void shutdown()
  {
    shutdownFlag = true;
    interrupt();
  }
  
  public void run()
  {
    while ( !shutdownFlag )
    {
      try
      {
        List connections = connectionList.getActiveConnections();
        
        ListIterator i = connections.listIterator();
        
        while ( i.hasNext() )
        {    
          NodeConnection connection = (NodeConnection)i.next();

          try
          {
            if ( NodeConnection.STATUS_OK == connection.getStatus() )
            {
              long elapsedTime = System.currentTimeMillis() -
                                 connection.getSendTime();  
                                 
              if ( elapsedTime >= SLEEP_TIME )
              {
                Log.getLog().logDebug("Sending keep alive ping to: " +
                                      connection.getHost() );
                                      
                PingMessage keepAlivePing = new PingMessage();
                keepAlivePing.setTTL((byte)1);
                connection.prioritySend(keepAlivePing);                                      
              }                                 
            }
          }    
          catch (IOException io)
          {
            Log.getLog().log(io);  
          }    
        }    
        
        sleep(SLEEP_TIME);
      }
      catch (Exception e)
      {
        Log.getLog().log(e);
      }
    }
  }
}
