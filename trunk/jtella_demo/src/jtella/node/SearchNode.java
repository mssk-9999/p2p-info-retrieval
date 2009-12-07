package jtella.node;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lights.extensions.FastTupleSpace;
import lights.interfaces.ITupleSpace;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import p2p.info.retrieval.lucene.thread.SearchThread;
import p2p.info.retrieval.lucene.util.QueryBuilder;
import polyester.CleaningWorker;
import polyester.Worker;

public class SearchNode {

	private static final Logger logger = Logger.getLogger(SearchNode.class);

	private static ServerSocket serverSocket;
	private static final int SERVER_PORT = 8085;
	
	private static List<Worker> threads = new ArrayList<Worker>();

	public static void main(String[] args) {
		try {
			// Set up the logging
			BasicConfigurator.configure();
			Logger.getLogger("com.kenmccrary.jtella").setLevel(Level.WARN);

			ITupleSpace tupleSpace = new FastTupleSpace();
			SearchService searchService = new SearchService(tupleSpace);
			threads.add(searchService);

			CleaningWorker cleaning = new CleaningWorker(tupleSpace);
			threads.add(cleaning);

			searchService.start();
			logger.info("----------SearchService started----------");
			cleaning.start();
			logger.info("----------Cleaning started----------");
			
			openServerSocket();
			
			while(true){
				Socket clientSocket = null;
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					throw new RuntimeException("Error accepting client connection", e);
				}
//				new Thread(new SearchThread(clientSocket)).start();
				
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					
					String msg = input.readLine();
					
					output.close();
					input.close();
					logger.info("Request processed: " + msg);

					tupleSpace.out(JTellaTupleFactory.createDirectSearchTuple(msg));
					
				} catch (Exception e) {
					logger.error("Exception receiving stream", e);
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void openServerSocket() {
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + SERVER_PORT, e);
		}
	}

}
