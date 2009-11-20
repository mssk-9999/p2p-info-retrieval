package jtella.node;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.demo.SearchFiles;
import org.apache.lucene.document.Document;


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
	private static final String LOGFILE = "mynode.log4j.properties";
	//	private JTellaGUI gui;

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


	/**
	 * @param args not used
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(LOGFILE);

		JTellaNode node = null;
		if (args.length==0){ //nothing is provided : use default values
			System.out.println("Usage: JTellaNode [IPAddress port] \n Using default values.");
			node = new JTellaNode();	
		}

		else if (args.length==2){
			node = new JTellaNode(args[0], Integer.parseInt(args[1]));
		}
		else {
			System.out.println("Invalid number of arguments! \n Usage: JTellaNode [IPAddress port] \n Using default values.");
			node = new JTellaNode();	
		}

	}

		public void finalize() {
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


	public void receiveSearch(SearchMessage searchMessage) {
		String criteria = searchMessage.getSearchCriteria();
		//		gui.incomingMsg("search:"+criteria);
		List<Document> list = null;
		try {
			list = SearchFiles.doSimpleSearch(criteria);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String msg = "{";
		if (list != null) {
			for(Document d : list) {
				msg += "{";
				msg += "title : ";
				msg += d.get("title");
				msg += ", ";
				msg += "path : ";
				msg += d.get("path");
				msg += "};";
			}
		}
		msg += "}";
		injectSearchReply(msg, String.valueOf(searchMessage.hashCode()));
		try {
			System.out.println("Search: " + criteria);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
