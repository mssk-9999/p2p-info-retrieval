package p2p.info.retrieval.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import p2p.info.retrieval.web.model.JsonReaderResponse;
import p2p.info.retrieval.web.model.Result;

@RemoteProxy(name = "SearchFilesInterface")
public class SearchFiles {

	@RemoteMethod
	public JsonReaderResponse<Result> getResults(String query) {
		System.out.println("Query: " + query);
		List<Result> results = new ArrayList<Result>();
		List<Document> docs = null;
		try {
			docs = org.apache.lucene.demo.SearchFiles.doSimpleSearch(query);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
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
