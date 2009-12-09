package jtella.node;

import java.net.InetAddress;
import java.util.List;

import lights.Field;
import lights.Tuple;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.Logger;

import polyester.AbstractWorker;

import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;

public class SearchService extends AbstractWorker implements MessageReceiver{

	private static final Logger logger = Logger.getLogger(SearchService.class);

	private static final String DEFAULT_HOST_ADDRESS = "127.0.0.0";

	private JTellaAdapter jta;

	private static ITuple gnutellaSearchTemplate;
	private static ITuple directSearchTemplate;
	private static ITuple searchReplyTemplate;


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

	public SearchService(ITupleSpace tupleSpace) throws Exception {
		super(tupleSpace);
		
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

		initJTA();
		logger.info("Node Created");
	}

	//Create a new SearchService and Run it.
//	public static void main(String[] args) {
//		try {
//			// Set up the logging
//			BasicConfigurator.configure();
//
//			tupleSpace = new FastTupleSpace();
//			SearchService searchService = new SearchService();
//			threads.add(searchService);
//
//			CleaningWorker cleaning = new CleaningWorker(tupleSpace);
//			threads.add(cleaning);
//
//			searchService.start();
//			logger.info("----------SearchService started----------");
//			cleaning.start();
//			logger.info("----------Cleaning started----------");
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}

	public void finalize() {
		jta.shutdown();
	}

	/**
	 * Match the expression in the tuple against the index in Lucene
	 * @return documents in the database that match the expression
	 */
	//	private List<Document> retrieveMatches(String query) {
	//
	//		List<Document> results = new ArrayList<Document>();
	//		try {		
	//			results = SearchFiles.doSimpleSearch(query);
	//		} catch (Exception e) {
	//			logger.error("Exception in retrieveMatches", e);
	//		}
	//		return results;
	//	}

	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple tuple) {
		if(template.matches(directSearchTemplate)) {
			logger.info("Found search from front end");
			String jsonString = (String) (( (Field) tuple.get(PAYLOAD))).getValue();
			
			jta.searchNetwork(jsonString, jsonString, this);
		}

		return null;

		//		logger.info("JsonString: " + jsonString);
		//		
		//		JSONObject searchObj = (JSONObject)JSONValue.parse(jsonString);
		//		String query = (String) searchObj.get("query");
		//		
		//		logger.info("Query: " + query);
		//		
		//		List<Document> docList = retrieveMatches(query);
		//		String jsonList = ResultsBuilder.docListToJson(docList);
		//		
		//		logger.info("jsonList: " + jsonList);
		//		
		//		searchObj.remove("query");
		//		searchObj.put("searchResults", jsonList);
		//		
		//		ITuple returnTuple = new Tuple();
		//		returnTuple.add(new Field().setValue("Result"));
		//		returnTuple.add(new Field().setValue(searchObj));
		//		
		//		List<ITuple> returnList = new ArrayList<ITuple>(1);
		//		returnList.add(returnTuple);
		//		return returnList;
	}

	@Override
	public void receivePush(PushMessage arg0) {
		// TODO Auto-generated method stub

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
			msg.concat(searchReplyMessage.getFileRecord(i).getName());

		logger.info("Search Reply: " + msg);

		try {
			this.getTS().out(JTellaTupleFactory.createSearchReplyTuple(msg));
		} catch (TupleSpaceException e) {
			logger.error("Problem adding to tuple space - search reply from host: " + searchReplyMessage.getIPAddress(), e);
		}

	}

}
