package p2p.info.retrieval.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) throws Exception {
		System.out.println("Query: " + query);
		List<Result> results = new ArrayList<Result>();
		List<Document> docs = null;
		boolean mustPropagate = true;

		// Perform the search
		try {
			docs = org.apache.lucene.demo.SearchFiles.doSimpleSearch(query, mustPropagate);
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
}
