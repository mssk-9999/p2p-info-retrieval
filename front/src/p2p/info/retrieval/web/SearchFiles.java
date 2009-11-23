package p2p.info.retrieval.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.proxy.ScriptProxy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {

	private static final Logger logger = Logger.getLogger(SearchFiles.class);
	//	private List<Result> results = new ArrayList<Result>();
	private static Map<String, ScriptSession> sessions = new HashMap<String, ScriptSession>();

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) throws Exception {
		logger.info("New Query: " + query);
		
		ScriptSession session = WebContextFactory.get().getScriptSession();
		String sessionId = session.getId();
		logger.info("Session id: " + sessionId);
		
		ScriptSession existingSession = sessions.get(sessionId);

		// Keep track of whether we have to update the stored session
		boolean sessionChanged = false;
		
		// Session has yet to be stored... aka its a new search
		if(existingSession == null) {
			existingSession = session;
			sessionChanged = true;
		}
		
		List<Result> results = (List<Result>)existingSession.getAttribute("results");

		// This is the first time we've queried
		// so include the local results
		if(results == null) {
			results = getLocalResults(query);
			sessionChanged = true;
		}

		if(sessionChanged) {
			existingSession.setAttribute("results", results);
			sessions.put(sessionId, existingSession);
		}
		
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("sessionId", sessionId);
		jsonQuery.put("query", query.trim());

		// Propagate to other nodes
		org.apache.lucene.demo.SearchFiles.propagateSearch(jsonQuery.toJSONString());

		return new JsonReaderResponse<Result>(results);
	}

	private List<Result> getLocalResults(String query) {
		List<Document> docs;
		List<Result> localResults = null;
		try {
			// Perform the local search
			docs = org.apache.lucene.demo.SearchFiles.doSimpleSearch(query);

			// Create result objects.
			localResults = Result.getResults(docs);
		} catch (Exception e) {
			logger.error("Exception in getLocalResults", e);
		}

		return localResults;
	}

	/**
	 * JSON string format ex.
	 * {
	 * "sessionId" : "Some ID 1234",
	 * "searchResults" : [{"path" : "somePath", "modified" : some date}, {"path" : "someOtherPath", "modified" : some other date}]
	 * }
	 * @param line
	 */
	public static void receiveSearchReply(String line) {
		logger.info("Result: " + line);
		
		JSONObject obj = (JSONObject)JSONValue.parse(line);
		String sessionId = (String)obj.get("sessionId");
		JSONArray searchResults = (JSONArray)obj.get("searchResults");

		List<Result> newResults = Result.getResultsFromArray(searchResults);
		ScriptSession session = sessions.get(sessionId);
		
//				results.addAll(newResults);


//				ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
//				thread.addScriptSession(WebContextFactory.get().getScriptSession());

	}

	//	@RemoteMethod
	//	public void populateScriptSession() {
	//		String tabId = (String) WebContextFactory.get().getSession().getAttribute("tabId"); // this may come from the HttpSession for example
	//		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
	//		scriptSession.setAttribute("tabId", tabId);
	//	}
}
