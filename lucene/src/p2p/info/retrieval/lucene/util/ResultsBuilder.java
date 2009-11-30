package p2p.info.retrieval.lucene.util;

import java.util.List;

import org.apache.lucene.document.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResultsBuilder {
	public static String docListToJson(List<Document> docList) {
		JSONArray resultArr = new JSONArray();
		for(Document d : docList) {
			JSONObject resultObj = new JSONObject();
			resultObj.put("path", d.get("path"));
			resultObj.put("modified", d.get("modified"));
			resultObj.put("size", d.get("size"));
			resultArr.add(resultObj);
		}
		return resultArr.toJSONString();
	}
}
