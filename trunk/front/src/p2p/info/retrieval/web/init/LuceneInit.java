package p2p.info.retrieval.web.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import p2p.info.retrieval.lucene.IndexManager;
import p2p.info.retrieval.lucene.SearchIndex;

public class LuceneInit extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7995392547965873471L;

	private static Logger logger;

	private int watchID;
	private static IndexManager manager;
	private String docDirPath;

	public void init() {
		try {
			initLogger();
			initIndexer();
			initGnutella();
			initDirWatcher();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initGnutella() {

		// Create gwebcaches.list if it does not already exist
		try {
		    File file = new File("gwebcaches.list");

		    // Create file if it does not exist
		    boolean success = file.createNewFile();
		    if (success) {
		        logger.info("Created gwebcaches.list in directory - " + file.getAbsolutePath());
		    } else {
		    	logger.info("Using existing gwebcaches.list in directory - " + file.getAbsolutePath());
		    }
		} catch (IOException e) {
		}

		SearchIndex.initNode();
	}

	public void destroy() {
		try {
			logger.info("Destroying the directory watcher");
			destroyDirWatcher();
		} catch (JNotifyException e) {
			logger.error("Problem destroying dir watcher", e);
		}
	}

	private void initLogger() {
		String prefix =  getServletContext().getRealPath("/");
		String file = getInitParameter("log4j-init-file");
		// if the log4j-init-file is not set, then no point in trying
		if(file != null) {
			PropertyConfigurator.configure(prefix+file);
			logger = Logger.getLogger(LuceneInit.class);
		}
	}
	
	private void initIndexer() throws Exception {
		InputStream is = null;
		String prefix =  getServletContext().getRealPath("/");
		String file = getInitParameter("lucene-init-file");

		if(file != null) {

			try {
				is = new FileInputStream(prefix+file);
				Properties prop = new Properties();
				prop.load(is);

				String indexPath = prop.getProperty("lucene.index", "index");
				docDirPath = prop.getProperty("lucene.dirToIndex", "blah");

				File index = new File(indexPath);
				manager = new IndexManager(index);

				if(!index.exists()) {
					logger.info("Creating index of " + index.getAbsolutePath());
					manager.createIndex(docDirPath);
				}
				if(index.canRead()) {
					SearchIndex.setIndex(index);
				} else {
					throw new SecurityException("Cannot read the specified index - " + index.getAbsolutePath());
				}
				try {
					is.close();
				} catch (Exception e) {}
			} catch (Exception e) {
				try {
					is.close();
				} catch (Exception e1) {}
				throw e;
			}
		}
	}
	
	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}


	private void moveJNotifyLib() throws IOException {
		String libs = getServletContext().getRealPath("/WEB-INF/lib");
		File JNotifyLibSrc = new File(libs+"/jnotify.dll");
		File JNotifyLibDest = new File("jnotify.dll");
		if(!JNotifyLibDest.exists()) {
			logger.info("Copying file \"" + JNotifyLibSrc.getAbsolutePath() + "\" to \"" + JNotifyLibDest.getAbsolutePath() + "\"");
			try {
				copy(JNotifyLibSrc, JNotifyLibDest);
			} catch(IOException e) {
				logger.warn("Could not copy file " + JNotifyLibSrc.getAbsolutePath(), e);
			}
		}
	}
	
	private void loadJNotifyLibPath() throws Exception {
		// Reset the "sys_paths" field of the ClassLoader to null.
		Class clazz = ClassLoader.class;
		Field field = clazz.getDeclaredField("sys_paths");
		boolean accessible = field.isAccessible();
		if (!accessible)
			field.setAccessible(true);
		Object original = field.get(clazz);
		// Reset it to null so that whenever "System.loadLibrary" is called, it will be reconstructed with the changed value.
		field.set(clazz, null);
		try {
			// Change the value and load the library.
//			String libs = getServletContext().getRealPath("/WEB-INF/lib");
			String libs = "/";
			logger.debug("Loading JNotify native library from path - " + libs);
			System.setProperty("java.library.path", libs);
			System.loadLibrary("jnotify");
			if(System.getProperty("java.library.path").equals(libs))
				logger.debug("JNotify native library loaded successfully");
			else
				logger.warn("Problem loading JNotify native library. JNotify will not be used.");
		}
		finally {
			//Revert back the changes.
			field.set(clazz, original);
			field.setAccessible(accessible);
		}

	}

	private void initDirWatcher() throws Exception {
		
		// Move the native library to a safer location
//		moveJNotifyLib();
		
		// Load the JNotify native library
//		loadJNotifyLibPath();
		
		int mask =  JNotify.FILE_CREATED | 
		JNotify.FILE_DELETED | 
		JNotify.FILE_MODIFIED| 
		JNotify.FILE_RENAMED;
		boolean watchSubtree = true;
		watchID = JNotify.addWatch(docDirPath, mask, watchSubtree, new JNotifyListener()
		{
			public void fileRenamed(int wd, String rootPath, String oldName, String newName)
			{
				logger.info("JNotifyTest.fileRenamed() : wd #" + wd + " root = " + rootPath
						+ ", " + oldName + " -> " + newName);
				try {
					manager.removeDocument(rootPath+File.separator+oldName);
					manager.addDocument(rootPath+File.separator+newName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void fileModified(int wd, String rootPath, String name)
			{
				logger.info("JNotifyTest.fileModified() : wd #" + wd + " root = " + rootPath
						+ ", " + name);

				try {
					manager.removeDocument(rootPath+File.separator+name);
					manager.addDocument(rootPath+File.separator+name);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void fileDeleted(int wd, String rootPath, String name)
			{
				logger.info("JNotifyTest.fileDeleted() : wd #" + wd + " root = " + rootPath
						+ ", " + name);
				try {
					manager.removeDocument(rootPath+File.separator+name);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void fileCreated(int wd, String rootPath, String name)
			{
				logger.info("JNotifyTest.fileCreated() : wd #" + wd + " root = " + rootPath
						+ ", " + name);
				try {
					manager.addDocument(rootPath+File.separator+name);
				} catch (Exception e) {
					logger.error("Exception adding a document to the index", e);
				}
			}
		});
	}

	private void destroyDirWatcher() throws JNotifyException {
		boolean res = JNotify.removeWatch(watchID);
		if (!res)
		{
			logger.error("Invalid watch ID - " + watchID);
		}
	}

	
	
	

}
