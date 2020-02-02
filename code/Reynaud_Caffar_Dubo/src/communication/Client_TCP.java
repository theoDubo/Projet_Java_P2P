package communication;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import fonctionnement.ClientManager;

public class Client_TCP implements Client {
	private String racine;
	private Socket socket;

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket("localhost",40000);
		System.out.println("Connexion...");
	}

	public String getracine() {
		return racine;
	}
	
	public Socket getSocket() {
		return socket;
	}

	//set le fichier racine avec un chemin relatif
	public void setRacine(String r) throws Exception { 
		if (r.charAt(0)!='.' && r.charAt(r.length())!='/') throw new Exception("invalid path definition");
		else racine =r;
	}
	
	public static void main(String[] args) {
		Client_TCP client = new Client_TCP();
		try {
			client.connect();
			client.setRacine("./dossierClient/");
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread t = new Thread(new ClientManager(client));
		t.start();
	}
	
	public void deconnect() throws IOException {
		socket.close();
	}
}
