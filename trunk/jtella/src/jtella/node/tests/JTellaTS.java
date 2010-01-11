package jtella.node.tests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JTellaTS extends TestCase {

	private static final Logger logger = Logger.getRootLogger();
	private static final String jtellaTS = "JTella_TS";
	private PrintWriter out;
	private BufferedReader in;
	private Socket serverSocket;
	private static InputStream is;
	private static OutputStream os;

	private JSONObject createSearch() {
		JSONObject obj = new JSONObject();
		obj.put("sessionId", Long.toString(new Date().getTime()));
		obj.put("searchResults", "String");
		obj.put("randoTag", "some value");
		return obj;
	}	

	@Override
	protected void setUp() throws Exception {

		BasicConfigurator.configure();

		serverSocket = null;
		out = null;
		in = null;

		InetAddress lh = InetAddress.getLocalHost();

		try {
			serverSocket = new Socket(lh, 8085);
			is = serverSocket.getInputStream();
			os = serverSocket.getOutputStream();
		} catch (UnknownHostException e) {
			logger.error("Don't know about host: " + lh.getHostAddress(), e);
			System.exit(1);
		} catch (IOException e) {
			logger.error("Couldn't get I/O for the connection to: " + lh.getHostAddress(), e);
			System.exit(1);
		}
	}



	@Override
	protected void tearDown() throws Exception {
		out.close();
		in.close();
		serverSocket.close();
	}



	public void testPortComm() throws Exception {

		//		String searchMsg = createSearch();
		JSONObject obj = createSearch();
		//		obj.writeJSONString(out);
		String jsonString = obj.toJSONString();
		logger.debug("Search Message: " + jsonString);

		Writer writer = new OutputStreamWriter(os, "US-ASCII");
		PrintWriter out = new PrintWriter(writer, true);
		out.println(jsonString);
//		BufferedReader in = new BufferedReader(new InputStreamReader(is, "US-ASCII"));
//		String line;
//		while ((line = in.readLine()) != null) {
//			System.out.println(line);
//		}

		//		logger.debug("From JTella: " + msg);

	}
}
