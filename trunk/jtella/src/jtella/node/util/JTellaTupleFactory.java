package jtella.node.util;

import polyester.TupleFactory;
import lights.interfaces.ITuple;

public class JTellaTupleFactory extends TupleFactory {
	
	private static final String SEARCH_VERB = "SEARCH";
	private static final String SEARCH_REPLY_VERB = "SEARCH_REPLY";
	private static final String DIRECT_SEARCH_VERB = "DIRECT_SEARCH";

	private static final int HOW_MANY_FIELDS = 1;
	
	
	public static ITuple createSearchTemplate() {
		return createQueryTupleTemplate(SEARCH_VERB, HOW_MANY_FIELDS);
	}
	
	public static ITuple createDirectSearchTemplate() {
		return createQueryTupleTemplate(DIRECT_SEARCH_VERB, HOW_MANY_FIELDS);
	}
	
	public static ITuple createSearchReplyTemplate() {
		return createQueryTupleTemplate(SEARCH_REPLY_VERB, HOW_MANY_FIELDS);
	}
	
	public static ITuple createSearchTuple(String msg) {
		return createTuple(SEARCH_VERB, msg);
	}
	
	public static ITuple createDirectSearchTuple(String msg) {
		return createTuple(DIRECT_SEARCH_VERB, msg);
	}
	
	public static ITuple createSearchReplyTuple(String msg) {
		return createTuple(SEARCH_REPLY_VERB, msg);
	}
}
