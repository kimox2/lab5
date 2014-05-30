import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaServerClient extends java.rmi.server.UnicastRemoteObject
		implements ReplicaServerClientInterface {

	private static final long serialVersionUID = 1L;

	private String address;
	private Registry registry;
	private String name;
	int portNumber;
	private HashMap<Long, ArrayList<Long>> transactions;
	private HashMap<String, Object> filesMutex;
	private HashMap<Long, String> transFileMap;
	private ArrayList<String> servers;
	private ArrayList<ReplicaServerClientInterface> replicaServers;
	private MasterServerClientInterface masterServer;
	private String masterAddress;
	private int masterPort;
	private String directoryPath;

	protected ReplicaServerClient(String name, String address, int portNumber,
			String masterAddress, int masterPort, String directoryPath)
			throws RemoteException, IOException {
		// super();
		this.directoryPath = directoryPath;
		String dirPath = "./" + name + "/" + directoryPath;
		File f = new File(dirPath);
		f.mkdir();
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

		transactions = new HashMap<Long, ArrayList<Long>>();
		filesMutex = new HashMap<String, Object>();
		transFileMap = new HashMap<Long, String>();

		servers = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(
				"repServers.txt"));
		String s;
		while ((s = reader.readLine()) != null)
			servers.add(s);
		reader.close();

		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
		this.masterServer = null;
	}

	@Override
	public WriteMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		ArrayList<Long> msgs;
		String filename = data.getFileName();
		if (!transactions.containsKey(txnID)) {
			msgs = new ArrayList<Long>();
			transactions.put(txnID, msgs);
			transFileMap.put(txnID, filename);
		} else
			msgs = transactions.get(txnID);
		msgs.add(msgSeqNum);

		if (!filesMutex.containsKey(filename))
			filesMutex.put(filename, new Object());

		File f = new File("tmp" + txnID + "-" + msgSeqNum + "-" + filename);
		if (!f.exists())
			f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
		bw.write(data.getFileData());
		bw.close();

		return null;
	}

	private FileContent read2(String filePath, String fileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = reader.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		FileContent fc = new FileContent(fileName, sb.toString());
		reader.close();
		return fc;
	}

	@Override
	public FileContent read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		// the check file in the directory ./replica name
		String path = "./" + name + "/" + directoryPath + "/" + fileName;
		System.out.println(path);
		File f = new File(path);
		if (!f.exists())
			throw new FileNotFoundException();
		Object mutex = filesMutex.get(fileName);
		if (mutex != null)
			synchronized (mutex) {
				return read2(path, fileName);
			}
		else {
			return read2(path, fileName);
		}
	}

	private String getContent(String filePath) throws IOException {
		File f = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringBuilder sb = new StringBuilder("");
		String s;
		while ((s = reader.readLine()) != null)
			sb.append(s + "\n");
		reader.close();
		f.delete();
		return sb.toString();
	}

	@Override
	public boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException {
		initReplicaServers();
		String fileName = transFileMap.get(txnID);
		Object mutex = filesMutex.get(fileName);
		int msgsSize = transactions.get(txnID).size();
		System.out.println(numOfMsgs + " " + msgsSize);
		try {
			masterServer.releaseSem(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (numOfMsgs != msgsSize) {
			throw new MessageNotFoundException();
		}
		synchronized (mutex) {
			try {
				File f2 = new File("./" + name + "/" + directoryPath + "/"
						+ fileName);
				System.out.println(f2.getAbsolutePath());
				if (!f2.exists())
					f2.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f2, true));
				FileContent fc = new FileContent(fileName, "");
				boolean success = true;
				for (int i = 1; i <= numOfMsgs; i++) {
					String content = getContent("tmp" + txnID + "-" + i + "-"
							+ fileName);
					bw.write(content);
					bw.flush();
					fc.setFileData(content);
					for (ReplicaServerClientInterface replica : replicaServers) {
						success = replica.atomicWrite(fc);
						if (!success)
							return false;
					}

				}

				bw.close();
				transactions.remove(txnID);
				transFileMap.remove(txnID);
				boolean b = masterServer.updateMetaData(fileName, this.name);
				// masterServer.releaseSem(fileName);
				return b;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
				return false;
			}

		}

	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		// TODO Auto-generated method stub

		if (!transactions.containsKey(txnID))
			return false;

		int msgsSize = transactions.get(txnID).size();
		String fileName = transFileMap.get(txnID);
		for (int i = 1; i <= msgsSize; i++) {
			String filePath = "tmp" + txnID + "-" + i + "-" + fileName;
			File tmp = new File(filePath);
			if (tmp.exists())
				tmp.delete();
		}

		try {
			
			masterServer.releaseSem(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		File f = new File("tmp" + txnID + "-" + fileName);
		f.delete();
		transFileMap.remove(txnID);
		transactions.remove(txnID);
		return true;
	}

	@Override
	public boolean atomicWrite(FileContent file) throws RemoteException {
		try {
			File f = new File("./" + this.name + "/" + directoryPath + "/"
					+ file.getFileName());

			if (!f.exists())
				f.createNewFile();

			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.write(file.getFileData());
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}

	private void initReplicaServers() {
		if (masterServer == null) {
			try {
				Registry tempRegistry = LocateRegistry.getRegistry(
						masterAddress, masterPort);
				masterServer = (MasterServerClientInterface) (tempRegistry
						.lookup("MasterServerClient"));
			} catch (Exception e) {

			}
		}
		if (replicaServers != null)
			return;

		replicaServers = new ArrayList<ReplicaServerClientInterface>();
		String[] st;
		String serverName;
		String serverAddress;
		String serverPort;

		for (String s : servers) {
			st = s.split(":");
			serverName = st[0];
			if (serverName.equals(this.name))
				continue;
			serverAddress = st[1];
			serverPort = st[2];
			try {
				Registry tempRegistry = LocateRegistry.getRegistry(
						serverAddress, (new Integer(serverPort)).intValue());
				ReplicaServerClientInterface rep = (ReplicaServerClientInterface) (tempRegistry
						.lookup(serverName));
				replicaServers.add(rep);
			} catch (Exception e) {

			}
		}

	}

	public static void main(String[] args) throws RemoteException, IOException {
		String name = args[0];
		String address = args[1];
		int portNumber = Integer.parseInt(args[2]);
		String masterAddress = args[3];
		int masterPort = Integer.parseInt(args[4]);
		String directoryPath = args[5];

		ReplicaServerClient rep = new ReplicaServerClient(name, address,
				portNumber, masterAddress, masterPort, directoryPath);
		;
	}
}
