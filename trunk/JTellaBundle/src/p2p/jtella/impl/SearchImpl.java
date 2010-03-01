package p2p.jtella.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.InvalidSyntaxException;

import p2p.jtella.Activator;
import p2p.jtella.JTellaAdapter;
import p2p.jtella.model.IClient;
import p2p.jtella.model.IIndexer;
import p2p.jtella.service.SearchService;

import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;

/**
 * 
 * @author Steve
 *
 */
public class SearchImpl implements SearchService, MessageReceiver {

	private static final String defaultHostAddress = "127.0.0.1";
	private static final Logger logger = Logger.getLogger(SearchImpl.class);
	private JTellaAdapter jta;
	private List<IIndexer> indexers;
	private List<IClient> clients;
	
	public SearchImpl() throws Exception {
		initJTA();
	}

	public SearchImpl(File hostsFile) throws Exception {
		initJTA();
		readHostsFile(hostsFile);
	}

	private void initJTA() throws Exception {
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
	}

	private void readHostsFile(File hostsFile) {
		BufferedReader in = null;		
		try {
			in = new BufferedReader(new FileReader(hostsFile));
			String str;
			while ((str = in.readLine()) != null) {
				if (str.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}")) {
					String[] hostParts = str.split(":", 2);

					String ipAddress = hostParts[0];
					int port = Integer.parseInt(hostParts[1]);

					jta.getConnection().getHostCache().addHost(ipAddress, port);
				}
			}
		} catch (IOException e) {
			logger.warn("Problem reading hosts file", e);
		} finally {
			try{ in.close(); } catch(Exception e) {};
		}
	}

	//////////////// MessageReceiver Methods ////////////////

	@Override
	public void receiveSearch(SearchMessage searchMessage) {
		try {
			indexers = Activator.getIndexers();
			if(indexers == null)
				throw new RuntimeException("No indexers are installed");
			
			String query = searchMessage.getSearchCriteria();
			String results = "";
			if(indexers != null) {
				for(IIndexer i : indexers) {
					String newResults = i.search(query);
					if(newResults != null && !newResults.trim().isEmpty())
						results = results.concat(newResults);
				}
			}
			sendSearchReply(results, searchMessage);
		} catch (Exception e) {
			logger.warn("Problem getting indexers. This node will not search. " + e.getMessage());
		}
	}

	private void sendSearchReply(String replyMessage, SearchMessage originalMessage) {
		jta.sendMessageWithID(replyMessage, String.valueOf(originalMessage.hashCode()));
	}

	@Override
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {
		String msg = "";
		int numRecords = searchReplyMessage.getFileCount();
		for(int i = 0; i < numRecords; i++)
			msg = msg.concat(searchReplyMessage.getFileRecord(i).toString());

		logger.info("Got reply: "+msg);

		boolean success = true;
		try {
			clients = Activator.getClients();
			if(clients == null)
				throw new RuntimeException("No clients are installed");
		} catch (InvalidSyntaxException e) {
			logger.warn("Problem getting clients. " + e.getMessage());
			success = false;
		}

		if(success) {
			// TODO: Split this up so we only return results to the correct client
			if(!msg.isEmpty()) {
				for(IClient c : clients) {
					c.receiveSearchReply(msg);
				}
			}
		}

	}

	@Override
	public void receivePush(PushMessage arg0) {
		// Not implemented
	}

	//////////////// SearchService Methods ////////////////

	@Override
	public void searchNetwork(String query) {
		// The ID in this call (2nd param) does not seem to have any effect
		jta.searchNetwork(query, String.valueOf(query.hashCode()), this);
		
		boolean success = true;
		try {
			indexers = Activator.getIndexers();
			if(indexers == null)
				throw new RuntimeException("No indexers are installed");
		} catch (InvalidSyntaxException e) {
			logger.warn("Problem getting indexers. This node will not search. " + e.getMessage());
			success = false;
		}

		if(success) {
			try {
				clients = Activator.getClients();
				if(clients == null)
					throw new RuntimeException("No clients are installed");
			} catch (InvalidSyntaxException e) {
				logger.warn("Problem getting clients. This node will not search. " + e.getMessage());
				success = false;
			}
		}
		
		if(success) {
			String results = "";
			for(IIndexer i : indexers) {
				String newResults = i.search(query);
				if(newResults != null && !newResults.trim().isEmpty())
					results = results.concat(newResults);
			}

			// TODO: Split this up so we only return results to the correct client
			if(!results.isEmpty()) {
				for(IClient c : clients) {
					c.receiveSearchReply(results);
				}
			}
		}
		
	}
	
	@Override
	public void destroy() {
		jta.shutdown();
	}

}
