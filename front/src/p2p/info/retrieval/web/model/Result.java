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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import p2p.info.retrieval.web.util.SizeFormatter;

@DataTransferObject/*(converter = ObjectConverter.class)*/
public class Result {

	private static final Logger logger = Logger.getLogger(Result.class);

	private String path;

	private String modified;

	private String size;

	private String respondingIP;

	@RemoteProperty
	public String getPath() {
		return path;
	}

	@RemoteProperty
	public Date getModified() {
		Date date = null;
		try {
			date = DateTools.stringToDate(modified);
		} catch (ParseException e) {
			logger.warn("Could not parse 'modified' field - it will be null");
		}
		return date;
	}

	@RemoteProperty
	public String getSize() {
		return SizeFormatter.format(Long.parseLong(size));
	}

	@RemoteProperty
	public String getRespondingIP() {
		return respondingIP;
	}

	public Result(Document doc) {
		this.modified = doc.get("modified");
		this.path = doc.get("path");
		this.size = doc.get("size");
		this.respondingIP = "127.0.0.0";
	}

	public static List<Result> getResults(List<Document> docs) {
		List<Result> results = new ArrayList<Result>(docs.size());
		for(Document doc: docs)
			results.add(new Result(doc));
		return results;
	}

	private Result(JSONObject obj) {
		this.modified = (String)obj.get("modified");
		this.path = (String)obj.get("path");
		this.size = (String)obj.get("size");
		this.respondingIP = (String)obj.get("respondingIP");
	}

	public static List<Result> getResultsFromArray(JSONArray searchResults) {

		List<Result> results = new ArrayList<Result>(searchResults.size());

		for(Object obj : searchResults) {
			results.add(new Result((JSONObject)obj));
		}

		return results;
	}

}
