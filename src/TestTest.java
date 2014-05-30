import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class TestTest {

	public static void test1() throws FileNotFoundException, RemoteException,
			IOException {
		//read
		Client c1 = new Client();
		c1.write2("sale7.txt");
		c1 = new Client();
		c1.write2("hossam.txt");
		
		//Write 
		for (int i = 0; i < 5; i++) {
			System.out.println("Client num " + i + "start read file f1");
			c1 = new Client();
			c1.read1("sale7.txt");
		}
		for (int i = 0; i < 5; i++) {
			System.out.println("Client num " + i + "start read file f1");
			c1 = new Client();
			c1.read1("hossam.txt");
		}

		
	}

	// Test abort
	public static void test2() throws FileNotFoundException, RemoteException,
			IOException {
		
		// start abort new file 
		Client c1 = new Client();
		c1.abort1("sale7.txt");
		//Abort new created file
		Client c2 = new Client();
		c2.abort2("newFile.txt");
	}

	public static void main(String[] args) throws FileNotFoundException, RemoteException, IOException {
//		test1();
		test2();
	}

}
