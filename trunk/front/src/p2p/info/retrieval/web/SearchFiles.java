package p2p.info.retrieval.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.directwebremoting.Container;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.extend.ScriptSessionManager;
import org.directwebremoting.json.JsonUtil;
import org.directwebremoting.json.parse.JsonDecoder;
import org.directwebremoting.json.parse.JsonParser;
import org.directwebremoting.json.parse.JsonParserFactory;
import org.directwebremoting.json.types.JsonArray;
import org.directwebremoting.json.types.JsonObject;
import org.directwebremoting.json.types.JsonString;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;
import p2p.info.retrieval.web.model.ReverseAjaxThread;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {

	private static final Logger logger = Logger.getLogger(SearchFiles.class);
	private static Map<String, ScriptSession> sessions = new HashMap<String, ScriptSession>();

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) throws Exception {
		logger.info("New Query: " + query);
		
		try {
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

			JsonObject jsonQuery = new JsonObject();
			jsonQuery.put("sessionId", new JsonString(sessionId));
			jsonQuery.put("query", new JsonString(query.trim()));

			// Propagate to other nodes
			org.apache.lucene.demo.SearchFiles.propagateSearch(jsonQuery.toExternalRepresentation());

			return new JsonReaderResponse<Result>(results);
		} catch (Exception e) {
			logger.error("Exception in getResults - ", e);
			throw new Exception("Problem getting the results: " + e.getMessage());
		}
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
	 * @throws Exception 
	 */
	//	@RemoteMethod
	public static void receiveSearchReply(String line) throws Exception {
		logger.info("Result: " + line);

		try {
			JsonObject obj = new JsonString(line).getJsonObject();

			JsonArray searchResults = obj.get("searchResults").getJsonArray();
			List<Result> newResults = Result.getResultsFromArray(searchResults);

			Container container = ServerContextFactory.get().getContainer();
			ScriptSessionManager manager = container.getBean(ScriptSessionManager.class);

			String sessionId = obj.get("sessionId").getString();
			ScriptSession session = manager.getScriptSession(sessionId, null, null);

			//TODO: add data to return

			ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
			thread.addScriptSession(session);
		} catch (Exception e) {
			logger.error("Exception in receiveSearchReply", e);
			throw new Exception("Problem receiving the search reply: " + e.getMessage());
		}

	}
}
