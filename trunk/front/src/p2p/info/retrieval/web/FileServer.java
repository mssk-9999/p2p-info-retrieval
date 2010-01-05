package p2p.info.retrieval.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class FileServer extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(FileServer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -5341632801720179769L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) {
		logger.info("Params: Content Type - " + req.getContentType());
		if(req.getContentType().equals("")) {
			
		}
	}

}
