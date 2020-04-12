package communication;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fonctionnement.ServerManager;

public class Serveur_FTP implements Runnable, Serveur {
	// Declaration des variables
	private ServerSocket socket ;
	private String root;

	// Methode permettant d'ecouter sur le socket
	public void ecoute()throws Exception {
		// Declaration des variables
		Socket sss;
		while(true) {
			System.out.println("En attente ...");
			// On accepte la connexion de socket
			sss = socket.accept(); 
			System.out.println("connexion acceptee : "+sss);
			// On declare un thread que l'on initialise en liant un client a un socket si la racine n'est pas nulle
			Thread t;
			if (root!=null)t = new Thread(new ServerManager(sss,root,this));
			else t = new Thread(new ServerManager(sss,this));
			// Demarrage du thread
			t.start();
		}
	}
	
	// Constructeur prenant en parametres un entier et une chaine de caracteres
	public Serveur_FTP (int val,String root) throws Exception{
		// Creation d'un socket server sur le port 40000
		socket = new ServerSocket(val);		
		this.root=root;
	}

	// Methode permettant de retourner le server socket
	public ServerSocket getSocket() {
		return socket;
	}

	// Methode permettant de deconnecter un socket
	public void deconnexion(Socket sss) {
		try {
			// Fermeture du socket en parametre
			sss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Methode de demarrage
	public void run() {
		try {
			// On lance l'ecoute
			this.ecoute();		
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}