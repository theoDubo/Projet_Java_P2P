package communication;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import fonctionnement.ServeurProxy;

public class Serveur_ProxyCom implements Runnable, Serveur {
	private ServerSocket socket ;
	private ArrayList<Integer> portsArray = new ArrayList<Integer>();

	public ArrayList<Integer> addPort(int port) {
		this.portsArray.add(port);
		return this.portsArray;
	}

	public void ecoute()throws Exception {
		Socket sss;
		while(true) {
			System.out.println("En attente ...");
			sss = socket.accept(); 
			System.out.println("connexion acceptee : "+sss);
			DataInputStream entreeSocket = new DataInputStream(sss.getInputStream());
			this.addPort(entreeSocket.readInt());
			Thread t = new Thread(new ServeurProxy(sss, this));
			t.start();
		}
	}
	
	public Serveur_ProxyCom (int val) throws Exception{ // fonction d'écoute du serveur 
		// Création d'un socket server sur le port 40000
		socket = new ServerSocket(val);		
	}

	public ServerSocket getSocket() {
		return socket;
	}

	public void deconnexion(Socket sss) {
		try {
			sss.close(); //ferme le socket en paramètre
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			this.ecoute();		
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}