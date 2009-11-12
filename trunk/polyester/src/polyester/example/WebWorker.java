package polyester.example;

import java.util.*;

import lights.*;
import lights.extensions.XMLField;
import lights.interfaces.*;
//import lights.extensions.*;
import polyester.AbstractWorker;
import polyester.TupleFactory;
//import polyester.Worker;
//import polyester.util.*;

import org.w3c.dom.*;

//import com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl;

public class WebWorker extends AbstractWorker {
	
	public WebWorker(ITupleSpace space) {
		super(space);
		//the template that this worker looks for is [literal:"Result"; formal:DeferredDocumentImpl]
		ITuple t = new Tuple();
		t.add(new Field().setValue("Result"));	
		/* With Document fields, matching works but it's difficult or impossible to keep track of repeated tuples because 
		 * of non-standard implementations, and the lack of hashcode() and equals() in these implementations
		 */
		t.add(TupleFactory.createXMLTemplateField());

		this.addQueryTemplate(t);
	}
	
	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple query) {
		
		System.out.println("New Result ! ");
		
		
		Document doc = (Document) ( (XMLField) query.get(PAYLOAD)).getValue();
		System.out.println(doc.getBaseURI());
		
		
		//return nothing
		return new ArrayList<ITuple>();
	}


	//all methods below obsolete because now using AbstractWorker
	/*@Override
	public void work() {
		this.displayResults(retrieve());
	}* /
	
		
	/**
	 * Synchronous search of tuples in network and local database
	 * (an asynchronous version could use listeners 
	 * instead of the return value)
	 * @param query is a tuple 
	 * @return list of tuples that matched the request
	 * /
	public ITuple[] retrieve() {
		
		ITuple t = new Tuple();
		t.add(new Field().setValue("Result"));	
		t.add(new Field().setType(DeferredDocumentImpl.class));
		
		//post the tuple and retrieve the results
		
		ITuple[] results = null;
		try {
			results = space.ing(t); // if we want to consume the tuples that are read.
			//results = space.rdg(t); //if we don't want to remove the read tuples.
		} catch (TupleSpaceException e) {}
		
		return results;
	}* /
	
	/**
	 * Very non-fancy display of the results retrieved from the tuple-space
	 * In a real implementation the payload is probably in HTML and can
	 * be displayed on a browser.
	 * /
	public void displayResults(ITuple[] results) {
		System.out.println("Results: ");
		if (results == null) {System.out.println("None!");return;}
		for (ITuple t : results) {
			Document doc = (Document) ( (Field) t.get(PAYLOAD)).getValue();
			System.out.println(doc.getBaseURI());
		}
	}

	
	//=============================================================
	//=========possibly obsolete methods===========================
	//=============================================================
	
	/**
	 * Synchronous search of tuples in network and local database
	 * (an asynchronous version could use listeners 
	 * instead of the return value)
	 * @param query is a tuple 
	 * @return list of tuples that matched the request
	 * /
	public ITuple[] search(ITuple t) {
		
		//post the tuple and retrieve the results
		
		ITuple[] results = null;
		try {
			results = space.rdg(t);
		} catch (TupleSpaceException e) {}
		
		return results;
	}
	
	
	/**
	 * Synchronous search of tuples in network and local database
	 * (an asynchronous version could use listeners 
	 * instead of the return value)
	 * @param query is a tuple 
	 * @return list of tuples that matched the request, in String format
	 * /
	public List<String> searchResultsAsStringList(ITuple t) {
		
		//post the tuple and retrieve the results
		
		ITuple[] results = search(t);
		
		if (results == null) return null;
		
		List<String> l = new ArrayList<String>();
		
		for (ITuple temp : results) {
			l.add(temp.toString());
		}	
		
		return l;
	}
*/
	}
