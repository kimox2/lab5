import java.io.Serializable;

public class ReplicaLoc implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String serverAddress;
	int serverPort;

	public ReplicaLoc(String name, String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.name = name;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public String getName() {
		return name;
	}

	public int getServerPort() {
		return serverPort;
	}
}
