package p2p.info.retrieval.web.shutdown;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import p2p.info.retrieval.jtella.node.JTellaAdapter;

public class JTellaNodeShutdown extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(JTellaNodeShutdown.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -1870167349555416310L;

	public void destroy() {
		logger.info("Shutting down JTella");
		JTellaAdapter.getInstance().shutdown();
	}
}
