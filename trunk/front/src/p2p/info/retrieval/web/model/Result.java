package p2p.info.retrieval.web.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@DataTransferObject(converter = ObjectConverter.class)
public class Result {
	
	private static final Logger logger = Logger.getLogger(Result.class);
	
	@RemoteProperty
	public String path;
	
	@RemoteProperty
	public Date modified;
	
	public Result(Document doc) {
		try {
			this.modified = DateTools.stringToDate(doc.get("modified"));
		} catch (ParseException e) {
			logger.warn("Could not parse 'modified' field - it will be null");
		}
		this.path = doc.get("path");
	}
	
	public static List<Result> getResults(List<Document> docs) {
		List<Result> results = new ArrayList<Result>(docs.size());
		for(Document doc: docs)
			results.add(new Result(doc));
		return results;
	}
	
	private Result(JSONObject obj) {
		try {
			this.modified = DateTools.stringToDate((String)obj.get("modified"));
		} catch (ParseException e) {
			logger.warn("Could not parse 'modified' field - it will be null");
		}
		this.path = (String)obj.get("path");
	}
	
	public static List<Result> getResultsFromArray(JSONArray arr) {
		
		List<Result> results;
		
//		JSONArray arr = (JSONArray) JSONValue.parse(JSONString);
		
		results = new ArrayList<Result>(arr.size());
		
		for(Object obj : arr) {
			results.add(new Result((JSONObject) obj));
		}
		
		return results;
	}

}
