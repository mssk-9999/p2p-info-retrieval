package jtella.node;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import jtella.node.util.JTellaTupleFactory;
import lights.Field;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import polyester.AbstractWorker;

import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;

public class SearchService extends AbstractWorker implements MessageReceiver{

	private static final Logger logger = Logger.getLogger(SearchService.class);

	private static final String DEFAULT_HOST_ADDRESS = "127.0.0.1";

	private JTellaAdapter jta;

	private static ITuple gnutellaSearchTemplate;
	private static ITuple directSearchTemplate;
	private static ITuple searchReplyTemplate;

	private static List<SocketChannel> searchNodes;
	private static List<SocketChannel> retrievalNodes;

	private void initJTA() throws Exception {
		InetAddress host = null;
		String addr = DEFAULT_HOST_ADDRESS;
		try {
			host = InetAddress.getLocalHost();
			addr = host.getHostAddress();
		} catch (Exception e) {
			logger.error("Problem getting host", e);
			throw new Exception("Problem getting host - " + e.getMessage());
		}

		jta = JTellaAdapter.getInstance();
		jta.setHost(addr);

		//sign up to receive search messages
		jta.addSearchListener(this);
	}
	
	private void initTupleTemplates() {
		// Incoming Gnutella searches
		gnutellaSearchTemplate = JTellaTupleFactory.createSearchTemplate();
		//add template [see AbstractWorker]
		addQueryTemplate(gnutellaSearchTemplate);
		
		// Incoming front end searches
		directSearchTemplate = JTellaTupleFactory.createDirectSearchTemplate();
		//add template [see AbstractWorker]
		addQueryTemplate(directSearchTemplate);

		// Incoming search replies
		searchReplyTemplate = JTellaTupleFactory.createSearchReplyTemplate();
		//add template [see AbstractWorker]
		addQueryTemplate(searchReplyTemplate);
	}

	public SearchService(ITupleSpace tupleSpace) throws Exception {
		super(tupleSpace);
		
		initTupleTemplates();
		
		searchNodes = new ArrayList<SocketChannel>();
		retrievalNodes = new ArrayList<SocketChannel>();

		initJTA();
		logger.info("Node Created");
	}

	@Override
	public void finalize() {
		logger.info("SearchService shutting down");
		jta.shutdown();
	}

	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple tuple) {
		if(template.matches(directSearchTemplate)) {
			logger.info("Found search from front end");
			String jsonString = (String) (( (Field) tuple.get(PAYLOAD))).getValue();
			
			// Send over the network
			jta.searchNetwork(jsonString, jsonString, this);
		}
		
		if(template.matches(gnutellaSearchTemplate)) {
			logger.info("Notifying retrievers");
			JSONObject jsonNotifier = new JSONObject();
			jsonNotifier.put("notify", "incoming search");
			// Notify retrievers
			for(SocketChannel s : retrievalNodes) {
				try {
					ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
					writeBuffer.put(jsonNotifier.toJSONString().getBytes());
					writeBuffer.flip();
					while (writeBuffer.hasRemaining()) {
						s.write(writeBuffer);
					}
					writeBuffer.clear();
				} catch (IOException e) {
					logger.error("Couldn't retrieve output stream for node: " + s.socket().getInetAddress().getHostAddress());
				}
			}
		}
		
		if(template.matches(searchReplyTemplate)) {
			logger.info("Notifying searchers");
			JSONObject jsonNotifier = new JSONObject();
			jsonNotifier.put("notify", "search reply");
			// Notify searchers
//			for(SocketChannel s : searchNodes) {
			for(SocketChannel s : retrievalNodes) {
				try {
					logger.debug("Writing...");
//					Writer writer = new OutputStreamWriter(s.socket().getOutputStream(), "US-ASCII");
//					PrintWriter out = new PrintWriter(writer, true);
//					out.println(jsonNotifier.toJSONString());
					
					ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
					writeBuffer.put(jsonNotifier.toJSONString().getBytes());
					writeBuffer.flip();
					while (writeBuffer.hasRemaining()) {
						s.write(writeBuffer);
					}
					s.close();
//					long nbytes = 0;
//					long toWrite = writeBuffer.remaining();
//				 
//					// loop on the channel.write() call since it will not necessarily
//					// write all bytes in one shot
//					try {
//					    while (nbytes != toWrite) {
//					    	nbytes += s.write(writeBuffer);
//					    }
//					} catch (ClosedChannelException cce) {}
					writeBuffer.clear();
				} catch (IOException e) {
					logger.error("Couldn't retrieve output stream for node: " + s.socket().getInetAddress().getHostAddress());
				}
			}
		}

		return new ArrayList<ITuple>();
	}

	@Override
	public void receivePush(PushMessage arg0) {
		// Unsupported

	}

	@Override
	public void receiveSearch(SearchMessage searchMessage) {

		String msg = searchMessage.getSearchCriteria();

		logger.info("Search Message: " + msg);

		try {
			this.getTS().out(JTellaTupleFactory.createSearchTuple(msg));
		} catch (TupleSpaceException e) {
			logger.error("Problem adding search to tuple space", e);
		}
	}

	@Override
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {

		String msg = "";

		for(int i = 0; i < searchReplyMessage.getFileCount(); i++)
			msg += searchReplyMessage.getFileRecord(i).getName();

		logger.info("Search Reply: " + msg);

		try {
			this.getTS().out(JTellaTupleFactory.createSearchReplyTuple(msg));
		} catch (TupleSpaceException e) {
			logger.error("Problem adding to tuple space - search reply from host: " + searchReplyMessage.getIPAddress(), e);
		}

	}

	public static void registerNode(ITuple t, SocketChannel socketChannel) {
		if(t.matches(directSearchTemplate)) {
			searchNodes.add(socketChannel);
		} else {
			retrievalNodes.add(socketChannel);
		}
		
	}

}
