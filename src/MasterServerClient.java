import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class MasterServerClient  extends java.rmi.server.UnicastRemoteObject implements MasterServerClientInterface {

	String address;
	Registry registry; 

	public int receiveMessage(String x) throws RemoteException{
		return x.length()*5;
	}
	
	public MasterServerClient() throws RemoteException{
		try{  
			address = (InetAddress.getLocalHost()).toString();
		}
		catch(Exception e){
			System.out.println("can't get inet address.");
		}
		int port=3233; 
		System.out.println("this address=" + address +  ",port=" + port);
		try{
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("rmiServer", this);
			
		}
		catch(RemoteException e){
			System.out.println("remote exception"+ e);
		}
	}
	
	@Override
	public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WriteMsg write(FileContent data) throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
	

	}

	

}
