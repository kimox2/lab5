import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ReplicaServerClient extends java.rmi.server.UnicastRemoteObject
		implements ReplicaServerClientInterface {

	private static final long serialVersionUID = 1L;

	String address;
	Registry registry;
	String name;
	int portNumber;

	protected ReplicaServerClient(String name, String address, int portNumber)
			throws RemoteException {
//		super();
		this.name = name;
		this.address = address;
		this.portNumber = portNumber;
		System.out.println("this address=" + address + ",port=" + portNumber);
		try {
			registry = LocateRegistry.createRegistry(portNumber);
			registry.rebind(name, this);
		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}
	}

	@Override
	public WriteMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileContent read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		// the check file in the directory ./replica name
		String path = "./" + name + "/" + fileName;
		File f = new File(path);
		if (!f.exists())
			throw new FileNotFoundException();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = reader.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		FileContent fc = new FileContent(fileName, sb.toString());
		return fc;
	}

	@Override
	public boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
