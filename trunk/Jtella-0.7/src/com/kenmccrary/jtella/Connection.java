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

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import com.kenmccrary.jtella.util.Log;
import com.kenmccrary.jtella.util.SocketFactory;

/**
 *  Represents a connection to an application communicating with the
 *  GNUTella protocol
 *
 */
public abstract class Connection implements Runnable
{
  /**
   *  Connection is attempting to connected to GNUTella
   */
  public final static int STATUS_CONNECTING = 0;

  /**
   *  Connection is operating normally
   */
  public final static int STATUS_OK = 1;

  /**
   *  Connection is not operating normally
   */
  public final static int STATUS_FAILED = 2;

   /**
   *  Connection has been stopped
   *
   */
  public final static int STATUS_STOPPED = 3;

  /**
   *  Connection created by another servant
   *
   */
  public final static int CONNECTION_INCOMING = 0;

  /**
   *  Connection created be JTella servant
   *
   */
  public final static int CONNECTION_OUTGOING = 1;

  /**
   *  Backlog level which will drop ping messages
   *
   */
  private final static int BACKLOG_PING_LEVEL = 5;

  /**
   *  Backlog level which will drop pong messages
   *
   */
  private final static int BACKLOG_PONG_LEVEL = 10;

  /**
   *  Backlog level which will drop query messages
   *
   */
  private final static int BACKLOG_QUERY_LEVEL = 15;

  /**
   *  Backlog level which will drop query reply messages
   *
   */
  private final static int BACKLOG_QUERYREPLY_LEVEL = 20;

  /**
   *  Backlog level which will drop push messages
   *
   */
  private final static int BACKLOG_PUSH_LEVEL = 25;

  private static String SERVER_READY = "GNUTELLA OK\n\n";
  private static String SERVER_REJECT = "JTella Reject\n\n";
  private static String V06_CONNECT_STRING = "GNUTELLA CONNECT/0.6\r\n";
  private static String V06_SERVER_READY = "GNUTELLA/0.6 200 OK\r\n";
  private static String V06_SERVER_REJECT = "GNUTELLA/0.6 503\r\n";
  private static String V06_AGENT_HEADER = "User-Agent: JTella\r\n";
  private static String CRLF = "\r\n";
  private List messageBacklog;
  private Thread connectionThread;
  protected boolean shutdownFlag = false;
  protected Socket socket;
  protected DataInputStream inputStream;
  protected DataOutputStream outputStream;
  protected AsyncSender asyncSender;
  protected Router router;
  protected ConnectionData connectionData;
  protected String host;
  protected int port;
  protected int status;
  protected int type;
	protected int inputCount = 0;
	protected int outputCount = 0;
  protected int droppedCount = 0;
	protected long createTime;
  protected long sendTime;

  /**
   *  Construct the Connection using host/port information
   *
   *  @param router message router
   *  @param host can be a machine name or IP address
   *  @param port port to use
   */
  Connection(Router router,
             String host,
             int port,
             ConnectionData connectionData) throws UnknownHostException,
                                                   IOException
  {
		createTime = System.currentTimeMillis();
    this.router = router;
    this.host = host;
    this.port = port;
    this.connectionData = connectionData;
    messageBacklog = Collections.synchronizedList(new LinkedList());
    type = CONNECTION_OUTGOING;
  }

  /**
   *  Construct the connection with an existing socket
   *
   *  @param router message router
   *  @param socket socket connection to another servant
   */
  Connection(Router router,
             Socket socket,
             ConnectionData connectionData) throws IOException
  {
		createTime = System.currentTimeMillis();
    this.router = router;
    this.socket = socket;
    this.connectionData = connectionData;
    host = socket.getInetAddress().getHostName();
    port = socket.getPort();
    type = CONNECTION_INCOMING;
    messageBacklog = Collections.synchronizedList(new LinkedList());
    initSocket();
  }

  /**
   *  Constructor helper
   *
   */
  private void initSocket() throws IOException
  {
    socket.setSoTimeout(7000);
    socket.setTcpNoDelay(true);
    inputStream = new DataInputStream(socket.getInputStream());
    outputStream = new DataOutputStream(socket.getOutputStream());
  }

  /**
   *  Retrieve the host this connection links to
   *
   */
  String getHost()
  {
    return host;
  }

  /**
   *  Stops the connection and cleans up
   *
   */
  public void shutdown()
  {
    shutdownFlag = true;
    
    if ( null != connectionThread )
    {  
      connectionThread.interrupt();
    }
    
    if ( null != asyncSender )
    {
      asyncSender.shutdown();
    }

    status = STATUS_STOPPED;
    try
    {
      if ( null != outputStream )
      {
        outputStream.close();
      }
      if ( null != inputStream )
      {
        inputStream.close();
      }
      if ( null != socket )
      {
        socket.close();
      }

    }
    catch (Exception e)
    {

    }
  }

  /**
   *  Starts an incomming connection to a node on the network,
   *  does initial handshake
   *
   *  @param true to accecpt connection, false to reject
   *  @return true for a good start, false otherwise
   */
  boolean startIncomingConnection(boolean accept)
  {
    boolean result = false;
    StringBuffer inputData = new StringBuffer(64);

    int gnutellaO4Index = -1;
    int gnutella06Index = -1;
    try
    {

      if ( !accept )
      {
        // reject the connection attempt
        outputStream.write(V06_SERVER_REJECT.getBytes());
        outputStream.flush();
      }

      byte[] data = new byte[inputStream.available()];
      inputStream.readFully(data);
      String request = new String(data);
      gnutellaO4Index = request.indexOf("\n\n");
      gnutella06Index = request.indexOf("\r\n\r\n");

      if ( -1 != gnutellaO4Index )
      {
        String response = SERVER_REJECT;

        // GNUTella 0.4 handshake
        if ( request.equals(connectionData.getConnectionGreeting()) )
        {
          // the 0.4 handshake is good
          result = true;
          response = SERVER_READY;
        }

        // write the response
        outputStream.write(response.getBytes());
        outputStream.flush();

        return result;
      }
      else
      {
        // GNUTella 0.6 handshake
        if ( request.startsWith("GNUTELLA CONNECT/", 0) )
        {
          // A connect string was recognized send the response indicating
          // the version number and user agent
          String response = V06_SERVER_READY +
                            V06_AGENT_HEADER +
                            CRLF;

          outputStream.write(response.getBytes());
          outputStream.flush();

          // Read the client's response and any headers
          // Only if the client accepts our headers will the connect succeed
          BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));

          String line = bufferedReader.readLine();

          if ( -1 != line.indexOf("200 OK") )
          {
            // success
            result = true;

            // read all headers
            while ( 0 != line.length() )
            {
              // todo store these for use
              line = bufferedReader.readLine();
            }
          }
        }
      }
    }
    catch(Exception e)
    {
    }
    finally
    {
      if ( result )
      {
        // start the asynch sender
        asyncSender =  new AsyncSender();
        asyncSender.start();

        // Now start monitoring network messages
        connectionThread = new Thread(this, "ConnectionThread");
        connectionThread.start();
        Log.getLog().logDebug("Incoming connection accepted");
      }
      else
      {
        shutdown();
      }
    }

    return result;

  }

  /**
   *  Starts an outgoing connection to a node on the network,
   *  does initial handshaking
   *
   *  @return true for a good start, false for failure
   */
  boolean startOutgoingConnection()
  {
    boolean result = true;
    try
    {
      if ( null == socket )
      {
        status = STATUS_CONNECTING;
        /*
        socket = new Socket(//InetAddress.getByName(host),
                            host,
                            port);
         */
        // ten second max wait
        socket = SocketFactory.getSocket(host, port, 10000);

        initSocket();

      }
      Log.getLog().logDebug("Sending greeting to : " + host);
      byte[] greetingData = connectionData.getConnectionGreeting().getBytes();
      outputStream.write(greetingData, 0, greetingData.length);
      outputStream.flush();

      byte[] data = new byte[SERVER_READY.length()];
      inputStream.readFully(data);
      String response = new String(data);

      if ( !response.equals(SERVER_READY) )
      {
        Log.getLog().logWarning("Connection rejection: " + host);
        return false;
      }

      Log.getLog().logInformation("Connection started on: " + host);

      // start the async sender
      asyncSender =  new AsyncSender();
      asyncSender.start();

      // Now start monitoring network messages
      connectionThread = new Thread(this, "ConnectionThread");
      connectionThread.start();

    }
    catch (Exception e)
    {
      Log.getLog().log(e);
      shutdown();
      result = false;
    }
    // todo finally if start false, close connection

    return result;
  }


  // TODO return a boolean indicating if the message was enqueued

  /**
   *  Send a priority message. Priority message are not subject to flow control
   *  dropping
   *
   *  @param m message
   */
  void prioritySend(Message m) throws IOException
  {
    if ( shutdownFlag )
    {
      return;
    }

    enqueueMessage(m, true);
  }

  /**
   *  Sends a Message through this connection
   *
   *  @param m message
   */
  void send(Message m) throws IOException
  {
    if ( shutdownFlag )
    {
      return;
    }

    enqueueMessage(m, false);

  }

  /**
   *  Sends a message down the connection and sends any response
   *  to <code>MessageReceiver</code>
   *
   */
  void sendAndReceive(Message m,
                      MessageReceiver receiver) throws IOException
  {
    if ( shutdownFlag )
    {
      return;
    }

    Log.getLog().logDebug("Connection sendAndReceive: " +
                           m.getType() +
                           " to to " +
                           socket.getInetAddress().getHostAddress());

    router.routeBack(m, receiver);

    prioritySend(m);
  }

  /**
   *  Adds a message to the send queue, subject to dropping due to a message
   *  backlog
   *
   */
  void enqueueMessage(Message message, boolean priority)
  {

    if ( droppedCount > ( .5 * outputCount ) )
    {
      // drop this connection, it is hung or performing at less than half our
      // send rate
      shutdown();
      return;
    }

    int type = message.getType();
    int backlogSize = messageBacklog.size();
    
    if ( !priority )
    {
      switch (type)
      {
        case Message.PING:
        {
          if ( backlogSize > BACKLOG_PING_LEVEL )
          {
            droppedCount++;
            return;
          }

          break;
        }

        case Message.PONG:
        {
          if ( backlogSize > BACKLOG_PONG_LEVEL )
          {
            droppedCount++;
            return;
          }

          break;
        }

        case Message.QUERY:
        {
          if ( backlogSize > BACKLOG_QUERY_LEVEL )
          {
            droppedCount++;
            return;
          }

          break;
        }

        case Message.QUERYREPLY:
        {
          if ( backlogSize > BACKLOG_QUERYREPLY_LEVEL )
          {
            droppedCount++;
            return;
          }

          break;
       }

        case Message.PUSH:
        {
          if ( backlogSize > BACKLOG_PUSH_LEVEL )
          {
            droppedCount++;
            
            // give up on this connection
            shutdown();
            return;
          }

         break;
        }
      }
    }
    
    MessageData messageData = new MessageData(message, priority);

    if ( priority )
    {
      messageBacklog.add(0, messageData);
    }
    else
    {
      // add non-priority messages at the end of the list
      synchronized ( messageBacklog )
      {
        // prevent race between size/add
        int currentSize = messageBacklog.size();
        messageBacklog.add(0 == currentSize ? 0 : currentSize - 1, 
                           messageData);
      }  
    }

    synchronized ( asyncSender )
    {
      asyncSender.notify();
    }
    
  }



  /**
   *  Handles a serious error on the connection
   *
   */
  void handleConnectionError(Exception e)
  {
    Log.getLog().logInformation("Shutting down connection: " + host);
    status = STATUS_FAILED;
    if ( null != e)
    {
      Log.getLog().log(e);
    }
    shutdown();
  }

  /**
   *  Get the connected host
   *
   *  @return host name
   */
  public String getConnectedServant()
  {
    return host;
  }

  /**
   *  Get the current status of the connection
   *
   *  @returns status
   */
  public int getStatus()
  {
    return status;
  }

  /**
   *  Get the type of connection, incoming or outgoing
   *
   *  @return connection type
   */
  public int getType()
  {
    return type;
  }

	/**
	 *  Get the message output count
	 *
	 *  @return output
	 */
	public int getMessageOutput()
	{
		return outputCount;
	}

	/**
	 *  Get the message input count
	 *
	 *  @return input
	 */
	public int getMessageInput()
	{
		return inputCount;
	}

  /**
   *  Get the number of messages dropped on this connection
   *
   *  @return dropcount
   */
  public int getMessageDropCount()
  {
    return droppedCount;
  }

	/**
	 *  Get the lenght of time the connection has lived
	 *
	 *  @return time in seconds
	 */
	public int getUpTime()
	{
		long msLife = System.currentTimeMillis() - createTime;

		return (int)(msLife/1000);
	}

  /**
   *  Returns the timestamp of the last send
   *
   *  @return timestamp
   */
  public long getSendTime()
  {
    return sendTime;
  }

  /**
   *  Message data stored in the message backlog
   *
   */
  class MessageData
  {
    private Message message;
    private boolean priority;

    /**
     * Constructs message data
     *
     *  @param message GNUTella message
     *  @param priority priority messages are not subject to dropping
     */
    MessageData(Message message, boolean priority)
    {
      this.message = message;
      this.priority = priority;
    }


    /**
     *  Get the message
     *
     *  @return message
     */
    Message getMessage()
    {
      return message;
    }

    /**
     *  Check if this is a priority message. Generally, messages originated by
     *  the JTella servant are considered priority messages
     *
     *  @return priority flag
     */
    boolean isPriority()
    {
      return priority;
    }
  }


  /**
   *  Provides a mechanism to send a message and handle the problem of
   *  blocking on write, due to an unresponsive servant on the connection
   *
   */
  class AsyncSender extends Thread
  {
    private boolean shutdown = false;

    AsyncSender()
    {
      super("AsyncSender");
    }

    /**
     *   Get the message
     *
     */
    public Message getMessage()
    {
      int size = messageBacklog.size();

      while ( 0 == size && !shutdown )
      {
        try
        {
          synchronized ( this )
          {
            wait();
          }              
        }
        catch (InterruptedException ie)
        {
        }

        size = messageBacklog.size();
      }
      
      if ( shutdown )
      {
        return null;
      }

      return ((MessageData)messageBacklog.remove(0)).getMessage();
    }

    public void shutdown()
    {
      shutdown = true;
      interrupt();
    }

    public void run()
    {
      while (!shutdown)
      {
        Message message = getMessage();

        try
        {
          Connection.this.sendTime = System.currentTimeMillis();
          byte[] messageBytes = message.getByteArray();
  		    outputCount++;

          Connection.this.outputStream.write(messageBytes,
                                             0,
                                             messageBytes.length);
          Connection.this.outputStream.flush();

          // temp
          /*
          StringBuffer buffer = new StringBuffer();
          for (int i = 0; i < messageBytes.length; i++) 
          {
            buffer.append("[" +
                          Integer.toHexString(messageBytes[i]) +
                          "]");
   
          }
          Log.getLog().logDebug("Sent message: " +
                                message.getType() +
                                "\n" +
                                buffer.toString());
          */
          // end temp
          
        }
        catch (Exception e)
        {
          shutdown();
          Log.getLog().log(e);
        }

      }
    }
  }
}


