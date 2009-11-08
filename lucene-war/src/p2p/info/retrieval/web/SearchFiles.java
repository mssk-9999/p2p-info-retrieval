package p2p.info.retrieval.web;

import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) {
		System.out.println("Query: " + query);
		int numberOfRows = 200;
		List<Result> results = new ArrayList<Result>(numberOfRows);
		// Create dummy objects.
		for (int i = 0; i < numberOfRows; i++) {
			results.add(new Result("Result " + i));
		}
		return new JsonReaderResponse<Result>(results);
	}
}
