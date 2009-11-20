package p2p.info.retrieval.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {
	
	private static final Logger logger = Logger.getLogger(SearchFiles.class);
	private static List<Result> results = new ArrayList<Result>();

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query, boolean returnLocalResults) throws Exception {
		logger.info("New Query: " + query);

		// Find the local results
		if(returnLocalResults)
			addLocalResults(query);
		
		// Propagate
		org.apache.lucene.demo.SearchFiles.propagateSearch(query);
		
		return new JsonReaderResponse<Result>(results);
	}
	
	private void addLocalResults(String query) {
		List<Document> docs;
		
		try {
			// Perform the local search
			docs = org.apache.lucene.demo.SearchFiles.doSimpleSearch(query);
			
			// Create result objects.
			List<Result> localResults = Result.getResults(docs);
			results.addAll(localResults);
		} catch (Exception e) {
			logger.error("Problem in addLocalResults - " + e.getMessage());
		}
	}

	public static void receiveSearchReply(String line) {
		logger.info("Result: " + line);
		
		List<Result> newResults = Result.getResults(line);
		results.addAll(newResults);

//		ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
//		thread.addScriptSession(WebContextFactory.get().getScriptSession());

	}

//	@RemoteMethod
//	public void populateScriptSession() {
//		String tabId = (String) WebContextFactory.get().getSession().getAttribute("tabId"); // this may come from the HttpSession for example
//		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
//		scriptSession.setAttribute("tabId", tabId);
//	}
}
