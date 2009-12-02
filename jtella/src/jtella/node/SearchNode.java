package jtella.node;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jtella.node.util.JTellaTupleFactory;

import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import p2p.info.retrieval.lucene.thread.SearchThread;
import p2p.info.retrieval.lucene.util.QueryBuilder;
import polyester.CleaningWorker;
import polyester.Worker;

public class SearchNode {

	private static final Logger logger = Logger.getLogger(SearchNode.class);

	private static ServerSocket serverSocket;
	private static final int SERVER_PORT = 8085;

	private static List<Worker> threads = new ArrayList<Worker>();

	public static void main(String[] args) {
		try {
			// Set up the logging
			BasicConfigurator.configure();
			Logger.getLogger("com.kenmccrary.jtella").setLevel(Level.WARN);

			ITupleSpace tupleSpace = new FastTupleSpace();
			SearchService searchService = new SearchService(tupleSpace);
			threads.add(searchService);

			CleaningWorker cleaning = new CleaningWorker(tupleSpace);
			threads.add(cleaning);

			searchService.start();
			logger.info("----------SearchService started----------");
			cleaning.start();
			logger.info("----------Cleaning started----------");

			acceptConnections(SERVER_PORT);

			//			openServerSocket();
			//			
			//			while(true){
			//				Socket clientSocket = null;
			//				try {
			//					clientSocket = serverSocket.accept();
			//				} catch (IOException e) {
			//					throw new RuntimeException("Error accepting client connection", e);
			//				}
			////				new Thread(new SearchThread(clientSocket)).start();
			//				
			//				try {
			//					BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			//					
			//					String msg = input.readLine();
			//					
			//					output.close();
			//					input.close();
			//					logger.info("Request processed: " + msg);
			//
			//					tupleSpace.out(JTellaTupleFactory.createDirectSearchTuple(msg));
			//					
			//				} catch (Exception e) {
			//					logger.error("Exception receiving stream", e);
			//				}
			//				
			//			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	// Accept connections for current time. Lazy Exception thrown.
	private static void acceptConnections(int port) throws Exception {
		// Selector for incoming time requests
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
		SelectionKey acceptKey = ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);

		int keysAdded = 0;

		// Here's where everything happens. The select method will
		// return when any operations registered above have occurred, the
		// thread has been interrupted, etc.
		while ((keysAdded = acceptSelector.select()) > 0) {
			// Someone is ready for I/O, get the ready keys
			Set<SelectionKey> readyKeys = acceptSelector.selectedKeys();
			Iterator<SelectionKey> i = readyKeys.iterator();

			// Walk through the ready keys collection and process date requests.
			while (i.hasNext()) {
				SelectionKey sk = (SelectionKey)i.next();
				i.remove();
				// The key indexes into the selector so you
				// can retrieve the socket that's ready for I/O
				ServerSocketChannel nextReady = 
					(ServerSocketChannel)sk.channel();
				// Accept the date request and send back the date string
				Socket s = nextReady.accept().socket();
				// Write the current time to the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String msg = getMessage(in);
				ITuple t = createTuple(msg);
//				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//				Date now = new Date();
//				out.println(now);
//				out.close();
			}
		}
	}
	
	private static String getMessage(BufferedReader in) {
		String msg = "", line;
		try {
			while((line = in.readLine()) != null)
				msg.concat(line);
		} catch (IOException e) {
			logger.error("Problem reading input", e);
		}
		return msg;
	}
	
	private static ITuple createTuple(String msg) throws Exception {
		JSONObject jsonObj = (JSONObject) JSONValue.parse(msg);
		
		if(jsonObj.get("sessionID") == null)
			throw new RuntimeException("Unrecognized message format. Message must contain a sessionID tag");
		
		ITuple t = null;
		
		if(jsonObj.get("query") != null) {
			//Treat as a search
			t = JTellaTupleFactory.createDirectSearchTuple(msg);
		}else if(jsonObj.get("searchResults") != null) {
			// treat as results
			t = JTellaTupleFactory.createSearchReplyTuple(msg);
		}else{
			throw new RuntimeException("Unrecognized message format. Message must contain either a query or searchResults tag");
		}
		return t;
	}


	//	private static void openServerSocket() {
	//		try {
	//			serverSocket = new ServerSocket(SERVER_PORT);
	//		} catch (IOException e) {
	//			throw new RuntimeException("Cannot open port: " + SERVER_PORT, e);
	//		}
	//	}

}
