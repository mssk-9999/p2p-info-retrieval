package jtella.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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

	private static List<Socket> searchNodes;
	private static List<Socket> retrievalNodes;

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
		
		searchNodes = new ArrayList<Socket>();
		retrievalNodes = new ArrayList<Socket>();

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
			for(Socket s : retrievalNodes) {
				try {
					PrintWriter out = new PrintWriter(s.getOutputStream());
					out.println(jsonNotifier.toJSONString());
				} catch (IOException e) {
					logger.error("Couldn't retrieve output stream for node: " + s.getInetAddress().getHostAddress());
				}
			}
		}
		
		if(template.matches(searchReplyTemplate)) {
			logger.info("Notifying searchers");
			JSONObject jsonNotifier = new JSONObject();
			jsonNotifier.put("notify", "search reply");
			// Notify searchers
			for(Socket s : searchNodes) {
				try {
					PrintWriter out = new PrintWriter(s.getOutputStream());
					out.println(jsonNotifier.toJSONString());
				} catch (IOException e) {
					logger.error("Couldn't retrieve output stream for node: " + s.getInetAddress().getHostAddress());
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

	public static void registerNode(ITuple t, Socket s) {
		if(t.matches(directSearchTemplate)) {
			searchNodes.add(s);
		} else {
			retrievalNodes.add(s);
		}
		
	}

}
