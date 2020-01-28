package fonctionnement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import communication.Serveur_FTP;

public class ServerManager implements Runnable {
	Socket socket;
	Serveur_FTP serveur;
	Byte[] tampon= new Byte[1024];
	String root="/home/theo/eclipse-workspace/Projet_java_P2P/Serveur/";

	public ServerManager(Socket socket, Serveur_FTP serveur) {
		super();
		this.socket = socket;
		this.serveur = serveur;
	}


	private void menu(Socket sss)throws Exception { //manque l'ajout de fonction mais sinon ça marche
		// TODO Auto-generated method stub		
		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));
		
		//lecture d'une chaine envoyé par le client
		String chaine = entreeSocket.readLine();
		int tst;
		String[] var;
		while (!chaine.equals("FIN")) {
			//vérification que la chaine ai au moins 2 paramètres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			var=chaine.split(" ");
			switch(var[0]) {
			case "get":
				if (var.length>2) throw new Exception("invalid parameter number");
				tst = get(var[1],sss);
				if (tst<0)throw new Exception("get Exception, failed");
				break;
			// les autres fonctions d'utilisations éventuels
			}
			chaine = entreeSocket.readLine();
		}
		serveur.deconnexion(sss);
	}


	public int get(String nomFichier,Socket sss) { // fonction de récupération d'un fichier
		int il;
		String nomF =root+nomFichier;
		byte[] tampon = new byte[1024];
		try {
		    // Le FileReader ouvre le fichier
		    FileInputStream fr = new FileInputStream(nomF);
		    // Le BufferReader charge le contenu du fichier
		    BufferedInputStream br = new BufferedInputStream(fr);
		    //construction d'un printStream pour envoyer du texte à travers la connexion socket
		    PrintStream sortieSocket = new PrintStream(sss.getOutputStream());
		    // On récupère la première ligne du fichier
		    il = br.read(tampon) ;
		    if (il < 0) {
		    	br.close();
		    	return -1;
		    }
		    // Tant que le fichier n'est pas finis
		    while( il > 0 ) {
		        // Et on lit la suivante
		        il = br.read(tampon) ;
		        sortieSocket.print(br);
		    }
		    // Ne pas oublier de fermer le BufferedReader
		    br.close() ;
		}
		// Gere les execptions
		catch(FileNotFoundException e) {
			e.printStackTrace();
		    return -1;
		}
		catch(IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.menu(socket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
