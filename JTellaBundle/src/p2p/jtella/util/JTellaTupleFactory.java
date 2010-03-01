package p2p.jtella.util;

import polyester.TupleFactory;
import lights.interfaces.ITuple;

public class JTellaTupleFactory extends TupleFactory {
	
	private static final String SEARCH_VERB = "SEARCH";
	private static final String SEARCH_REPLY_VERB = "SEARCH_REPLY";
	private static final String DIRECT_SEARCH_VERB = "DIRECT_SEARCH";

	private static final int NUM_SEARCH_FIELDS = 1;
	private static final int NUM_INCOMING_SEARCH_REPLY_FIELDS = 1;
	private static final int NUM_OUTGOING_SEARCH_REPLY_FIELDS = 2;
	
	
	public static ITuple createRemoteSearchTemplate() {
		return createQueryTupleTemplate(SEARCH_VERB, NUM_SEARCH_FIELDS);
	}
	
	public static ITuple createLocalSearchTemplate() {
		return createQueryTupleTemplate(DIRECT_SEARCH_VERB, NUM_SEARCH_FIELDS);
	}
	
	public static ITuple createIncomingSearchReplyTemplate() {
		return createQueryTupleTemplate(SEARCH_REPLY_VERB, NUM_INCOMING_SEARCH_REPLY_FIELDS);
	}
	
	public static ITuple createOutgoingSearchReplyTemplate() {
		return createQueryTupleTemplate(SEARCH_REPLY_VERB, NUM_OUTGOING_SEARCH_REPLY_FIELDS);
	}
	
	public static ITuple createRemoteSearchTuple(String msg) {
		return createTuple(SEARCH_VERB, msg);
	}
	
	public static ITuple createLocalSearchTuple(String msg) {
		return createTuple(DIRECT_SEARCH_VERB, msg);
	}
	
	public static ITuple createOutgoingSearchReplyTuple(String msg, String originalMessage) {
		String[] msgs = {msg,originalMessage};
		return createTuple(SEARCH_REPLY_VERB, msgs);
	}

	public static ITuple createIncomingSearchReplyTuple(String msg) {
		return createTuple(SEARCH_REPLY_VERB, msg);
	}
}
