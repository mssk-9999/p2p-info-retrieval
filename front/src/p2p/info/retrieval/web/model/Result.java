package p2p.info.retrieval.web.model;

import java.text.DateFormat;
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
import org.directwebremoting.json.types.JsonArray;
import org.directwebremoting.json.types.JsonObject;
import org.directwebremoting.json.types.JsonValue;

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
	
	private Result(JsonObject obj) {
		DateFormat format = DateFormat.getInstance();
		try {
			this.modified = format.parse(obj.get("modified").getString());
		} catch (ParseException e) {
			logger.warn("Could not parse 'modified' field - it will be null");
		}
		this.path = obj.get("path").getString();
	}
	
	public static List<Result> getResultsFromArray(JsonArray searchResults) {
		
		List<Result> results = new ArrayList<Result>(searchResults.size());
		
		for(JsonValue obj : searchResults) {
			results.add(new Result(obj.getJsonObject()));
		}
		
		return results;
	}

}
