package p2p.info.retrieval.lucene.util;

import java.util.HashMap;

public class QueryBuilder {
	
	private static final String QUERY_END_TAG = "</query>";
	private static final String QUERY_START_TAG = "<query>";
	private static final String STARTAT_END_TAG = "</startat>";
	private static final String STARTAT_START_TAG = "<startat>";
	private static final String MAXRESULTS_END_TAG = "</maxresults>";
	private static final String MAXRESULTS_START_TAG = "<maxresults>";

	public static HashMap<String, String> parse( String line ) {
		HashMap<String, String> query = new HashMap<String, String>();
		
		int queryStart = line.indexOf(QUERY_START_TAG);
		int queryEnd = line.indexOf(QUERY_END_TAG);
		int startatStart = line.indexOf(STARTAT_START_TAG);
		int startatEnd = line.indexOf(STARTAT_END_TAG);
		int maxresultsStart = line.indexOf(MAXRESULTS_START_TAG);
		int maxresultsEnd = line.indexOf(MAXRESULTS_END_TAG);
		
		if(queryStart < 0 || queryEnd < 0 || startatStart < 0 || startatEnd < 0 || maxresultsStart < 0 || maxresultsEnd < 0) {
			System.out.println("Bad formatting");
			return null;
		}
		
		String queryString = line.substring(queryStart + QUERY_START_TAG.length(), queryEnd);
		String startat = line.substring(startatStart + STARTAT_START_TAG.length(), startatEnd);
		String maxresults = line.substring(maxresultsStart + MAXRESULTS_START_TAG.length(), maxresultsEnd);
		
		query.put("query", queryString);
		query.put("startat", startat);
		query.put("maxresults", maxresults);
		
		return query;
	}
}
