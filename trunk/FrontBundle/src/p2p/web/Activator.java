package p2p.web;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.directwebremoting.servlet.DwrServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import p2p.jtella.model.IClient;
import p2p.jtella.service.SearchService;

/**
 * Front Activator.
 *
 * @author Steve Gibson
 */
public class Activator implements BundleActivator {

	/**
	 * HttpService reference.
	 */
	private ServiceReference httpServiceRef;
	private ServiceReference[] gnutellaServiceRefs;
	private static Logger logger;

	/**
	 * Called when the OSGi framework starts our bundle
	 */
	public void start( BundleContext bc ) throws Exception {
		initLogger(bc);
		
		findSearcher(bc);
		
		registerService(bc);
		
		registerServlets(bc);
	}
	
	private void initLogger(BundleContext bc) {
//		String file = "/config/log4j.lcf";
//		File theFile = bc.getDataFile(file);
//		PropertyConfigurator.configure(theFile.getAbsolutePath());
		BasicConfigurator.configure();
		logger = Logger.getLogger(Activator.class);
		Logger.getLogger("org.directwebremoting").setLevel(Level.WARN);
	}

	private void findSearcher(BundleContext bc) throws InvalidSyntaxException {
		gnutellaServiceRefs = bc.getServiceReferences(SearchService.class.getName(), null);
		if(gnutellaServiceRefs != null) {
			List<SearchService> gnutellas = new ArrayList<SearchService>(gnutellaServiceRefs.length);
			for(ServiceReference ref : gnutellaServiceRefs) {
				SearchService searcher = (SearchService)bc.getService(ref);
				gnutellas.add(searcher);
			}
			logger.info("Got gnutella services. Count="+gnutellas.size());
			if(gnutellas.size() > 1)
				logger.info("Found more than one gnutella service. Using the first one.");
			SearchFiles.setGnutella(gnutellas.get(0));
		} else {
			logger.warn("No gnutella service references found");
		}
	}
	
	private void registerService(BundleContext bc) {
		bc.registerService(IClient.class.getName(), new SearchFiles(), null);
	}
	
	private void registerServlets(BundleContext bc) throws ServletException,
	NamespaceException {
		httpServiceRef = bc.getServiceReference( HttpService.class.getName() );
		if(httpServiceRef != null) {
			final HttpService httpService = (HttpService) bc.getService( httpServiceRef );
			if( httpService != null )
			{
				// create a default context to share between registrations
				final HttpContext httpContext = httpService.createDefaultHttpContext();
				// register the hello world servlet
				final Dictionary<String, String> dwrInitParams = new Hashtable<String, String>();
				dwrInitParams.put( "classes", "p2p.web.SearchFiles, p2p.web.model.Result" );
				dwrInitParams.put( "debug", "true" );
				dwrInitParams.put( "activeReverseAjaxEnabled", "true" );
//				dwrInitParams.put( "config-dwr", "/bin/p2p/web/dwr.xml" );
//				dwrInitParams.put( "welcomeFiles", "index.html, index.htm, index.jsp" );
				httpService.registerServlet(
						"/dwr",					// alias
						new DwrServlet(),		// registered servlet
						dwrInitParams,			// init params
						httpContext				// http context
				);

//				final Dictionary<String, String> initParams = new Hashtable<String, String>();
//				initParams.put( "log4j-init", "bin/p2p/web/log4j.lcf" );
//				httpService.registerServlet(
//						"/servlet",					// alias
//						new WebServlet(),		// registered servlet
//						initParams,				// init params
//						httpContext				// http context
//				);
				// register images as resources
				httpService.registerResources(
						"/",
						"/web",
						httpContext
				);
			}
		}
	}

	/**
	 * Called when the OSGi framework stops our bundle
	 */
	public void stop( BundleContext bc ) throws Exception {
		if(gnutellaServiceRefs != null && gnutellaServiceRefs.length > 0) {
			for(ServiceReference ref : gnutellaServiceRefs) {
				bc.ungetService(ref);
			}
		}
		if(httpServiceRef != null) {
			bc.ungetService( httpServiceRef );
			httpServiceRef = null;
		}
	}
}

