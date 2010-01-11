package jtella.node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jtella.node.util.JTellaTupleFactory;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

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

	private static CharsetDecoder asciiDecoder;

	private static final int BUFFER_SIZE = 1024;

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
			asciiDecoder = Charset.forName("US-ASCII").newDecoder();
			acceptConnections(SERVER_PORT);
		} catch(Exception e) {
			logger.error("Problem starting node", e);
		}
	}

	// Accept connections. Lazy Exception thrown.
	private static void acceptConnections(int port) throws Exception {

		ByteBuffer readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		Selector selector = null;
		ServerSocket serverSocket = null;

		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			serverSocket = serverSocketChannel.socket();
			InetSocketAddress inetSocketAddress = new InetSocketAddress(SERVER_PORT);
			serverSocket.bind(inetSocketAddress);

			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		} catch (IOException e) {
			logger.error("Unable to setup environment", e);
			System.exit(-1);
		}

		try {
			while (true) {

				int count = selector.select();

				// nothing to process
				if (count == 0) {
					continue;
				}

				Set<SelectionKey> keySet = selector.selectedKeys();
				Iterator<SelectionKey> itor = keySet.iterator();

				while (itor.hasNext()) {
					SelectionKey selectionKey = (SelectionKey) itor.next();
					itor.remove();

					Socket socket = null;
					SocketChannel channel = null;

					if (selectionKey.isAcceptable()) {
						logger.debug("Got acceptable key");
						try {
							socket = serverSocket.accept();
							logger.debug("Connection from: " + socket);
							channel = socket.getChannel();
						} catch (IOException e) {
							logger.error("Unable to accept channel", e);
							selectionKey.cancel();
						}

						if (channel != null) {
							try {
								logger.debug("Watch for something to read");
								channel.configureBlocking(false);
								channel.register(selector, SelectionKey.OP_READ);
							} catch (IOException e) {
								logger.error("Unable to use channel", e);
								selectionKey.cancel();
							}
						}
					}

					if (selectionKey.isReadable()) {
						readChannel(readBuffer, selectionKey);
					}
					logger.debug("Next...");
				}
			}
		} catch (IOException e) {
			logger.error("Error during select()", e);
		}
	}

	private static void readChannel(ByteBuffer readBuffer, SelectionKey selectionKey) {
		logger.debug("Reading channel");
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		readBuffer.clear();
		
		StringBuffer sb = new StringBuffer();
		int bytes = -1;
		try {
			while ((bytes = socketChannel.read(readBuffer)) > 0){
				logger.debug("Reading...");
				readBuffer.flip();
				String str = asciiDecoder.decode(readBuffer).toString( );
			    readBuffer.clear( );
			    sb.append(str);
			}
			
			ITuple t = createTuple(sb.toString());
			if(t != null) {
				logger.debug("Registering Channel");
				SearchService.registerNode(t, socketChannel);
				tupleSpace.out(t);
			}
			
		} catch (IOException e) {
			logger.error("Error writing back bytes", e);
			selectionKey.cancel();
		} catch (TupleSpaceException e) {
			logger.error("Error writting tuple", e);
		}

//		try {
//			logger.debug("Closing...");
//			socketChannel.close();
//		} catch (IOException e) {
//			logger.error("Error closing channel", e);
//			selectionKey.cancel();
//		}
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
