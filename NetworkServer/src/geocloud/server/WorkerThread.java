package geocloud.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * This class is Separate Worker thread for executing the single or continuous 
 * request/response coming from client. This class is executed by Thread pool
 * Executor service. This makes server high availability.
 * */
public class WorkerThread implements Runnable {
	private Socket client;
	//reference to Queues for Managing queue
	ConcurrentMap<String, BlockingQueue<String>> serverQueues;

	public WorkerThread(Socket aClient, ConcurrentMap<String, BlockingQueue<String>> aServerQueues) {
		client = aClient;
		serverQueues = aServerQueues;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
			// read request
			String request;
			while ((request = ReadRequest(br)) != null) {
				// Process the request
				System.out.println("Server received: " + request);
				ProcessRequest(request, pw);
			}
		}  catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		} finally {
			try {
				if (client != null)
					client.close();
			} catch (IOException e) {
				System.out.println("Thread closed: " + e.getMessage());
			}
		}
	}

	private void ProcessRequest(String request, PrintWriter pw) {
		// Read request type
		int firstSpace = request.indexOf(" ");
		String requestType = request.substring(0, firstSpace);
		String queueId;
		String element = null;
		if (requestType.equals("PUT")) {
			// get queueID and element
			int secondSpace = request.indexOf(" ", firstSpace + 1);
			queueId = request.substring(firstSpace + 1, secondSpace);
			element = request.substring(secondSpace + 1);
		} else {
			queueId = request.substring(firstSpace + 1);
		}
		try {
			// PUT request
			if (requestType.equals("PUT")) {
				// create new queue if not present
				BlockingQueue<String> queue = new LinkedBlockingQueue<String>(100);
				queue.put(element);
				// adds new queue if not present and return null value.
				// if already present, return existing queue
				queue = serverQueues.putIfAbsent(queueId, queue);
				if (queue != null) {
					// Queue already exists
					synchronized (queue) {
						queue.put(element);
					}
				}
				// sending response back to client
				pw.print("OK" + "\n");
			} else if (requestType.equals("GET")) {
				// GET request
				BlockingQueue<String> queue = serverQueues.get(queueId);
				if (queue != null) {
					String elem = queue.take();
					pw.print(elem + "\n");
				} else {
					pw.print("ERR_NO_QUEUE:" + queueId + "\n");
				}
			} else if (requestType.equals("DELETE")) {
				// removes queue if present and returns that queue
				// otherwise returns null if not present.
				BlockingQueue<String> queue = serverQueues.remove(queueId);
				if (queue != null) {
					// queue removed
					synchronized (queue) {
						queue.clear();
					}
					queue = null;
					pw.print("OK" + "\n");
				} else {
					pw.print("ERR_NO_QUEUE:" + queueId + "\n");
				}
			}
		} catch (InterruptedException ex) {
			pw.print("ERR_NO_QUEUE:" + queueId + "\n");
		}
		pw.flush();
	}

	//read request stream from client
	private String ReadRequest(BufferedReader br) throws Exception {
		StringBuilder sb = new StringBuilder();
		int ch;
		// build the string
		while (((ch = br.read()) != -1) && ch != '\n') {
			sb.append((char) ch);
		}

		if (sb.toString().isEmpty() && sb.toString().equals("")) {
			return null;
		} else {
			return sb.toString();
		}
	}
}
