package p2p.info.retrieval.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;
import p2p.info.retrieval.web.model.ReverseAjaxThread;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {


	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) throws Exception {
		System.out.println("Query: " + query);
		List<Result> results = new ArrayList<Result>();
		List<Document> docs = null;

		// Propagate
		org.apache.lucene.demo.SearchFiles.propagateSearch(query);

		// Perform the local search
		try {
			docs = org.apache.lucene.demo.SearchFiles.doSimpleSearch(query);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create result objects.
		for (Document doc : docs) {
			results.add(new Result(doc));
		}
		return new JsonReaderResponse<Result>(results);
	}

	public static void receiveSearchReply(String line) {
		System.out.println("Result: " + line);

		ReverseAjaxThread thread = ReverseAjaxThread.getInstance();
		thread.addScriptSession(WebContextFactory.get().getScriptSession());

	}

//	@RemoteMethod
//	public void populateScriptSession() {
//		String tabId = (String) WebContextFactory.get().getSession().getAttribute("tabId"); // this may come from the HttpSession for example
//		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
//		scriptSession.setAttribute("tabId", tabId);
//	}
}
