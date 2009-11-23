package jtella.node;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.demo.SearchFiles;
import org.apache.lucene.document.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import com.kenmccrary.jtella.GNUTellaConnection;
import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;

/**
 * Example JTella node, to be used as a controller with the JTellaAdapter and JTellaGUI.
 * 
 * @author alan
 *
 */
public class JTellaNode implements MessageReceiver {

	private static final int defaultListenPort = 6346;
	//	private JTellaGUI gui;
	private static final Logger logger = Logger.getLogger(JTellaNode.class);
	private JTellaAdapter jta;


	/** default constructor
	 * 
	 */
	public JTellaNode() {
		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String addr = address.getHostAddress();

		jta = JTellaAdapter.getInstance();
		jta.setHost(addr);

		//sign up to receive search messages
		jta.addSearchListener(this);
	}

	/* default constructor 2
	 * 
	 */
	public JTellaNode(String IP, int port) {
		jta = JTellaAdapter.getInstance();
		jta.setHost(IP);

		//		gui = new JTellaGUI("The JTella GUI", this);


		/* Instantiate an anonymous subclass of WindowAdapter, and register it
		 * as the frame's WindowListener.
		 * windowClosing() is invoked when the frame is in the process of being
		 * closed to terminate the application.
		 */
		//		gui.addWindowListener(
		//				new WindowAdapter() {
		//					public void windowClosing(WindowEvent e) {
		//						jta.shutdown();
		//						System.exit(0);
		//					}
		//				}
		//		);

		// Size the window to fit the preferred size and layout of its
		// subcomponents, then show the window.
		//		gui.pack();
		//		gui.setVisible(true);

		//sign up to receive search messages
		jta.addSearchListener(this);


	}


	public JTellaNode(String indexPath) {
		this();
		SearchFiles.setIndex(indexPath);
	}

	/**
	 * @param args not used
	 */
	public static void main(String[] args) {
		
		

		JTellaNode node = null;
		if (args.length==0){ //nothing is provided : use default values
			System.out.println("Usage: JTellaNode [indexPath] \n Using default values.");
			node = new JTellaNode();	
		}

		else if (args.length==1){
			node = new JTellaNode(args[0]);
		}

//		else if (args.length==2){
//			node = new JTellaNode(args[0], Integer.parseInt(args[1]));
//		}
		else {
			System.out.println("Invalid number of arguments! \n Usage: JTellaNode [indexPath] \n Using default values.");
			node = new JTellaNode();	
		}

	}

	public void finalize() {
		logger.info("Node shutting down...");
		jta.shutdown();
		System.exit(0);
	}



	public void injectmessage(String msg, String id) {

		jta.searchNetwork(msg, id, this);

	}

	// TODO: implement method body
	public void injectSearchReply(String msg, String id) {

		jta.sendMessageWithID(msg, id);

	}

	public void receivePush(PushMessage pushMessage) {
		//do nothing

	}


	@SuppressWarnings("unchecked")
	public void receiveSearch(SearchMessage searchMessage) {
		String criteria = searchMessage.getSearchCriteria();
		logger.info("Search: " + criteria);
		
		List<Document> list = null;
		
		JSONObject searchObj = (JSONObject)JSONValue.parse(criteria);
		String sessionId = (String)searchObj.get("sessionId");
		String searchStr = (String)searchObj.get("query");
		try {
			list = SearchFiles.doSimpleSearch(searchStr);
		} catch (Exception e) {
			logger.error("Exception performing search", e);
		}

		JSONObject resultsObj = new JSONObject();
		resultsObj.put("sessionId", sessionId);
		
		JSONArray resultArr = new JSONArray();
		if (list != null) {
			for(Document d : list) {
				JSONObject resultObj = new JSONObject();
				resultObj.put("path", d.get("path"));
				resultObj.put("modified", d.get("modified"));
				resultArr.add(resultObj);
			}
		}
		resultsObj.put("searchResults", resultArr);
		injectSearchReply(resultsObj.toJSONString(), String.valueOf(searchMessage.hashCode()));
		
	}

	/**
	 * 
	 */
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {
		String output = "";//= searchReplyMessage..toString();
		output = "Search Response from :"+searchReplyMessage.getIPAddress()+":\n" + output ;
		for (int i =0;i<searchReplyMessage.getFileCount();i++){
			output += searchReplyMessage.getFileRecord(i).getName() + "\n";
		}

		
		p2p.info.retrieval.web.SearchFiles.receiveSearchReply(output);

		//		gui.callBack(output);
	}

	public GNUTellaConnection getConnection() {
		return jta.getConnection();

	}

}
