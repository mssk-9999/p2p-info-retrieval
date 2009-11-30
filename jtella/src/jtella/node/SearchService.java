package jtella.node;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import lights.Field;
import lights.Tuple;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import polyester.AbstractWorker;
import polyester.CleaningWorker;
import polyester.Worker;
import polyester.util.SearchString;

import com.kenmccrary.jtella.MessageReceiver;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;

public class SearchService extends AbstractWorker implements MessageReceiver{

	private static final Logger logger = Logger.getLogger(SearchService.class);

	private static List<Worker> threads = new ArrayList<Worker>();

	private static ITupleSpace tupleSpace;

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

	private SearchService() throws Exception {
		super(tupleSpace);
		
		// Incoming Gnutella searches
		gnutellaSearchTemplate = new Tuple();
		gnutellaSearchTemplate.add(new Field().setValue("Search")); //must be exact match	
		gnutellaSearchTemplate.add(new Field().setType(SearchString.class));
		
		//add template [see AbstractWorker]
		addQueryTemplate(gnutellaSearchTemplate);
		
		// Incoming front end searches
		directSearchTemplate = new Tuple();
		directSearchTemplate.add(new Field().setValue("Search")); //must be exact match
		directSearchTemplate.add(new Field().setValue("direct")); //must be exact match
		directSearchTemplate.add(new Field().setType(SearchString.class));

		//add template [see AbstractWorker]
		addQueryTemplate(directSearchTemplate);

		// Incoming search replies
		searchReplyTemplate = new Tuple();
		searchReplyTemplate.add(new Field().setValue("SearchReply")); //must be exact match	
		searchReplyTemplate.add(new Field().setType(SearchString.class));

		//add template [see AbstractWorker]
		addQueryTemplate(searchReplyTemplate);

		initJTA();
		logger.info("Node Created");
	}

	//Create a new SearchService and Run it.
	public static void main(String[] args) {
		try {
			// Set up the logging
			BasicConfigurator.configure();

			tupleSpace = new FastTupleSpace();
			SearchService searchService = new SearchService();
			threads.add(searchService);

			CleaningWorker cleaning = new CleaningWorker(tupleSpace);
			threads.add(cleaning);

			searchService.start();
			logger.info("----------SearchService started----------");
			cleaning.start();
			logger.info("----------Cleaning started----------");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void finalize() {
		jta.shutdown();
		for(Worker w: threads) {
			w.stop();
		}
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
			SearchString ss = (SearchString) (( (Field) tuple.get(PAYLOAD))).getValue();
			String jsonString = ss.getValue();
			
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

	private ITuple createGnutellaSearchTuple(String msg) {
		SearchString ss = new SearchString(msg);

		ITuple returnTuple = new Tuple();
		returnTuple.add(new Field().setValue("Search"));
		returnTuple.add(new Field().setValue(ss));

		return returnTuple;
	}
	
	private ITuple createDirectSearchTuple(String msg) {
		SearchString ss = new SearchString(msg);

		ITuple returnTuple = new Tuple();
		returnTuple.add(new Field().setValue("Search"));
		returnTuple.add(new Field().setValue("direct"));
		returnTuple.add(new Field().setValue(ss));

		return returnTuple;
	}

	@Override
	public void receiveSearch(SearchMessage searchMessage) {

		String msg = searchMessage.getSearchCriteria();

		logger.info("Search Message: " + msg);

		try {
			tupleSpace.out(createGnutellaSearchTuple(msg));
		} catch (TupleSpaceException e) {
			logger.error("Problem adding search to tuple space", e);
		}
	}

	private ITuple createSearchReplyTuple(String msg) {
		SearchString ss = new SearchString(msg);

		ITuple returnTuple = new Tuple();
		returnTuple.add(new Field().setValue("SearchReply"));
		returnTuple.add(new Field().setValue(ss));

		return returnTuple;
	}

	@Override
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {

		String msg = "";

		for(int i = 0; i < searchReplyMessage.getFileCount(); i++)
			msg.concat(searchReplyMessage.getFileRecord(i).getName());

		logger.info("Search Reply: " + msg);

		try {
			tupleSpace.out(createSearchReplyTuple(msg));
		} catch (TupleSpaceException e) {
			logger.error("Problem adding to tuple space - search reply from host: " + searchReplyMessage.getIPAddress(), e);
		}

	}

}
