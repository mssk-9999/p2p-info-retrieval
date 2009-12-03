package jtella.node.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import lights.Field;
import lights.Tuple;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import p2p.info.retrieval.lucene.SearchService;
import polyester.CleaningWorker;
import polyester.util.SearchString;

public class JTellaTS extends TestCase {

	private static final Logger logger = Logger.getLogger(JTellaTS.class);
	private static final String jtellaTS = "JTella_TS";

	private String createSearch() {
		JSONObject obj = new JSONObject();
		obj.put("query", "String");
		return obj.toJSONString();
	}

	public void testPassThroughTS() throws Exception {
		// Set up the logging
		BasicConfigurator.configure();

		ITupleSpace ts = new FastTupleSpace(jtellaTS);
		SearchService searchService = new SearchService(ts);
		CleaningWorker cleaning = new CleaningWorker(ts);

		searchService.start();
		logger.info("----------SearchService started----------");
		cleaning.start();
		logger.info("----------Cleaning started----------");

		//create and populate first tuple
		ITuple t = new Tuple();
		t.add(new Field().setValue("Search"));
		t.add(new Field().setValue("direct"));

		SearchString s = null;

		try {
			String searchString = createSearch();
			s = new SearchString(searchString); 
		}
		catch (Exception e) {return;} //caution: no tuple goes out if invalid search

		t.add(new Field().setValue(s));

		//put it in the tuple space
		try {
			ts.out(t);
		}
		catch (TupleSpaceException e) {}

		Thread.sleep(5000L);

		searchService.stop();
		cleaning.stop();
	}

	public void testPortComm() throws Exception {
		Socket serverSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			InetAddress lh = InetAddress.getLocalHost();
			serverSocket = new Socket(lh, 8085);
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					serverSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: localhost.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for "
					+ "the connection to: localhost.");
			e.printStackTrace();
			System.exit(1);
		}

		String searchMsg = createSearch();
		System.out.print("Search Message: " + searchMsg);
		out.println(searchMsg);

		out.close();
		in.close();
		serverSocket.close();

	}
}
