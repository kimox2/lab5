import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	public MasterServerClientInterface mServer;

	public MasterServerClientInterface connectMaster() {
		MasterServerClientInterface rmiServer = null;
		Registry registry;
		String serverAddress = "127.0.1.1";
		String serverPort = "3030";
		try {
			registry = LocateRegistry.getRegistry(serverAddress, (new Integer(
					serverPort)).intValue());
			rmiServer = (MasterServerClientInterface) (registry
					.lookup("MasterServerClient"));
			return rmiServer;
		} catch (Exception e) {
		}
		return rmiServer;
	}

	public ReplicaServerClientInterface connectReplica(ReplicaLoc repServerLoc) {
		ReplicaServerClientInterface repServer = null;
		Registry registry;
		String serverAddress = repServerLoc.getServerAddress();
		int serverPort = repServerLoc.getServerPort();
		try {
			registry = LocateRegistry.getRegistry(serverAddress, serverPort);
			repServer = (ReplicaServerClientInterface) (registry
					.lookup(repServerLoc.getName()));
			return repServer;
		} catch (Exception e) {
		}
		return repServer;
	}

	public void read(String fileName) throws FileNotFoundException,
			RemoteException, IOException {
		// get master server

	}

	// test some write and reads with different write sequence number
	public void test1() throws RemoteException, IOException {
		String fileName = "TestHossam.txt";
		FileContent fcw = new FileContent(fileName, "");
		WriteMsg wm = mServer.write(fcw);
		ReplicaServerClientInterface repServer2 = connectReplica(wm.getLoc());
		fcw.setFileData("Hello Hossam1\n");
		repServer2.write(wm.getTransactionId(), 1, fcw);
		fcw.setFileData("Hello Hossam3\n");
		repServer2.write(wm.getTransactionId(), 3, fcw);
		fcw.setFileData("Hello Hossam2\n");
		repServer2.write(wm.getTransactionId(), 2, fcw);
		try {
			boolean bl = repServer2.commit(wm.getTransactionId(), 3);
			System.out.println(bl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get set of replicas ip addresses
		ReplicaLoc repServerLocs[] = mServer.read(fileName);
		// choose one
		ReplicaLoc repServerLoc = repServerLocs[0];
		// connect to it
		ReplicaServerClientInterface repServer = connectReplica(repServerLoc);
		// read file from it

		FileContent fc = repServer.read(fileName);
		System.out.println(fc.getFileData());
	}

	// Test abort
	public void test2() throws RemoteException, IOException {
		// Same as Test 1 but abort instead of commit and read the file
		String fileName = "TestHossam.txt";
		FileContent fcw = new FileContent(fileName, "");
		WriteMsg wm = mServer.write(fcw);
		ReplicaServerClientInterface repServer2 = connectReplica(wm.getLoc());
		fcw.setFileData("Hello Hossam1\n");
		repServer2.write(wm.getTransactionId(), 1, fcw);
		fcw.setFileData("Hello Hossam3\n");
		repServer2.write(wm.getTransactionId(), 3, fcw);
		fcw.setFileData("Hello Hossam2\n");
		repServer2.write(wm.getTransactionId(), 2, fcw);
		try {

			boolean bl = repServer2.abort(wm.getTransactionId());
			System.out.println(bl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get set of replicas ip addresses
		ReplicaLoc repServerLocs[] = mServer.read(fileName);
		// choose one
		ReplicaLoc repServerLoc = repServerLocs[0];
		// connect to it
		ReplicaServerClientInterface repServer = connectReplica(repServerLoc);
		// read file from it

		FileContent fc = repServer.read(fileName);
		System.out.println(fc.getFileData());

		// Test abort of newly created file
		fileName = "TestHossam2.txt";
		fcw = new FileContent(fileName, "");
		wm = mServer.write(fcw);
		repServer2 = connectReplica(wm.getLoc());
		fcw.setFileData("Hello Hossam1\n");
		repServer2.write(wm.getTransactionId(), 1, fcw);
		fcw.setFileData("Hello Hossam3\n");
		repServer2.write(wm.getTransactionId(), 3, fcw);
		fcw.setFileData("Hello Hossam2\n");
		repServer2.write(wm.getTransactionId(), 2, fcw);
		try {

			boolean bl = repServer2.abort(wm.getTransactionId());
			System.out.println(bl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Test concurrent read and write
	public void test3() throws RemoteException, IOException {
		// test concurrent write
		// write in file TestHossam.txt from two clients
		// client1
		String fileName = "TestHossam.txt";
		FileContent fcw = new FileContent(fileName, "");
		WriteMsg wm = mServer.write(fcw);
		ReplicaServerClientInterface repServer2 = connectReplica(wm.getLoc());
		fcw.setFileData("Hello Hossam1\n");
		repServer2.write(wm.getTransactionId(), 1, fcw);
		fcw.setFileData("Hello Hossam3\n");
		repServer2.write(wm.getTransactionId(), 3, fcw);
		fcw.setFileData("Hello Hossam2\n");
		repServer2.write(wm.getTransactionId(), 2, fcw);
		try {

			boolean bl = repServer2.commit(wm.getTransactionId(), 3);
			System.out.println(bl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// client2
		fcw = new FileContent(fileName, "");
		wm = mServer.write(fcw);
		repServer2 = connectReplica(wm.getLoc());
		fcw.setFileData("not Hello Hossam1\n");
		repServer2.write(wm.getTransactionId(), 1, fcw);
		fcw.setFileData("not Hello Hossam3\n");
		repServer2.write(wm.getTransactionId(), 3, fcw);
		fcw.setFileData("not Hello Hossam2\n");
		repServer2.write(wm.getTransactionId(), 2, fcw);
		try {

			boolean bl = repServer2.commit(wm.getTransactionId(), 3);
			System.out.println(bl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// many reads on the same file
		// read 5 times
		for (int i = 0; i < 5; i++) {
			// get set of replicas ip addresses
			ReplicaLoc repServerLocs[] = mServer.read(fileName);
			// choose one
			ReplicaLoc repServerLoc = repServerLocs[0];
			// connect to it
			ReplicaServerClientInterface repServer = connectReplica(repServerLoc);
			// read file from it

			FileContent fc = repServer.read(fileName);
			System.out.println(fc.getFileData());
		}

	}

	public void start() throws FileNotFoundException, RemoteException,
			IOException {
		mServer = connectMaster();
		// test1();
//		test2();
		test3();

	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		Client c = new Client();
		c.start();
	}

}
