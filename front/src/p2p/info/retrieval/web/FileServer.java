package p2p.info.retrieval.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import p2p.info.retrieval.web.init.LuceneInit;

public class FileServer extends HttpServlet {

	private static final Logger logger = Logger.getLogger(FileServer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -5341632801720179769L;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		//get the 'path' parameter
		String fileName = (String) req.getParameter("path");
		if (fileName == null || fileName.equals(""))
			throw new ServletException("Invalid or non-existent file parameter in UrlServlet servlet.");

		if (!fileName.startsWith("/"))
			fileName = "/" + fileName;

		File file = null;
		ServletOutputStream stream = null;
		FileInputStream fstream = null;
		BufferedInputStream buf = null;

		try {
			file = new File(LuceneInit.getDocDirPath()+ fileName);
			if(!file.exists())
				throw new ServletException("Invalid or non-existent file parameter in Url. The requested file does not exist.");

			Long fileSize = (Long)file.getTotalSpace();

			stream = res.getOutputStream();

			//set response headers
			res.setContentType("text/plain");
			res.addHeader("Content-Disposition", "attachment; filename="
					+ fileName);
			res.setContentLength(fileSize.intValue());
			fstream = new FileInputStream(file);
			buf = new BufferedInputStream(fstream);
			
			int readBytes = 0;

			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1)
				stream.write(readBytes);
		} catch (IOException ioe) {
			throw new ServletException(ioe.getMessage());
		} catch (ServletException se) {
			logger.error("Problem getting file.", se);
		} finally {
			if (stream != null)
				stream.close();
			if (fstream != null)
				fstream.close();
			if (buf != null)
				buf.close();
		}
	}

}
