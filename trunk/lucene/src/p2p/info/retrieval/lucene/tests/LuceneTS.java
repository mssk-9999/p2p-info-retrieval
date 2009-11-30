package p2p.info.retrieval.lucene.tests;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import p2p.info.retrieval.lucene.SearchService;
import polyester.CleaningWorker;
import polyester.util.SearchString;
import junit.framework.TestCase;
import lights.Field;
import lights.Tuple;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.IValuedField;
import lights.interfaces.TupleSpaceException;

public class LuceneTS extends TestCase {

	private static final Logger logger = Logger.getLogger(LuceneTS.class);
	private static final String luceneTS = "Lucene_TS";
	
	private String createSearch() {
		JSONObject obj = new JSONObject();
		obj.put("query", "String");
		return obj.toJSONString();
	}
	
	public void testPassThroughTS() throws Exception {
		// Set up the logging
		BasicConfigurator.configure();
		
		ITupleSpace ts = new FastTupleSpace(luceneTS);
		SearchService searchService = new SearchService(ts);
		CleaningWorker cleaning = new CleaningWorker(ts);
		
		searchService.start();
		logger.info("----------SearchService started----------");
		cleaning.start();
		logger.info("----------Cleaning started----------");
		
		//create and populate first tuple
		ITuple t = new Tuple();
		IValuedField f = new Field().setValue("Search");
		t.add(f);
		
		SearchString s = null;

		try {
			String searchString = createSearch();
			s = new SearchString(searchString); 
		}
		catch (Exception e) {return;} //caution: no tuple goes out if invalid search
		
		t.add(new Field().setValue(s));
		
		//put it in the tuple space
		try {
			ts.out(t);
		}
		catch (TupleSpaceException e) {}
		
		Thread.sleep(5000L);
	}
}
