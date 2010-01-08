package p2p.info.retrieval.web.model;

import org.apache.log4j.Logger;

import p2p.info.retrieval.web.SearchFiles;

public class LocalResultsThread extends Thread {
	
	private static final Logger logger = Logger.getLogger(LocalResultsThread.class);
	private String results;

	public LocalResultsThread(String results) {
		this.results = results;
		logger.info("LocalResultsThread created...");
	}

	@Override
	public void run() {
		try {
			SearchFiles.receiveSearchReply(results);
		} catch (Exception e) {
			logger.error("Exception in LocalResultsThread.run()", e);
		}
	}

}
