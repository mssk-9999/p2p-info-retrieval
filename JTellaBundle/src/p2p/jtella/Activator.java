package p2p.jtella;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import p2p.jtella.impl.SearchImpl;
import p2p.jtella.model.IClient;
import p2p.jtella.model.IIndexer;
import p2p.jtella.service.SearchService;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Logger logger;
	private SearchImpl jtella;
	private static ServiceReference[] indexerServiceRefs;
	private static ServiceReference[] clientServiceRefs;

	@Override
	public void start(BundleContext bc) throws Exception {
		initLogger();
		context = bc;
		File f = new File("hosts.list");
		jtella = new SearchImpl(f);
		bc.registerService(SearchService.class.getName(), jtella, null);
	}

	@Override
	public void stop(BundleContext bc) throws Exception {
		if(indexerServiceRefs != null && indexerServiceRefs.length > 0) {
			for(ServiceReference ref : indexerServiceRefs)
				bc.ungetService(ref);
		}
		
		// Shutdown GNUTella
		jtella.destroy();
	}
	
	private void initLogger() {
//		String prefix =  getServletContext().getRealPath("/");
//		String file = getInitParameter("log4j-init-file");
//		// if the log4j-init-file is not set, then no point in trying
//		if(file != null) {
//			PropertyConfigurator.configure(prefix+"/"+file);
		BasicConfigurator.configure();
		Logger.getLogger("com.dan").setLevel(Level.WARN);
		Logger.getLogger("protocol.com.dan").setLevel(Level.WARN);
		Logger.getLogger("com.kenmccrary").setLevel(Level.WARN);
		Logger.getLogger("protocol.com.kenmccrary").setLevel(Level.WARN);
		
		logger = Logger.getLogger(Activator.class);
//		}
	}

	public static List<IIndexer> getIndexers() throws InvalidSyntaxException {
		List<IIndexer> indexers = null;
		indexerServiceRefs = context.getServiceReferences(IIndexer.class.getName(), null);
		if(indexerServiceRefs != null){
			indexers = new ArrayList<IIndexer>(indexerServiceRefs.length);
			for(ServiceReference ref : indexerServiceRefs) {
				IIndexer indexer = (IIndexer)context.getService(ref);
				indexers.add(indexer);
			}
			logger.info("Got lucene services. Count="+indexers.size());
		}
		return indexers;
	}
	
	public static List<IClient> getClients() throws InvalidSyntaxException {
		List<IClient> clients = null;
		clientServiceRefs = context.getServiceReferences(IClient.class.getName(), null);
		if(clientServiceRefs != null){
			clients = new ArrayList<IClient>(clientServiceRefs.length);
			for(ServiceReference ref : clientServiceRefs) {
				IClient client = (IClient)context.getService(ref);
				clients.add(client);
			}
			logger.info("Got client services. Count="+clients.size());
		}
		return clients;
	}

}
