package p2p.lucene.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import junit.framework.TestCase;
import p2p.lucene.IndexManager;

public class IndexFunctionTests extends TestCase {

	private IndexManager manager;
	private File docDirectory;
	private File indexDirectory;
	private String query;
	private File testDoc;
	private String newSearchTerm;

	protected void setUp() throws Exception {
		BasicConfigurator.configure();
		docDirectory = new File("/Users/Steve/workspace/front_demo/dir-to-index");
		indexDirectory = new File("/Users/Steve/workspace/front_demo/index");
		testDoc = new File(docDirectory.getPath()+File.separator+"new_document.txt");
		query = "aString";
		newSearchTerm = "newText";
		manager = IndexManager.getInstance();
		manager.initIndex(indexDirectory);
	}

	protected void tearDown() throws Exception {
		manager.destroy();
	}

	public void testCreateIndex() throws Exception {
//		assertFalse("Index dir already exists", indexDirectory.exists());
		createIndex();
		String testQuery = "file";
		String result = search(testQuery);
		removeIndex();
		JSONObject obj = (JSONObject)JSONValue.parse(result);
		JSONArray searchResults = (JSONArray)obj.get("searchResults");
		assertFalse("File does not exist in index", searchResults.isEmpty());
		
	}

	private void removeIndex() {
		removeDir(indexDirectory);
	}
	
	// This doesn't work... :(
	private void removeDir(File dir) {
		if(dir.isDirectory() && dir.listFiles().length > 0){
			for(File file : dir.listFiles()){
				removeDir(file);
			}
		}
		
		dir.delete();
	}

	private void createIndex() {
		manager.createIndex(docDirectory);
	}

	public void testAddDocument() throws Exception {
		createIndex();
		addDocument();
		String result = search(query);
		removeDocument();
		
		JSONObject obj = (JSONObject)JSONValue.parse(result);
		JSONArray searchResults = (JSONArray)obj.get("searchResults");
		assertFalse("File does not exist in index", searchResults.isEmpty());
	}

	private void addDocument() throws IOException, Exception {
		assertTrue("Document not created - it exists already", testDoc.createNewFile());
		long length = testDoc.length();
		writeToDocument(query);
		assertTrue("Did not write to document", testDoc.length() > length);
		
		manager.addDocument(testDoc);
	}

	private void writeToDocument(String addition) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(testDoc));
		out.write(" "+addition);
		out.close();
	}
	
	private String search(String query) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("query", query);
		String line = obj.toJSONString();
		
		return manager.search(line);
	}

	public void testRemoveDocument() throws Exception {
		createIndex();
		addDocument();
		removeDocument();
		String result = search(query);
		JSONObject obj = (JSONObject)JSONValue.parse(result);
		JSONArray searchResults = (JSONArray)obj.get("searchResults");
		assertTrue("File still exists in index", searchResults.isEmpty());
	}

	private void removeDocument() throws Exception {
		testDoc.delete();
		manager.removeDocument(testDoc);
	}
	
	public void testSearch() throws Exception {
		createIndex();
		addDocument();
		
		String result = search(query);
		System.out.println("Result: "+result);
		
		removeDocument();
	}
	
	public void testUpdateDocument() throws Exception {
		createIndex();
		addDocument();
		updateDocument();
		String result = search(newSearchTerm);
		removeDocument();
		
		JSONObject obj = (JSONObject)JSONValue.parse(result);
		JSONArray searchResults = (JSONArray)obj.get("searchResults");
		assertFalse("File does not exist in index", searchResults.isEmpty());
	}

	private void updateDocument() throws IOException, Exception {
		assertTrue("testDoc does not exist", testDoc.exists());
		writeToDocument(newSearchTerm);
		
		manager.updateDocument(testDoc);
	}

}
