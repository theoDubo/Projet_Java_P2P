package communication;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import fonctionnement.ClientManager;

public class Client_TCP {
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
	
	public void main() {
		Thread t = new Thread(new ClientManager(this));
		t.start();
	}
	
	public void deconnect() throws IOException {
		socket.close();
	}
}
