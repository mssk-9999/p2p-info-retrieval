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

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Socket;

import com.kenmccrary.jtella.util.Log;

/**
 *  Connection to a servant on the network
 *
 */
public class NodeConnection extends Connection
{

  private static final int SEQUENTIAL_READ_ERROR_LIMIT = 3;
  
  /**
   *  Construct the Connection using host/port information
   *
   *  @param router message router
   *  @param host can be a machine name or IP address
   *  @param port port to use
   */
  NodeConnection(Router router,
                 String host,
                 int port,
                 ConnectionData connectionData) throws UnknownHostException,
                                                       IOException
  {
    super(router, host, port, connectionData);
  }

  /**
   *  Construct the connection with an existing socket
   *
   *  @param router message router
   *  @param socket socket connection to another servant
   */
  NodeConnection(Router router,
                 Socket socket,
                 ConnectionData connectionData) throws IOException
  {
    super(router, socket, connectionData);
  }

  /**
   *  Connection operation
   */
  public void run()
  {
    status = STATUS_OK;
    int sequentialReadError = 0;
    
    try
    {
      // Give an inital ping
      send(new PingMessage());

      while( !shutdownFlag )
START:{
  
        if ( sequentialReadError >= SEQUENTIAL_READ_ERROR_LIMIT )
        {
          shutdown();
          continue;
        }
        
        // Read a message
        short[] message = new short[Message.SIZE];
        for (int i = 0; i < message.length; i++)
        {
          try
          {
            message[i] = (short)inputStream.readUnsignedByte();
          }
          catch (IOException io)
          {
            Log.getLog().logDebug("read timeout, sending ping");

            // try to recover from read timeout with a ping
            PingMessage keepAlivePing = new PingMessage();
            keepAlivePing.setTTL((byte)1);
            prioritySend(keepAlivePing);
            sequentialReadError++;
            break START;
          }
        }

        sequentialReadError = 0;
        Message readMessage = MessageFactory.createMessage(message, this);

        if ( null == readMessage )
        {
          Log.getLog().logError("MessageFactory.createMessage() returned null");
          continue;
        }

        int payloadSize = readMessage.getPayloadLength();

        if ( !readMessage.validatePayloadSize() )
        {
          handleConnectionError(null);
          Log.getLog().logInformation("Received invalid message from: " +
                                      host +
                                      ", message type: " +
                                      readMessage.getType());
          continue;
        }

        if (payloadSize > 0 )
        {
          short[] payload = new short[payloadSize];
          // Read the payload
          for (int p = 0; p < payloadSize; p++ )
          {
            payload[p] = (short)inputStream.readUnsignedByte();
          }

          readMessage.addPayload(payload);
        }

        Log.getLog().logDebug("Read message from " +
                              host +
                              " : " +
                              readMessage.toString());

				// count the i/o
				inputCount++;

        // Message is read, route it
        boolean routeOK = router.route(readMessage, this);

        if ( !routeOK )
        {
          // indicates an overrun router, too many connections
          Log.getLog().logDebug("Connection shut down, overrun router");
          shutdown();
          continue;
        }

				// always give an ack pong to avoid disconnection
        if ( readMessage instanceof PingMessage )
        {
          Log.getLog().logInformation("Responding to ping");
          PongMessage pong = new PongMessage(readMessage.getGUID(),
                                             (short)connectionData.getIncomingPort(),
                                             InetAddress.getLocalHost().getHostAddress() ,
                                             connectionData.getSharedFileCount(),
                                             connectionData.getSharedFileSize());
          send(pong);
        }
      }

    }
    catch (Exception e)
    {
      handleConnectionError(e);
    }
  }
}
