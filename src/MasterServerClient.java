import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MasterServerClient extends java.rmi.server.UnicastRemoteObject
		implements MasterServerClientInterface {

	String address;
	Registry registry;
	// contain the replica address in theis form name,ip:portnumber
	HashMap<String, String> repAddress = new HashMap<String, String>();
	//contain the primary replica for the file
	HashMap<String, ArrayList<String>> filesRep = new HashMap<String, ArrayList<String>>();

	public MasterServerClient() throws IOException {
		try {
			address = (InetAddress.getLocalHost()).toString();
		} catch (Exception e) {
			System.out.println("can't get inet address.");
		}
		int port = 3030;
		System.out.println("this address=" + address + ",port=" + port);
		try {
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("MasterServerClient", this);

		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}
		initReplicas();
	}

	public void initReplicas() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				"repServers.txt"));
		String s;
		String st[];
		// add the reblicas to hashmap its name is key and the address is the
		// value
		while ((s = reader.readLine()) != null) {
			st = s.split(":");
			ReplicaServerClient rep = new ReplicaServerClient(st[0], st[1],
					Integer.parseInt(st[2]));
			repAddress.put(st[0], st[1] + ":" + st[2]);
		}
		reader = new BufferedReader(new FileReader("repFiles.txt"));
		// add the primary replicas of the files to hasmap the file name is key
		// and the replica list is the value
		while ((s = reader.readLine()) != null) {
			st = s.split(":");
			ArrayList<String> lis;
			if (filesRep.containsKey(st[0]))
				lis = filesRep.get(st[0]);
			else
				lis = new ArrayList<String>();

			lis.add(st[1]);
			filesRep.put(st[0], lis);
		}
	}

	@Override
	public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		// search for file in hashmap and get it's replicas address
		if (!filesRep.containsKey(fileName))
			throw new FileNotFoundException();
		else {

			ReplicaLoc[] locs = new ReplicaLoc[repAddress.size()];
			int top = 0;
			String st[];
			for (Map.Entry<String, String> e : repAddress.entrySet()) {
				st = e.getValue().split(":");
				locs[top++] = new ReplicaLoc(e.getKey(), st[0],
						Integer.parseInt(st[1]));
			}

			return locs;
		}
	}

	@Override
	public WriteMsg write(FileContent data) throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws IOException {
		MasterServerClient msc = new MasterServerClient();

	}

}
