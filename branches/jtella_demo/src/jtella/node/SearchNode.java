package jtella.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jtella.node.util.JTellaTupleFactory;
import lights.Field;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import polyester.CleaningWorker;
import polyester.Worker;

public class SearchNode {

	private static final Logger logger = Logger.getLogger(SearchNode.class);

	private static final int SERVER_PORT = 8085;

	private static List<Worker> threads = new ArrayList<Worker>();

	private static ITupleSpace tupleSpace;
	private static final String searchNodeName = "JTellaSearchService";
	private static JSONParser parser;

	public static void main(String[] args) {
		try {
			// Set up the logging
			BasicConfigurator.configure();
			Logger.getLogger("com.kenmccrary.jtella").setLevel(Level.WARN);

			tupleSpace = new FastTupleSpace();
			
			SearchService searchService = new SearchService(tupleSpace);
			searchService.setName(searchNodeName);
			threads.add(searchService);

			CleaningWorker cleaning = new CleaningWorker(tupleSpace);
			threads.add(cleaning);

			searchService.start();
			logger.info("----------SearchService started----------");
//			cleaning.start();
//			logger.info("----------Cleaning started----------");
			
			parser = new JSONParser();

			acceptConnections(SERVER_PORT);
		} catch(Exception e) {
			logger.error("Problem starting node", e);
		}
	}

	// Accept connections. Lazy Exception thrown.
	private static void acceptConnections(int port) throws Exception {
		// Selector for incoming requests
		Selector acceptSelector = SelectorProvider.provider().openSelector();

		// Create a new server socket and set to non blocking mode
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);

		// Bind the server socket to the local host and port

		InetAddress lh = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(lh, port);
		ssc.socket().bind(isa);

		// Register accepts on the server socket with the selector. This
		// step tells the selector that the socket wants to be put on the
		// ready list when accept operations occur, so allowing multiplexed
		// non-blocking I/O to take place.
		ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);

		// Here's where everything happens. The select method will
		// return when any operations registered above have occurred, the
		// thread has been interrupted, etc.
		while ((acceptSelector.select()) > 0) {
			// Someone is ready for I/O, get the ready keys
			Set<SelectionKey> readyKeys = acceptSelector.selectedKeys();
			Iterator<SelectionKey> i = readyKeys.iterator();

			// Walk through the ready keys collection and process requests.
			while (i.hasNext()) {
				SelectionKey sk = (SelectionKey)i.next();
				i.remove();
				
				// The key indexes into the selector so you
				// can retrieve the socket that's ready for I/O
				ServerSocketChannel nextReady = (ServerSocketChannel)sk.channel();
				
				// Accept the request
				Socket s = nextReady.accept().socket();
				logger.info("Accepted connection from host: " + s.getInetAddress().getHostAddress());
				
				// Read the request from the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String msg = getMessage(in);
				in.close();
				ITuple t = createTuple(msg);
				if(t != null) {
					SearchService.registerNode(t, s);
					tupleSpace.out(t);
				}
			}
		}
	}
	
	/**
	 * Retrieve the message from the input buffer
	 * @param in A BufferedReader object from which to read
	 * @return The message string
	 */
	private static String getMessage(BufferedReader in) {
		logger.info("Retrieving message from the input buffer");
		
		String msg = "", line;
		try {
			while((line = in.readLine()) != null)
				msg += line;
		} catch (IOException e) {
			logger.error("Problem reading input", e);
		}
		return msg;
	}
	
	/**
	 * Create either a search or reply tuple depending on the message type
	 * @param msg A JSON formatted string
	 * @return <ul>
	 * <li>Direct search formatted tuple if this is a query
	 * <li>Search reply formatted tuple if this is a search reply
	 * <li><code>null</code> otherwise
	 * </ul>
	 */
	private static ITuple createTuple(String msg) {
		logger.info("Creating Tuple from direct search: " + msg);
		
		if(msg.isEmpty()) {
			logger.warn("Direct search was empty. Returning null tuple.");
			return null;
		}
		
		ITuple t = null;
		try{
			JSONObject jsonObj = (JSONObject) parser.parse(msg);
			
			if(jsonObj.get("sessionId") == null)
				throw new RuntimeException("Unrecognized message format. Message must contain a sessionId tag.");
			
			if(jsonObj.get("query") != null) {
				//Treat as a search
				t = JTellaTupleFactory.createDirectSearchTuple(msg);
			}else if(jsonObj.get("searchResults") != null) {
				// treat as results
				t = JTellaTupleFactory.createSearchReplyTuple(msg);
			}else{
				throw new RuntimeException("Unrecognized message format. Message must contain either a query or searchResults tag.");
			}
		}catch(Exception e){
			logger.warn("Problem creating tuple - " + e.getMessage());
		}
		return t;
	}
	
	public void finalize() {
		for(Worker w: threads) {
			w.stop();
		}
	}

}
