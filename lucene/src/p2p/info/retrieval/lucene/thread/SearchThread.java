package p2p.info.retrieval.lucene.thread;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import p2p.info.retrieval.lucene.util.QueryBuilder;

public class SearchThread implements Runnable {

	protected Socket clientSocket = null;

	public SearchThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			
			String line = input.readLine();
			HashMap<String, String> request = QueryBuilder.parse(line);
			search(request);
			
			output.close();
			input.close();
			System.out.println("Request processed: " + line);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void search(HashMap<String, String> request) throws Exception {
		boolean error = false;                  //used to control flow for error messages
		String indexName = "C:/Users/Steve/workspace/lucene/demo-text-dir/index";       //local copy of the configuration variable
		IndexSearcher searcher = null;          //the searcher used to open/search the index
		Query query = null;                     //the Query created by the QueryParser
		TopDocs hits = null;                       //the search results
		int startindex = 0;                     //the first index displayed on this page
		int maxpage    = 50;                    //the maximum items displayed on this page
		String queryString = null;              //the query entered in the previous page
		String startVal    = null;              //string version of startindex
		String maxresults  = null;              //string version of maxpage
		int thispage = 0;                       //used for the for/next either maxpage or
												//hits.totalHits - startindex - whichever is
												//less

		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexName)), true); // only searching, so read-only=true
			searcher = new IndexSearcher(reader);         //create an indexSearcher for our page
			//NOTE: this operation is slow for large
			//indices (much slower than the search itself)
			//so you might want to keep an IndexSearcher 
			//open

		} catch (Exception e) {                         //any error that happens is probably due
			//to a permission problem or non-existant
			//or otherwise corrupt index

//			throw new Exception("ERROR opening the Index - " + e.getMessage());
			error = true;                                  //don't do anything up to the footer
		}


		if (error == false) {                                           //did we open the index?
			queryString = request.get("query");           //get the search criteria
			startVal    = request.get("startat");         //get the start index
			maxresults  = request.get("maxresults");      //get max results per page
			try {
				maxpage    = Integer.parseInt(maxresults);    //parse the max results first
				startindex = Integer.parseInt(startVal);      //then the start index  
			} catch (Exception e) { } //we don't care if something happens we'll just start at 0
			//or end at 50



//			if (queryString == null || queryString.isEmpty())
//				throw new ServletException("no query specified");

			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);           //construct our usual analyzer
			try {
				QueryParser qp = new QueryParser("contents", analyzer);
				query = qp.parse(queryString); //parse the 
			} catch (ParseException e) {                          //query and construct the Query
				//object
				//if it's just "operator error"
				//send them a nice error HTML

//				throw new Exception("Error while parsing query - "+ e.getMessage());

				error = true;	//don't bother with the rest of
								//the page
			}
		}


		if (error == false && searcher != null) {                     // if we've had no errors
			// searcher != null was to handle
			// a weird compilation bug 
			thispage = maxpage;                                   // default last element to maxpage
			hits = searcher.search(query, maxpage);                        // run the query 
			if (hits.totalHits == 0) {                             // if we got no results tell the user

//				<p> I'm sorry I couldn't find what you were looking for. </p>

				error = true;                                        // don't bother with the rest of the
				// page
			}
		}

		if (error == false && searcher != null) {

			if ((startindex + maxpage) > hits.totalHits) {
				thispage = hits.totalHits - startindex;      // set the max index to maxpage or last
			}                                                   // actual search result whichever is less

			for (int i = 0; i < hits.totalHits; i++) {  // for each element

				Document doc = searcher.doc(hits.scoreDocs[i].doc);                    //get the next document 
				String doctitle = doc.get("title");            //get its title
				String url = doc.get("path");                  //get its path field
				if (url != null && url.startsWith("../webapps/")) { // strip off ../webapps prefix if present
					url = url.substring(10);
				}
				if ((doctitle == null) || doctitle.equals("")) //use the path if it has no title
					doctitle = url;

			}

		}                                            //then include our footer.
		if (searcher != null) {
				searcher.close();
		}
	}

}
