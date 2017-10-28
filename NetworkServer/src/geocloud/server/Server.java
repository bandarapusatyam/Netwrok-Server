package geocloud.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	private ServerSocket server = null;
	private int port = 43626;
	private boolean isStopped = false;
	//For serving the request in separate thread
	private ExecutorService threadPool=null;
	//for Managing multiple queues
	private ConcurrentMap<String, BlockingQueue<String>> serverQueues=null;
	
	public Server(int aPort) {
		port = aPort;
		threadPool = Executors.newCachedThreadPool();
		//String type is QueueId as key and BlockingQueue is value
		serverQueues = new ConcurrentHashMap<String, BlockingQueue<String>>();
	}

	public void run() {
		try {
			server = new ServerSocket(port);
			while (!IsStopped()) {
				Socket client = server.accept();
				//Execute the request in Separate thread
				threadPool.execute(new WorkerThread(client, serverQueues));
			}
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}catch (Exception e) {
			System.out.println("Server Socket: " + e.getMessage());
		}finally {
			CloseServer();
		}
	}
	
	public synchronized boolean IsStopped() {
		return isStopped;
	}
	
	//This method is for testing purpose. 
	//can be called from ServerMain class after some time for closing the the server
	public synchronized void DoStop() {
		isStopped = true;
		CloseServer();
	}
	
	private void CloseServer() {
		try {
			if(threadPool!=null) {
				threadPool.shutdownNow();
			}
			if(server!=null)
				server.close();
		} catch (Exception e) {
			System.out.println("Server Socket closed: "+e.getMessage());
		}
	}
}
