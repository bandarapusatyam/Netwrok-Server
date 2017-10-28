package clienttest.client;

public class ClientTests {
	public ClientTests() {
	}
	/*
	 * Test class for Server. 
	 * This uses Client class for making request/response.
	 * */

	public void SingleThread(String host, int port) {
		PrintTest("SingleThread", true);
		int queueId = 111;
		
		Thread client = new Client(host, port, queueId,ClientType.PUTCLIENT);
		client.start();
		ThreadSleep(1000);
		
		Thread client1 = new Client(host, port, queueId,ClientType.GETCLIENT);
		client1.start();
		ThreadSleep(1000);
		
		Thread client3 = new Client(host, port, queueId,ClientType.PUTGETCLIENT);
		client3.start();
		ThreadSleep(2000);
		
		Thread client4 = new Client(host, port, queueId,ClientType.DELETECLIENT);
		client4.start();
		ThreadSleep(1000);
		
		Thread client5 = new Client(host, port, queueId,ClientType.DELETEPUTCLIENT);
		client5.start();
		ThreadSleep(2000);
		
		Thread client6 = new Client(host, port, queueId,ClientType.DELETEGETCLIENT);
		client6.start();
		ThreadSleep(1000);
		PrintTest("SingleThread", false);
	}

	public void SameQueueMultipleClients(String host, int port) {
		PrintTest("SameQueueMultipleClients", true);
		Client []threads=new Client[40];
		int queueId = 111;
		//starting clients
		for(int i=0; i<threads.length;i++) {
			if(i<threads.length/2) {
				threads[i] = new Client(host, port, queueId,ClientType.RECURRINGPUTCLIENT);
			}else {
				threads[i] = new Client(host, port, queueId,ClientType.RECURRINGGETCLIENT);
			}
			threads[i].start();
		}
		
		ThreadSleep(5*1000);
		//stopping clients
		for(int i=0; i<threads.length;i++) {
			threads[i].DoStop();
		}
		ThreadSleep(10);
		PrintTest("SameQueueMultipleClients", false);
	}
	
	
	public void DeletePutGetSameQueueMultipleClients(String host, int port) {
		PrintTest("DeletePutGetSameQueueMultipleClients", true);
		Client []threads=new Client[10];
		int queueId = 111;
		for(int i=0; i<threads.length;i++) {
			if(i<threads.length/2) {
				threads[i] = new Client(host, port, queueId,ClientType.RECURRINGPUTCLIENT);
			}else {
				threads[i] = new Client(host, port, queueId,ClientType.RECURRINGGETCLIENT);
			}
			threads[i].start();
		}
		
		Client deleteThread = new Client(host, port, queueId,ClientType.RECURRINGDELETECLIENT);
		deleteThread.start();
		
		ThreadSleep(10*1000);
		//stopping clients
		for(int i=0; i<threads.length;i++) {
			threads[i].DoStop();
		}
		deleteThread.DoStop();
		ThreadSleep(10);
		PrintTest("DeletePutGetSameQueueMultipleClients", false);
	}
	
	public void MultipleQueuesMultipleClients(String host, int port) {
		PrintTest("MultipleQueuesMultipleClients", true);
		Client []threads=new Client[14];
		int queueId = 111;
		//starting clients
		for(int i=0; i<threads.length;i++) {
			int id = queueId+(i%7);
			if(i<threads.length/2) {
				threads[i] = new Client(host, port, id,ClientType.RECURRINGPUTCLIENT);
			}else {
				threads[i] = new Client(host, port, id,ClientType.RECURRINGGETCLIENT);
			}
			threads[i].start();
		}
		
		ThreadSleep(10*1000);
		//stopping clients
		for(int i=0; i<threads.length;i++) {
			threads[i].DoStop();
		}
		ThreadSleep(10);
		PrintTest("MultipleQueuesMultipleClients", false);
	}
	
	public void DeletePutGetMultipleQueuesMultipleClients(String host, int port) {
		PrintTest("DeletePutGetMultipleQueuesMultipleClients", true);
		Client []threads=new Client[10];
		int queueId = 111;
		for(int i=0; i<threads.length;i++) {
			int id = queueId+(i%5);
			if(i<threads.length/2) {
				threads[i] = new Client(host, port,id ,ClientType.RECURRINGPUTCLIENT);
			}else {
				threads[i] = new Client(host, port, id,ClientType.RECURRINGGETCLIENT);
			}
			threads[i].start();
		}
		
		Client []deleteThreads=new Client[5];
		for(int i=0; i<deleteThreads.length;i++) {
			deleteThreads[i] = new Client(host, port, queueId+(i%5),ClientType.RECURRINGDELETECLIENT);
			deleteThreads[i].start();
		}
		
		ThreadSleep(10*1000);
		//stopping clients
		for(int i=0; i<threads.length;i++) {
			threads[i].DoStop();
		}
		
		for(int i=0; i<deleteThreads.length;i++) {
			deleteThreads[i].DoStop();
		}

		ThreadSleep(10);
		PrintTest("DeletePutGetMultipleQueuesMultipleClients", false);
	}

	private void ThreadSleep(int millis) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("Exception: "+e.getMessage());
		}
	}
	
	private void PrintTest(String testName, boolean isStart) {
		System.out.println("----------------------------------------------------------------------------------");
		if(isStart) {
			System.out.println("Starting test "+testName);
		}else {
			System.out.println("Ending test "+testName);
		}
		System.out.println("----------------------------------------------------------------------------------");
	}
}
