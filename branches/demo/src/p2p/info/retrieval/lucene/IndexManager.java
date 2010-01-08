package p2p.info.retrieval.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import p2p.info.retrieval.lucene.model.FileDocument;

public class IndexManager {

	private static final Logger logger = Logger.getLogger(IndexManager.class);
	private static File indexDir;
	private static File docDir;

	public IndexManager() {
		this(new File("index"));
	}

	public IndexManager(File indexDir) {
		this.indexDir = indexDir;
	}

	public void createIndex(String dirToIndex) {
		docDir = new File(dirToIndex);
		if (!docDir.exists() || !docDir.canRead()) {
			logger.warn("Document directory '" + docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
			return;
		}

		Date start = new Date();
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
			logger.info("Indexing to directory '" +indexDir+ "'...");
			indexDocs(writer, docDir);
			logger.info("Optimizing...");
			writer.optimize();
			writer.close();
			analyzer.close();

			Date end = new Date();
			logger.info(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			logger.error(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
	}

	private void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				logger.trace("adding " + file);
				try {
					writer.addDocument(FileDocument.Document(file, docDir));
				}
				// at least on windows, some temporary files raise this exception with an "access denied" message
				// checking if the file can be read doesn't help
				catch (FileNotFoundException fnfe) {}
			}
		}
	}

	public void addDocument(String fileName) throws Exception {
		File DOC_DIR = new File(fileName);
		if (!DOC_DIR.exists() || !DOC_DIR.canRead()) {
			logger.warn("Document or directory '" + DOC_DIR.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
			return;
		}

		if(!indexDir.exists()) {
			logger.warn("Index does not exist. Exiting.");
			return;
		}

		Date start = new Date();
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), analyzer, false, IndexWriter.MaxFieldLength.LIMITED);
			logger.info("Adding '" +DOC_DIR+ "' to index '" + indexDir + "'...");
			indexDocs(writer, DOC_DIR);
			logger.info("Optimizing...");
			writer.optimize();
			writer.close();
			analyzer.close();

			Date end = new Date();
			logger.info(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			logger.error(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
	}

	public void removeDocument(String fileName) throws Exception {
		if(!indexDir.exists()) {
			logger.warn("Index does not exist. Exiting.");
			return;
		}

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		QueryParser parser = new QueryParser("path", analyzer);
		Query query = parser.parse(fileName);

		Directory directory = FSDirectory.open(indexDir);
		//		IndexReader reader = IndexReader.open(directory, false); // we don't want read-only because we are about to delete

		//		reader.deleteDocument(docNum);
		IndexWriter writer = new IndexWriter(directory, analyzer, false, IndexWriter.MaxFieldLength.LIMITED);
		writer.deleteDocuments(query);
		logger.info("Removed '" +fileName+ "' from index");
		writer.close();
		directory.close();
		analyzer.close();
	}

	public void updateDocument(String fileName) throws Exception {
		// remove then add
	}

}
