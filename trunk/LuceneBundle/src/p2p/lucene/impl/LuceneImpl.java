package p2p.lucene.impl;

import java.io.File;

import org.apache.commons.jci.listeners.AbstractFilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.log4j.Logger;

import p2p.jtella.model.IIndexer;
import p2p.lucene.IndexManager;

public class LuceneImpl implements IIndexer {

	private static final long serialVersionUID = -7995392547965873471L;
	private static final Logger logger = Logger.getLogger(LuceneImpl.class);

	private IndexManager manager;
	private File docDir;
	private FilesystemAlterationMonitor fam;
	private boolean isManagerInitialized = false;
	private File index;

	public LuceneImpl() throws Exception {
		docDir = new File("/Users/Steve/workspace/front_demo/dir-to-index");
		index = new File("/Users/Steve/workspace/front_demo/index");
		if(docDir.mkdir())
			logger.debug("Documents directory did not exist. It has now been created.");
		
		/* Init the file monitor before the index because "File Create" 
		 * events are triggered for every existing file on fam.start();
		 */
		initFam();
		initLucene();
		logger.debug("Lucene Initialized");
	}
	
	private class MyFilesystemAlterationListener extends AbstractFilesystemAlterationListener {
		public void onDirectoryCreate( final File dir ) {
			super.onDirectoryCreate(dir);
			logger.debug("Dir created: "+dir.getAbsolutePath());
		}
		public void onDirectoryChange( final File dir ) {
			super.onDirectoryChange(dir);
			logger.debug("Dir changed: "+dir.getAbsolutePath());
		}
		public void onDirectoryDelete( final File dir ) {
			super.onDirectoryDelete(dir);
			logger.debug("Dir deleted: "+dir.getAbsolutePath());
		}

		public void onFileCreate( final File file) {
			super.onFileCreate(file);
			logger.debug("File created: "+file.getAbsolutePath());
			if(isManagerInitialized) {
				try {
					manager.addDocument(file);
				} catch (Exception e) {
					logger.error("Problem adding file to index", e);
				}
			}
		}
		public void onFileChange( final File file ) {
			super.onFileChange(file);
			logger.debug("File changed: "+file.getAbsolutePath());
			if(isManagerInitialized) {
				try {
					manager.updateDocument(file);
				} catch (Exception e) {
					logger.error("Problem updating file in index", e);
				}
			}
		}
		public void onFileDelete( final File file ) {
			super.onFileDelete(file);
			logger.debug("File deleted: "+file.getAbsolutePath());
			if(isManagerInitialized) {
				try {
					manager.removeDocument(file);
				} catch (Exception e) {
					logger.error("Problem removing file from index", e);
				}
			}
		}
	}

	private void initFam() {
		try {
			fam = new FilesystemAlterationMonitor();
			MyFilesystemAlterationListener listener = new MyFilesystemAlterationListener();
			fam.addListener(docDir, listener);
			fam.start();
			listener.waitForFirstCheck();
		} catch (Exception e) {
			logger.error("Problem initializing the file monitor", e);
		}
		logger.info("Monitoring...");
	}

	public void destroy() {
		try {
			fam.stop();
			logger.info("Destroyed the directory watcher");
			manager.destroy();
			logger.info("Destroyed the index manager");
		} catch (Exception e) {
			logger.error("Problem destroying the lucene bundle", e);
		}
	}

	private void initLucene() throws Exception {

		try {
			manager = IndexManager.getInstance();
			boolean exists = manager.initIndex(index);
			isManagerInitialized = true;

			if(!exists) {
				logger.info("Creating index of " + index.getAbsolutePath());
				manager.createIndex(docDir);
			}
		} catch (Exception e) {
			logger.error("Problem setting up Lucene Indexer", e);
			throw e;
		}
	}

	@Override
	public String search(String query) {
		String result = null;
		try {
			result = manager.search(query);
		} catch (Exception e) {
			logger.error("Error performing search", e);
		}
		return result;
	}

}
