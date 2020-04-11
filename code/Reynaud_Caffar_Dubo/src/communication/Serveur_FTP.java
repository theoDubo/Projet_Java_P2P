package communication;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import fonctionnement.ServerManager;

public class Serveur_FTP implements Runnable, Serveur {
	private ServerSocket socket ;
	
	public void ecoute()throws Exception {
		Socket sss;
		while(true) {
			System.out.println("En attente ...");
			sss = socket.accept(); 
			System.out.println("connexion acceptee : "+sss);
			Thread t = new Thread(new ServerManager(sss, this)); // création d'un thread par client lié à un socket
			t.start();
		}
	}
	public Serveur_FTP (int val) throws Exception{ // fonction d'écoute du serveur 
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


