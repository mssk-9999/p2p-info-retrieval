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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.io.IOException;
import java.util.List;

import com.kenmccrary.jtella.util.Log;


/**
 * Manager for incomming connections
 *
 *
 */
class IncomingConnectionManager extends ConnectionManager
{
   private static final int SOCKET_RETRY_COUNT = 5;
   private ServerSocket serverSocket;
   private HostCache hostCache;

   /**
    *  Constructs the incoming connection manager
    *
    */
   IncomingConnectionManager(Router router,
                             ConnectionList connectionList,
                             ConnectionData connectionData,
                             HostCache hostCache) throws IOException
   {
     super(router, connectionList, connectionData);
     this.hostCache = hostCache;
     serverSocket = createServerSocket();
     serverSocket.setSoTimeout(1000);
   }

   /**
    *  Creates a <code>ServerSocket</code> using an alternate port if needed
    *
    *  @throws IOException if socket creation fails
    *  @return socket 
    */
  ServerSocket createServerSocket() throws IOException
  {
    ServerSocket socket = null;
    int retryCount = SOCKET_RETRY_COUNT;
     
    int port = connectionData.getIncomingPort();
    while ( null == socket )
    {      
      try
      {
        socket = new ServerSocket(port);
      }
      catch (BindException be)
      {
        port++;
        retryCount--;
        
        if ( 0 == retryCount )
        {
          throw be;
        }
        // todo mechanism to explicitly communicate the error to appl
        
      }
    }      
  
    connectionData.setIncomingPort(port);   
    return socket;
  }
   
  /**
   *  Main processing loop
   *
   */
  public void run()
  {
    while ( !isShutdown() )
    {
        // clean dead connections
        int numLive = connectionList.cleanDeadConnections
                                    (NodeConnection.CONNECTION_INCOMING);

        // check for need to reduce connection count
        if( numLive > connectionData.getIncommingConnectionCount() )
        {
          connectionList.reduceActiveIncomingConnections
                         (connectionData.getIncommingConnectionCount());
        }

        try
        {
          Socket socket = serverSocket.accept();


          if (numLive < connectionData.getIncommingConnectionCount() &&
              !connectionList.contains(socket.getInetAddress().getHostAddress()))
          {
              NodeConnection connection = new NodeConnection(router,
                                                             socket,
                                                             connectionData);
            
              // accept the new connection
              connection.startIncomingConnection(true);
              connectionList.addConnection(connection);
          }
          else
          {
              // The information connection will send a number of pongs and
              // disconnect
              InformationConnection connection = new InformationConnection(router,
                                                               socket,
                                                               connectionData,
                                                               hostCache);

              connection.startIncomingConnection(true);
              // this is a transient connection, does not need to be
              // in the connection list
          }
        }
        catch (IOException io)
        {
          // ignore
        }

    }

    if ( null != serverSocket )
    {
      try
      {
        serverSocket.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

}
