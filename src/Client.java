import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

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
		//get master server
		MasterServerClientInterface mServer = connectMaster();
		//get set of replicas ip addresses
		ReplicaLoc repServerLocs[] = mServer.read(fileName);
		//choose one
		ReplicaLoc repServerLoc = repServerLocs[0];
		//connect to it
		ReplicaServerClientInterface repServer = connectReplica(repServerLoc);
		//read file from it
		FileContent fc = repServer.read(fileName);
		System.out.println(fc.getFileDate());
	}

	public void start() throws FileNotFoundException, RemoteException, IOException {
		read("f2.txt");
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		Client c = new Client();
		c.start();
	}

}
