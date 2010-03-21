package p2p.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.extend.ScriptSessionManager;
import org.directwebremoting.impl.DefaultScriptSessionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import p2p.jtella.model.IClient;
import p2p.jtella.service.SearchService;
import p2p.web.model.Result;
import p2p.web.model.ReverseAjaxThread;

@RemoteProxy(name="SearchFilesInterface")
public class SearchFiles implements IClient {

	private static final Logger logger = Logger.getLogger(SearchFiles.class);
	private static Map<String, ScriptSession> sessions = new HashMap<String, ScriptSession>();

	private static SearchService gnutella;

	public static void setGnutella(SearchService indexers) {
		SearchFiles.gnutella = indexers;
	}

	@SuppressWarnings("unchecked")
	@RemoteMethod
	public void getResults(String query, String storeId, String callback) throws Exception {
		logger.debug("New Query: " + query);

		try {

			if(SearchFiles.gnutella == null)
				throw new RuntimeException("No gnutella service was configured");

			// Get the DWR script session
			ScriptSession session = WebContextFactory.get().getScriptSession();
			
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
//			jsonQuery.put("callback", callback.trim());
//			jsonQuery.put("storeId", storeId.trim());

			logger.debug("Search Created: " + jsonQuery.toJSONString());

			SearchFiles.gnutella.searchNetwork(jsonQuery.toJSONString());

		} catch (Exception e) {
			logger.error("Exception in getResults - ", e);
			throw new Exception("Problem getting the results: " + e.getMessage());
		}
	}

	/**
	 * JSON string format ex.
	 * {
	 * "sessionId" : "Some ID 1234",
	 * "searchResults" : [{"path" : "somePath", "modified" : some date}, {"path" : "someOtherPath", "modified" : some other date}]
	 * }
	 * @param reply
	 * @throws Exception 
	 */
	//	@RemoteMethod
	public void receiveSearchReply(String reply) {
		logger.debug("Result: " + reply);

		try {
			JSONObject obj = (JSONObject)JSONValue.parse(reply);

			JSONArray searchResults = (JSONArray)obj.get("searchResults");
			String respondingIP = (String)obj.get("respondingIP");
			List<Result> newResults = Result.getResultsFromArray(searchResults, respondingIP);

			// Get the stored DWR session object
			String sessionId = (String) obj.get("sessionId");
//			String callback = (String) obj.get("callback");
//			String storeId = (String) obj.get("storeId");

			ScriptSession session = sessions.get(sessionId);
			
//			ScriptSessionManager ssm = new DefaultScriptSessionManager();
//			ScriptSession session = ssm.getScriptSession(sessionId);
//			ssm.setScriptSessionTimeout(100*60);
			
//			if(session.isInvalidated())
//				logger.debug("Script invalid "+session.getId()+" timeout is "+ssm.getScriptSessionTimeout());
//			else
//				logger.debug("Script still valid "+ssm.getScriptSessionTimeout()+" storeID: "+session.getAttribute("storeId"));

			// Make sure the callback is set to something
			if(session.getAttribute("callback") == null)
				session.setAttribute("callback", "Ext.emptyFn");
			
//			session.setAttribute("callback", callback);
//			session.setAttribute("storeId", storeId);

			session.setAttribute("results", newResults);

			// Start the reverse ajax thread
			ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
			thread.addScriptSession(session);
		} catch (Exception e) {
			logger.error("Exception in receiveSearchReply", e);
//			throw new Exception("Problem receiving the search reply: " + e.getMessage());
		}

	}
}
