package geocloud.server;

public class ServerMain {

	public static void main(String []args) {
		//if want to test with different port, can pass through command line arguments,
		// else takes default port 43626
		int port = 43626;
		if(args.length>=1)
			port = Integer.parseInt(args[0]);
		
		Server server= new Server(port);
		new Thread(server).start();
		
		/*try {
			Thread.sleep(50*1000);
			server.DoStop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}
}
