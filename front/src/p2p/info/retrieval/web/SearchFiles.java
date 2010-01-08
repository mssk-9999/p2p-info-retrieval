package p2p.info.retrieval.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import p2p.info.retrieval.lucene.SearchIndex;
import p2p.info.retrieval.web.model.Result;
import p2p.info.retrieval.web.model.ReverseAjaxThread;

@RemoteProxy(name="SearchFilesInterface")
public class SearchFiles {

	private static final Logger logger = Logger.getLogger(SearchFiles.class);
	private static Map<String, ScriptSession> sessions = new HashMap<String, ScriptSession>();

	@SuppressWarnings("unchecked")
	@RemoteMethod
	public void getResults(String query, String storeId, String callback) throws Exception {
		logger.info("New Query: " + query);
		
		try {
			// Get the DWR script session
			ScriptSession session = WebContextFactory.get().getScriptSession();
			// Create a unique (local) id for the search using the current time
			String sessionId = session.getId();
			
			logger.debug("Session id: " + sessionId);
			logger.debug("Store id: " + storeId.trim());
			logger.debug("Callback: " + callback.trim());
			
			if(!sessions.containsKey(sessionId))
				sessions.put(sessionId, session);
			
			session.setAttribute("callback", callback.trim());
			session.setAttribute("storeId", storeId.trim());

			JSONObject jsonQuery = new JSONObject();
			jsonQuery.put("sessionId", sessionId);
			jsonQuery.put("query", query.trim());

			// Search locally
			SearchIndex.doLocalSearch(jsonQuery.toJSONString());
			
		} catch (Exception e) {
			logger.error("Exception in getResults - ", e);
			throw new Exception("Problem getting the results: " + e.getMessage());
		}
	}

//	private List<Result> getLocalResults(String query) {
//		List<Document> docs;
//		List<Result> localResults = null;
//		try {
//			// Perform the local search
//			docs = SearchIndex.doSimpleSearch(query);
//
//			// Create result objects.
//			localResults = Result.getResults(docs);
//		} catch (Exception e) {
//			logger.error("Exception in getLocalResults", e);
//		}
//
//		return localResults;
//	}

	/**
	 * JSON string format ex.
	 * {
	 * "sessionId" : "Some ID 1234",
	 * "searchResults" : [{"path" : "somePath", "modified" : some date}, {"path" : "someOtherPath", "modified" : some other date}]
	 * }
	 * @param line
	 * @throws Exception 
	 */
	//	@RemoteMethod
	public static void receiveSearchReply(String line) throws Exception {
		logger.info("Result: " + line);

		try {
			JSONObject obj = (JSONObject)JSONValue.parse(line);

			JSONArray searchResults = (JSONArray)obj.get("searchResults");
			List<Result> newResults = Result.getResultsFromArray(searchResults);

			// Get the stored DWR session object
			String sessionId = (String) obj.get("sessionId");
			ScriptSession session = sessions.get(sessionId);

			// Make sure the callback is set to something
			if(session.getAttribute("callback") == null)
				session.setAttribute("callback", "Ext.emptyFn");
			
			session.setAttribute("results", newResults);

			// Start the reverse ajax thread
			ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
			thread.addScriptSession(session);
		} catch (Exception e) {
			logger.error("Exception in receiveSearchReply", e);
			throw new Exception("Problem receiving the search reply: " + e.getMessage());
		}

	}
}
