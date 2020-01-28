package communication;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import fonctionnement.ServerManager;

public class Serveur_FTP implements Serveur {
	private ServerSocket socket ;
	public void  ecoute() throws Exception{ // fonction d'écoute du serveur 
		// Création d'un socket server sur le port 40000
		Socket sss;
		socket = new ServerSocket(40000);		
		while(true) {
			sss = socket.accept(); 
			Thread t = new Thread(new ServerManager(sss, this)); // création d'un thread par client lié à un socket
			t.start();
		}
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
	
	public static void main(String[] args) {
		Serveur_FTP sr = new Serveur_FTP();
		try {
			sr.ecoute();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}


