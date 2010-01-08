package p2p.info.retrieval.jtella.node;

//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import protocol.com.kenmccrary.jtella.Connection;
import com.kenmccrary.jtella.ConnectionData;
import com.kenmccrary.jtella.GNUTellaConnection;
import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;
import com.kenmccrary.jtella.HostCache;
import com.kenmccrary.jtella.Host;
import com.kenmccrary.jtella.SearchSession;


/**
 * Implements a Network Adapter using the Gnutella 0.6 peer-to-peer protocol (as ultrapeer).
 * 
 * @author Michael Yartsev 
 * @author Alan Davoust
 * @version 1.0
 */

public class JTellaAdapter implements MessageReceiver {

    /** Local port where Gnutella listens for incoming connections. */
    //TODO:check if it's really used!
    public static final String LOCAL_PORT = "6346";

    /** Logger used by this adapter. */
    private static Logger logger = Logger.getLogger(JTellaAdapter.class);
    
    /** local ip address*/
    private static String localhost;
    
    private static GNUTellaConnection c;
    
    private static Map<String, SearchSession> openSessions;
    
    private static Map<String, SearchMessage> messageTable;
    
//    private static Map<String, MessageReceiver> searchResponseListeners;
    
    private static Set<MessageReceiver> searchListeners;
    
    /**
     * Creates a JTella Adapter
     */
    private JTellaAdapter() {
    	initialize();
//    	initializeHostCache();
    	messageTable= new HashMap<String, SearchMessage>();
    	openSessions = new HashMap<String, SearchSession>();
    	searchListeners = new HashSet<MessageReceiver>();
    	logger.info("JTella adapter initialized.");
    }
    
    /**
     * http://en.wikipedia.org/wiki/Singleton_pattern
     * 
     * Singleton pattern used with the JTellaAdapter, so that there's only one instance
     * of the adapter running at any given time.
     */
    private static class SingletonHolder {
    	private final static JTellaAdapter INSTANCE = new JTellaAdapter();
    }
    
    /**
     * Returns a singleton instance of the JTella adapter
     * @return a singleton instance of the JTella adapter
     */
    public static JTellaAdapter getInstance() {
    	return SingletonHolder.INSTANCE;
    }
    
    public void setHost(String host)
    {
    	localhost = host;
    }
    
    /**
     * Add an event listener listening for incoming QUERY messages.
     * @param listener
     */
    public void addSearchListener(MessageReceiver listener){
    
    	searchListeners.add(listener);
    }
    
    /*
    public void addSearchReplyListener(String qid, MessageReceiver listener){
    
    	searchResponseListeners.put(qid, listener);
    	
    }*/
    
    
    /**
     * Initializes the adapter
     */
    private void initialize() {
		try {
			ConnectionData connData = new ConnectionData();
			connData.setIncommingConnectionCount(10);
			connData.setOutgoingConnectionCount(10);
			connData.setUltrapeer(true);
			connData.setIncomingPort(Integer.valueOf(LOCAL_PORT).intValue());
			connData.setAgentHeader("up2p");
			
			c = new GNUTellaConnection(connData);
			c.getSearchMonitorSession(this);
			
			
			//String incomingPort = getProperty(LOCAL_PORT);
			String incomingPort = LOCAL_PORT;
			/*if(incomingPort == null) {
				LOG.error("Invalid port number property: " + incomingPort + ". Setting to default");
				incomingPort = "6346";
			}*/
			
			openSessions = new HashMap<String,SearchSession>();
			
			logger.info("Listening for incoming connections on port: " + incomingPort);
			c.getConnectionData().setIncomingPort(Integer.parseInt(incomingPort));
			logger.debug("JTellaAdapter:: init: about to start the GnutellaConnection" );
			c.start();
			logger.debug("JTellaAdapter:: init: GnutellaConnection started" );
		} 
		catch(NumberFormatException e) {
			logger.debug("NumberFormatException while initializing JTella adapter: " + e.getMessage());	
		}
    	catch (UnknownHostException e) {
			logger.error("UnknownHostException while initializing JTellaAdapter: " + e.getMessage());
		} 
		catch (IOException e) {
			logger.error("IOException while initializing JTellaAdapter: " + e.getMessage());
		}
    }
    
    /**
     * Initializes the Host Cache
     */
    private void initializeHostCache() {
    	logger.info("== Initializing Host Cache ==");
    	
    	//For this simple example the list of hosts will be hard coded right here

		Host host1= new Host("123.456.789.23",6346, 1, 1);
//		Host h2 = new Host ("141.41.29.78",16229,1,1);
//		Host h3 = new Host ("66.74.15.4",31311,1,1);
		//add new hosts if you want...
		
		
    	//Connect to these hosts (if any)
		HostCache hostCache =  c.getHostCache();
    	
		hostCache.addHost(host1);
//		hostCache.addHost(h2);
//    	
//		hostCache.addHost(h3);
    	
		
    	logger.info("== Finished initializing Host Cache ==");
    }
    

	/**
     * Performs a search on the GNutella network [responses by callback of this.receiveSearchReply]
     */
	public void searchNetwork(String query, String uniqueId, MessageReceiver receiver) {
		logger.debug("User requesting to search the network. Query:: " + query);
		
		SearchSession session = c.createSearchSession(query, 0, 10, 0, receiver);
		//store the open search session so we can close it some day!!
		openSessions.put(uniqueId, session);
		
		logger.debug("JTellaAdapter:exiting search method");        
	}

	public void shutdown() {
		//TODO - is there anything else to the shutdown sequence?
		logger.info("Stopping Gnutella connection");
		c.stop();
		for (SearchSession s: openSessions.values()){
			s.close();
		}
	}
	
	/** close an open search session
	 * 
	 */
	public void closeSession(String id)
	{
		SearchSession s = openSessions.remove(id);
		if (s != null) 
			s.close();
	}

	/**
	 * receive a search message from the network. This message will be forwarded to all listeners. 
	 * They must then use the hashcode of the message object as a way of responding to this particular message.
	 * TODO: use a different interface and provide the reply-to-id  
	 */
	public void receiveSearch(SearchMessage message) {
		logger.debug("Received a search message");

		//Parse message ?
		String rawmsg = message.getSearchCriteria().trim();

		if (!messageTable.containsKey(String.valueOf(message.hashCode()))){ //we're not searching for this particular id
			//Store a reference to the originating connection of the search message to be able to return the answer
			messageTable.put(String.valueOf(message.hashCode()),message);

			//here we notify all the listeners who have signed up to receive Gnutella QUERY messages with this adapter
			for (MessageReceiver list: searchListeners){
				list.receiveSearch(message);
			}
		}
		else {
			//otherwise we just ignore the query, we've already had it [the exact same id]
			logger.info("Ignoring search message with id "+ String.valueOf(message.hashCode()) +" already being processed" );
		}
	}



	/**
	 *  implementation of ReceiveSearchReply in the JTella interface: receives responses from the network.
	 *  @param searchReplyMessage: the JTella message received from the network
	 */
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {
		//do nothing... search replies should be received directly by the invoking object.
	}

	/**at the moment we don't handle push messages*/
	public void receivePush(PushMessage arg0) {
	}
	
    
	public void sendMessageWithID(String xmlmsg, String qid){
		logger.debug(" -Sending the search response");
		logger.debug("content="+xmlmsg);

		
		//get the message of the original query
		logger.debug("getting original msg with id:"+qid);
		SearchMessage message = messageTable.get(qid) ;


		//TODO: remove messages from the table, periodically.
		//idea: I could make a queue and drop the oldest ones when I reach a size limit
		SearchReplyMessage replyMessage;
		//sometimes the local IP address hasn't been set.


		replyMessage= new SearchReplyMessage(message, (short)0, getHost(), 0);
			/*adapter.setHost(c.getConnectionData().getGatewayIP()); doesn't work, gives MAC address
		LOG.debug("local IP:"+adapter.getHost());*/
			replyMessage.addFileRecord(new SearchReplyMessage.FileRecord(0,0,xmlmsg, "nohash"));

			try {
				// output the response on the Gnutella connection where the search came from
				message.getOriginatingConnection().send(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("JTellaAdapter: Finished sending search response");
		}

	/**
	 * get local IP address
	 * @return
	 */
	private String getHost() {
		return localhost;
	}

	public GNUTellaConnection getConnection() {
		// TODO Auto-generated method stub
		return c;
	}
		
	}
