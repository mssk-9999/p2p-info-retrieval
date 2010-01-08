package p2p.info.retrieval.jtella.node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import p2p.info.retrieval.lucene.SearchIndex;
import p2p.info.retrieval.web.SearchFiles;

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

	private static final String defaultHostAddress = "localhost";
	private static final int defaultListenPort = 6346;
	private static final Logger logger = Logger.getLogger(JTellaNode.class);
	private static final String LOG_PROPERTY_FILE = "com.dan.jtella.log4j.properties";
	private static final String PROTOCOL_LOG_PROPERTY_FILE = "protocol.com.dan.jtella.log4j.properties";
	private JTellaAdapter jta;


	/** default constructor
	 * @throws Exception 
	 * 
	 */
	public JTellaNode(String hostsFile) throws Exception {
		InetAddress host = null;
		String addr = defaultHostAddress;
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
		
		try {
		    BufferedReader in = new BufferedReader(new FileReader(hostsFile));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	if (str.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}")) {
					String[] hostParts = str.split(":", 2);

					String ipAddress;
					int port;
					try {
						ipAddress = hostParts[0];
						port = Integer.parseInt(hostParts[1]);
					}
					catch (Exception e) {
						logger.error(e);
						return;
					}
					jta.getConnection().getHostCache().addHost(ipAddress, port);
				}
		    }
		    in.close();
		} catch (IOException e) {
		}

	}

	/* default constructor 2
	 * 
	 */
	public JTellaNode(String IP, int port) {
		jta = JTellaAdapter.getInstance();
		jta.setHost(IP);

		//sign up to receive search messages
		jta.addSearchListener(this);


	}

	/**
	 * @param args not used
	 */
	//	public static void main(String[] args) {
	//
	//		try {
	//
	//			BasicConfigurator.configure();
	//			Logger.getLogger("com.dan").setLevel(Level.WARN);
	//			Logger.getLogger("protocol.com.dan").setLevel(Level.WARN);
	//			Logger.getLogger("com.kenmccrary").setLevel(Level.WARN);
	//			//			Logger.getLogger("org.apache.lucene").setLevel(Level.INFO);
	//
	//			JTellaNode node = null;
	//			if (args.length==0){ //nothing is provided : use default values
	//				System.out.println("Usage: JTellaNode [indexPath] \n Using default values.");
	//				node = new JTellaNode();	
	//			}
	//
	//			else if (args.length==1){
	//				node = new JTellaNode(args[0]);
	//			}
	//
	//			//		else if (args.length==2){
	//			//			node = new JTellaNode(args[0], Integer.parseInt(args[1]));
	//			//		}
	//			else {
	//				System.out.println("Invalid number of arguments! \n Usage: JTellaNode [indexPath] \n Using default values.");
	//				node = new JTellaNode();	
	//			}
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//	}

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
		System.out.println("Search: " + criteria);

		List<Document> list = null;

		try {
			Object obj = JSONValue.parse(criteria);
			JSONObject searchObj = (JSONObject)obj;
			String sessionId = (String)searchObj.get("sessionId");
			String searchStr = (String)searchObj.get("query");

			list = SearchIndex.doSimpleSearch(searchStr);

			JSONObject resultsObj = new JSONObject();
			resultsObj.put("sessionId", sessionId);

			JSONArray resultArr = new JSONArray();
			if (list != null) {
				for(Document d : list) {
					JSONObject resultObj = new JSONObject();
					resultObj.put("path", d.get("path"));
					resultObj.put("modified", d.get("modified"));
					resultObj.put("size", d.get("size"));
					resultArr.add(resultObj);
				}
			}
			resultsObj.put("searchResults", resultArr);
			injectSearchReply(resultsObj.toJSONString(), String.valueOf(searchMessage.hashCode()));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in receiveSearch", e);
		}

	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {
		String output = "";

		try{
			output += searchReplyMessage.getFileRecord(0).getName();

			String respondingIP = searchReplyMessage.getIPAddress();

			JSONObject reply = (JSONObject)JSONValue.parse(output);
			JSONArray results = (JSONArray)reply.get("searchResults");

			JSONArray tempArray = new JSONArray();

			for(Object obj: results) {
				JSONObject result = (JSONObject)obj;
				result.put("respondingIP", respondingIP);
				tempArray.add(result);
			}

			reply.put("searchResults", tempArray);

			SearchFiles.receiveSearchReply(reply.toJSONString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public GNUTellaConnection getConnection() {
		return jta.getConnection();
	}

}
