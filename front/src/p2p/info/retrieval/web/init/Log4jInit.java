package p2p.info.retrieval.web.init;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit extends HttpServlet {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 4846650084386901742L;

	public void init() {
	    String prefix =  getServletContext().getRealPath("/");
	    String file = getInitParameter("log4j-init-file");
	    // if the log4j-init-file is not set, then no point in trying
	    if(file != null) {
	      PropertyConfigurator.configure(prefix+file);
	    }
	  }

	  public void doGet(HttpServletRequest req, HttpServletResponse res) {}


}
