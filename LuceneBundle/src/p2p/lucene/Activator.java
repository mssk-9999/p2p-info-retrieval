package p2p.lucene;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import p2p.jtella.model.IIndexer;
import p2p.lucene.impl.LuceneImpl;

public class Activator implements BundleActivator {

	private static Logger logger;
	private LuceneImpl lucene;

	@Override
	public void start(BundleContext context) throws Exception {
		initLogger();
		
		lucene = new LuceneImpl();
		context.registerService(IIndexer.class.getName(), lucene, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		lucene.destroy();
	}

	private void initLogger() {
		//		String prefix =  getServletContext().getRealPath("/");
		//		String file = getInitParameter("log4j-init-file");
		//		// if the log4j-init-file is not set, then no point in trying
		//		if(file != null) {
		//			PropertyConfigurator.configure(prefix+"/"+file);
		BasicConfigurator.configure();
		Logger.getLogger("org.apache.commons.jci").setLevel(Level.WARN);
		logger = Logger.getLogger(Activator.class);
		//		}
	}

	
}

