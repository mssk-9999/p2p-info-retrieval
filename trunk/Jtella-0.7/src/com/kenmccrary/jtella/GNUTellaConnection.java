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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Collections;

import com.kenmccrary.jtella.util.Log;
import com.kenmccrary.jtella.util.LoggingThreadGroup;

/**
 *  The GNUTellaConnection represents a connection to the GNUTella 
 *  <STRONG>network</STRONG>. The connection consists of one or more
 *  socket connections to servant nodes on the network.<p>
 *
 *  The following is an example initiating a GNUTellaConnection.
 *
 * <PRE>
 *     GNUTellaConnection c = new GNUTellaConnection("gnutellahosts.com", 6346);
 *     c.start();
 * </PRE>
 */
public class GNUTellaConnection
{
  private static String greeting = "GNUTELLA CONNECT/0.4\n\n";
  private static String SERVER_READY = "GNUTELLA OK";

  private boolean shutdownFlag;
  private static ConnectionData connectionData;
  private HostCache hostCache;
  private ConnectionList connectionList;
  private Router router;
  private IncomingConnectionManager incomingConnectionManager;
  private OutgoingConnectionManager outgoingConnectionManager;
  private HostFeed hostFeed;
  private SearchMonitorSession searchMonitorSession;
  private KeepAliveThread keepAliveThread;

  /**
   *  Constructs an empty connection, the application must add a host cache or
   *  servant to generate activity
   */
  public GNUTellaConnection() throws UnknownHostException,
                                     IOException 
  {
     this(null);
  } 

  /**
   *  Construct the connection using default connection data and the supplied
   *  information regarding the host cache on the network
   *
   *  @param host can be a machine name or IP address
   *  @param port port to use
   *
   **/
  public GNUTellaConnection(String host,
                            int port) throws UnknownHostException,
                                            IOException 
  {
     this(null, host, port);
  } 
  
  /**
   *  Construct the connection specifying connection data. The connection will
   *  not have access to a host cache unless specified later.
   *
   *  @param connData connection data
   **/
  public GNUTellaConnection(ConnectionData connData) throws UnknownHostException,
                                                            IOException 
  {
    Log.getLog().logInformation("Network connection initializing");
    Log.getLog().logInformation(System.getProperties().toString());

    if ( null != connData )
    {    
      connectionData = connData;
    }    
    else        
    {    
      connectionData = new ConnectionData();  
    }  
        
    // the cache of known gnutella hosts
    hostCache = new HostCache();
    
    // the connection list contains the connections to GNUTella
    connectionList = new ConnectionList();
    
    // the router routes messages received on the connections
    router = new Router(connectionList, hostCache);
    
    // the host feed is a connection to a GNUTella cache
    hostFeed = new HostFeed(hostCache, router, connectionData);
    
    // Maintains appropriate incoming connections
    incomingConnectionManager = new IncomingConnectionManager(router,
                                                              connectionList, 
                                                              connectionData,
                                                              hostCache);
    
    outgoingConnectionManager = new OutgoingConnectionManager(router,
                                                              hostCache,
                                                              connectionList,
                                                              connectionData);
                                                              
    keepAliveThread = new KeepAliveThread(connectionList);
  } 

     
  /**
   *  Construct the connection, providing <code>ConnectionData</code>
   *  to initialize the connection and the address of a host cache servant
   *
   *  @oaran connectionData configuration data for the connection
   *  @param host can be a machine name or IP address
   *  @param port port to use
   */
  public GNUTellaConnection(ConnectionData connData, 
                            String host, 
                            int port) throws UnknownHostException,
                                             IOException
  {
    this(connData);
    hostFeed.addHost(host, port);
  }
  
  /** 
   *  Starts the connection
   *
   *
   */
  public void start()
  {
    try
    {
      // run the components
      router.start();
      hostFeed.start();
      incomingConnectionManager.start();
      outgoingConnectionManager.start();
      keepAliveThread.start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
	
	/**
	 *  Stop the connection, after execution the <code>GNUTellaConnection</code>
	 *  is unusable. A new connection must be created if needed. If a 
	 *  temporary disconnect from NodeConnections is desired, the connection count
	 *  requests can be set to 0
	 *
	 */
	public void stop()
	{
    keepAliveThread.shutdown();
		incomingConnectionManager.shutdown();
		outgoingConnectionManager.shutdown();
		connectionList.reduceActiveIncomingConnections(0);
		connectionList.reduceActiveOutgoingConnections(0);
		connectionList.stopOutgoingConnectionAttempts();
		hostFeed.shutdown();
		router.shutdown();
	}
  
  /**
   *  Get the current <code>HostCache</code>. Using the <code>HostCache</code>
   *  an application can query the known hosts, and add and remove hosts
   *
   *  @return host cache
   */
  public HostCache getHostCache()
  {
    return hostCache;  
  }    

  /**
   *  Query if we are online with the network, with at least one active
   *  node connection
   *
   *  @return true if online, false otherwise
   */
  public boolean isOnline()
  {
    if ( null == connectionList )
    {    
      return false;
    }  
      
    return !connectionList.getActiveIncomingConnections().isEmpty() ||
           !connectionList.getActiveOutgoingConnections().isEmpty();
  }
  
  /**
   *  Get the <code>ConnectionData</code> settings
   *
   *  @return connection data
   */
  public ConnectionData getConnectionData()
  {
    return connectionData;  
  }

  /** 
   *  Creates a session to conduct network searches
   *
   *  @param query search query
   *  @param maxResults maximum result set size
   *  @param minSpeed minimum speed for responding servants
   *  @param receiver receiver for search responses
   *  @return session
   */
  public SearchSession createSearchSession(String query, 
                                           int maxResults, 
                                           int minSpeed,
                                           MessageReceiver receiver)
  {
    return new SearchSession(query, maxResults, minSpeed, this, router, receiver);
  }

  /**
   *  Get a search monitor session to monitor query requests
   *  flowing through this network connection. 
	 *
   *  @param searchReceiver message receiver
   */
  public SearchMonitorSession getSearchMonitorSession(MessageReceiver searchReceiver)
  {
		return new SearchMonitorSession(router, searchReceiver);
  }

  /**
   *  Creates a file serving session. <code>FileServerSession</code> can respond
   *  with a query hit
   *
   *  @param receiver message receiver
   */
  public FileServerSession createFileServerSession(MessageReceiver receiver)
  {
    return new FileServerSession(router, receiver);
  }

  // todo the two methods below should possibly be merged
  // consider if the ConnectionList should be publicly available
  
  /**
   *  Gets the current list of connections to GNUTella
   *
   *  @return list of connections
   */
  public LinkedList getConnectionList()
  {
    return connectionList.getList();  
  }    
  
  
  /**
   * Get the connection list
   */
  ConnectionList getConnections()
  {
    return connectionList;
  }
  
  /**
   *  Attempts an outgoing connection on the specified host
   *
   *  @param ipAddress host IP
   *  @param port port number
   */
  public void addConnection(String ipAddress, int port)
  {
    outgoingConnectionManager.addImmediateConnection(ipAddress, port);
  }
  
  /**
   * Informs the system of a host cache servant on the network
   *
   *  @param ipAddress host IP
   *  @param port port number
   */
  public void addHostCacheServant(String ipAddress, int port)
  {
    hostFeed.addHost(ipAddress, port);
  }
  
  /**
   *  Removes host cache servant information
   *
   *  @param ipAddress host IP
   *  @param port port number
   */
  public void removeHostCacheServant(String ipAddress, int port)
  {
    hostFeed.removeHost(ipAddress, port);
  }
  
  /**
   *  Get the servant identifier the <code>GnutellaConnection</code> 
   *  is using. The servant identifier is used in connection with Push
   *  message processing
   *
   *  @return servant identifier
   */
  public short[] getServantIdentifier()
  {
    return Utilities.getClientIdentifier();
  }
  
  /**
   *  Sends a message to all connections
   *
   *  @param m message to broadcast
   *  @param receiver message receiver
   */
  void broadcast(Message m, MessageReceiver receiver)
  {
    List connections = connectionList.getActiveConnections();
    
    Log.getLog().logDebug("Broadcasting message, type: " +
                           m.getType() +
                           ", to " +
                           connections.size() +
                           " connections");
    
    ListIterator i = connections.listIterator();
        
    while ( i.hasNext() )
    {    
      NodeConnection connection = (NodeConnection)i.next();

      try
      {
        connection.sendAndReceive(m, receiver);  
      }    
      catch (IOException io)
      {
        Log.getLog().log(io);  
      }    
    }    
  }    

}
