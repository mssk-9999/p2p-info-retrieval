README

JTella

-------------------------------------------------------------------------------

INTRODUCTION

The JTella API project is an effort to create a simple, easy to use Java 
programming interface for the GNUTella network. The goal is to make it easy
to produce Java applications and tools for accessing the GNUTella network.

Comments, bug reports, etc. to http://jtella.sf.net.

-------------------------------------------------------------------------------

RELEASE INFORMATION
-------------------------------------------------------------------------------
v0.9

- Some additional classes to be able to acquire hosts from GWebCaches 
  (v0.8 by Dan Meyers, 2004)
- Change of logging system to Apache Log4j.
- Changes in the accepted message [payload] sizes : now PING / PONG messages may 
  come with GGEP extension blocks; QUERY messages are accepted up to 4Kb. 
  (previously the servent would have dropped the connection)
- A working example is included : MyJTellaNode is a simple GUI with which one may 
  connect to a Gnutella network (using a hard-coded IP/port in class JTellaAdapter),
  and see incoming Query messages, plus manually inject queries and receive responses.
  
  A separate binary release is available (executable jar file).
   
v0.7

- A rudimentary flow control system added to connections. Each connection has
  and outbound message backlog. Messages are dropped at certain backlog levels
  based on the importance of the message. This helps maintain connections with
  servants on slower connections.
- SearchSession updated to support sending PUSH messages 
- Updated Connection to accepted GNUTella v0.6 handshake on incoming connections
- New method added to SearchReplyMessage to return replying servant's vendor
  code (Bearshare extension)
- SearchReplyMessage constructor added supporting the vendor code
- SearchReplyMessage now correctly parses the GNOTella extension for mp3 data,
  although the data is not ignored.
- A method added to Message to return its originating connection
- Accessor method added to the SearchReplyMessage to return the client
  identifier. 
- Router updated to verify return connection exists before routing Ping and
  Query messages
- IncomingConnectionManager automatically tries alternate ports when a
  BindException occurs
- InformationConnection added. When no input slots are available and a servant,
  attempts connection this connection briefly connects and sends a series of 
  pong messages.
- Accessor methods added to PONG message  
- HostFeed fixed to not spin loop when no HostCaches are set (reported by Dan 
  Ruderman)
- Fixed a byte-ordering bug in PongMessage (reported by Dan Ruderman)
- Fixed bug in which response PONG guid did not match request PING guid
- Fixed some Router issues with properly updating TTL/Hops on routed messages
- Changed Connection to implementing Runnable rather than extending Thread to 
  avoid memory leak with unstarted Threads. (Reported by Jean Vaucher) 
- Changed Push Routing to use Client Identifier rather than the descriptor GUID. 
  This conforms to the latest specification. (Reported by Nathan Baugman)
- IncomingConnectionManager bug fixed that prevented complete disconnection  
-------------------------------------------------------------------------------
v0.6

- A new KeepAlive Thread sends a TTL=1 Ping at regular intervals. This makes
  the connections more long-lived.
- TTL=1 Ping also sent on an interrupted exception in message processing. The
  effect is to recover some connections about to time-out.
- Redesigned SearchSession to send search queries to newly connected 
  NodeConnections (reported by Pavan Lulla and Lee). This approach does a better 
  job of obtaining search results.
- Added a method to GNUTellaConnection to return the servant identifier
- Added the connection handshake string as a config parameter in ConnectionData.
  This enables private file sharing networks.
- Updated OutgoingConnectionManager to reuse connection threads
- Created a new utility class SocketFactory to force connection attempts to
  to use a timeout of ten seconds
- Fixed a deadlock bug in routetable that occurred after a large number of 
  processed messages
- Fixed a bug in Message that caused errors with messages larger that 256 bytes
  (reported by Jamie Doornbas)
- Fixed a byte ordering bug in PushMessage.getFileIndex()

-------------------------------------------------------------------------------
v0.5

- Updated debug/tracing code, it now defaults to inactive. Set the system
  property JTella.Debug to some value to activate and produce jtella.log file
- A new example, Node.java, can be used to test JTella with other GNUTella hosts
  in a multi-servant test
- Support for utilizing multiple host cache servents added
- New GNUTella constructors added
- Added a method, GNUTellaConnection.addConnection() for directly adding a host
- SearchReplyMessage.getDownloadSpeed() added
- New base Connection class and a subclass for communicating with HostCache servants
- Added an interface, DownloadConstants, with constants for connection bandwidth
- Modified NodeConnection to immediately Ping a new connection, improves connection
  establishment
- Connection.shutdown is now public to enable an application to close connections
- Corrected Pong routing to store the servant even if Pong message is ultimately 
  not forwarded
- Updated Connection to guard against socket write blocking
- Fixed multiple byte ordering bugs in SearchReplyMessage
- Fixed a bug in SearchMonitorSession which prevented closing and creating a new
  working session
- Fixed a bug in GNUTellaConnection.broadcast in which incoming connections were 
  ignored (reported by Ramdass)
- Fixed a null pointer bug in SearchReplyMessage which prevented routing

-------------------------------------------------------------------------------
v0.4

- Connection management redesigned 
- Application access to the host cache added 
- Application access to connection information added 
- Renamed NetworkConnection class to GNUTellaConnection 
- Renamed BaseConnection class to Node Connection 
- close() method added to all sessions 
- Protection against runaway memory use in message router 
- Fixed a bug in SearchReplyMessage.getPort() (reported by Paul Wojcik) 
- License changed from GPL to LGPL 

-------------------------------------------------------------------------------
v0.3

Change log:
Added FileServerSession to support Query Hit and Push messages
New example, "FileServerExample" to illustrate Query response and Push
Minor tweak to the connection management
Fixed an out of synch on tcp stream problem with Bearshare (reported by Paul Wojcik)

-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
v0.2

The changes for v0.2 are intended to improve the stability of the system. JTella
now does validation on incoming messages to verify a valid payload size. Some
malformed messages on the GNUTella network have extremely large payload sizes 
specified, this was causing JTella applications to suffer an OutOfMemory error 
when an attempt was made to allocate memory for these large payloads.

JTella will react to an invalid payload by disconnecting from the servant passing
in the malformed messages. In addition, JTella now harvests servent information
from the Pong messages routed, this increases the likelyhood that usable servants
will be in the host cache when needed.

-------------------------------------------------------------------------------
v0.1

The introductory release, the code at this point is mostly proof of concept. 
Searching and monitoring are supported, with most functionality fairly 
rudimentary. A log file "jtella.log" is produced while JTella is in use.

The API is packaged in jtella-0.1\lib\jtella-01.jar, and two examples are provided
in the examples directory.

JTella requires Java 1.2 and to this point has been run on Windows platforms.
I would be interested in reports of its use on other platforms.
