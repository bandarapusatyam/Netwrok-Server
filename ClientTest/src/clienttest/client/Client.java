package clienttest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
	private Socket client = null;
	private String host = null;
	private int port = 43626;
	private int queueId = 0;
	private ClientType type=ClientType.PUTCLIENT;
	private boolean isStopped = false;

	public Client(String aHost, int aPort, int aQueueId, ClientType aType) {
		host = aHost;
		port = aPort;
		queueId = aQueueId;
		type = aType;
	}

	public void run() {
		try {
			client = new Socket(host, port);
			PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			//Send the request and read response from server
			ProcessRequestResponse(br, pw);
		} catch (IOException e) {
			if(!IsStopped())
				System.out.println("Exception: "+e.getMessage());
		} catch (Exception e) {
			if(!IsStopped())
				System.out.println("Exception: "+e.getMessage());
		}finally {
			if(client!=null)
				CloseClient();
		}
	}
	
	//For making different kind of requests in single or multiple times
	private void ProcessRequestResponse(BufferedReader br, PrintWriter pw) throws Exception {
		//Single Requests
		if(type == ClientType.PUTCLIENT) {
			pw.print("PUT "+queueId+" Request from " + Thread.currentThread().getId()+"\n");
			pw.flush();
			System.out.println("PUT Response from server: "+ReadResponse(br));
		}else if(type == ClientType.GETCLIENT) {
			pw.print("GET "+queueId+"\n");
			pw.flush();
			System.out.println("GET Response from server: "+ReadResponse(br));
		}else if(type==ClientType.DELETECLIENT){
			pw.print("DELETE "+queueId+"\n");
			pw.flush();
			System.out.println("DELETE Response from server: "+ReadResponse(br));
		}else if(type==ClientType.PUTGETCLIENT) {
			pw.print("PUT "+queueId+" Request from " + Thread.currentThread().getId()+"\n");
			pw.print("GET "+queueId+"\n");
			pw.flush();
			System.out.println("PUT Response from server: "+ReadResponse(br));
			System.out.println("GET Response from server: "+ReadResponse(br));
		}else if(type==ClientType.DELETEPUTCLIENT) {
			pw.print("DELETE "+queueId+"\n");
			pw.print("PUT "+queueId+" Request from " + Thread.currentThread().getId()+"\n");
			pw.flush();
			System.out.println("DELETE Response from server: "+ReadResponse(br));
			System.out.println("PUT Response from server: "+ReadResponse(br));
		}else if(type==ClientType.DELETEGETCLIENT) {
			pw.print("DELETE "+queueId+"\n");
			pw.print("GET "+queueId+"\n");
			pw.flush();
			System.out.println("DELETE Response from server: "+ReadResponse(br));
			System.out.println("GET Response from server: "+ReadResponse(br));
		} else if (type == ClientType.RECURRINGPUTCLIENT) {
			//Continuous PUT request
			int count = 0;
			pw.print("PUT " + queueId + " Request "+(++count)+" from thread id " + Thread.currentThread().getId() + "\n");
			pw.flush();
			String response;
			while ((response = ReadResponse(br)) != null && !IsStopped()) {
				pw.print("PUT " + queueId + " Request "+(++count)+" from thread id  " + Thread.currentThread().getId() + "\n");
				pw.flush();
				System.out.println("PUT Response from server: "+response);
			}
		}else if(type == ClientType.RECURRINGGETCLIENT) {
			//Continuous GET request
			pw.print("GET "+queueId+"\n");
			pw.flush();
			String response;
			while ((response = ReadResponse(br)) != null && !IsStopped()) {
				pw.print("GET "+queueId+"\n");
				pw.flush();
				System.out.println("GET Response from server: "+response);
			}
		}
		else if(type == ClientType.RECURRINGDELETECLIENT) {
			//Continuous DELETE request
			int count = 0;
			pw.print("DELETE "+queueId+"\n");
			pw.flush();
			String response;
			while ((response = ReadResponse(br)) != null && !IsStopped()) {
				System.out.println("DELETE Response from server: "+response+" :count "+(++count)+" :QueueID "+queueId);
				Thread.sleep(100);
				pw.print("DELETE "+queueId+"\n");
				pw.flush();
			}
		}
	}

	public void CloseClient() {
		try {
			client.close();
		} catch (IOException e) {
			System.out.println("Exception: "+e.getMessage());
		}
	}
	
	//Reading the Stream from Server
	private String ReadResponse(BufferedReader br) throws Exception {
		StringBuilder sb = new StringBuilder();
		int ch;
		while (((ch = br.read()) != -1) && ch != '\n') {
			sb.append((char) ch);
		}
		
		if (sb.toString().isEmpty() && sb.toString().equals("")) {
			return null;
		} else {
			return sb.toString();
		}
	}
	
	public synchronized boolean IsStopped() {
		return isStopped;
	}
	
	//For stopping the client
	public synchronized void DoStop() {
		isStopped = true;
		notifyAll();
		CloseClient();
	}
}
