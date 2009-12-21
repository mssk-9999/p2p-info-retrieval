package jtella.node.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class JTellaTS extends TestCase {

	private static final Logger logger = Logger.getLogger(JTellaTS.class);
	private static final String jtellaTS = "JTella_TS";
	private PrintWriter out;
	private BufferedReader in;
	private Socket serverSocket;

	private String createSearch() {
		JSONObject obj = new JSONObject();
		obj.put("sessionId", Long.toString(new Date().getTime()));
		obj.put("searchResults", "String");
		obj.put("randoTag", "some value");
		return obj.toJSONString();
	}	

	@Override
	protected void setUp() throws Exception {
		serverSocket = null;
		out = null;
		in = null;
		
		InetAddress lh = InetAddress.getLocalHost();

		try {
			serverSocket = new Socket(lh, 8085);
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + lh.getHostAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + lh.getHostAddress());
			e.printStackTrace();
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
		
		String searchMsg = createSearch();
		System.out.print("Search Message: " + searchMsg);
		out.println(searchMsg);

	}
}
