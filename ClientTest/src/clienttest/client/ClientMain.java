package clienttest.client;

public class ClientMain {
	
	public static void main(String []args) {
		//if want to test with different host, can pass through command line arguments,
		// else takes default host localhost
		String host = "localhost";
		if(args.length>=1)
			host = args[0];
		
		//if want to test with different port, can pass through command line arguments,
		// else takes default port 43626
		int port = 43626;
		if(args.length>=2)
			port = Integer.parseInt(args[0]);
		
		ClientTests clientMain=new ClientTests();
		//Following are tests for server using client class.
		clientMain.SingleThread(host, port);
		clientMain.SameQueueMultipleClients(host, port);
		clientMain.DeletePutGetSameQueueMultipleClients(host, port);
		clientMain.MultipleQueuesMultipleClients(host, port);
		clientMain.DeletePutGetMultipleQueuesMultipleClients(host, port);
	}
}
