package p2p.info.retrieval.lucene;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jtella.node.JTellaNode;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import p2p.info.retrieval.web.model.LocalResultsThread;

/** Simple command-line based search demo. */
public class SearchIndex {

	private static JTellaNode node = null;
	private static File index = new File("index");
	private static final Logger logger = Logger.getLogger(SearchIndex.class);
	private static final int HITS_PER_PAGE = 10;

	// TODO: Remove this in favor of the gnutella service 
	public static void initNode() {
		try{
			if(node == null)
				node = new JTellaNode();
		} catch (Exception e) {
			logger.error("Problem initializing JTellaNode instance", e);
		}
	}

	public static void setIndex(File index) {
		SearchIndex.index = index;
	}

	public static void doLocalSearch(String line) throws Exception {

		// TODO: Move the propagation to its own method/class/etc
		node.injectmessage(line, "someId");

		JSONObject jsonObj = (JSONObject)JSONValue.parse(line);
		String query = (String) jsonObj.get("query");
		String sessionId = (String) jsonObj.get("sessionId");

		List<Document> results = doSimpleSearch(query);
		if (results == null)
			return;

		JSONObject resultsObj = new JSONObject();
		resultsObj.put("sessionId", sessionId);

		JSONArray resultArr = new JSONArray();
		if (results != null) {
			for(Document d : results) {
				JSONObject resultObj = new JSONObject();
				resultObj.put("path", d.get("path"));
				resultObj.put("modified", d.get("modified"));
				resultObj.put("size", d.get("size"));
				resultArr.add(resultObj);
			}
		}
		resultsObj.put("searchResults", resultArr);

		// We want to make this asynchronous for better re-usability of the code
		LocalResultsThread thread = new LocalResultsThread(resultsObj.toJSONString());
		thread.start();

	}

	public static List<Document> doSimpleSearch(String line) throws Exception {
		String field = "contents";
		IndexReader reader;
		try{
			reader = IndexReader.open(FSDirectory.open(index), true); // only searching, so read-only=true
		}catch(Exception e) {
			logger.warn("Exception in doSimpleSearch - Could not open index: " + index.getAbsolutePath());
			return null;
		}
		Searcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		QueryParser parser = new QueryParser(field, analyzer);
		Query query = parser.parse(line);

		// Collect enough docs to show 1 pages
		TopScoreDocCollector collector = TopScoreDocCollector.create(HITS_PER_PAGE, false);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		List<Document> docs = new ArrayList<Document>(hits.length);
		for(int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			docs.add(doc);
		}
		
		reader.close();
		searcher.close();

		return docs;
	}

}
