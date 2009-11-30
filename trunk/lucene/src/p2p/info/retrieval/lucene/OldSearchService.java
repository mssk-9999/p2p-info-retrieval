package p2p.info.retrieval.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import p2p.info.retrieval.lucene.thread.SearchThread;

public class OldSearchService implements Runnable{

	protected int          serverPort   = 8080;
	protected ServerSocket serverSocket = null;
	protected boolean      isStopped    = false;
	protected Thread       runningThread= null;

	protected static OldSearchService server = null;

	public OldSearchService(int port){
		this.serverPort = port;
	}

	public void run(){
		synchronized(this){
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(! isStopped()){
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if(isStopped()) {
					System.out.println("Server Stopped.") ;
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			new Thread(new SearchThread(clientSocket)).start();
		}
		System.out.println("Server Stopped.") ;
	}


	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port 8080", e);
		}
	}


	//Create a new Server and Run it.
	public static void main(String[] args){

//		BufferedReader in;

		System.out.println("Service Started");

		server = new OldSearchService(8080);
		new Thread(server).start();

//		try {
//			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
//			while(true) {
//				System.out.println("Enter Command: (q to quit)");
//				String command = in.readLine();
//				if(command.equals("q")){
//					if(server == null) {
//						System.out.println("No server running");
//						return;
//					}
//					System.out.println("Stopping Server");
//
//					server.stop();
//					break;
//				}
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

}
